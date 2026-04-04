# Parametrix AI Insurance - AI Context Documentation

> This document is designed for AI models (like Claude, GPT) working on this codebase.
> It provides essential context, architecture details, and conventions.

## 📋 Project Overview

**Parametrix AI Insurance** is a microservices-based parametric insurance platform for gig workers in India. It provides automatic claim processing based on environmental triggers (weather, air quality) without manual claim filing.

### Key Business Logic

1. **Workers register** → Risk score calculated → Policy purchased
2. **Environmental event occurs** (flood, AQI spike, etc.)
3. **Trigger engine detects** affected policies automatically
4. **Claims generated** → Fraud check → Payout processed

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │ Mobile App   │  │  Admin UI    │  │ Admin Dashboard      │   │
│  │ (Workers)    │  │  (Next.js)   │  │ (Weather Simulator)  │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                       │
│                                                                  │
│  • Routes requests to microservices                              │
│  • JWT Authentication                                            │
│  • Rate limiting                                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       ▼                      ▼                      ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│ Risk Engine  │    │Trigger Engine│    │ Claim Service    │
│ (Port 8081)  │    │ (Port 8082)  │    │ (Port 8084)      │
│              │    │              │    │                  │
│ • Risk score │    │ • Weather    │    │ • Claim mgmt     │
│ • Premium    │    │   monitoring │    │ • Auto-claims    │
│   calculation│    │ • Event      │    │ • Status updates │
└──────────────┘    │   detection  │    └──────────────────┘
                    └──────────────┘              │
                           │                      ▼
                           │            ┌──────────────────┐
                           │            │ Fraud Detection  │
                           │            │ (Port 8083)      │
                           │            │                  │
                           │            │ • ML fraud check │
                           │            │ • Pattern detect │
                           │            └──────────────────┘
                           │                      │
                           ▼                      ▼
                    ┌──────────────────────────────────────┐
                    │           Apache Kafka               │
                    │                                      │
                    │  Topics:                             │
                    │  • worker-registrations              │
                    │  • policy-purchases                  │
                    │  • environment-disruptions           │
                    │  • claims                            │
                    │  • payouts                           │
                    └──────────────────────────────────────┘
                                      │
                                      ▼
                    ┌──────────────────────────────────────┐
                    │         Payout Service               │
                    │         (Port 8085)                  │
                    │                                      │
                    │  • UPI/Bank transfers                │
                    │  • Instant payouts                   │
                    └──────────────────────────────────────┘

Additional Services:
┌──────────────────┐    ┌──────────────────┐
│ Admin Simulator  │    │ Admin Dashboard  │
│ (Port 8091)      │    │ (Port 3000)      │
│                  │    │                  │
│ • Weather sim    │    │ • Next.js 14     │
│ • Demo triggers  │    │ • Service mgmt   │
└──────────────────┘    └──────────────────┘
```

---

## 🔧 Technology Stack

### Backend (Java 21 + Spring Boot 3.2)
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Language runtime |
| Spring Boot | 3.2.0 | Application framework |
| Lombok | 1.18.32 | Boilerplate reduction |
| Spring Kafka | 3.2.0 | Event streaming |
| Spring Data MongoDB | 3.2.0 | Database access |
| MapStruct | 1.5.5 | DTO mapping |

### Frontend (Admin Dashboard)
| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 16.2.2 | React framework |
| shadcn/ui | Latest | UI components |
| Tailwind CSS | 4.x | Styling |
| TypeScript | 5.x | Type safety |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local orchestration |
| Apache Kafka | Event streaming |
| MongoDB | Document database |
| Redis | Caching (optional) |

---

## ⚠️ CRITICAL: Java/Lombok Compatibility

**MUST use Java 21. Lombok 1.18.32 is NOT compatible with Java 25!**

```bash
# Check Java version
java -version
# Should output: openjdk 21.x.x

# Set JAVA_HOME if needed (macOS)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home

