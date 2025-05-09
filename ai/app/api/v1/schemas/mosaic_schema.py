from pydantic import BaseModel, Field
from typing import List, Optional

# 요청 스키마 (camelCase → snake_case 매핑)
class MosaicRequest(BaseModel):
    original_video_url: str = Field(..., alias="originalVideoUrl", example="app/videos/sample.mp4")
    images: List[str] = Field(..., example=[
        "app/images/face1.jpg",
        "app/images/face2.jpg"
    ])

    class Config:
        validate_by_name = True
        populate_by_name = True

# 응답 내림 구조
class ProcessedVideoResult(BaseModel):
    video_url: str = Field(..., alias="videoUrl", example="https://your-bucket.s3.ap-northeast-2.amazonaws.com/videos/abc123_video.mp4")
    class Config:
        validate_by_name = True
        populate_by_name = True


# 응답 스키마 전체
class ProcessedVideoResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess", example=True)
    code: int = Field(..., example=200)
    message: str = Field(..., example="영상 처리가 완료되었습니다.")
    result: Optional[ProcessedVideoResult]

    class Config:
        validate_by_name = True
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": " 영상 처리가 완료되었습니다.",
                "result": {
                    "videoUrl": "https://your-bucket.s3.ap-northeast-2.amazonaws.com/videos/abc123_video.mp4"
                }
            }
        }