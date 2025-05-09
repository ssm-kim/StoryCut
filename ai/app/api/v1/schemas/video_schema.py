from pydantic import BaseModel, Field
from typing import List, Optional
from app.api.v1.schemas.post_schema import PostResult
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
    result: Optional[PostResult] 

    class Config:
        validate_by_name = True
        populate_by_name = True
