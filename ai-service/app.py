import asyncio
from threading import Thread
import uvicorn
from fastapi_app import app as fastapi_app  # Chỗ bạn define FastAPI
from rag_engine import RAGEngine
from grpc_server import serve_grpc  # Đây là hàm bạn đã viết
from cron_job import cron_update  # Đây chính là hàm bạn viết

# Khởi tạo RAGEngine
rag = RAGEngine()

# Hàm chạy FastAPI
def start_fastapi():
    uvicorn.run(fastapi_app, host="0.0.0.0", port=8000)

async def main():
    # Chạy gRPC server và cron job cùng lúc
    await asyncio.gather(
        serve_grpc(rag),
        cron_update(rag)
    )

if __name__ == "__main__":
    # FastAPI chạy thread riêng
    Thread(target=start_fastapi).start()

    # gRPC + cron chạy asyncio
    asyncio.run(main())
