import httpx
from app.api.v1.schemas.video_post_response_schema import VideoPostResponse

SPRINGBOOT_API_URL = "http://your-springboot-url/api/video"  # 실제 주소로 수정하세요


# ✅ 영상 등록 (POST)
async def post_video_to_springboot(
    token: str,
    video_name: str,
    video_url: str,
    thumbnail_url: str,
    original_video_id: int | None,
    is_blur: bool
) -> VideoPostResponse:
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    payload = {
        "videoName": video_name,
        "videoUrl": video_url,
        "thumbnail": thumbnail_url,
        "isBlur": is_blur
    }

    if original_video_id is not None:
        payload["originalVideoId"] = original_video_id

    async with httpx.AsyncClient() as client:
        response = await client.post(SPRINGBOOT_API_URL, headers=headers, json=payload)

    if response.status_code != 200:
        raise RuntimeError(f"Spring Boot API 실패: {response.status_code} - {response.text}")

    return VideoPostResponse.parse_obj(response.json())


# ✅ 영상 조회 (GET)
async def get_video_from_springboot(
    video_id: int,
    token: str
) -> VideoPostResponse:
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    async with httpx.AsyncClient() as client:
        response = await client.get(f"{SPRINGBOOT_API_URL}/{video_id}", headers=headers)

    if response.status_code != 200:
        raise RuntimeError(f"Spring Boot API 실패: {response.status_code} - {response.text}")

    return VideoPostResponse.parse_obj(response.json())