# Build command
mvn clean install
```

---

## 📁 Project Structure

```
gigshield-ai-insurance/
├── services/                    # Backend microservices
│   ├── pom.xml                 # Parent POM (defines versions)
│   ├── common/                 # Shared models, DTOs, events
│   ├── api-gateway/            # REST API gateway (8080)
│   ├── risk-engine/            # Risk scoring (8081)
│   ├── trigger-engine/         # Environmental triggers (8082)
│   ├── fraud-detection/        # Fraud ML models (8083)
│   ├── claim-service/          # Claims management (8084)
│   ├── payout-service/         # Payment processing (8085)
│   ├── admin-simulator/        # Weather simulation (8091)
│   ├── build-multiarch.sh      # Multi-arch Docker builds
│   └── BUILD.md                # Build documentation
├── admin-dashboard/            # Next.js admin UI (3000)
│   ├── src/
│   │   ├── app/               # Next.js app router pages
│   │   ├── components/        # React components
│   │   └── lib/               # API client, utilities
│   └── package.json
├── docker-compose.yml          # Container orchestration
└── README.md                   # Project readme
```

---

## 🎯 Key Domain Models

### EnvironmentDisruptionEvent (Kafka Event)

```java
// Location: services/common/src/main/java/com/gigshield/common/event/EnvironmentDisruptionEvent.java
public class EnvironmentDisruptionEvent {
    private TriggerType triggerType;    // NOT 'disruptionType'
    private double measuredValue;        // NOT 'value'
    private double latitude;
    private double longitude;
    private double threshold;
    private String source;
    private Severity severity;           // LOW, MEDIUM, HIGH, CRITICAL
    private LocalDateTime timestamp;
    private String city;
    private int affectedWorkers;
}
```

### TriggerType Enum

```java
// Location: services/common/src/main/java/com/gigshield/common/model/Claim.java
public enum TriggerType {
    HEAVY_RAIN,
    FLOOD,
    AIR_POLLUTION,
    EXTREME_HEAT,
    EXTREME_COLD
}
```

### Severity Enum

```java
// Location: services/common/src/main/java/com/gigshield/common/event/EnvironmentDisruptionEvent.java
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
```

---

## 🔌 Service Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | REST API entry point |
| Risk Engine | 8081 | Risk score calculation |
| Trigger Engine | 8082 | Environmental monitoring |
| Fraud Detection | 8083 | ML-based fraud scoring |
| Claim Service | 8084 | Claims lifecycle |
| Payout Service | 8085 | UPI/Bank payments |
| Kafka UI | 8090 | Kafka management |
| Admin Simulator | 8091 | Weather simulation |
| Admin Dashboard | 3000 | Next.js admin UI |
| MongoDB | 27017 | Database |
| Kafka | 9092 | Message broker |
| Zookeeper | 2181 | Kafka coordination |

---

## 📝 Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `worker-registrations` | API Gateway | Risk Engine | New worker signups |
| `policy-purchases` | API Gateway | Claim Service | New policies |
| `environment-disruptions` | Trigger Engine, Admin Simulator | Claim Service | Weather events |
| `claims` | Claim Service | Fraud Detection, Payout | Claim lifecycle |
| `payouts` | Payout Service | - | Payment confirmations |

---

## 🏗️ Common Module (gigshield-common)

**Artifact ID**: `gigshield-common` (NOT `common`)

```xml
<dependency>
    <groupId>com.gigshield</groupId>
    <artifactId>gigshield-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

Contains:
- **Models**: Worker, Policy, Claim, Payout
- **DTOs**: Request/Response objects
- **Events**: Kafka event classes
- **Enums**: TriggerType, Severity, ClaimStatus

---

## 🐳 Docker Multi-Architecture Support

The project supports both AMD64 (Intel/AMD) and ARM64 (Apple Silicon, AWS Graviton).

```bash
# Build for both architectures
cd services
./build-multiarch.sh

# Build specific service
docker buildx build --platform linux/amd64,linux/arm64 \
  -t gigshield/api-gateway:latest .
```

**Builder**: `gigshield-multiarch`
**Platforms**: `linux/amd64,linux/arm64`

---

## 🎮 Admin Simulator API

The simulator allows triggering weather events for demo/testing.

### Endpoints

```
POST /api/simulations/trigger
GET  /api/simulations
GET  /api/simulations/{id}
GET  /api/simulations/city/{city}
DELETE /api/simulations/{id}
```

