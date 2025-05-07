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

router = APIRouter()


# ✅ 이미지 업로드
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


# ✅ 영상 업로드 (썸네일 포함)
@router.post("/upload-video/", response_model=VideoUploadResponse)
async def upload_video(
    file: UploadFile = File(...),
    authorization: str = Header(..., alias="Authorization")
):
    try:
        # 1. 로컬 저장
        video_name = save_uploaded_video_local(file)

        # 2. 썸네일 생성 + S3 업로드
        thumbnail_url = generate_and_upload_thumbnail(video_name)

        # 3. 영상 S3 업로드
        file.file.seek(0)
        s3_url = save_uploaded_video(file, video_name)

        # 4. Spring Boot 전송
        spring_response = await post_video_to_springboot(
            token=authorization.replace("Bearer ", ""),
            video_name=video_name,
            video_url=s3_url,
            thumbnail_url=thumbnail_url,
            original_video_id=None,
            is_blur=False
        )

        # 5. 응답
        video_id = spring_response.result.video_id

        return VideoUploadResponse(
            is_success=True,
            code=200,
            message="영상 업로드 성공",
            result=VideoUploadResult(video_id=video_id)
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"❌ 처리 실패: {str(e)}")
