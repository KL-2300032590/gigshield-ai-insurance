#!/bin/bash

# Parametrix Multi-Architecture Build Script
# Builds Docker images for both AMD64 (Intel/Windows) and ARM64 (Apple Silicon/Mac)
# Usage: ./build-multiarch.sh [--push] [--service SERVICE_NAME]

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PLATFORMS="linux/amd64,linux/arm64"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-parametrix}"  # Default registry/namespace
BUILD_CONTEXT="."
PUSH_FLAG=""
SPECIFIC_SERVICE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --push)
            PUSH_FLAG="--push"
            echo -e "${YELLOW}Push mode enabled: Images will be pushed to registry${NC}"
            shift
            ;;
        --service)
            SPECIFIC_SERVICE="$2"
            echo -e "${BLUE}Building specific service: $SPECIFIC_SERVICE${NC}"
            shift 2
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Usage: $0 [--push] [--service SERVICE_NAME]"
            exit 1
            ;;
    esac
done

# Check Docker buildx availability
if ! docker buildx version &> /dev/null; then
    echo -e "${RED}Error: Docker buildx is not available${NC}"
    echo "Please install Docker Desktop or enable buildx"
    exit 1
fi

# Check if builder exists, create if not
BUILDER_NAME="parametrix-multiarch"
if ! docker buildx inspect "$BUILDER_NAME" &> /dev/null; then
    echo -e "${YELLOW}Creating multi-arch builder: $BUILDER_NAME${NC}"
    docker buildx create --name "$BUILDER_NAME" --driver docker-container --bootstrap --platform "$PLATFORMS"
fi

# Use the builder
echo -e "${BLUE}Using builder: $BUILDER_NAME${NC}"
docker buildx use "$BUILDER_NAME"

# Verify Java 21 is available
echo -e "${BLUE}Checking Java version...${NC}"
if [ -d "/Library/Java/JavaVirtualMachines/jdk-21.jdk" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
    export PATH=$JAVA_HOME/bin:$PATH
    echo -e "${GREEN}Using Java 21: $JAVA_HOME${NC}"
else
    echo -e "${YELLOW}Warning: Java 21 not found in default location${NC}"
    echo "Attempting to use system Java..."
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$java_version" != "21" ]; then
    echo -e "${RED}Error: Java 21 is required, but found version $java_version${NC}"
    echo "Please install Java 21 and set JAVA_HOME"
    exit 1
fi
echo -e "${GREEN}Java version verified: $java_version${NC}"

# Build Maven packages first (required for Docker builds)
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Building Maven packages with Java 21...${NC}"
echo -e "${BLUE}========================================${NC}\n"

if [ -n "$SPECIFIC_SERVICE" ]; then
    if [ "$SPECIFIC_SERVICE" != "common" ]; then
        # Build common first as dependency
        echo -e "${YELLOW}Building common module (dependency)...${NC}"
        mvn clean install -pl common -am -DskipTests -q
    fi
    echo -e "${YELLOW}Building $SPECIFIC_SERVICE...${NC}"
    mvn clean package -pl "$SPECIFIC_SERVICE" -am -DskipTests
else
    echo -e "${YELLOW}Building all services...${NC}"
    mvn clean install -DskipTests
fi

echo -e "${GREEN}Maven build completed successfully!${NC}\n"

# Define services to build
if [ -n "$SPECIFIC_SERVICE" ]; then
    SERVICES=("$SPECIFIC_SERVICE")
else
    SERVICES=(
        "api-gateway"
        "risk-engine"
        "trigger-engine"
        "fraud-detection"
        "claim-service"
        "payout-service"
        "admin-simulator"
    )
fi

# Build each service for multiple architectures
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Building Multi-Architecture Docker Images${NC}"
echo -e "${BLUE}Platforms: $PLATFORMS${NC}"
echo -e "${BLUE}========================================${NC}\n"

BUILD_SUCCESS=true

for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}Building $service for multiple architectures...${NC}"
    
    IMAGE_NAME="$DOCKER_REGISTRY/$service:latest"
    DOCKERFILE_PATH="$service/Dockerfile"
    
    # Check if Dockerfile exists
    if [ ! -f "$DOCKERFILE_PATH" ]; then
        echo -e "${RED}Error: Dockerfile not found at $DOCKERFILE_PATH${NC}"
        BUILD_SUCCESS=false
        continue
    fi
    
    # Build command
    BUILD_CMD="docker buildx build \
        --platform $PLATFORMS \
        -t $IMAGE_NAME \
        -f $DOCKERFILE_PATH \
        --builder $BUILDER_NAME"
    
    # Add push flag if specified
    if [ -n "$PUSH_FLAG" ]; then
        BUILD_CMD="$BUILD_CMD $PUSH_FLAG"
    else
        BUILD_CMD="$BUILD_CMD --load 2>/dev/null || $BUILD_CMD"
    fi
    
    BUILD_CMD="$BUILD_CMD $BUILD_CONTEXT"
    
    # Execute build
    if eval $BUILD_CMD; then
        echo -e "${GREEN}✓ Successfully built $service${NC}\n"
    else
        echo -e "${RED}✗ Failed to build $service${NC}\n"
        BUILD_SUCCESS=false
    fi
done

# Summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Build Summary${NC}"
echo -e "${BLUE}========================================${NC}"

if [ "$BUILD_SUCCESS" = true ]; then
    echo -e "${GREEN}All images built successfully!${NC}"
    echo -e "\nBuilt for platforms: ${GREEN}$PLATFORMS${NC}"
    
    if [ -n "$PUSH_FLAG" ]; then
        echo -e "Images pushed to: ${GREEN}$DOCKER_REGISTRY${NC}"
    else
        echo -e "\n${YELLOW}Note: Images are built but not pushed to registry${NC}"
        echo -e "To push images, run: ${BLUE}$0 --push${NC}"
    fi
    
    echo -e "\n${GREEN}Multi-architecture build completed!${NC}"
    echo -e "These images can now run on:"
    echo -e "  • Intel/AMD (linux/amd64) - Windows, Linux servers"
    echo -e "  • Apple Silicon (linux/arm64) - M1/M2/M3 Macs"
    echo -e "  • AWS Graviton, Raspberry Pi, etc."
    
    exit 0
else
    echo -e "${RED}Some builds failed. Check the output above for details.${NC}"
    exit 1
fi
