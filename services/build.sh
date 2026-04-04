#!/bin/bash

# Parametrix Services Build Script
# Ensures Java 21 is used for Lombok compatibility

set -e  # Exit on error

echo "========================================="
echo "Parametrix Services Build Script"
echo "========================================="
echo ""

# Check if Java 21 is available
JAVA_21_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"

if [ ! -d "$JAVA_21_HOME" ]; then
    echo "❌ ERROR: Java 21 not found at $JAVA_21_HOME"
    echo ""
    echo "Please install Java 21 or update JAVA_21_HOME in this script"
    echo "Available Java installations:"
    /usr/libexec/java_home -V 2>&1 | grep -v "Matching"
    exit 1
fi

# Set Java 21
export JAVA_HOME="$JAVA_21_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java version
echo "✓ Using Java:"
java -version 2>&1 | head -1
echo ""

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven not found. Please install Maven 3.9+"
    exit 1
fi

echo "✓ Using Maven:"
mvn -version | head -1
echo ""

# Build
echo "========================================="
echo "Building all services..."
echo "========================================="
echo ""

if [ "$1" == "--skip-tests" ] || [ "$1" == "-DskipTests" ]; then
    echo "⚡ Running: mvn clean install -DskipTests"
    mvn clean install -DskipTests
else
    echo "🧪 Running: mvn clean install (with tests)"
    mvn clean install
fi

# Check result
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✅ BUILD SUCCESS"
    echo "========================================="
    echo ""
    echo "All services built successfully!"
    echo ""
    echo "Next steps:"
    echo "  - Build Docker images: cd .. && docker-compose build"
    echo "  - Start services: cd .. && docker-compose up"
    echo ""
else
    echo ""
    echo "========================================="
    echo "❌ BUILD FAILED"
    echo "========================================="
    echo ""
    echo "Check the error messages above."
    echo "Common issues:"
    echo "  - Wrong Java version (need Java 21)"
    echo "  - Dependency issues (try: mvn clean install -U)"
    echo ""
    exit 1
fi
