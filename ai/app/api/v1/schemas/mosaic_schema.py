from pydantic import BaseModel, Field
from typing import List, Optional

class ProcessedVideoResult(BaseModel):
    videoUrl: str = Field(..., example="https://your-bucket.s3.ap-northeast-2.amazonaws.com/vimosaic/abc123_video.mp4")

class ProcessedVideoResponse(BaseModel):
    isSuccess: bool = Field(..., example=True)
    code: int = Field(..., example=200)
    message: str = Field(..., example="🎬 영상 처리가 완료되었습니다.")
    result: Optional[ProcessedVideoResult]

    class Config:
        schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "🎬 영상 처리가 완료되었습니다.",
                "result": {
                    "videoUrl": "https://your-bucket.s3.ap-northeast-2.amazonaws.com/vimosaic/abc123_video.mp4",
                }
            }
        }
