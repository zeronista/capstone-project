#!/bin/bash
# =============================================================================
# Build script cho tất cả AI Voice microservices
# Chạy từ thư mục gốc project: ./src/main/docker_ai_voice/build-all.sh
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$(dirname "$SCRIPT_DIR")")")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  AI Voice Microservices Builder${NC}"
echo -e "${BLUE}========================================${NC}"

# Parse arguments
BUILD_TYPE="${1:-gpu}"  # gpu, cpu, or all
PUSH_IMAGES="${2:-false}"
REGISTRY="${DOCKER_REGISTRY:-}"

# Function to build a service
build_service() {
    local SERVICE_NAME=$1
    local SERVICE_DIR="$SCRIPT_DIR/$SERVICE_NAME"
    local DOCKERFILE="$SERVICE_DIR/Dockerfile"
    
    if [ ! -f "$DOCKERFILE" ]; then
        echo -e "${YELLOW}⚠ Skipping $SERVICE_NAME - Dockerfile not found${NC}"
        return 0
    fi
    
    echo -e "\n${GREEN}▶ Building $SERVICE_NAME...${NC}"
    
    cd "$SERVICE_DIR"
    
    case $BUILD_TYPE in
        "gpu")
            echo -e "${BLUE}  Building GPU image...${NC}"
            docker build --target production-gpu -t "${SERVICE_NAME}:gpu" -t "${SERVICE_NAME}:latest" .
            ;;
        "cpu")
            echo -e "${BLUE}  Building CPU image...${NC}"
            docker build --target production-cpu -t "${SERVICE_NAME}:cpu" .
            ;;
        "all")
            echo -e "${BLUE}  Building GPU image...${NC}"
            docker build --target production-gpu -t "${SERVICE_NAME}:gpu" -t "${SERVICE_NAME}:latest" .
            echo -e "${BLUE}  Building CPU image...${NC}"
            docker build --target production-cpu -t "${SERVICE_NAME}:cpu" .
            ;;
    esac
    
    # Push to registry if specified
    if [ "$PUSH_IMAGES" = "true" ] && [ -n "$REGISTRY" ]; then
        echo -e "${BLUE}  Pushing to $REGISTRY...${NC}"
        docker tag "${SERVICE_NAME}:latest" "${REGISTRY}/${SERVICE_NAME}:latest"
        docker push "${REGISTRY}/${SERVICE_NAME}:latest"
    fi
    
    echo -e "${GREEN}✓ $SERVICE_NAME built successfully${NC}"
}

# Function to check Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}✗ Docker is not installed${NC}"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}✗ Docker daemon is not running${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Docker is available${NC}"
}

# Function to check NVIDIA Docker (for GPU builds)
check_nvidia() {
    if [ "$BUILD_TYPE" = "gpu" ] || [ "$BUILD_TYPE" = "all" ]; then
        if docker run --rm --gpus all nvidia/cuda:12.1.0-base-ubuntu22.04 nvidia-smi &> /dev/null; then
            echo -e "${GREEN}✓ NVIDIA GPU support available${NC}"
        else
            echo -e "${YELLOW}⚠ NVIDIA GPU not available - GPU images will build but may not run locally${NC}"
        fi
    fi
}

# Main execution
echo -e "\n${BLUE}Checking prerequisites...${NC}"
check_docker
check_nvidia

echo -e "\n${BLUE}Build configuration:${NC}"
echo -e "  Build type: ${GREEN}$BUILD_TYPE${NC}"
echo -e "  Push images: ${GREEN}$PUSH_IMAGES${NC}"
echo -e "  Registry: ${GREEN}${REGISTRY:-local}${NC}"

# Build all services
echo -e "\n${BLUE}Building services...${NC}"

# List of services to build (add new services here)
SERVICES=(
    "asr-service"
    # "tts-service"      # Future: Text-to-Speech
    # "nlu-service"      # Future: Natural Language Understanding
    # "dialogue-service" # Future: Dialogue Management
)

for service in "${SERVICES[@]}"; do
    build_service "$service"
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  All services built successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

# Print usage instructions
echo -e "\n${BLUE}To run services:${NC}"
echo -e "  cd $SCRIPT_DIR/asr-service"
echo -e "  docker compose up -d asr-gpu    # For GPU"
echo -e "  docker compose up -d asr-cpu    # For CPU"

echo -e "\n${BLUE}To check health:${NC}"
echo -e "  curl http://localhost:8001/health"
