import os
from dotenv import load_dotenv

# 📌 .env 경로를 현재 파일 기준으로 상위에서 명확히 지정
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ENV_PATH = os.path.join(BASE_DIR, ".env")
load_dotenv(dotenv_path=ENV_PATH)


class Settings:
    """
    🔧 .env 파일 기반 설정 클래스
    AWS, DB 등 프로젝트 전역 환경 변수들을 여기에서 관리
    """

    def __init__(self):
        # ☁️ AWS S3 설정
        self.AWS_REGION = os.getenv("AWS_REGION", "ap-northeast-2")
        self.S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME", "my-shortcut-bucket")
        self.AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
        self.AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")


settings = Settings()
