import httpx
from app.api.v1.schemas.post_schema import UploadRequest, UploadResponse, CompleteRequest, CompleteResponse

BASE_URL = "https://k12d108.p.ssafy.io/api/v1/spring"
# BASE_URL = "http://localhost:8080/api/v1/spring"
async def post_video_to_springboot_upload(
    token: str,
    payload: UploadRequest
) -> UploadResponse:
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(
            f"{BASE_URL}/video",
            headers=headers,
            json=payload.dict(by_alias=True)
        )

    response.raise_for_status()
    return UploadResponse.parse_obj(response.json())


async def post_video_to_springboot_complete(
    token: str,
    payload: CompleteRequest
) -> CompleteResponse:
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    async with httpx.AsyncClient() as client:
        response = await client.patch(
            f"{BASE_URL}/video/complete",
            headers=headers,
            json=payload.dict(by_alias=True)
        )

    response.raise_for_status()
    return CompleteResponse.parse_obj(response.json())


async def get_video_from_springboot(
    video_id: int,
    token: str
) -> CompleteResponse:
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{BASE_URL}/video/{video_id}",
            headers=headers
        )

    response.raise_for_status()
    return CompleteResponse.parse_obj(response.json())
