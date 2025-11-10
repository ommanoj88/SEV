# run_app_fixed.py - Complete Application Launcher

## 🎯 Overview

This is the **FIXED and COMPLETE** version of `run_app.py` that resolves all critical issues found in the original version.

## ✅ What Was Fixed

### 1. **PostgreSQL Now Included** ✅
- PostgreSQL is now properly started as part of infrastructure services
- Added to `INFRASTRUCTURE_SERVICES` list
- Port 5432 added to PORTS dictionary
- Database initialization integrated into startup flow

### 2. **Comprehensive Prerequisite Checks** ✅
- ✅ Docker installation and version
- ✅ Docker Compose installation and version
- ✅ PostgreSQL client (psql) availability
- ✅ Node.js detection (optional, for local dev)
- ✅ Java detection (optional, for local dev)
- ✅ Firebase credentials file check
- ✅ Environment variables validation (FIREBASE_API_KEY, FIREBASE_AUTH_DOMAIN)

### 3. **Automatic Database Initialization** ✅
- Automatically runs `reset_database.py` after PostgreSQL starts
- Creates all 8 required databases:
  - evfleet_auth
  - evfleet_fleet
  - evfleet_charging
  - evfleet_maintenance
  - evfleet_driver
  - evfleet_analytics
  - evfleet_notification
  - evfleet_billing
- Runs all Flyway migrations
- Graceful handling if databases already exist

### 4. **Service Health Monitoring** ✅
- Waits for services to become healthy before proceeding
- Checks Docker container status
- Timeout protection (120s for business services)
- Clear feedback on service health

### 5. **Better Error Handling** ✅
- Graceful handling of missing dependencies
- Clear error messages with suggested fixes
- Option to skip prerequisite checks (with warning)
- Keyboard interrupt handling

### 6. **Enhanced Status Display** ✅
- Shows all service endpoints
- Includes health check URLs
- Clear access instructions
- Color-coded output

## 📋 Prerequisites

Before running this script, ensure you have:

1. **Docker** (required)
   - Download: https://www.docker.com/get-started
   - Verify: `docker --version`

2. **Docker Compose** (required)
   - Usually included with Docker Desktop
   - Verify: `docker-compose --version`

3. **PostgreSQL Client** (optional, but recommended)
   - Windows: Download from https://www.postgresql.org/download/windows/
   - Mac: `brew install postgresql`
   - Linux: `sudo apt-get install postgresql-client`
   - Verify: `psql --version`

4. **Python 3.7+** (required)
   - Verify: `python --version`

5. **Firebase Credentials** (optional, for Auth service)
   - Place `firebase-service-account.json` in `backend/auth-service/`
   - Set environment variables: `FIREBASE_API_KEY`, `FIREBASE_AUTH_DOMAIN`

## 🚀 Usage

### Start Application
```bash
python run_app_fixed.py start
```

This will:
1. ✅ Check all prerequisites
2. ✅ Build Docker images (with --no-cache)
3. ✅ Start PostgreSQL, Redis, RabbitMQ
4. ✅ Initialize databases (run migrations)
5. ✅ Start Eureka discovery service
6. ✅ Start API Gateway
7. ✅ Start all 8 business microservices
8. ✅ Start frontend application
9. ✅ Wait for services to be healthy
10. ✅ Display service endpoints

### Stop Application
```bash
python run_app_fixed.py stop
```

### Check Status
```bash
python run_app_fixed.py status
```

### Restart Application
```bash
python run_app_fixed.py restart
```

### Clean Rebuild
```bash
python run_app_fixed.py clean
```

### Advanced Options

#### Skip Docker Build (Faster)
```bash
python run_app_fixed.py start --skip-build
```

#### Skip Prerequisite Checks (Not Recommended)
```bash
python run_app_fixed.py start --skip-prereq-check
```

## 🔍 What's Different from Original run_app.py

| Feature | Original run_app.py | run_app_fixed.py |
|---------|-------------------|------------------|
| PostgreSQL startup | ❌ Missing | ✅ Included |
| Prerequisite checks | ❌ None | ✅ Comprehensive |
| Database initialization | ❌ Manual | ✅ Automatic |
| Service health monitoring | ❌ Not used | ✅ Active |
| Firebase verification | ❌ None | ✅ Checks file & env vars |
| Error messages | ⚠️ Basic | ✅ Detailed with solutions |
| Port cleanup | ✅ Working | ✅ Enhanced |
| Docker build | ✅ Working | ✅ Enhanced with feedback |
| Service endpoints display | ✅ Basic | ✅ Complete |

## 📊 Service Architecture

### Services Started (in order):

1. **Infrastructure** (wait 20s)
   - postgres (Port 5432)
   - redis (Port 6379)
   - rabbitmq (Ports 5672, 15672)

2. **Discovery** (wait 30s)
   - eureka-server (Port 8761)

3. **Gateway** (wait 20s)
   - api-gateway (Port 8080)

4. **Business Services** (wait 40s)
   - auth-service (Port 8081)
   - fleet-service (Port 8082)
   - charging-service (Port 8083)
   - maintenance-service (Port 8084)
   - driver-service (Port 8085)
   - analytics-service (Port 8086)
   - notification-service (Port 8087)
   - billing-service (Port 8088)

5. **Frontend** (wait 10s)
   - frontend (Port 3000)

## 🌐 Service Endpoints

