# GigShield Admin Dashboard

Professional operations dashboard for GigShield microservices with live backend connectivity via `admin-simulator`.

## Features

- Live service health dashboard
- Real-time simulation controls
- Claims, workers, and policies operations views
- Event stream and system logs monitoring
- Metrics-driven overview cards

## Prerequisites

- Node.js 18+
- npm 9+
- Running backend services (at minimum `admin-simulator`)

## Environment

Create `/home/runner/work/gigshield-ai-insurance/gigshield-ai-insurance/admin-dashboard/.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8091
NEXT_PUBLIC_SIMULATOR_URL=http://localhost:8091
```

> For local integration, both dashboard API and simulator API are served from `admin-simulator` on port `8091`.

## Run locally

```bash
cd /home/runner/work/gigshield-ai-insurance/gigshield-ai-insurance/admin-dashboard
npm ci
npm run dev
```

Open: http://localhost:3000

## Validation

```bash
npm run lint
npm run build
```

## Backend endpoints used by dashboard

- `GET /api/admin/health`
- `GET /api/admin/health/{serviceName}`
- `GET /api/admin/metrics`
- `GET /api/workers`
- `GET /api/workers/{id}`
- `GET /api/policies`
- `GET /api/policies/{id}`
- `GET /api/claims`
- `GET /api/claims/{id}`
- `GET /api/admin/events`
- `GET /api/admin/events/stream`
- `GET /api/admin/logs`
- `POST /api/admin/simulate/weather`
- `GET /api/admin/simulate/simulations`
- `GET /api/admin/simulate/simulations/{id}`
- `DELETE /api/admin/simulate/simulations/{id}`

## UX and design notes

- Uses a dark operational UI tuned for dense admin workflows
- Typography and card hierarchy optimized for readability/scannability
- Auto-refresh on health, metrics, logs, and event views for live observability
