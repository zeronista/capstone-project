"""
ASR Service - Whisper Large V3
FastAPI server for Speech-to-Text transcription

Sử dụng faster-whisper với CTranslate2 backend để tối ưu performance.
Hỗ trợ cả GPU (CUDA) và CPU.
"""

import os
import uuid
import time
import tempfile
import logging
from pathlib import Path
from typing import Optional, List
from contextlib import asynccontextmanager

from fastapi import FastAPI, UploadFile, File, HTTPException, Query, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings

from faster_whisper import WhisperModel

# ============================================
# Configuration
# ============================================

class Settings(BaseSettings):
    """Application settings từ environment variables"""
    
    # Model settings
    model_size: str = Field(default="large-v3", description="Whisper model size")
    device: str = Field(default="auto", description="Device: auto, cuda, cpu")
    compute_type: str = Field(default="float16", description="Compute type: float16, int8, float32")
    
    # Server settings
    max_file_size_mb: int = Field(default=100, description="Max upload file size in MB")
    temp_dir: str = Field(default="/app/temp", description="Temp directory for audio files")
    
    # Model cache
    model_cache_dir: str = Field(default="/app/models", description="Model cache directory")
    
    # Performance
    num_workers: int = Field(default=1, description="Number of worker threads")
    beam_size: int = Field(default=5, description="Beam size for decoding")
    
    class Config:
        env_file = ".env"


settings = Settings()

# ============================================
# Logging
# ============================================

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("asr-service")

# ============================================
# Global Model Instance
# ============================================

whisper_model: Optional[WhisperModel] = None


