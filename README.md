# GigShield – AI Powered Parametric Insurance for Gig Workers

AI-powered parametric insurance platform that protects gig delivery workers from income loss caused by external disruptions such as extreme weather and environmental conditions.

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 18+** (with npm)
- **Docker & Docker Compose** (for infrastructure)

### Run with Docker Compose (Recommended)

```bash
# Start all services (MongoDB, Redis, Kafka, Backend, Frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

**Access Points:**
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- Kafka UI: http://localhost:8090

### Run Locally (Development)

**1. Start Infrastructure**
```bash
docker-compose up -d mongodb redis zookeeper kafka
```

**2. Build & Run Backend**
```bash
cd services/common && mvn clean install
cd ../api-gateway && mvn spring-boot:run &
cd ../risk-engine && mvn spring-boot:run &
cd ../trigger-engine && mvn spring-boot:run &
cd ../fraud-detection && mvn spring-boot:run &
cd ../claim-service && mvn spring-boot:run &
cd ../payout-service && mvn spring-boot:run &
```

**3. Run Frontend**
```bash
cd frontend
npm install
npm run dev
```

### Run Tests

```bash
# Backend tests (from services directory)
cd services/common && mvn test
cd ../api-gateway && mvn test
# ... repeat for each service

# Frontend tests
cd frontend && npm test
```

---

# Overview

Gig economy delivery workers rely on daily deliveries to earn income. Workers on platforms like Swiggy and Zomato are highly affected by external disruptions such as heavy rain, floods, extreme heat, or severe air pollution.

These disruptions reduce deliveries and can significantly impact their weekly earnings.

Currently, gig workers **do not have automated income protection systems** that compensate them for such disruptions.

GigShield proposes an **AI-powered parametric insurance platform** that detects disruptions automatically and triggers payouts to affected workers.

---

# Problem Statement

Gig delivery workers depend on daily work availability. When disruptions occur:

- Heavy rainfall stops deliveries
- Severe pollution prevents outdoor work
- Flooded roads block delivery routes
- Extreme temperatures reduce working hours

These disruptions can reduce **20–30% of a worker's weekly income**, and currently workers bear the full financial loss.

Traditional insurance systems are not suitable because they require **manual claims and slow processing**.

---

# Proposed Solution

GigShield introduces a **parametric insurance system** designed specifically for gig workers.

Instead of filing claims manually, the platform:

1. Monitors environmental conditions in real time  
2. Detects disruption events automatically  
3. Validates worker location and activity  
4. Processes claims automatically  
5. Sends compensation instantly

This creates a **zero-touch insurance system** for gig workers.

---

# Weekly Insurance Model

Workers subscribe to a **weekly micro-insurance policy** aligned with their earning cycle.

Example Policy:

- Weekly Premium: ₹20  
- Coverage Limit: ₹800  
- Policy Duration: 7 days  

Premiums are dynamically adjusted using an **AI risk scoring model** based on environmental risk in the worker’s location.

---

# Parametric Triggers

The system automatically monitors external data sources to detect disruption events.

Example triggers include:

- Heavy rainfall above threshold  
- Severe air pollution levels (AQI)  
- Flood alerts  
- Extreme temperature conditions  

When a disruption occurs, the system automatically triggers a claim.

---

# System Architecture

The platform uses an **event-driven architecture** built around Kafka and Redis.

Worker actions and environmental events are published to a **Kafka event streaming system**, enabling services to process events asynchronously.

Core services include:

- Risk Engine (AI-based premium calculation)
- Trigger Engine (detect disruption events)
- Fraud Detection Service
- Claim Processing Service
- Payout Service

Redis is used as a **real-time caching layer** for policy data, worker activity, and disruption monitoring.

---

# Architecture Diagram

![Architecture](architecture/system-architecture.png)

---

# Core Components

### Worker App
Allows gig workers to register, purchase policies, and track coverage.

### Backend API
Handles user requests and publishes events to Kafka.

### Kafka Event System
Central message broker enabling event-driven communication between services.

### Risk Engine
Uses AI models to calculate disruption risk and determine weekly premium pricing.

### Trigger Engine
Detects environmental disruption events and initiates automatic claims.

### Fraud Detection
Validates worker location, activity patterns, and claim legitimacy.

### Claim Service
Handles automated claim processing.

### Payout Service
Simulates payout through a payment gateway.

### Redis Cache
Stores real-time policy data and disruption monitoring information.

---

# AI Components

### Risk Prediction Model

Uses environmental data such as:

- Rainfall history
- Air quality levels
- Seasonal weather patterns
- Location-based environmental risks

Output:

Risk score used to dynamically calculate weekly insurance premiums.

### Fraud Detection Model

Uses anomaly detection to identify suspicious claims such as:

- Location mismatch
- Duplicate claims
- Abnormal claim patterns

---

# Technology Stack

| Layer | Technology |
|-------|------------|
| Frontend | Next.js 14, Tailwind CSS, Framer Motion |
| Backend | Spring Boot 3.2, Java 17 |
| Event Streaming | Apache Kafka |
| Caching | Redis |
| Database | MongoDB |
| API Documentation | OpenAPI 3.0 |
| Containerization | Docker, Docker Compose |

### External API Integrations
- Weather Data: OpenWeatherMap API (stub mode for demo)
- Air Quality: WAQI API (stub mode for demo)
- Payments: Razorpay Sandbox (simulation)

---

# Microservices Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND (Next.js :3000)                    │
│  Worker Dashboard │ Policy Purchase │ Claims │ Payout Tracking  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ REST API
┌───────────────────────────▼─────────────────────────────────────┐
│                  API GATEWAY (:8080)                            │
│         Authentication │ Rate Limiting │ Request Routing        │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Kafka Events
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  RISK ENGINE  │   │TRIGGER ENGINE │   │FRAUD DETECTION│
│   (:8081)     │   │   (:8082)     │   │   (:8083)     │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ▼
                ┌───────────────────┐
                │  CLAIM SERVICE    │
                │    (:8084)        │
                └─────────┬─────────┘
                          ▼
                ┌───────────────────┐
                │  PAYOUT SERVICE   │
                │    (:8085)        │
                └───────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  MongoDB │ Redis │ Kafka │ Zookeeper                            │
└─────────────────────────────────────────────────────────────────┘
```

