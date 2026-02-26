# TinyTrack — Baby Development Tracker

A full-stack web application for tracking baby development: measurements, feeding, diapers, sleep, and WHO growth percentiles.

## Tech Stack

- **Backend**: Spring Boot 3 (Kotlin), PostgreSQL, Flyway, JWT auth
- **Frontend**: React 18, Vite, TypeScript, shadcn/ui, Tailwind CSS, TanStack Query
- **Infrastructure**: Docker Compose

## Quick Start

### Prerequisites
- Docker and Docker Compose v2+

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env — at minimum change JWT_SECRET to a long random string
```

### 2. Start everything

```bash
docker compose up --build
```

The app will be available at:
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api/v1/
- **PostgreSQL**: localhost:5432

### 3. Register your first account

Open http://localhost:5173 and click **Register**.

---

## Development Setup (without Docker)

### Backend

```bash
cd backend
# Requires Java 21+
./gradlew bootRun
```

Needs a local PostgreSQL instance. Set env vars or update `application-dev.yml`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login, returns JWT + refresh token |
| POST | `/api/v1/auth/refresh` | Exchange refresh token for new access token |
| POST | `/api/v1/auth/logout` | Invalidate refresh token |
| GET/POST | `/api/v1/families` | List / create families |
| POST | `/api/v1/families/{id}/invite` | Invite member by email |
| GET/POST | `/api/v1/children` | List / create children |
| GET/PUT/DELETE | `/api/v1/children/{id}` | Get / update / delete child |
| GET/POST | `/api/v1/children/{id}/measurements` | Measurements CRUD |
| GET/POST | `/api/v1/children/{id}/feeding-logs` | Feeding logs CRUD |
| GET/POST | `/api/v1/children/{id}/diaper-logs` | Diaper logs CRUD |
| GET/POST | `/api/v1/children/{id}/sleep-logs` | Sleep logs CRUD |
| GET | `/api/v1/children/{id}/growth-analysis` | WHO percentile analysis |
| GET | `/api/v1/children/{id}/export?format=pdf` | Export PDF report |
| GET | `/api/v1/children/{id}/export?format=csv` | Export CSV data |
| GET/PUT | `/api/v1/users/me` | Get / update current user profile |

---

## Environment Variables

See `.env.example` for all variables with descriptions.

---

## Features

- **JWT authentication** with refresh tokens
- **Multi-family support** — invite a partner by email
- **Growth charts** with WHO percentile bands (P3, P15, P50, P85, P97)
- **Bilingual** — English and Hungarian (language preference saved to profile)
- **Email reminders** if no feeding log recorded within a configurable time window
- **Export** child data as PDF (with charts) or CSV
- **Mobile-first** responsive UI
