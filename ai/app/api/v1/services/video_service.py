import os
import httpx
from app.api.v1.services.springboot_service import get_video_from_springboot
from app.api.v1.services.mosaic_service import run_mosaic_pipeline
from app.api.v1.services.subtitle_service import subtitles

async def download_video_to_local(videoUrl: str, save_path: str):
    async with httpx.AsyncClient() as client:
        response = await client.get(videoUrl)
        if response.status_code != 200:
            raise RuntimeError(f"영상 다운로드 실패: {response.status_code} - {response.text}")
        with open(save_path, "wb") as f:
            f.write(response.content)

async def process_video_job(
    prompt: str,
    video_id: int,
    images: list,
    subtitle: bool,
    token: str,
) -> str:

    video_info = await get_video_from_springboot(video_id, token)
    video_name = video_info.result.video_name
    video_path = os.path.join("app/videos", video_name)
    new_video_path = None
    is_blur = False

    if not os.path.isfile(video_path):
        print(f"{video_name} 다운로드 중...")
        await download_video_to_local(video_info.result.video_url, video_path)


    if images:
        new_video_path = await run_mosaic_pipeline(video_path, images, 5, 3)
        os.remove(video_path)
        video_path = new_video_path
        is_blur = True


    if subtitle:
        new_video_path = await subtitles(video_path)
        video_path = new_video_path

    return video_path, is_blur
