import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    # 📦 DB 설정
    # DB_HOST: str = os.getenv("DB_HOST", "localhost")
    # DB_PORT: str = os.getenv("DB_PORT", "3306")
    # DB_USERNAME: str = os.getenv("DB_USERNAME", "root")
    # DB_PASSWORD: str = os.getenv("DB_PASSWORD", "1234")
    # DB_NAME: str = os.getenv("DB_NAME", "storycut")

    @property
    def SQLALCHEMY_DATABASE_URL(self) -> str:
        return f"mysql+pymysql://{self.DB_USERNAME}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    # ☁️ S3 설정
    AWS_REGION: str = os.getenv("AWS_REGION", "ap-northeast-2")
    S3_BUCKET_NAME: str = os.getenv("S3_BUCKET_NAME", "my-shortcut-bucket")
    AWS_ACCESS_KEY_ID: str = os.getenv("AWS_ACCESS_KEY_ID")
    AWS_SECRET_ACCESS_KEY: str = os.getenv("AWS_SECRET_ACCESS_KEY")

settings = Settings()
