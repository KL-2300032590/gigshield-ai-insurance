# GigShield – AI Powered Parametric Insurance for Gig Workers

AI-powered parametric insurance platform that protects gig delivery workers from income loss caused by external disruptions such as extreme weather and environmental conditions.

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

Frontend  
React

Backend  
Spring Boot

Event Streaming  
Apache Kafka

Caching  
Redis

Database  
PostgreSQL

Machine Learning  
Python + Scikit-learn

External APIs  
Weather APIs  
Air Quality APIs

Payments  
Razorpay Sandbox / UPI Simulation

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
