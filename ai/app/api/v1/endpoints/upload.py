from fastapi import APIRouter, UploadFile, File, Depends
from typing import List
import os
from uuid import uuid4
from ..services.upload_service import save_uploaded_images,save_uploaded_video

router = APIRouter()
@router.post("/images", summary="이미지 업로드")
async def upload_images(
    files: List[UploadFile] = File(...),
):
    try:
        image_urls = save_uploaded_images(files)

        return {
            "isSuccess": True,
            "code": 200,
            "message": "요청에 성공하였습니다.",
            "result": {"imageUrls": image_urls}
        }

    except RuntimeError as e:
        return JSONResponse(
            status_code=400,
            content={
                "isSuccess": False,
                "code": 400,
                "message": str(e),
                "result": None
            }
        )

@router.post("/video", summary="영상 업로드")
async def upload_video(file: UploadFile = File(...)):
    try:
        video_url = save_uploaded_video(file)
        return {
            "isSuccess": True,
            "code": 200,
            "message": "요청에 성공하였습니다.",
            "result": {"originalVideoUrl": video_url}
        }

    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={
                "isSuccess": False,
                "code": 500,
                "message": f"업로드 실패: {str(e)}",
                "result": None
            }
        )