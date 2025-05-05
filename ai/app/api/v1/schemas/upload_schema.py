from pydantic import BaseModel, Field
from typing import List, Optional

class ImageUploadResult(BaseModel):
    imageUrls: List[str]

class VideoUploadResult(BaseModel):
    originalVideoUrl: str

class ImageUploadResponse(BaseModel):
    isSuccess: bool
    code: int
    message: str
    result: Optional[ImageUploadResult]

    class Config:
        json_schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "이미지 업로드 성공",
                "result": {
                    "imageUrls": ["app/images/db6ae97fc8e4443b92bdb782162e474f.png"]
                }
            }
        }

class VideoUploadResponse(BaseModel):
    isSuccess: bool
    code: int
    message: str
    result: Optional[VideoUploadResult]

    class Config:
        json_schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "영상 업로드 성공",
                "result": {
                    "originalVideoUrl": "https://s3.amazonaws.com/bucket/video.mp4"
                }
            }
        }

class ErrorResponse(BaseModel):
    isSuccess: bool = Field(default=False, example=False)
    code: int = Field(..., example=400)
    message: str = Field(..., example="에러 메시지")
    result: Optional[None] = Field(default=None, example=None)

    class Config:
        json_schema_extra = {
            "example": {
                "isSuccess": False,
                "code": 400,
                "message": "에러 메시지",
                "result": None
            }
        }

