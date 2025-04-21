from transformers import pipeline
from sentence_transformers import SentenceTransformer, util
import torch

class RAGEngine:
    def __init__(self):
        self.qa_pipeline = pipeline("question-answering", model="distilbert-base-uncased-distilled-squad")
        self.embedder = SentenceTransformer('all-MiniLM-L6-v2')

        # Load context nội bộ
        with open("data/knowledge.txt", "r", encoding="utf-8") as f:
            self.documents = f.readlines()
        self.doc_embeddings = self.embedder.encode(self.documents, convert_to_tensor=True)

    def ask(self, question):
        question_embedding = self.embedder.encode(question, convert_to_tensor=True)
        scores = util.cos_sim(question_embedding, self.doc_embeddings)[0]
        top_k = torch.topk(scores, k=1)
        best_context = self.documents[top_k.indices[0]]

        result = self.qa_pipeline(question=question, context=best_context)
        return result["answer"]
