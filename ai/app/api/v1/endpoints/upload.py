import os
from fastapi import APIRouter, UploadFile, File, HTTPException, Header
from typing import List

from app.api.v1.services.upload_service import (
    save_uploaded_images,
    save_uploaded_video_local,
    save_uploaded_video,
    generate_and_upload_thumbnail 
)
from app.api.v1.services.springboot_service import post_video_to_springboot
from app.api.v1.schemas.upload_schema import (
    ImageUploadResponse, VideoUploadResponse, ErrorResponse,
    ImageUploadResult, VideoUploadResult
)
from app.api.v1.schemas.post_schema import PostRequest 

router = APIRouter()


@router.post(
    "/images",
    response_model=ImageUploadResponse,
    responses={400: {"model": ErrorResponse, "description": "이미지 업로드 실패"}},
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
        raise HTTPException(status_code=400, detail=f"이미지 업로드 실패: {str(e)}")


@router.post("/videos", response_model=VideoUploadResponse, summary="영상 업로드")
async def upload_video(
    file: UploadFile = File(...),
    authorization: str = Header(..., alias="Authorization")
):
    token = authorization.replace("Bearer ", "")
    
    try:
        # 1. 로컬 저장
        local_path = save_uploaded_video_local(file)

        # 2. 썸네일 생성 + S3 업로드
        thumbnail_url = generate_and_upload_thumbnail(local_path)
        video_name= os.path.basename(local_path)
        # 3. 영상 S3 업로드
        s3_url = save_uploaded_video(local_path, video_name)

        # 4. Spring Boot 전송 - PostRequest 활용
        payload = PostRequest(
            video_name=video_name,
            video_url=s3_url,
            thumbnail=thumbnail_url,
            original_video_id=None,
            is_blur=False
        )

        spring_response = await post_video_to_springboot(token, payload)

        # 5. 응답
        return VideoUploadResponse(
            is_success=True,
            code=200,
            message="영상 업로드 성공",
            result=VideoUploadResult(video_id=spring_response.result.video_id)
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
