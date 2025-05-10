from pydantic import BaseModel, Field
from typing import List, Optional
from app.api.v1.schemas.post_schema import PostResult

class VideoProcessRequest(BaseModel):
    prompt: str
    video_id: int = Field(..., alias="videoId")
    images: Optional[List[str]] = None
    subtitle: bool

    class Config:
        allow_population_by_field_name = True


class VideoPostResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[PostResult]

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "🎬 영상 처리 완료",
                "result": {
                    "videoId": 1,
                    "memberId": 1,
                    "videoName": "영상 제목",
                    "videoUrl": "https://example.com/video.mp4",
                    "thumbnail": "https://example.com/thumbnail.jpg",
                    "originalVideoId": None,
                    "createdAt": "2025-05-02T08:54:00.000Z",
                    "updatedAt": "2025-05-02T08:54:00.000Z"
                }
            }
        }