def load_model():
    """Load Whisper model vào memory"""
    global whisper_model
    
    logger.info(f"Loading Whisper model: {settings.model_size}")
    logger.info(f"Device: {settings.device}, Compute type: {settings.compute_type}")
    
    start_time = time.time()
    
    whisper_model = WhisperModel(
        settings.model_size,
        device=settings.device,
        compute_type=settings.compute_type,
        download_root=settings.model_cache_dir,
        num_workers=settings.num_workers,
    )
    
    load_time = time.time() - start_time
    logger.info(f"✅ Model loaded successfully in {load_time:.2f}s")
    
    return whisper_model


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle management - load model on startup"""
    # Startup
    logger.info("🚀 Starting ASR Service...")
    load_model()
    
    yield
    
    # Shutdown
    logger.info("👋 Shutting down ASR Service...")


# ============================================
# FastAPI App
# ============================================

app = FastAPI(
    title="ASR Service - Whisper Large V3",
    description="Speech-to-Text API using OpenAI Whisper Large V3 model",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Trong production, giới hạn origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================
# Request/Response Models
# ============================================

class TranscriptionSegment(BaseModel):
    """Một đoạn transcript với timestamp"""
    id: int
    start: float
    end: float
    text: str
    confidence: Optional[float] = None


class TranscriptionResponse(BaseModel):
    """Response từ API transcription"""
    success: bool
    text: str = Field(description="Full transcript text")
    language: str = Field(description="Detected language code")
    language_probability: float = Field(description="Language detection confidence")
    duration: float = Field(description="Audio duration in seconds")
    segments: List[TranscriptionSegment] = Field(description="Transcript segments with timestamps")
    processing_time: float = Field(description="Processing time in seconds")


class HealthResponse(BaseModel):
    """Health check response"""
    status: str
    model_loaded: bool
    model_size: str
    device: str
    compute_type: str


class ErrorResponse(BaseModel):
    """Error response"""
    success: bool = False
    error: str
    detail: Optional[str] = None


# ============================================
# Helper Functions
# ============================================

def cleanup_temp_file(file_path: str):
    """Xóa file tạm sau khi xử lý"""
    try:
        if os.path.exists(file_path):
            os.remove(file_path)
            logger.debug(f"Cleaned up temp file: {file_path}")
    except Exception as e:
        logger.warning(f"Failed to cleanup temp file {file_path}: {e}")


async def save_upload_file(upload_file: UploadFile) -> str:
    """Lưu uploaded file vào temp directory"""
    # Tạo temp directory nếu chưa có
    os.makedirs(settings.temp_dir, exist_ok=True)
    
    # Tạo unique filename
    file_ext = Path(upload_file.filename or "audio.wav").suffix or ".wav"
    temp_filename = f"{uuid.uuid4()}{file_ext}"
    temp_path = os.path.join(settings.temp_dir, temp_filename)
    
    # Lưu file
    content = await upload_file.read()
    
    # Check file size
    file_size_mb = len(content) / (1024 * 1024)
    if file_size_mb > settings.max_file_size_mb:
        raise HTTPException(
            status_code=413,
            detail=f"File size ({file_size_mb:.1f}MB) exceeds limit ({settings.max_file_size_mb}MB)"
        )
    
    with open(temp_path, "wb") as f:
        f.write(content)
    
    return temp_path


# ============================================
# API Endpoints
# ============================================

@app.get("/", tags=["Info"])
async def root():
    """Root endpoint - API info"""
    return {
        "service": "ASR Service",
        "model": f"whisper-{settings.model_size}",
        "version": "1.0.0",
        "docs": "/docs",
    }


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy" if whisper_model else "loading",
        model_loaded=whisper_model is not None,
        model_size=settings.model_size,
        device=settings.device,
        compute_type=settings.compute_type,
    )


@app.post(
    "/transcribe",
    response_model=TranscriptionResponse,
    responses={
        400: {"model": ErrorResponse},
        413: {"model": ErrorResponse},
        500: {"model": ErrorResponse},
    },
    tags=["ASR"],
)
async def transcribe_audio(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(..., description="Audio file (wav, mp3, webm, m4a, etc.)"),
    language: Optional[str] = Query(
        default=None,
        description="Language code (e.g., 'vi', 'en'). Auto-detect if not provided."
    ),
    task: str = Query(
        default="transcribe",
        description="Task: 'transcribe' or 'translate' (to English)"
    ),
    beam_size: int = Query(
        default=5,
        ge=1,
        le=10,
        description="Beam size for decoding (1-10)"
    ),
    word_timestamps: bool = Query(
        default=False,
        description="Include word-level timestamps"
    ),
    vad_filter: bool = Query(
        default=True,
        description="Enable Voice Activity Detection filter"
    ),
):
    """
    Transcribe audio file to text using Whisper model.
    
    **Supported formats:** wav, mp3, webm, m4a, ogg, flac, etc.
    
    **Languages:** Auto-detect hoặc chỉ định mã ngôn ngữ (vi, en, ja, ko, zh, ...)
    
    **Example:**
    ```bash
    curl -X POST "http://localhost:8000/transcribe" \\
         -F "file=@audio.wav" \\
         -F "language=vi"
    ```
    """
    if not whisper_model:
        raise HTTPException(status_code=503, detail="Model not loaded yet")
    
    # Validate file
    if not file.filename:
        raise HTTPException(status_code=400, detail="No file provided")
    
    temp_path = None
    
    try:
        # Save uploaded file
        temp_path = await save_upload_file(file)
        logger.info(f"Processing file: {file.filename} ({file.content_type})")
        
        start_time = time.time()
        
        # Transcribe
        segments_result, info = whisper_model.transcribe(
            temp_path,
            language=language,
            task=task,
            beam_size=beam_size,
            word_timestamps=word_timestamps,
            vad_filter=vad_filter,
        )
        
        # Convert segments to list
        segments = []
        full_text_parts = []
        
        for segment in segments_result:
            segments.append(TranscriptionSegment(
                id=segment.id,
                start=round(segment.start, 3),
                end=round(segment.end, 3),
                text=segment.text.strip(),
                confidence=round(segment.avg_logprob, 4) if hasattr(segment, 'avg_logprob') else None,
            ))
            full_text_parts.append(segment.text.strip())
        
        full_text = " ".join(full_text_parts)
        processing_time = time.time() - start_time
        
        logger.info(
            f"✅ Transcribed: {len(segments)} segments, "
            f"{info.duration:.1f}s audio, "
            f"lang={info.language} ({info.language_probability:.2%}), "
            f"took {processing_time:.2f}s"
        )
        
        # Schedule cleanup
        background_tasks.add_task(cleanup_temp_file, temp_path)
        
        return TranscriptionResponse(
            success=True,
            text=full_text,
            language=info.language,
            language_probability=round(info.language_probability, 4),
            duration=round(info.duration, 3),
            segments=segments,
            processing_time=round(processing_time, 3),
        )
        
    except Exception as e:
        # Cleanup on error
        if temp_path:
            cleanup_temp_file(temp_path)
        
        logger.error(f"❌ Transcription failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post(
    "/transcribe/simple",
    tags=["ASR"],
)
async def transcribe_simple(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...),
    language: Optional[str] = Query(default=None),
):
    """
    Simplified transcription endpoint - chỉ trả về text.
    
    Dùng cho integration đơn giản, tương thích với format của OpenAI/Gemini ASR API.
    """
    if not whisper_model:
        raise HTTPException(status_code=503, detail="Model not loaded yet")
    
    temp_path = None
    
    try:
        temp_path = await save_upload_file(file)
        
        segments_result, info = whisper_model.transcribe(
            temp_path,
            language=language,
            beam_size=settings.beam_size,
            vad_filter=True,
        )
        
        full_text = " ".join([s.text.strip() for s in segments_result])
        
        background_tasks.add_task(cleanup_temp_file, temp_path)
        
        # Format giống OpenAI Whisper API
        return {
            "text": full_text,
            "language": info.language,
        }
        
    except Exception as e:
        if temp_path:
            cleanup_temp_file(temp_path)
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/languages", tags=["Info"])
async def list_languages():
    """List các ngôn ngữ được hỗ trợ"""
    # Whisper hỗ trợ 99 ngôn ngữ
    common_languages = {
        "vi": "Vietnamese",
        "en": "English",
        "zh": "Chinese",
        "ja": "Japanese",
        "ko": "Korean",
        "th": "Thai",
        "id": "Indonesian",
        "ms": "Malay",
        "fr": "French",
        "de": "German",
        "es": "Spanish",
        "it": "Italian",
        "pt": "Portuguese",
        "ru": "Russian",
        "ar": "Arabic",
        "hi": "Hindi",
    }
    
    return {
        "supported": common_languages,
        "total_languages": 99,
        "note": "Set language=null for auto-detection",
    }


# ============================================
# Error Handlers
# ============================================

@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": exc.detail,
        }
    )


@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    logger.error(f"Unhandled exception: {exc}")
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "error": "Internal server error",
            "detail": str(exc),
        }
    )


# ============================================
# Main
# ============================================

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=6868,
        reload=True,
        log_level="info",
    )
