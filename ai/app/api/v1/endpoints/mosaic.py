import os
import shutil
from uuid import uuid4
from app.api.v1.services.upload_service import save_uploaded_images
from fastapi import APIRouter, UploadFile, File, Form, BackgroundTasks
from fastapi.responses import FileResponse
from typing import List
from app.api.v1.services.mosaic_service import run_mosaic_pipeline  # ✅ 서비스 임포트
UPLOAD_DIR = "app/vimosaic"
router = APIRouter()  # ✅ 라우터 정의
@router.post("/process-video/")
async def process_video_endpoint(
    background_tasks: BackgroundTasks,
    video_file: UploadFile = File(...),
    target_images: List[UploadFile] = File(...),  # ✅ 여러 이미지 받기
    detect_interval: int = Form(5),
    num_segments: int = Form(3)
):
    os.makedirs(UPLOAD_DIR, exist_ok=True)

    video_filename = f"{uuid4().hex}_{video_file.filename}"
    video_path = os.path.join(UPLOAD_DIR, video_filename)

    with open(video_path, "wb") as vf:
        shutil.copyfileobj(video_file.file, vf)

    # ✅ 여러 타깃 이미지 저장
    target_paths = save_uploaded_images(target_images[:2])  # 최대 2개만 허용

    def mosaic_task():
        run_mosaic_pipeline(
            input_path=video_path,
            target_paths=target_paths,
            detect_interval=detect_interval,
            num_segments=num_segments
        )

    background_tasks.add_task(mosaic_task)

    return {
        "message": "🎥 영상 처리 중입니다.",
        "video": video_filename,
        "targets": [os.path.basename(p) for p in target_paths]
    }