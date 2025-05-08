from transformers import pipeline
from sentence_transformers import SentenceTransformer, util
import torch
import httpx
import asyncio

class RAGEngine:
    def __init__(self):
        self.qa_pipeline = pipeline("question-answering", model="distilbert-base-uncased-distilled-squad")
        self.embedder = SentenceTransformer('all-MiniLM-L6-v2')
        self.documents = []
        self.doc_embeddings = None
        self.knowledge_file = "data/knowledge.txt"

        # Load initial static knowledge
        self.load_knowledge_base()

    def load_knowledge_base(self):
        try:
            with open(self.knowledge_file, "r", encoding="utf-8") as f:
                self.documents = f.readlines()
            self.doc_embeddings = self.embedder.encode(self.documents, convert_to_tensor=True)
            print(f"[RAGEngine] Loaded {len(self.documents)} documents from static knowledge.txt")
        except Exception as e:
            print(f"[RAGEngine] Failed to load knowledge.txt: {e}")

    async def update_from_product_service(self):
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get("http://product-service:8082/api/products", timeout=5.0)
                response.raise_for_status()
                products = response.json()
                print(f"[RAGEngine] Fetched {len(products)} products from Product Service.")

                self.documents = [f"{prod['name']} - {prod['description']}" for prod in products]
                self.doc_embeddings = self.embedder.encode(self.documents, convert_to_tensor=True)

        except Exception as e:
            print(f"[RAGEngine] Failed to fetch product-service: {e}")
            # Fallback static file
            self.load_knowledge_base()

    def ask(self, question):
        if not self.documents:
            return "Xin lỗi, tôi hiện không có dữ liệu để trả lời."

        question_embedding = self.embedder.encode(question, convert_to_tensor=True)
        scores = util.cos_sim(question_embedding, self.doc_embeddings)[0]
        top_k = torch.topk(scores, k=1)
        best_context = self.documents[top_k.indices[0]]

        result = self.qa_pipeline(question=question, context=best_context)
        return result["answer"]
