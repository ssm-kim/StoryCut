import os
import logging
from fastapi import APIRouter, Header, HTTPException
from app.api.v1.services.mosaic_service import run_mosaic_pipeline
from app.api.v1.schemas.mosaic_schema import MosaicRequest
from app.api.v1.schemas.video_schema import VideoPostResponse
from app.api.v1.services.springboot_service import get_video_from_springboot, post_video_to_springboot
from app.api.v1.services.upload_service import generate_and_upload_thumbnail, save_uploaded_video
from app.api.v1.schemas.post_schema import PostRequest


logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

router = APIRouter()

@router.post("", response_model=VideoPostResponse, summary="모자이크")
async def process_video_from_json(
    request: MosaicRequest,
    authorization: str = Header(...)
):
    try:
        token = authorization.replace("Bearer ", "")
        video_info = await get_video_from_springboot(request.video_id, token)

        if video_info.result.is_blur:
            raise HTTPException(
                status_code=400,
                detail="이미 모자이크 처리된 영상입니다."
            )

        video_path = await run_mosaic_pipeline(
            input_path=video_info.result.video_url,
            target_paths=request.images[:2],
            detect_interval=5,
            num_segments=3
        )
        video_name = os.path.basename(video_path)
        logger.info(f"처리된 영상 경로: {video_path}")

        thumbnail_url = await generate_and_upload_thumbnail(video_path)
        logger.info(f"썸네일 URL: {thumbnail_url}")

        s3_url = await save_uploaded_video(video_path, video_name)
        logger.info(f"영상 S3 URL: {s3_url}")

        payload = PostRequest(
            video_name=video_name,
            video_url=s3_url,
            thumbnail=thumbnail_url,
            original_video_id=request.video_id,
            is_blur=True
        )
        spring_response = await post_video_to_springboot(token, payload)

        return VideoPostResponse(
            is_success=True,
            code=200,
            message="영상 처리 완료",
            result=spring_response.result
        )

    except Exception as e:
        logger.exception("예외 발생:")
        raise HTTPException(status_code=500, detail=f"처리 중 오류 발생: {str(e)}")
