from fastapi import FastAPI
from pydantic import BaseModel
import grpc
import proto.ai_pb2_grpc as ai_pb2_grpc
import proto.ai_pb2 as ai_pb2

app = FastAPI()

channel = grpc.insecure_channel('localhost:50051')
stub = ai_pb2_grpc.AIServiceStub(channel)

class AskRequest(BaseModel):
    question: str

class AskResponse(BaseModel):
    answer: str

@app.post("/api/ai/ask", response_model=AskResponse)
async def ask_ai(request: AskRequest):
    response = stub.Ask(ai_pb2.AIResquest(question=request.question))
    return AskResponse(answer=response.answer)
