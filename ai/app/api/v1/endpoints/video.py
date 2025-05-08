import os
from app.api.v1.services.upload_service import generate_and_upload_thumbnail, save_uploaded_video
from app.api.v1.schemas.post_schema import PostRequest
from app.api.v1.services.springboot_service import post_video_to_springboot
from fastapi import APIRouter, Header, HTTPException, Depends
from app.api.v1.schemas.video_schema import VideoPostResponse, VideoProcessRequest
from app.api.v1.services.video_service import process_video_job

router = APIRouter()

@router.post("", response_model=VideoPostResponse, summary="영상 요약 + 모자이크 + 자막 처리")
async def process_video(
    request: VideoProcessRequest,
    authorization: str = Header(...)
):
    token = authorization.replace("Bearer ", "")
    try:
        

        video_path, is_blur  = await process_video_job(
            prompt=request.prompt,
            video_id=request.video_id,
            images=request.images,
            subtitle=request.subtitle,
            token=token
        )
        video_name=os.path.basename(video_path)                                                  
        thumbnail_url = generate_and_upload_thumbnail(video_path)

        # 3. 영상 S3 업로드
        s3_url = save_uploaded_video(video_path, video_name)

        # 4. Spring Boot 전송 - PostRequest 활용
        payload = PostRequest(
            video_name=video_name,
            video_url=s3_url,
            thumbnail=thumbnail_url,
            original_video_id=request.video_id,
            is_blur=is_blur
        )

        spring_response = await post_video_to_springboot(token, payload)

        return VideoPostResponse(
            is_success=True,
            code=200,
            message="🎬 영상 처리 완료",
            video_id=spring_response.result.video_id
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"서버 오류: {str(e)}")
