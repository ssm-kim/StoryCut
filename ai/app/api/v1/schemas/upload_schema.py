from pydantic import BaseModel, Field
from typing import List, Optional


# ✅ 이미지 업로드 결과
class ImageUploadResult(BaseModel):
    image_urls: List[str] = Field(..., alias="imageUrls")

    class Config:
        populate_by_name = True


# ✅ 영상 업로드 결과
class VideoUploadResult(BaseModel):
    video_id: int = Field(..., alias="videoId")

    class Config:
        populate_by_name = True


# ✅ 이미지 업로드 응답
class ImageUploadResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[ImageUploadResult]

    class Config:
        validate_by_name = True
        populate_by_name = True
        json_schema_extra = {
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


# ✅ 영상 업로드 응답
class VideoUploadResponse(BaseModel):
    is_success: bool = Field(..., alias="isSuccess")
    code: int
    message: str
    result: Optional[VideoUploadResult]

    class Config:
        validate_by_name = True
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "isSuccess": True,
                "code": 200,
                "message": "영상 업로드 성공",
                "result": {
                    "videoId": 1
                }
            }
        }


# ✅ 공통 에러 응답
class ErrorResponse(BaseModel):
    is_success: bool = Field(default=False, alias="isSuccess", example=False)
    code: int = Field(..., example=400)
    message: str = Field(..., example="에러 메시지")
    result: Optional[None] = Field(default=None, example=None)

    class Config:
        validate_by_name = True
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "isSuccess": False,
                "code": 400,
                "message": "에러 메시지",
                "result": None
            }
        }
