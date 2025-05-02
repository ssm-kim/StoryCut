from fastapi import FastAPI
from app.api.v1.endpoints import upload  
from fastapi.staticfiles import StaticFiles
app = FastAPI()

# video API 엔드포인트 등록

app.mount("/static", StaticFiles(directory="app"), name="static")

app.include_router(upload.router, prefix="/api/upload", tags=["upload"])
