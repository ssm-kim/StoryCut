import os
import subprocess
from uuid import uuid4
from fastapi import UploadFile
from typing import List
from botocore.exceptions import BotoCoreError, ClientError
from app.core.config import settings
from app.dependencies.s3 import get_s3_client

UPLOAD_DIR = "app/images"
VIDEO_DIR = "app/videos"
os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(VIDEO_DIR, exist_ok=True)


# === 이미지 로컬 저장 ===
def save_uploaded_images(files: List[UploadFile]) -> List[str]:
    saved_urls = []

    try:
        for file in files:
            ext = file.filename.split(".")[-1]
            filename = f"{uuid4().hex}.{ext}"
            file_path = os.path.join(UPLOAD_DIR, filename)

            with open(file_path, "wb") as buffer:
                buffer.write(file.file.read())

            saved_urls.append(f"{UPLOAD_DIR}/{filename}")
        return saved_urls

    except Exception as e:
        raise RuntimeError(f"이미지 저장 중 오류 발생: {str(e)}")


# === 영상 로컬 저장 (파일명 반환) ===
def save_uploaded_video_local(file: UploadFile) -> str:
    try:
        ext = file.filename.split(".")[-1]
        filename = f"video_{uuid4().hex}.{ext}"
        file_path = os.path.join(VIDEO_DIR, filename)

        with open(file_path, "wb") as buffer:
            buffer.write(file.file.read())

        return file_path  

    except Exception as e:
        raise RuntimeError(f"로컬 영상 저장 실패: {str(e)}")


# === S3 영상 저장 (파일명 지정) ===
def save_uploaded_video(local_path: str, filename: str, s3_client=None) -> str:
    if s3_client is None:
        s3_client = get_s3_client()

    try:
        with open(local_path, "rb") as f:
            s3_key = f"videos/{filename}"
            s3_client.upload_fileobj(
                f,
                settings.S3_BUCKET_NAME,
                s3_key,
                ExtraArgs={"ContentType": "video/mp4"}
            )
        return f"https://{settings.S3_BUCKET_NAME}.s3.{settings.AWS_REGION}.amazonaws.com/{s3_key}"

    except (BotoCoreError, ClientError, Exception) as e:
        raise RuntimeError(f"S3 업로드 실패: {str(e)}")

    
# === 썸네일 생성 및 S3 업로드 ===
def generate_and_upload_thumbnail(video_path: str, s3_client=None) -> str:
    try:
        thumbnail_filename = f"thumb_{uuid4().hex}.jpg"
        thumbnail_path = os.path.join(UPLOAD_DIR, thumbnail_filename)

        # ffmpeg로 썸네일 생성 (1초 지점 기준)
        command = [
            "ffmpeg", "-y", "-i", video_path,
            "-ss", "00:00:01.000",
            "-vframes", "1",
            thumbnail_path
        ]
        subprocess.run(command, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

        # S3 업로드
        if s3_client is None:
            s3_client = get_s3_client()

        s3_key = f"thumbnails/{thumbnail_filename}"
        with open(thumbnail_path, "rb") as f:
            s3_client.upload_fileobj(
                f,
                settings.S3_BUCKET_NAME,
                s3_key,
                ExtraArgs={"ContentType": "image/jpeg"}
            )

        return f"https://{settings.S3_BUCKET_NAME}.s3.{settings.AWS_REGION}.amazonaws.com/{s3_key}"

    except subprocess.CalledProcessError:
        raise RuntimeError("❌ ffmpeg 스틸컷 생성 실패")
    except (BotoCoreError, ClientError, Exception) as e:
        raise RuntimeError(f"썸네일 처리 실패: {str(e)}")
