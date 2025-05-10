from fastapi import APIRouter, HTTPException
from app.api.v1.services.mosaic_service import run_mosaic_pipeline
from app.api.v1.schemas.mosaic_schema import ProcessedVideoResponse, MosaicRequest, ProcessedVideoResult

router = APIRouter()

@router.post("/process-video/", response_model=ProcessedVideoResponse)
async def process_video_from_json(request: MosaicRequest):
    try:
        output_path = await run_mosaic_pipeline(
            input_path=request.original_video_url,
            target_paths=request.images[:2],
            detect_interval=5,
            num_segments=3
        )

        return ProcessedVideoResponse(
            is_success=True,
            code=200,
            message="영상 처리가 완료되었습니다.",
            result=ProcessedVideoResult(video_url=output_path)
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 중 오류 발생: {str(e)}")
