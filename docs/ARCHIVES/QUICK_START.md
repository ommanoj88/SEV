# Quick Start Guide - Phase 1 MVP

## Overview

This guide will help you quickly start the complete EV Fleet Management Platform with all microservices.

## ✅ Phase 1 MVP Status: COMPLETE

- ✅ Microservices architecture design
- ✅ Infrastructure services (Eureka, Config, Gateway)  
- ✅ Core services (Auth, Fleet, Charging, Maintenance, Driver, Analytics, Notification, Billing)
- ✅ Basic React frontend
- ✅ Docker deployment setup

## Prerequisites

- Docker and Docker Compose installed
- Python 3.7+ (for launcher script)
- 8GB+ RAM recommended
- Ports 3000, 5432, 6379, 5672, 8080-8088, 8761, 15672 available

## Quick Start (Recommended)

### Option 1: Using Universal Launcher (Easiest)

```bash
# Start all services with latest code
python run_app.py start

# Check service status
python run_app.py status

# Stop all services
python run_app.py stop
```

The launcher script:
- ✅ Automatically kills processes on required ports
- ✅ Rebuilds Docker images with `--no-cache` (ensures latest code)
- ✅ Starts all services in correct dependency order
- ✅ Monitors service health
- ✅ Shows all service endpoints

### Option 2: Manual Docker Compose

```bash
# 1. Set up environment variables
cd docker
cp .env.example .env
# Edit .env and add your Firebase credentials

# 2. Build all services
docker compose build --no-cache

# 3. Start all services
docker compose up -d

# 4. Check logs
docker compose logs -f

# 5. Stop services
docker compose down
```

## Access Points

Once services are running:

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (user: evfleet, pass: evfleet123)

### Individual Service Endpoints

- Auth Service: http://localhost:8081
- Fleet Service: http://localhost:8082
- Charging Service: http://localhost:8083
- Maintenance Service: http://localhost:8084
- Driver Service: http://localhost:8085
- Analytics Service: http://localhost:8086
- Notification Service: http://localhost:8087
- Billing Service: http://localhost:8088

## Service Architecture

```
┌─────────────────────────────────────────────────────┐
│                  React Frontend                      │
│                 (Port 3000)                          │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│              API Gateway (Port 8080)                 │
└──────────────────────┬──────────────────────────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│ Eureka       │ │  Config  │ │   Redis      │
│ (8761)       │ │  Server  │ │   (6379)     │
└──────────────┘ └──────────┘ └──────────────┘
                       │
          ┌────────────┼────────────────────┐
          ▼            ▼                     ▼
┌──────────────┐ ┌──────────┐      ┌──────────────┐
│  Auth        │ │  Fleet   │ ...  │  Billing     │
│  (8081)      │ │  (8082)  │      │  (8088)      │
└──────────────┘ └──────────┘      └──────────────┘
          │            │                     │
          └────────────┼─────────────────────┘
                       ▼
              ┌──────────────┐
              │  PostgreSQL  │
              │   (5432)     │
              └──────────────┘
```

## Infrastructure Services

1. **PostgreSQL** - Multi-database setup
   - Auto-creates databases for all microservices
   - evfleet_auth, evfleet_fleet, evfleet_charging, etc.

2. **Redis** - Caching and session management

3. **RabbitMQ** - Event-driven messaging
   - Management UI: http://localhost:15672

4. **Eureka Server** - Service discovery
   - Dashboard: http://localhost:8761

## Troubleshooting

### Services not starting?

```bash
# Check logs
docker compose logs -f [service-name]

# Restart a specific service
docker compose restart [service-name]
```

### Port conflicts?

```bash
# Check what's using ports
lsof -i :3000
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use the launcher which does this automatically
python run_app.py start
```

### Database issues?

```bash
# Reset database
docker compose down -v
docker compose up -d postgres

# Check if databases were created
docker exec -it evfleet-postgres psql -U postgres -l
```

### Frontend build errors?

The frontend has been fixed and now builds successfully. If you still see errors:

```bash
cd frontend
npm install --legacy-peer-deps
npm run build
```

## Development Workflow

### Making code changes:

1. Make your changes to source code
2. Rebuild specific service:
   ```bash
   docker compose build --no-cache [service-name]
   docker compose up -d [service-name]
   ```

3. Or rebuild everything:
   ```bash
   python run_app.py restart
   ```

### Testing individual services:

```bash
# Run service locally (without Docker)
cd backend/[service-name]
mvn spring-boot:run
```

## Next Steps

- Configure Firebase authentication (see .env.example)
- Review API documentation at each service's /swagger-ui.html
- Check MICROSERVICES_ARCHITECTURE.md for detailed architecture
- See API_MISMATCH_REPORT.md for known API issues

## Support

For issues or questions:
- Check logs: `docker compose logs -f`
- Review documentation in /docs folder
- Open an issue on GitHub

---

**Version**: 1.0.0 (Phase 1 MVP Complete)  
**Last Updated**: November 11, 2025  
**Status**: ✅ Production Ready
