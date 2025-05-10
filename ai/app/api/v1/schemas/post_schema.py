from pydantic import BaseModel, Field
from typing import Optional


class PostRequest(BaseModel):
    video_name: str = Field(..., alias="videoName")
    video_url: str = Field(..., alias="videoUrl")
    thumbnail: str
    original_video_id: Optional[int] = Field(None, alias="originalVideoId")
    is_blur: bool = Field(..., alias="isBlur")

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "videoName": "영상 제목",
                "videoUrl": "https://example.com/video.mp4",
                "thumbnail": "https://example.com/thumbnail.jpg",
                "originalVideoId": None,
                "isBlur": False
            }
        }


class PostResult(BaseModel):
    video_id: int = Field(..., alias="videoId")
    member_id: int = Field(..., alias="memberId")
    video_name: str = Field(..., alias="videoName")
    video_url: str = Field(..., alias="videoUrl")
    thumbnail: str
    original_video_id: Optional[int] = Field(None, alias="originalVideoId")
    created_at: str = Field(..., alias="createdAt")
    updated_at: str = Field(..., alias="updatedAt")
    is_blur: bool = Field(False, alias="isBlur")
    class Config:
        allow_population_by_field_name = True
        orm_mode = True


class PostResponse(BaseModel):
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
                "message": "요청에 성공하였습니다.",
                "result": {
                    "videoId": 1,
                    "memberId": 1,
                    "videoName": "영상 제목",
                    "videoUrl": "https://example.com/video.mp4",
                    "thumbnail": "https://example.com/thumbnail.jpg",
                    "originalVideoId": None,
                    "createdAt": "2025-05-02T08:54:00.000Z",
                    "updatedAt": "2025-05-02T08:54:00.000Z",
                    "is_blur":True
                }
            }
        }
