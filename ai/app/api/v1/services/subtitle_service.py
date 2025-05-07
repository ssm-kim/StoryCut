import whisper
import os
import subprocess
import torch
import re
import cv2
import uuid

base_dir = "app/subtitle"
os.makedirs(base_dir, exist_ok=True)

# ✅ 전역에서 Whisper 모델 1회만 로드
if not torch.cuda.is_available():
    raise RuntimeError("❌ CUDA 사용 불가. GPU 설정을 확인하세요.")
whisper_model = whisper.load_model("medium").to("cuda")

def get_video_resolution(video_path: str) -> tuple[int, int]:
    cap = cv2.VideoCapture(video_path)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    cap.release()
    return width, height

def format_time_ass(seconds: float) -> str:
    h = int(seconds // 3600)
    m = int((seconds % 3600) // 60)
    s = int(seconds % 60)
    cs = int(round((seconds - int(seconds)) * 100))
    return f"{h:01}:{m:02}:{s:02}.{cs:02}"

def subtitles(video_path: str) -> str:
    uid = uuid.uuid4().hex
    audio_path = os.path.join(base_dir, f"{uid}_temp_audio.wav")
    ass_path = os.path.join(base_dir, f"{uid}_subtitle.ass")
    ffmpeg_ass_path = ass_path.replace("\\", "/")
    output_path = os.path.join(base_dir, f"{uid}_subtitled.mp4").replace("\\", "/")

    try:
        # 1. 오디오 추출
        subprocess.run([
            "ffmpeg", "-y", "-i", video_path,
            "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", audio_path
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        # 2. 자막 추출
        result = whisper_model.transcribe(
            audio_path,
            language="ko",
            task="transcribe",
            compression_ratio_threshold=2.8,
            logprob_threshold=-1.0,
            no_speech_threshold=0.5
        )

        # 3. 해상도 및 스타일 계산
        width, height = get_video_resolution(video_path)
        base_ref = max(width, height)
        fontsize = max(16, int(48 * base_ref / 1080))
        margin_v = max(10, int(30 * base_ref / 1080 * (2.2 if height > width else 1)))

        # 4. ASS 자막 생성
        with open(ass_path, "w", encoding="utf-8") as f:
            f.write("[Script Info]\n")
            f.write(f"PlayResX: {width}\nPlayResY: {height}\nScriptType: v4.00+\n\n")

            f.write("[V4+ Styles]\n")
            f.write("Format: Name, Fontname, Fontsize, PrimaryColour, BackColour, Bold, Italic, "
                    "Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, "
                    "Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n")
            f.write(f"Style: Default,Arial,{fontsize},&H00FFFFFF,&H80000000,0,0,0,0,100,100,0,0,"
                    f"3,2,0,2,30,30,{margin_v},1\n\n")

            f.write("[Events]\n")
            f.write("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n")

            for seg in result["segments"]:
                text = seg["text"].strip().replace("\n", " ")
                if seg.get("no_speech_prob", 0.0) > 0.6: continue
                if seg.get("avg_logprob", -10.0) < -1.2: continue
                if text == "": continue
                if re.match(r"^\[\(?[^\]]+\)?\]$", text): continue

                start = format_time_ass(seg["start"])
                end = format_time_ass(seg["end"])
                f.write(f"Dialogue: 0,{start},{end},Default,,0,0,0,,{text}\n")

        # 5. 영상에 자막 입히기
        subprocess.run([
            "ffmpeg", "-y", "-i", video_path,
            "-vf", f"ass={ffmpeg_ass_path}",
            "-c:v", "libx264", "-c:a", "aac", "-b:a", "192k", output_path
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        return output_path

    finally:
        # ✅ 예외가 발생해도 임시 파일 정리
        for path in [audio_path, ass_path]:
            try:
                if os.path.exists(path):
                    os.remove(path)
            except Exception as e:
                print(f"⚠️ 임시 파일 삭제 중 오류: {e}")
