import grpc
from concurrent import futures
import proto.ai_pb2_grpc as ai_pb2_grpc
import proto.ai_pb2 as ai_pb2

class AIServiceServicer(ai_pb2_grpc.AIServiceServicer):
    def __init__(self, rag):
        self.rag = rag

    def Ask(self, request, context):
        answer = self.rag.ask(request.question)
        return ai_pb2.AIResponse(answer=answer)

def serve_grpc(rag):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ai_pb2_grpc.add_AIServiceServicer_to_server(AIServiceServicer(rag), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("[gRPC Server] Running on port 50051...")
    server.wait_for_termination()
