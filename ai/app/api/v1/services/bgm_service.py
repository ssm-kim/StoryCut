import os
import subprocess
import wave
import contextlib
import webrtcvad
import functools
from uuid import uuid4
from pathlib import Path
from pydub import AudioSegment
from transformers import AutoProcessor, MusicgenForConditionalGeneration
import torch
import numpy as np
import soundfile as sf
import requests
import re
import asyncio
import logging

# === 로거 설정 ===
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

UPLOAD_DIR = "app/videos"
os.makedirs(UPLOAD_DIR, exist_ok=True)

loop = asyncio.get_event_loop()

def blend_audio_segments(seg1, seg2, overlap_len):
    min_overlap = min(overlap_len, len(seg1), len(seg2))
    if min_overlap <= 0:
        return np.concatenate([seg1, seg2])
    fade_in = np.linspace(0, 1, min_overlap)
    fade_out = 1 - fade_in
    overlap = seg1[-min_overlap:] * fade_out + seg2[:min_overlap] * fade_in
    overlap = np.clip(overlap, -1.0, 1.0)
    return np.concatenate([seg1[:-min_overlap], overlap, seg2[min_overlap:]])

async def generate_bgm(prompt, segment_duration=10, total_duration=60, device='cuda' if torch.cuda.is_available() else 'cpu'):
    processor = AutoProcessor.from_pretrained("facebook/musicgen-small")
    model = MusicgenForConditionalGeneration.from_pretrained("facebook/musicgen-small").to(device)
    model.eval()

    sample_rate = 32000
    overlap_samples = int(0.5 * sample_rate)
    generated_audio = None
    if(int(total_duration // segment_duration)==0) :
        num_segments = int(total_duration // segment_duration) + 1
    else :
        num_segments = int(total_duration // segment_duration)

    for _ in range(num_segments):
        inputs = processor(text=[prompt], padding=True, return_tensors="pt").to(device)
        with torch.no_grad():
            tokens_per_second = 100
            max_new_tokens = int(segment_duration * tokens_per_second)
            audio = model.generate(
                **inputs,
                do_sample=True,
                guidance_scale=1,
                max_new_tokens=int(max_new_tokens / 2)
            )
        audio = audio.squeeze().cpu().numpy()
        audio = np.clip(audio, -1.0, 1.0)

        rms = np.sqrt(np.mean(audio ** 2))
        dbfs = 20 * np.log10(rms) if rms > 0 else -100

        if dbfs < -35 and dbfs > -100:
            logger.info(f"🔊 구간 볼륨이 작음({dbfs:.2f}dB), 10dB 증폭")
            audio = audio * (10 ** (10 / 20))
            audio = np.clip(audio, -1.0, 1.0)
        elif dbfs <= -80:
            logger.info(f"⏩ 구간 볼륨이 너무 작음({dbfs:.2f}dB), 건너뜀")
            continue

        generated_audio = audio if generated_audio is None else blend_audio_segments(generated_audio, audio, overlap_samples)

    required_length = int(sample_rate * total_duration)
    if len(generated_audio) < required_length:
        repeat_times = (required_length // len(generated_audio)) + 1
        generated_audio = np.tile(generated_audio, repeat_times)

    peak = np.max(np.abs(generated_audio))
    if peak > 0:
        generated_audio = generated_audio / peak * 0.95

    bgm_path = os.path.join(UPLOAD_DIR, f"{uuid4().hex}_bgm.wav")
    await loop.run_in_executor(None, sf.write, bgm_path, generated_audio[:required_length], sample_rate)
    
    model.cpu()
    del model
    del processor
    if device == "cuda":
        torch.cuda.empty_cache()

    return bgm_path

async def extract_audio(video_path, audio_path):
    cmd = ["ffmpeg", "-y", "-i", video_path, "-ac", "1", "-ar", "16000", "-f", "wav", audio_path]
    run = functools.partial(subprocess.run, cmd, check=True)
    await loop.run_in_executor(None, run)
    if not os.path.exists(audio_path):
        raise FileNotFoundError(f"음성 추출 실패 또는 파일 생성되지 않음: {audio_path}")

async def get_duration(video_path):
    cmd = [
        "ffprobe", "-v", "error", "-show_entries", "format=duration",
        "-of", "default=noprint_wrappers=1:nokey=1", video_path
    ]
    run = functools.partial(subprocess.run, cmd, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True)
    result = await loop.run_in_executor(None, run)
    return float(result.stdout.strip())

async def detect_voice_regions(wav_path, frame_duration=30):
    def _inner():
        vad = webrtcvad.Vad(3)
        regions = []
        with contextlib.closing(wave.open(wav_path, 'rb')) as wf:
            sample_rate = wf.getframerate()
            frame_bytes = int(sample_rate * frame_duration / 1000 * 2)
            n_frames = int(wf.getnframes() * 2 / frame_bytes)
            for i in range(n_frames):
                frame = wf.readframes(int(frame_bytes / 2))
                if len(frame) < frame_bytes:
                    break
                timestamp = i * frame_duration / 1000
                if vad.is_speech(frame, sample_rate):
                    regions.append((timestamp, timestamp + frame_duration / 1000))
        return regions

    loop = asyncio.get_event_loop()
    regions = await loop.run_in_executor(None, _inner)
    logger.info("감지된 음성 구간: %s", regions)
    return merge_regions(regions)


def merge_regions(regions, merge_threshold=0.2, min_region_length=0.4):
    if not regions:
        return []
    merged = [regions[0]]
    for current in regions[1:]:
        prev = merged[-1]
        if current[0] - prev[1] <= merge_threshold:
            merged[-1] = (prev[0], current[1])
        else:
            if prev[1] - prev[0] >= min_region_length:
                merged.append(current)
            else:
                merged[-1] = (prev[0], current[1])
    return [r for r in merged if r[1] - r[0] >= min_region_length]

async def adjust_bgm_dynamic(bgm_path, voice_path, voice_regions, total_duration, output_path):
    def _inner():
        original_bgm = AudioSegment.from_file(bgm_path).set_channels(1).set_frame_rate(32000).set_sample_width(2)
        voice_audio = AudioSegment.from_file(voice_path).set_channels(1).set_frame_rate(16000).set_sample_width(2)
        bgm = AudioSegment.silent(duration=0)
        while len(bgm) < int(total_duration * 1000):
            bgm += original_bgm
        bgm = bgm[:int(total_duration * 1000)].normalize()

        fade_ms = 300
        output = AudioSegment.silent(duration=0)
        prev_end = 0
        prev_bgm_gain = -5

        for start, end in voice_regions:
            gap = start - prev_end
            if gap > 2.0:
                chunk = bgm[int(prev_end * 1000):int(start * 1000)].apply_gain(-10).fade_in(fade_ms)
            else:
                chunk = bgm[int(prev_end * 1000):int(start * 1000)].apply_gain(prev_bgm_gain).fade_in(fade_ms)
            output += chunk

            voice_chunk = voice_audio[int(start * 1000):int(end * 1000)]
            voice_db = voice_chunk.dBFS if voice_chunk.dBFS != float('-inf') else -40
            bgm_gain = -max(15, voice_db + 35)
            logger.info(f"voice_db: {voice_db}, bgm_gain: {bgm_gain}, 구간: {start}-{end}")
            chunk = bgm[int(start * 1000):int(end * 1000)].apply_gain(bgm_gain).fade_out(fade_ms)
            output += chunk
            prev_end = end
            prev_bgm_gain = bgm_gain

        if prev_end < total_duration:
            chunk = bgm[int(prev_end * 1000):].apply_gain(-5).fade_in(fade_ms)
            output += chunk
        output = output.normalize().apply_gain(5)
        output.export(output_path, format="wav")

    loop = asyncio.get_event_loop()
    await loop.run_in_executor(None, _inner)


async def fast_merge(video_path, voice_audio, bgm_audio, output_path):
    no_audio = os.path.join(UPLOAD_DIR, f"{uuid4().hex}_no_audio.mp4")
    mixed_audio = os.path.join(UPLOAD_DIR, f"{uuid4().hex}_mixed.aac")

    cmds = [
        ["ffmpeg", "-y", "-i", video_path, "-c", "copy", "-an", no_audio],
        ["ffmpeg", "-y", "-i", voice_audio, "-i", bgm_audio,
         "-filter_complex", "[0][1]amix=inputs=2:duration=first:dropout_transition=2",
         "-ar", "32000", "-c:a", "aac", "-b:a", "192k", mixed_audio],
        ["ffmpeg", "-y", "-i", no_audio, "-i", mixed_audio, "-c:v", "copy", "-c:a", "aac", "-shortest", output_path]
    ]

    for cmd in cmds:
        run = functools.partial(subprocess.run, cmd, check=True)
        await loop.run_in_executor(None, run)

    return no_audio, mixed_audio

async def has_audio_stream(video_path: str) -> bool:
    cmd = [
        "ffprobe", "-v", "error", "-select_streams", "a",
        "-show_entries", "stream=index", "-of", "csv=p=0", video_path
    ]
    run = functools.partial(subprocess.run, cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    result = await loop.run_in_executor(None, run)
    return bool(result.stdout.strip())

async def process_bgm_service(video_path: str, prompt: str):
    def gemini_translate_ko_to_en(prompt_ko):
        api_key = os.getenv("GEMINI_API_KEY")
        url = f"https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key={api_key}"
        headers = {"Content-Type": "application/json"}

        data = {
            "contents": [
                {
                    "role": "user",
                    "parts": [
                        {
                            "text": (
                                "Translate the following Korean phrase into a natural, concise English phrase suitable as a prompt "
                                "for an AI background music generator. The result should be short and expressive, and end with 'background music'.\n\n"
                                f"Korean: {prompt_ko}\nEnglish:"
                            )
                        }
                    ]
                }
            ]
        }

        response = requests.post(url, headers=headers, json=data)
        if response.status_code == 200:
            try:
                result = response.json()
                prompt_en = result['candidates'][0]['content']['parts'][0]['text'].strip()
                return re.sub(r"[^a-zA-Z0-9 ,.!?]", "", prompt_en)
            except Exception:
                logger.warning("⚠️ 응답 파싱 실패: %s", result)
                return ""
        else:
            logger.error("❌ Gemini API 오류: %s", response.text)
            return ""

    loop = asyncio.get_event_loop()
    prompt = await loop.run_in_executor(None, gemini_translate_ko_to_en, prompt)
    video_path = str(Path(video_path).resolve())
    uid = uuid4().hex
    voice_path = os.path.join(UPLOAD_DIR, f"{uid}_voice.wav")
    adjusted_bgm_path = os.path.join(UPLOAD_DIR, f"{uid}_adjusted_bgm.wav")
    output_path = os.path.join(UPLOAD_DIR, f"{uid}_output.mp4")

    if not os.path.exists(video_path):
        raise FileNotFoundError(f"❌ 입력 영상 파일 없음: {video_path}")

    duration = await get_duration(video_path)
    bgm_path = await generate_bgm(prompt, total_duration=duration)

    if await has_audio_stream(video_path):
        logger.info("🗣 오디오 있음 - 음성 추출 및 믹싱 시작")
        await extract_audio(video_path, voice_path)
        regions = await detect_voice_regions(voice_path)
        if regions:
            await adjust_bgm_dynamic(bgm_path, voice_path, regions, duration, adjusted_bgm_path)
        else:
            await loop.run_in_executor(None, os.rename, bgm_path, adjusted_bgm_path)

        no_audio, mixed_audio = await fast_merge(video_path, voice_path, adjusted_bgm_path, output_path)

        for path in [voice_path, bgm_path, adjusted_bgm_path, no_audio, mixed_audio]:
            if path and os.path.exists(path):
                try:
                    os.remove(path)
                except Exception as e:
                    logger.warning(f"⚠️ 파일 삭제 실패: {path} ({e})")
    else:
        logger.info("🔇 오디오 없음 - BGM 단독 삽입")
        logger.info(f"▶️ ffmpeg 명령 실행: {video_path} + {bgm_path} -> {output_path}")
        await loop.run_in_executor(None, subprocess.run, [
            "ffmpeg", "-y", "-i", video_path, "-i", bgm_path,
            "-c:v", "copy", "-c:a", "aac", "-shortest", output_path
        ])

        if os.path.exists(bgm_path):
            try:
                os.remove(bgm_path)
            except Exception as e:
                logger.warning(f"⚠️ 파일 삭제 실패: {bgm_path} ({e})")

    logger.info(f"✅ 최종 결과 저장 완료: {output_path}")
    return output_path
