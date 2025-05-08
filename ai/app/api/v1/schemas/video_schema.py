from pydantic import BaseModel, Field
from typing import List, Optional

# 요청 스키마
class VideoProcessRequest(BaseModel):
    prompt: str
    video_id: int = Field(..., alias="videoId")
    images: Optional[List[str]] = None
    subtitle: bool

    class Config:
        validate_by_name = True
        populate_by_name = True

# 응답 스키마
class VideoPostResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    video_id: int = Field(..., alias="video_id")

    class Config:
        validate_by_name = True
        populate_by_name = True
