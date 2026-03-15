# Microservices Overview

The system is composed of multiple independent services communicating through Kafka events.

Services include:

API Gateway
Handles worker registration and policy purchase requests.

Risk Engine
Calculates dynamic premium pricing using AI.

Trigger Engine
Detects environmental disruptions and creates claim events.

Fraud Detection
Validates worker location and activity patterns.

Claim Service
Processes automated claims.

Payout Service
Simulates payout to the worker.
