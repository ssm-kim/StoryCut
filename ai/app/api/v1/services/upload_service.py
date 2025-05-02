import os
from uuid import uuid4
from fastapi import UploadFile
from typing import List
import boto3
from botocore.exceptions import BotoCoreError, ClientError
from app.core.config import settings


UPLOAD_DIR = "app/images"
os.makedirs(UPLOAD_DIR, exist_ok=True)

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


# === S3 영상 저장 ===
s3_client = boto3.client(
    "s3",
    region_name=settings.AWS_REGION,
    aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
    aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
)

def save_uploaded_video(file: UploadFile) -> str:
    ext = file.filename.split(".")[-1]
    filename = f"videos/video_{uuid4().hex}.{ext}"

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