FROM python:3.10-slim

ENV PYTHONUNBUFFERED=1
ENV TRANSFORMERS_CACHE=/app/.cache

RUN apt-get update && apt-get install -y build-essential git && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .

RUN pip install --upgrade pip && \
    pip install --prefer-binary torch==2.1.0 && \
    pip install --prefer-binary -r requirements.txt

COPY . .

CMD ["python", "app.py"]