### Service Responsibilities

| Service | Port | Responsibility |
|---------|------|----------------|
| api-gateway | 8080 | REST API, JWT Auth, Worker management |
| risk-engine | 8081 | AI risk scoring, premium calculation |
| trigger-engine | 8082 | Weather/AQI monitoring, disruption detection |
| fraud-detection | 8083 | Claim validation, anomaly detection |
| claim-service | 8084 | Automated claim processing |
| payout-service | 8085 | Payment gateway integration |

### Kafka Topics

| Topic | Publisher | Consumers |
|-------|-----------|-----------|
| `gigshield.worker.registered` | API Gateway | Risk Engine |
| `gigshield.policy.purchased` | API Gateway | Risk Engine, Claim Service |
| `gigshield.environment.disruption` | Trigger Engine | Fraud Detection, Claim Service |
| `gigshield.claim.initiated` | Claim Service | Fraud Detection |
| `gigshield.claim.validated` | Fraud Detection | Claim Service |
| `gigshield.claim.approved` | Claim Service | Payout Service |
| `gigshield.payout.completed` | Payout Service | API Gateway |

---

# Development Roadmap

Phase 1 – Ideation and System Design  
Architecture design, AI modeling, and prototype.

Phase 2 – Automation and Protection  
Worker onboarding, policy management, dynamic premium calculation.

Phase 3 – Scale and Optimization  
Fraud detection, automated claims, instant payout simulation, analytics dashboards.

---

# Expected Impact

GigShield provides gig workers with a **financial safety net during environmental disruptions**.

By combining **AI risk modeling, real-time event processing, and automated parametric insurance**, the platform enables scalable protection for India's rapidly growing gig economy.

---

## 📁 Project Structure

```
gigshield-ai-insurance/
├── services/
│   ├── common/              # Shared models, DTOs, events
│   ├── api-gateway/         # REST API & Authentication
│   ├── risk-engine/         # AI Premium Calculation
│   ├── trigger-engine/      # Environmental Monitoring
│   ├── fraud-detection/     # Claim Validation
│   ├── claim-service/       # Claim Processing
│   └── payout-service/      # Payment Integration
├── frontend/                # Next.js Web Application
├── architecture/            # System design diagrams
├── docs/                    # Documentation
├── prototype/               # Original prototype specs
└── docker-compose.yml       # Full stack orchestration
```

---

## 🔧 Configuration

### Environment Variables

**Backend Services:**
```
MONGODB_URI=mongodb://localhost:27017/gigshield
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost
JWT_SECRET=your-secret-key
```

**Frontend:**
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

### API Keys (Optional)
For production, configure real API keys:
- `WEATHER_API_KEY` - OpenWeatherMap API key
- `AQI_API_KEY` - WAQI API token
- `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` - Payment gateway

---

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Worker registration
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Workers
- `GET /api/workers/me` - Get current worker
- `PUT /api/workers/me` - Update profile

### Policies
- `POST /api/policies/purchase` - Purchase policy
- `GET /api/policies` - List policies
- `GET /api/policies/active` - Get active policy

### Claims
- `GET /api/claims` - List claims
- `GET /api/claims/{id}` - Get claim details

### Payouts
- `GET /api/payouts` - List payouts

---

## 🧪 Testing

### Backend Unit Tests
```bash
# Run all service tests
for dir in common api-gateway risk-engine trigger-engine fraud-detection claim-service payout-service; do
  cd services/$dir && mvn test && cd ../..
done
```

### Frontend Tests
```bash
cd frontend
npm test              # Run tests
npm run test:watch    # Watch mode
npm run test:coverage # Coverage report
```

---

## 📝 License

MIT License - see LICENSE file for details.
