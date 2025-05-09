from pydantic import BaseModel, Field
from typing import List, Optional

# 요청 스키마
class MosaicRequest(BaseModel):
    original_video_url: str = Field(..., alias="originalVideoUrl", example="app/videos/sample.mp4")
    images: List[str] = Field(..., example=[
        "app/images/face1.jpg",
        "app/images/face2.jpg"
    ])

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "originalVideoUrl": "app/videos/sample.mp4",
                "images": [
                    "app/images/face1.jpg",
                    "app/images/face2.jpg"
                ]
            }
        }

# 응답 result 구조
class ProcessedVideoResult(BaseModel):
    video_url: str = Field(..., alias="videoUrl", example="https://your-bucket.s3.ap-northeast-2.amazonaws.com/videos/abc123_video.mp4")

    class Config:
        allow_population_by_field_name = True


# 전체 응답 구조
class ProcessedVideoResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess", example=True)
    code: int = Field(..., example=200)
    message: str = Field(..., example="영상 처리가 완료되었습니다.")
    result: Optional[ProcessedVideoResult]

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "영상 처리가 완료되었습니다.",
                "result": {
                    "videoUrl": "https://your-bucket.s3.ap-northeast-2.amazonaws.com/videos/abc123_video.mp4"
                }
            }
        }
