# Local Testing Guide - EV Fleet Management Platform

**Version:** 1.0.0  
**Last Updated:** November 4, 2025  
**Status:** Ready for Local Testing

---

## Overview

This guide provides step-by-step instructions for running and testing the entire EV Fleet Management Platform locally on your machine. All services, including backend microservices, frontend application, and infrastructure components, can run without external dependencies except for Firebase authentication (optional for testing with mock data).

---

## Prerequisites

Before starting, ensure you have the following installed:

### Required Software
- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 18+** and npm - [Download](https://nodejs.org/)
- **Docker** and **Docker Compose** - [Download](https://www.docker.com/products/docker-desktop/)
- **Git** - [Download](https://git-scm.com/downloads)

### Optional (for production features)
- **Firebase Account** - For authentication (can use mock data for testing)
- **Mapbox Account** - For maps (frontend will work without it, just no map rendering)

### System Requirements
- **RAM:** Minimum 8GB, Recommended 16GB
- **Disk Space:** At least 5GB free
- **Operating System:** Windows 10/11, macOS, or Linux

---

## Quick Start (5 Minutes)

This is the fastest way to get everything running:

```bash
# 1. Clone the repository
git clone <repository-url>
cd SEV

# 2. Start infrastructure services
cd docker
docker-compose up -d postgres redis rabbitmq

# 3. Wait for services to be healthy (about 30 seconds)
docker-compose ps

# 4. Start the frontend
cd ../frontend
npm install
npm start

# 5. Frontend will open at http://localhost:3000
# The app will work with mock data since backend services are optional for basic testing
```

---

## Complete Setup Instructions

### Step 1: Start Infrastructure Services

Infrastructure services (PostgreSQL, Redis, RabbitMQ) are required for backend services to work.

```bash
cd SEV/docker
docker-compose up -d postgres redis rabbitmq
```

**Verify services are running:**
```bash
docker-compose ps
```

You should see:
- `evfleet-postgres` - healthy
- `evfleet-redis` - healthy
- `evfleet-rabbitmq` - healthy

**Service URLs:**
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- RabbitMQ Management UI: http://localhost:15672 (user: `evfleet`, pass: `evfleet123`)

### Step 2: Build Backend Services (Optional)

Backend services are **optional** for local testing since the frontend has comprehensive mock data fallbacks. However, if you want to test the full stack:

```bash
cd SEV/backend

# Build all services
for service in eureka-server config-server api-gateway auth-service fleet-service charging-service maintenance-service driver-service analytics-service notification-service billing-service; do
  echo "Building $service..."
  cd $service
  mvn clean package -DskipTests
  cd ..
done
```

**Note:** This will take about 5-10 minutes the first time as Maven downloads dependencies.

### Step 3: Run Backend Services (Optional)

Start services in this order:

```bash
# Terminal 1 - Eureka Server (Service Discovery)
cd backend/eureka-server
mvn spring-boot:run

# Wait for Eureka to start (about 30 seconds)
# Then in new terminals:

# Terminal 2 - Config Server
cd backend/config-server
mvn spring-boot:run

# Terminal 3 - API Gateway
cd backend/api-gateway
mvn spring-boot:run

# Terminal 4 - Auth Service
cd backend/auth-service
mvn spring-boot:run

# Terminal 5 - Fleet Service
cd backend/fleet-service
mvn spring-boot:run

# Terminal 6 - Charging Service
cd backend/charging-service
mvn spring-boot:run

# Continue for other services as needed...
```

**Service URLs:**
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Auth Service: http://localhost:8081
- Fleet Service: http://localhost:8082
- Charging Service: http://localhost:8083
- Maintenance Service: http://localhost:8084
- Driver Service: http://localhost:8085
- Analytics Service: http://localhost:8086
- Notification Service: http://localhost:8087
- Billing Service: http://localhost:8088

### Step 4: Setup Frontend Environment

```bash
cd frontend

# Create .env file from example
cp .env.example .env
```

**Edit `.env` file:**
```env
# API Configuration (use these defaults for local testing)
REACT_APP_API_URL=http://localhost:8080/api/v1
REACT_APP_WEBSOCKET_URL=ws://localhost:8080/ws

# Firebase Configuration (OPTIONAL - app works without this)
REACT_APP_FIREBASE_API_KEY=your_api_key
REACT_APP_FIREBASE_AUTH_DOMAIN=your_domain
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
REACT_APP_FIREBASE_STORAGE_BUCKET=your_bucket
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
REACT_APP_FIREBASE_APP_ID=your_app_id

# Mapbox Configuration (OPTIONAL - maps won't render without this)
REACT_APP_MAPBOX_TOKEN=your_mapbox_token
```

**Note:** The frontend will work without Firebase or Mapbox credentials. You'll just see placeholder content where maps would normally appear.

### Step 5: Install Frontend Dependencies

```bash
cd frontend
npm install
```

**Expected output:** Should complete without errors. You may see some warnings, which can be ignored.

### Step 6: Start Frontend Development Server

```bash
npm start
```

The application will automatically open in your browser at http://localhost:3000

---

## Testing the Application

### Frontend Testing (No Backend Required)

The frontend includes comprehensive mock data for all features:

#### Test Scenarios:

**1. Fleet Management**
- Navigate to "Fleet Management" from sidebar
- You should see 3 mock vehicles:
  - Tesla Model 3 (85% battery, ACTIVE)
  - Tata Nexon EV (45% battery, CHARGING)
  - MG ZS EV (20% battery, INACTIVE)
- Click on a vehicle to see details
- All data displays correctly without backend

**2. Charging Management**
- Navigate to "Charging" from sidebar
- View 2 mock charging stations on the map
- Switch to "Sessions" tab to see charging history
- All sessions display with proper status

**3. Driver Management**
- Navigate to "Drivers" from sidebar
- View 3 mock drivers with performance metrics
- Check leaderboard rankings
- All performance data displays

**4. Analytics Dashboard**
- Navigate to "Analytics" from sidebar
- View fleet summary metrics
- Check energy consumption charts
- Review cost analysis
- All charts render with mock data

**5. Maintenance**
- Navigate to "Maintenance" from sidebar
- View maintenance schedules
- Check service history
- All records display properly

**6. Notifications**
- Click the bell icon in header
- View mock notifications and alerts
- Mark as read functionality works

### Full Stack Testing (With Backend)

If you're running backend services:

**1. Verify Service Registration**
- Open Eureka Dashboard: http://localhost:8761
- All services should appear in "Instances currently registered with Eureka"

**2. Test API Endpoints**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Get vehicles (requires auth)
curl http://localhost:8082/api/v1/vehicles

# Get charging stations
curl http://localhost:8083/api/v1/charging/stations
```

**3. Check RabbitMQ**
- Open RabbitMQ Management: http://localhost:15672
- Login with `evfleet` / `evfleet123`
- Verify exchanges and queues are created

**4. Database Verification**
```bash
# Connect to PostgreSQL
docker exec -it evfleet-postgres psql -U evfleet

# List databases
\l

# Connect to fleet database
\c fleet_db

# List tables
\dt

# Query vehicles (should be empty initially)
SELECT * FROM vehicles;
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Frontend won't start - "react-scripts: not found"
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

#### 2. Port already in use
```bash
# Find process using port (example: 3000)
# On Linux/Mac:
lsof -i :3000
kill -9 <PID>

# On Windows:
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

#### 3. Docker containers won't start
```bash
# Stop all containers
docker-compose down

# Remove volumes (WARNING: deletes data)
docker-compose down -v

# Start fresh
docker-compose up -d
```

#### 4. Maven build fails
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -DskipTests
```

#### 5. Backend service won't connect to database
- Verify PostgreSQL is running: `docker-compose ps postgres`
- Check service logs for connection errors
- Ensure database was created: `docker exec -it evfleet-postgres psql -U evfleet -c "\l"`

#### 6. TypeScript compilation errors in frontend
```bash
# Ensure correct TypeScript version
npm list typescript
# Should show 4.9.5

# If not, reinstall
npm install typescript@4.9.5
```

---

## Feature Testing Checklist

Use this checklist to verify all features work:

### Frontend Features
- [ ] Dashboard loads without errors
- [ ] Vehicle list displays
- [ ] Vehicle details page works
- [ ] Charging stations map renders (if Mapbox configured)
- [ ] Charging sessions list displays
- [ ] Driver list and leaderboard work
- [ ] Analytics charts render
- [ ] Maintenance schedules display
- [ ] Notifications appear in header
- [ ] Sidebar navigation works
- [ ] All pages are responsive (test on mobile view)

### Backend Features (If Running)
- [ ] Eureka shows all registered services
- [ ] API Gateway routes requests
- [ ] Health endpoints respond
- [ ] Database connections work
- [ ] RabbitMQ exchanges created
- [ ] Redis cache accessible
- [ ] Swagger UI available for each service

---

## Performance Expectations

### Startup Times
- **Infrastructure (Docker):** 30-60 seconds
- **Each Backend Service:** 30-60 seconds
- **Frontend:** 30-45 seconds

### Resource Usage
- **PostgreSQL:** ~100MB RAM
- **Redis:** ~20MB RAM
- **RabbitMQ:** ~150MB RAM
- **Each Java Service:** ~300-500MB RAM
- **Frontend (dev server):** ~200MB RAM

**Total for full stack:** ~3-4GB RAM

---

## Development Workflow

### Making Changes

**Frontend Changes:**
```bash
cd frontend
# Make your changes in src/
# Hot reload happens automatically
# No need to restart server
```

**Backend Changes:**
```bash
cd backend/<service-name>
# Make changes in src/
# Rebuild
mvn clean package -DskipTests
# Restart service
mvn spring-boot:run
```

### Testing Changes

**Frontend:**
```bash
# In browser DevTools, check Console for errors
# Verify Network tab for API calls
# Test responsive design with device toolbar
```

**Backend:**
```bash
# Check logs for errors
# Use Swagger UI to test endpoints
# Verify database changes with psql
```

---

## Stopping Services

### Stop Frontend
- Press `Ctrl+C` in the terminal running `npm start`

### Stop Backend Services
- Press `Ctrl+C` in each terminal running a service

### Stop Infrastructure
```bash
cd docker
docker-compose down

# To also remove data volumes:
docker-compose down -v
```

---

## Next Steps

After verifying local setup works:

1. **Add Firebase Authentication** - Set up Firebase project for real auth
2. **Configure Mapbox** - Get API token for map features
3. **External API Integration** - Connect to real charging networks, payment gateways
4. **Production Deployment** - Deploy to cloud (AWS, Azure, GCP)
5. **Monitoring Setup** - Configure Prometheus + Grafana
6. **CI/CD Pipeline** - Set up automated builds and deployments

---

## Support

### Getting Help

- **Documentation:** Check `/docs` folder in repository
- **Backend Architecture:** See `backend/MICROSERVICES_ARCHITECTURE.md`
- **Frontend Guide:** See `frontend/README.md`
- **Integration Guide:** See `frontend/BACKEND_INTEGRATION_GUIDE.md`

### Known Limitations

1. **External APIs:** Not connected (Tata Power, Statiq, Razorpay, etc.) - All mocked
2. **Real-time Updates:** WebSocket requires backend to be running
3. **Firebase Auth:** Optional for testing, frontend works with mock data
4. **Email/SMS:** Notification adapters are mocked
5. **Maps:** Require Mapbox token for rendering

These are **expected** and don't affect local testing with mock data.

---

## Success Criteria

You've successfully set up the platform locally if:

✅ Frontend loads at http://localhost:3000  
✅ All pages navigate without errors  
✅ Mock data displays correctly  
✅ No console errors in browser DevTools  
✅ Docker containers are healthy (if running backend)  
✅ Services registered in Eureka (if running backend)  

---

## Conclusion

The EV Fleet Management Platform is fully functional locally with comprehensive mock data. All features can be tested without backend services. For full-stack testing, backend services integrate seamlessly with the frontend.

**Ready for Production?** Follow deployment guides in `/docs` folder.

**Questions?** Check documentation or open an issue in the repository.

---

**Happy Testing! 🚗⚡**
