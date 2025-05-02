from typing import List, Union
from fastapi import APIRouter, UploadFile, File, Depends, HTTPException
from fastapi.responses import JSONResponse

from app.api.v1.services.upload_service import (
    save_uploaded_images,
    save_uploaded_video,
)
from app.dependencies.s3 import get_s3_client
from app.api.v1.schemas.upload_schema import (
    ImageUploadResponse,
    VideoUploadResponse,
    LocalErrorResponse,
    S3ErrorResponse
)

router = APIRouter()

@router.post(
    "/images",
    response_model=ImageUploadResponse,
    responses={
        400: {
            "model": LocalErrorResponse,
            "description": "이미지 업로드 실패: 파일이 없거나 잘못된 형식입니다."
        }
    },
    summary="이미지 업로드"
)
async def upload_images(files: List[UploadFile] = File(...)):
    try:
        image_urls = save_uploaded_images(files)
        return {
            "isSuccess": True,
            "code": 200,
            "message": "요청에 성공하였습니다.",
            "result": {"imageUrls": image_urls},
        }
    except ValueError as e:
        raise HTTPException(
            status_code=400,
            detail=f"이미지 업로드 실패: {str(e)}"
        )

@router.post(
    "/video",
    response_model=VideoUploadResponse,
    responses={
        500: {
            "model": S3ErrorResponse,
            "description": "S3 업로드 실패: 인증 오류, 네트워크 문제 등"
        }
    },
    summary="영상 업로드"
)
async def upload_video(file: UploadFile = File(...), s3_client=Depends(get_s3_client)):
    try:
        video_url = save_uploaded_video(file, s3_client)
        return {
            "isSuccess": True,
            "code": 200,
            "message": "요청에 성공하였습니다.",
            "result": {"originalVideoUrl": video_url},
        }
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"S3 업로드 실패: {str(e)}"
        )