After starting, access these URLs:

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Main application UI |
| Eureka Dashboard | http://localhost:8761 | Service discovery monitoring |
| API Gateway | http://localhost:8080 | Unified API endpoint |
| RabbitMQ Management | http://localhost:15672 | Message queue monitoring (user: evfleet, pass: evfleet123) |
| Auth Service Health | http://localhost:8081/actuator/health | Health check |
| Fleet Service Health | http://localhost:8082/actuator/health | Health check |
| Charging Service Health | http://localhost:8083/actuator/health | Health check |
| Maintenance Service Health | http://localhost:8084/actuator/health | Health check |
| Billing Service Health | http://localhost:8088/actuator/health | Health check |

## 🐛 Troubleshooting

### Issue: "Docker is not installed"
**Solution:** Install Docker Desktop from https://www.docker.com/get-started

### Issue: "PostgreSQL client not found"
**Solution:** This is a warning, not an error. PostgreSQL runs in Docker. But for database management, install psql client.

### Issue: "Firebase credentials not found"
**Solution:**
1. Get Firebase service account JSON from Firebase Console
2. Place it at: `backend/auth-service/firebase-service-account.json`

### Issue: "Port already in use"
**Solution:** Script automatically kills ports. If it fails, manually:
- Windows: `netstat -ano | findstr :8080` then `taskkill /F /PID <PID>`
- Mac/Linux: `lsof -ti:8080 | xargs kill -9`

### Issue: "Database initialization failed"
**Solution:**
1. Check PostgreSQL is running: `docker ps | grep postgres`
2. Manually run: `python reset_database.py`
3. Check PostgreSQL logs: `docker logs postgres`

### Issue: "Services not becoming healthy"
**Solution:**
1. Check Docker logs: `docker-compose logs <service-name>`
2. Verify all prerequisites are met
3. Try clean rebuild: `python run_app_fixed.py clean`

### Issue: "Build takes too long"
**Solution:**
- First build takes 10-15 minutes (downloads dependencies)
- Subsequent builds: Use `--skip-build` flag
- Or use: `python run_app_fixed.py start --skip-build`

## ⚡ Performance Tips

1. **First Run:**
   - Expected time: 15-20 minutes
   - Includes: image build, database init, service startup

2. **Subsequent Runs:**
   - With `--skip-build`: 3-5 minutes
   - Full rebuild: 10-12 minutes

3. **Development Workflow:**
   - Code changes: Rebuild specific service only
   - Config changes: Use `restart` command
   - Major changes: Use `clean` command

## 📝 Comparison with Original

### Original run_app.py Issues (Now Fixed):
1. ❌ **PostgreSQL not started** → ✅ Now included in infrastructure
2. ❌ **No prerequisite checks** → ✅ Comprehensive validation
3. ❌ **No database initialization** → ✅ Automatic setup
4. ❌ **Health checks not used** → ✅ Active monitoring
5. ❌ **No Firebase validation** → ✅ Checks credentials
6. ❌ **Basic error handling** → ✅ Detailed with solutions

### New Features Added:
- ✅ Automatic database initialization
- ✅ Service health monitoring
- ✅ Firebase credential verification
- ✅ Environment variable validation
- ✅ Enhanced status display with all endpoints
- ✅ Better error messages with solutions
- ✅ Skip options for faster iteration
- ✅ Keyboard interrupt handling
- ✅ Cross-platform compatibility improvements

## 🎉 All 18 PRs Implemented!

This script supports the complete EV Fleet Management System with all 18 PRs:

**Phase 1: Database & Data Model** ✅
- PR 1: Vehicle Fuel Type Support
- PR 2: Feature Flag System
- PR 3: Multi-Fuel Telemetry
- PR 4: Vehicle Queries

**Phase 2: API Enhancements** ✅
- PR 5: Vehicle CRUD APIs
- PR 6: Telemetry APIs
- PR 7: Trip Analytics
- PR 8: Feature Availability

**Phase 3: Charging Service** ✅
- PR 9: Charging Validation
- PR 10: Charging Analytics

**Phase 4: Maintenance Service** ✅
- PR 11: ICE Maintenance
- PR 12: Maintenance Cost Tracking

**Phase 5: Frontend Updates** ✅
- PR 13: Vehicle Forms
- PR 14: Vehicle List & Details
- PR 15: Station Discovery
- PR 16: Dashboard Overview

**Phase 6: Billing & Monetization** ✅
- PR 17: Pricing Tiers
- PR 18: Invoice Generation

## 🔄 Migration from Original

To migrate from `run_app.py` to `run_app_fixed.py`:

1. **Stop old services:**
   ```bash
   python run_app.py stop
   ```

2. **Use new script:**
   ```bash
   python run_app_fixed.py start
   ```

3. **Optional: Rename files:**
   ```bash
   mv run_app.py run_app_old.py
   mv run_app_fixed.py run_app.py
   ```

## 📚 Additional Resources

- Main README: `README.md`
- Database Reset Guide: `DATABASE_RESET_GUIDE.md`
- Docker Build Guide: `DOCKER_BUILD_GUIDE.md`
- Migration Strategy: `MIGRATION_STRATEGY_GENERAL_EV.md`
- PR Summaries: `PR*_IMPLEMENTATION_SUMMARY.md`

## 🆘 Support

If you encounter issues:
1. Check this README's Troubleshooting section
2. Run with verbose output: `python run_app_fixed.py start`
3. Check Docker logs: `docker-compose logs <service-name>`
4. Verify prerequisites: All checks must pass
5. Try clean rebuild: `python run_app_fixed.py clean`

## ✨ Success Indicators

Application is ready when you see:
- ✅ All services started
- ✅ All services are healthy!
- ✅ Application started successfully!
- 🌐 Access the application at: http://localhost:3000

---

**Made with ❤️ for EV Fleet Management Platform**
