from fastapi import FastAPI
from app.api.v1.endpoints import upload, video,mosaic
from fastapi.staticfiles import StaticFiles
from fastapi.openapi.utils import get_openapi

app = FastAPI()

# ✅ Bearer 인증 Swagger에 적용
def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema

    openapi_schema = get_openapi(
        title="Your API",
        version="1.0.0",
        description="API with Authorization header",
        routes=app.routes,
    )

    openapi_schema["components"]["securitySchemes"] = {
        "BearerAuth": {
            "type": "http",
            "scheme": "bearer",
            "bearerFormat": "JWT",
        }
    }

    for path in openapi_schema["paths"].values():
        for method in path.values():
            method.setdefault("security", [{"BearerAuth": []}])

    app.openapi_schema = openapi_schema
    return app.openapi_schema

# 🔽 Swagger에 커스텀 openapi 적용
app.openapi = custom_openapi

# ✅ static 파일 mount
app.mount("/static", StaticFiles(directory="app"), name="static")

# ✅ 라우터 등록
app.include_router(upload.router, prefix="/api/upload", tags=["upload"])
app.include_router(video.router, prefix="/api/videos", tags=["videos"])
app.include_router(mosaic.router, prefix="/api/mosaic", tags=["mosaic"])