import asyncio

async def cron_update(rag):
    while True:
        print("[CronJob] Updating knowledge base from Product Service...")
        await rag.update_from_product_service()
        await asyncio.sleep(3600)  # 1h
