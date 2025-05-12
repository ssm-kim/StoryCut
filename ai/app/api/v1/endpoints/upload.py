import os
from fastapi import APIRouter, UploadFile, File, HTTPException, Header
from typing import List

# 비동기 서비스 로직 불러오기
from app.api.v1.services.upload_service import (
    save_uploaded_images,
    save_uploaded_video_local,
    save_uploaded_video,
    generate_and_upload_thumbnail,
    save_uploaded_image
)
from app.api.v1.services.springboot_service import post_video_to_springboot
from app.api.v1.schemas.upload_schema import (
    ImageUploadResponse, ErrorResponse,
    ImageUploadResult,VideoUploadResponse,
    ImageUploadResponse,RoomThumbnailResponse,
    RoomThumbnailResult
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
        image_urls = await save_uploaded_images(files)
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

        local_path = await save_uploaded_video_local(file)
        video_name = os.path.basename(local_path)
        thumbnail_url = await generate_and_upload_thumbnail(local_path)
        s3_url = await save_uploaded_video(local_path, video_name)

        payload = PostRequest(
            video_name=video_name,
            video_url=s3_url,
            thumbnail=thumbnail_url,
            original_video_id=None,
            is_blur=False
        )
        spring_response = await post_video_to_springboot(token, payload)

        return VideoUploadResponse(
            is_success=True,
            code=200,
            message="영상 업로드 성공",
            result=spring_response.result
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post(
    "/room-thumbnails",
    response_model=RoomThumbnailResponse,
    responses={400: {"model": ErrorResponse, "description": "룸 썸네일 업로드 실패"}},
    summary="룸 썸네일 이미지 업로드"
)
async def upload_room_thumbnail(file: UploadFile = File(...)):
    try:
        uploaded_url = await save_uploaded_image(file)
        return RoomThumbnailResponse(
            is_success=True,
            code=200,
            message="룸 썸네일 업로드 성공",
            result=RoomThumbnailResult(url=uploaded_url)
        )
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"룸 썸네일 업로드 실패: {str(e)}")