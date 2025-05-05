from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from typing import List
import os, shutil
from uuid import uuid4

from app.api.v1.services.mosaic_service import run_mosaic_pipeline
from app.api.v1.services.upload_service import save_uploaded_images
from app.api.v1.schemas.mosaic_schema import ProcessedVideoResponse

router = APIRouter()

UPLOAD_DIR = "app/vimosaic"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/process-video/", response_model=ProcessedVideoResponse)
async def process_video_endpoint(
    video_file: UploadFile = File(...),
    target_images: List[UploadFile] = File(...),
    detect_interval: int = Form(5),
    num_segments: int = Form(3)
):
    try:
        # 1. 영상 저장
        video_filename = f"{uuid4().hex}_{video_file.filename}"
        video_path = os.path.join(UPLOAD_DIR, video_filename)
        with open(video_path, "wb") as vf:
            shutil.copyfileobj(video_file.file, vf)

        # 2. 타깃 이미지 저장 (최대 2개)
        target_paths = save_uploaded_images(target_images[:2])

        # 3. 모자이크 처리
        run_mosaic_pipeline(
            input_path=video_path,
            target_paths=target_paths,
            detect_interval=detect_interval,
            num_segments=num_segments
        )

        # 4. 성공 응답
        return {
            "isSuccess": True,
            "code": 200,
            "message": "🎬 영상 처리가 완료되었습니다.",
            "result": {
                "videoUrl": f"{video_filename}",
            }
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 중 오류 발생: {str(e)}")