### Request Example

```json
{
  "city": "Mumbai",
  "eventType": "HEAVY_RAIN",
  "severity": "HIGH",
  "simulatedValue": 150.0,
  "description": "Monsoon flooding simulation"
}
```

### Event Type Mapping

| eventType | TriggerType | Threshold |
|-----------|-------------|-----------|
| HEAVY_RAIN | HEAVY_RAIN | 100mm |
| FLOOD | FLOOD | 0.5m |
| HIGH_AQI | AIR_POLLUTION | 200 AQI |
| EXTREME_HEAT | EXTREME_HEAT | 42°C |
| EXTREME_COLD | EXTREME_COLD | 5°C |

---

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
cd services
mvn test

# Run specific service tests
mvn test -pl claim-service
```

### Test Configuration

For Mockito with unused stubs:
```java
@MockitoSettings(strictness = Strictness.LENIENT)
class MyServiceTest {
    // ...
}
```

---

## 🔨 Build Commands

### Backend

```bash
# Full build with tests
cd services
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Single service
mvn clean install -pl api-gateway -am
```

### Frontend (Admin Dashboard)

```bash
cd admin-dashboard
npm install
npm run build
npm run dev    # Development server on port 3000
```

### Docker

```bash
# Start all services
docker-compose up -d

# Rebuild specific service
docker-compose build api-gateway
docker-compose up -d api-gateway

# View logs
docker-compose logs -f claim-service
```

---

## 🗂️ Database Schema (MongoDB)

### Collections

| Collection | Service | Description |
|------------|---------|-------------|
| `workers` | API Gateway | Worker profiles |
| `policies` | API Gateway | Insurance policies |
| `claims` | Claim Service | Claim records |
| `payouts` | Payout Service | Payment records |
| `simulations` | Admin Simulator | Simulation history |

---

## 📊 Admin Dashboard Pages

| Route | Description |
|-------|-------------|
| `/` | Overview with service health, statistics |
| `/simulation` | Weather event simulator |
| `/claims` | Claims management table |
| `/policies` | Policy analytics |
| `/workers` | Worker management |
| `/events` | Real-time Kafka events |
| `/logs` | System log aggregation |

---

## 🚨 Common Issues & Solutions

### 1. Lombok Compilation Error

**Error**: `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

**Solution**: Use Java 21, not Java 25
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

### 2. Kafka Deserialization Error

**Error**: `ClassNotFoundException: EnvironmentDisruptionEvent`

**Solution**: Ensure type mappings match:
```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.type.mapping: "environment.disruption:com.gigshield.common.event.EnvironmentDisruptionEvent"
```

### 3. MongoDB Connection Issues

**Solution**: Ensure MongoDB is running
```bash
docker-compose up -d mongodb
```

### 4. Port Already in Use

**Solution**: Find and kill the process
```bash
lsof -i :8080
kill -9 <PID>
```

---

## 📈 Metrics & Monitoring

Services expose actuator endpoints:
- `/actuator/health` - Health status
- `/actuator/info` - Service info
- `/actuator/metrics` - Prometheus metrics

---

## 🔐 Security Notes

- JWT tokens for API authentication
- Admin dashboard requires auth (to be implemented)
- Sensitive configs in environment variables
- No secrets in code or config files

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Next.js Documentation](https://nextjs.org/docs)
- [shadcn/ui Components](https://ui.shadcn.com/)

---

## 🤖 AI Assistant Notes

When working on this codebase:

1. **Always use Java 21** - Lombok breaks on Java 25
2. **Field names matter** - Use `triggerType` not `disruptionType`, `measuredValue` not `value`
3. **Artifact ID is `gigshield-common`** - Not just `common`
4. **Kafka type mappings must match** - Check `application.yml` in each service
5. **Multi-arch builds require buildx** - The builder is `gigshield-multiarch`
6. **Tests use lenient Mockito** - Add `@MockitoSettings(strictness = Strictness.LENIENT)` if needed

---

*Last updated: April 2026*
*Total services: 8 (7 Java + 1 Next.js)*
*Total tests: 57 passing*
