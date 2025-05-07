from pydantic import BaseModel, Field
from typing import Optional


class VideoPostResult(BaseModel):
    video_id: int = Field(..., alias="videoId")
    member_id: int = Field(..., alias="memberId")
    video_name: str = Field(..., alias="videoName")
    video_url: str = Field(..., alias="videoUrl")
    thumbnail: str
    original_video_id: Optional[int] = Field(None, alias="originalVideoId")
    created_at: str = Field(..., alias="createdAt")
    updated_at: str = Field(..., alias="updatedAt")

    class Config:
        populate_by_name = True
        orm_mode = True


class VideoPostResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[VideoPostResult]

    class Config:
        populate_by_name = True
        validate_by_name = True
        json_schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "요청에 성공하였습니다.",
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
