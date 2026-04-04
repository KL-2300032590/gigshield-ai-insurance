# Parametrix Services Build Guide

## Prerequisites

- **Java 21** (Required - Lombok 1.18.32 is NOT compatible with Java 25)
- Maven 3.9+
- Docker Desktop (for containerized deployment)

## Java Version Setup

**CRITICAL:** You must use Java 21. Lombok 1.18.32 will fail with Java 25 with this error:
```
java.lang.NoSuchFieldException: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

### Set Java 21 as Default

1. **Check available Java versions:**
   ```bash
   /usr/libexec/java_home -V
   ```

2. **Set Java 21 for current session:**
   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
   export PATH=$JAVA_HOME/bin:$PATH
   ```

3. **Make it permanent (add to `~/.zshrc` or `~/.bash_profile`):**
   ```bash
   echo 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home' >> ~/.zshrc
   echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
   source ~/.zshrc
   ```

4. **Verify:**
   ```bash
   java -version
   # Should show: java version "21.0.10"
   ```

## Building the Project

### Option 1: Use the Build Script (Recommended)
```bash
cd services
./build.sh
```

### Option 2: Manual Maven Build
```bash
# Ensure Java 21 is active
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Build all services
cd services
mvn clean install
```

### Option 3: Build Without Tests (faster)
```bash
cd services
mvn clean install -DskipTests
```

## Test Results

All 49 tests should pass:
- Common: 19 tests (DateUtils, GeoUtils)
- API Gateway: 11 tests (WorkerService, AuthService)
- Risk Engine: 3 tests
- Trigger Engine: 6 tests
- Fraud Detection: 3 tests
- Claim Service: 3 tests
- Payout Service: 4 tests

## Docker Build

### Single Architecture (Current Platform)

```bash
# Build all service containers
docker-compose build

# Build specific service
docker-compose build api-gateway

# Start all services
docker-compose up

# Start in detached mode
docker-compose up -d
```

### Multi-Architecture Build (AMD64 + ARM64)

Build Docker images for both Intel/AMD (linux/amd64) and ARM (linux/arm64) architectures:

```bash
cd services

# Build all services for multiple platforms
./build-multiarch.sh

# Build and push to Docker registry
./build-multiarch.sh --push

# Build specific service only
./build-multiarch.sh --service api-gateway
```

**Supported Platforms:**
- `linux/amd64` - Intel/AMD x86_64 (Windows, Linux servers, AWS EC2)
- `linux/arm64` - ARM 64-bit (Apple Silicon M1/M2/M3, AWS Graviton)

**Benefits:**
- Deploy on any cloud provider (AWS, Azure, GCP)
- Run on Windows/Linux (Intel) and Mac (Apple Silicon)
- Consistent experience across platforms

**Note:** Multi-arch builds require Docker Buildx. The script will automatically:
1. Create a multi-platform builder if not exists
2. Build Maven packages with Java 21
3. Build Docker images for both architectures
4. Optionally push to registry with `--push` flag

## Troubleshooting

### Error: "TypeTag :: UNKNOWN"
**Cause:** Using Java 25 instead of Java 21  
**Solution:** Set `JAVA_HOME` to Java 21 (see above)

### Error: "Could not find artifact com.parametrix:parametrix-services:pom:1.0.0"
**Cause:** Parent POM not installed  
**Solution:** Run `mvn clean install` from the `services` directory

### Error: Lombok annotations not working
**Cause:** Wrong Lombok version or Java version  
**Solution:** Use Java 21 with Lombok 1.18.32 (already configured)

## Project Structure

```
services/
├── pom.xml                    # Parent POM (v1.0.0)
├── common/                    # Shared models, DTOs, utilities
├── api-gateway/               # API Gateway (port 8080)
├── risk-engine/               # Risk Calculation (port 8081)
├── trigger-engine/            # Environment Monitoring (port 8082)
├── fraud-detection/           # Fraud Detection (port 8083)
├── claim-service/             # Claim Processing (port 8084)
└── payout-service/            # Payout Processing (port 8085)
```

## Key Dependencies

- Spring Boot: 3.2.0
- Lombok: 1.18.32
- TestContainers: 1.20.1
- Jackson: 2.16.0
- Resilience4j: 2.1.0
- JJWT: 0.12.3

## Fixed Issues

✅ Lombok version mismatches  
✅ Kafka security vulnerabilities (trusted packages)  
✅ MongoDB query bug (missing city filter)  
✅ Broken tests (PayoutService, ClaimService)  
✅ Serialization errors (Razorpay DTOs)  
✅ Docker multi-stage builds
