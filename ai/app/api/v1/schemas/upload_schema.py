from pydantic import BaseModel, Field
from typing import List, Optional


class ImageUploadResult(BaseModel):
    image_urls: List[str] = Field(..., alias="imageUrls")

    class Config:
        allow_population_by_field_name = True


class VideoUploadResult(BaseModel):
    video_id: int = Field(..., alias="videoId")

    class Config:
        allow_population_by_field_name = True


class ImageUploadResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[ImageUploadResult]

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "이미지 업로드 성공",
                "result": {
                    "imageUrls": [
                        "app/images/db6ae97fc8e4443b92bdb782162e474f.png"
                    ]
                }
            }
        }


class VideoUploadResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[VideoUploadResult]

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "영상 업로드 성공",
                "result": {
                    "videoId": 1
                }
            }
        }


class ErrorResponse(BaseModel):
    is_success: bool = Field(default=False, alias="isSuccess", example=False)
    code: int = Field(..., example=400)
    message: str = Field(..., example="에러 메시지")
    result: Optional[None] = Field(default=None, example=None)

    class Config:
        allow_population_by_field_name = True
        schema_extra = {
            "example": {
                "isSuccess": False,
                "code": 400,
                "message": "에러 메시지",
                "result": None
            }
        }
