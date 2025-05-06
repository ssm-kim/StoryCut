from fastapi import APIRouter, UploadFile, File, HTTPException
from typing import List

from app.api.v1.services.upload_service import save_uploaded_images, save_uploaded_video_local
from app.api.v1.schemas.upload_schema import (
    ImageUploadResponse, VideoUploadResponse, ErrorResponse,
    ImageUploadResult, VideoUploadResult
)

router = APIRouter()

@router.post(
    "/images",
    response_model=ImageUploadResponse,
    responses={
        400: {
            "model": ErrorResponse,
            "description": "이미지 업로드 실패: 파일이 없거나 잘못된 형식입니다."
        }
    },
    summary="이미지 업로드"
)
async def upload_images(files: List[UploadFile] = File(...)):
    try:
        image_urls = save_uploaded_images(files)
        return ImageUploadResponse(
            is_success=True,
            code=200,
            message="요청에 성공하였습니다.",
            result=ImageUploadResult(image_urls=image_urls)
        )
    except Exception as e:
        raise HTTPException(
            status_code=400,
            detail=f"이미지 업로드 실패: {str(e)}"
        )


@router.post(
    "/video",
    response_model=VideoUploadResponse,
    responses={
        400: {
            "model": ErrorResponse,
            "description": "로컬 영상 저장 실패"
        },
        500: {
            "model": ErrorResponse,
            "description": "서버 내부 오류"
        }
    },
    summary="영상 업로드 (로컬 저장)"
)
async def upload_video(file: UploadFile = File(...)):
    try:
        video_url = save_uploaded_video_local(file)
        return VideoUploadResponse(
            is_success=True,
            code=200,
            message="요청에 성공하였습니다.",
            result=VideoUploadResult(original_video_url=video_url)
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"로컬 업로드 실패: {str(e)}"
        )
