# ============================================
# ASR Service - Whisper Large V3
# FastAPI server for Speech-to-Text
# ============================================

# ============================================
# GPU variant (cần NVIDIA GPU)
# Build với: docker build --target production-gpu -t asr-service:gpu .
# ============================================
FROM nvidia/cuda:12.1.0-cudnn8-runtime-ubuntu22.04 AS production-gpu

# Tránh interactive prompts
ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

# Install Python và dependencies hệ thống
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.11 \
    python3.11-venv \
    python3-pip \
    ffmpeg \
    libsndfile1 \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && ln -sf /usr/bin/python3.11 /usr/bin/python \
    && ln -sf /usr/bin/python3.11 /usr/bin/python3

# Set working directory
WORKDIR /app

# Copy requirements trước để tận dụng Docker cache
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Tạo thư mục cho model cache
RUN mkdir -p /app/models /app/temp

# Environment variables
ENV MODEL_SIZE=large-v3
ENV DEVICE=auto
ENV COMPUTE_TYPE=float16
ENV HF_HOME=/app/models
ENV TRANSFORMERS_CACHE=/app/models

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

# Run server
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

# ============================================
# CPU-only variant (không cần GPU)
# Build với: docker build --target production-cpu -t asr-service:cpu .
# ============================================
FROM python:3.11-slim AS production-cpu

ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV DEBIAN_FRONTEND=noninteractive

# Install ffmpeg và build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    libsndfile1 \
    curl \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements-cpu.txt requirements.txt
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

RUN mkdir -p /app/models /app/temp

ENV MODEL_SIZE=large-v3
ENV DEVICE=cpu
ENV COMPUTE_TYPE=int8
ENV HF_HOME=/app/models

EXPOSE 6868

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:6868/health || exit 1

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "6868"]
