import os
from uuid import uuid4
from fastapi import UploadFile
from typing import List
import boto3
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
                buffer.write(file.file.read())  # 메모리에서 바로 저장

            saved_urls.append(f"{UPLOAD_DIR}/{filename}")
        return saved_urls

    except Exception as e:
        raise RuntimeError(f"이미지 저장 중 오류 발생: {str(e)}")

# === 영상 로컬 저장 ===
def save_uploaded_video_local(file: UploadFile) -> str:
    try:
        ext = file.filename.split(".")[-1]
        filename = f"video_{uuid4().hex}.{ext}"
        file_path = os.path.join(VIDEO_DIR, filename)

        with open(file_path, "wb") as buffer:
            buffer.write(file.file.read())

        return file_path.replace("\\", "/")

    except Exception as e:
        raise RuntimeError(f"로컬 영상 저장 실패: {str(e)}")

# === S3 영상 저장 ===
def save_uploaded_video(file: UploadFile, s3_client=None) -> str:
    ext = file.filename.split(".")[-1]
    filename = f"videos/video_{uuid4().hex}.{ext}"

    if s3_client is None:
        s3_client = get_s3_client()  # 기본값으로 의존성 직접 해결

    try:
        s3_client.upload_fileobj(
            file.file,
            settings.S3_BUCKET_NAME,
            filename,
            ExtraArgs={"ContentType": file.content_type}
        )
        return f"https://{settings.S3_BUCKET_NAME}.s3.{settings.AWS_REGION}.amazonaws.com/{filename}"

    except (BotoCoreError, ClientError, Exception) as e:
        raise RuntimeError(f"S3 업로드 실패: {str(e)}")
