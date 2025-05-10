import os
import logging
from fastapi import APIRouter, Header, HTTPException
from app.api.v1.services.upload_service import (
    generate_and_upload_thumbnail,
    save_uploaded_video
)
from app.api.v1.schemas.post_schema import PostRequest
from app.api.v1.services.springboot_service import post_video_to_springboot
from app.api.v1.schemas.video_schema import VideoPostResponse, VideoProcessRequest
from app.api.v1.services.video_service import process_video_job

# 로깅 설정
logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

router = APIRouter()

@router.post("", response_model=VideoPostResponse, summary="영상 요약 + 모자이크 + 자막 처리")
async def process_video(
    request: VideoProcessRequest,
    authorization: str = Header(...)
):
    token = authorization.replace("Bearer ", "")
    try:
        logger.info("영상 처리 시작")

        video_path, is_blur = await process_video_job(
            prompt=request.prompt,
            video_id=request.video_id,
            images=request.images,
            subtitle=request.subtitle,
            token=token
        )
        video_name = os.path.basename(video_path)
        logger.info(f"처리된 영상 경로: {video_path}")

        thumbnail_url = await generate_and_upload_thumbnail(video_path)
        logger.info(f"썸네일 URL: {thumbnail_url}")

        s3_url = await save_uploaded_video(video_path, video_name)
        logger.info(f" 영상 S3 URL: {s3_url}")

        payload = PostRequest(
            video_name=video_name,
            video_url=s3_url,
            thumbnail=thumbnail_url,
            original_video_id=request.video_id,
            is_blur=is_blur
        )
        spring_response = await post_video_to_springboot(token, payload)
        logger.info("SpringBoot 업로드 완료")

        # 5. 클라이언트 응답 반환
        return VideoPostResponse(
            is_success=True,
            code=200,
            message=" 영상 처리 완료",
            result=spring_response.result
        )

    except Exception as e:
        logger.exception("서버 오류 발생:")
        raise HTTPException(status_code=500, detail=f"서버 오류: {str(e) or '알 수 없는 오류 발생'}")
