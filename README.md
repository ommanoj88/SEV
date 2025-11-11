# Commercial EV Fleet Management Platform

A cloud-native, microservices-based B2B SaaS platform for managing electric vehicle fleets in India.

## Project Overview

This platform provides end-to-end visibility and control over commercial electric vehicle fleets, integrating real-time telematics, charging infrastructure management, predictive maintenance, and business intelligence.

### Key Features
- Real-time GPS tracking and fleet monitoring
- Smart charging management with network integration
- Predictive maintenance and battery health analytics
- Driver behavior monitoring and performance tracking
- Cost optimization and TCO analytics
- Sustainability reporting and carbon footprint tracking
- Multi-tenant architecture with role-based access control
- **NEW**: Interactive dashboard with drill-down capabilities
- **NEW**: Comprehensive vehicle event tracking and history
- **NEW**: Vehicle genealogy reports (v.report) with PDF generation
- **NEW**: Real-time vehicle state management
- **NEW**: Document management system with expiry tracking
- **NEW**: Expense management with approval workflows
- **NEW**: Route optimization with multi-stop planning
- **NEW**: Customer management with feedback system

---

## Latest Enhancements

### Dashboard Interactivity (New)
- **Interactive Cards**: Click on dashboard cards to view detailed vehicle lists
- **Drill-Down Views**: Access comprehensive vehicle information through modal dialogs
- **Real-Time Updates**: Live status updates for fleet metrics

### Vehicle Event Tracking (New)
- **Event Sourcing**: Complete history of all vehicle events
  - Trip events (started, ended, paused, resumed)
  - Charging events (started, completed, failed)
  - Maintenance events (scheduled, completed, cancelled)
  - Alert events (raised, resolved, acknowledged)
  - Status changes and system events
- **Event History API**: Query events by type, severity, date range
- **Real-Time Monitoring**: Track all vehicle activities in production

### Vehicle Current State (New)
- **State Snapshots**: Real-time state of each vehicle
  - Location and movement data
  - Battery/fuel levels and health
  - Current activity (charging, maintenance, trip)
  - Alert status and connection status
- **Quick Access**: Optimized queries for current vehicle status
- **Performance Metrics**: Efficiency scores and utilization tracking

### Vehicle Report Generation - v.report (New)
- **Comprehensive Reports**: Generate detailed PDF reports with:
  - Vehicle information and specifications
  - Complete event history (genealogy)
  - Trip history and analytics
  - Maintenance records
  - Charging history (for EV/Hybrid)
  - Alert history
  - Performance metrics
  - Cost analysis
- **Genealogy Reports**: Focus on complete event timeline
- **Customizable**: Select which sections to include
- **Date Range**: Generate reports for specific time periods
- **UI Interface**: User-friendly report generation page at `/reports`

### Route Optimization & Customer Management (New - November 2025)
- **Route Planning**: Create and manage multi-stop delivery routes
  - Origin and destination with GPS coordinates
  - Multi-stop waypoint management with sequencing
  - Optimization criteria (Distance, Time, Fuel, Cost)
  - Traffic consideration and toll road options
  - Time window constraints for deliveries
  - Planned vs actual performance tracking
  - Route execution controls (Start, Complete, Cancel)
- **Customer Management**: Comprehensive customer relationship management
  - Individual and Business customer types
  - Complete contact and address information
  - GSTIN and PAN support for business customers
  - Credit limit and outstanding balance tracking
  - Delivery success/failure statistics
  - Service rating with 5-star system
  - Customer feedback with categories
  - Feedback response management
- **Proof of Delivery (POD)**: Built-in POD support
  - Signature capture path storage
  - Photo upload capability
  - Customer confirmation tracking
  - Delivery notes and completion status
- **UI Pages**: Full-featured web interfaces at `/routes` and `/customers`

---

## Technology Stack

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.0
- **Microservices**: Spring Cloud (Gateway, Eureka, Config)
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7+
- **Message Queue**: RabbitMQ 3.12+
- **Authentication**: Firebase Admin SDK

### Frontend
- **Framework**: React 18+
- **State Management**: Redux Toolkit
- **UI Library**: Material-UI / Ant Design
- **Maps**: Mapbox GL JS / Google Maps API
- **Charts**: Recharts / D3.js
- **Authentication**: Firebase SDK

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Build Tool**: Maven 3.9+

---

## Architecture

The application follows a microservices architecture with the following components:

### Infrastructure Services
1. **Eureka Server** (Port 8761) - Service Discovery
2. **Config Server** (Port 8888) - Centralized Configuration
3. **API Gateway** (Port 8080) - Single Entry Point

### Business Services
4. **Auth Service** (Port 8081) - User Authentication & Authorization
5. **Fleet Service** (Port 8082) - Vehicle & Fleet Management
6. **Charging Service** (Port 8083) - Charging Infrastructure Management
7. **Maintenance Service** (Port 8084) - Predictive Maintenance
8. **Driver Service** (Port 8085) - Driver Management
9. **Analytics Service** (Port 8086) - Business Intelligence & Reporting
10. **Notification Service** (Port 8087) - Alerts & Notifications
11. **Billing Service** (Port 8088) - Cost Tracking & Invoicing

### Data Stores
- **PostgreSQL**: Separate database for each microservice
- **Redis**: Caching and session management
- **RabbitMQ**: Event-driven communication

### Frontend
- **React App** (Port 3000) - Web Application

For detailed architecture documentation, see [docs/MICROSERVICES_ARCHITECTURE.md](docs/MICROSERVICES_ARCHITECTURE.md)

---

## Project Structure

```
SEV/
├── backend/
│   ├── eureka-server/          # Service Discovery
│   ├── config-server/          # Configuration Management
│   ├── api-gateway/            # API Gateway
│   ├── auth-service/           # Authentication Service
│   ├── fleet-service/          # Fleet Management Service
│   ├── charging-service/       # Charging Management Service
│   ├── maintenance-service/    # Maintenance Service
│   ├── driver-service/         # Driver Management Service
│   ├── analytics-service/      # Analytics Service
│   ├── notification-service/   # Notification Service
│   └── billing-service/        # Billing Service
├── frontend/                   # React Application
├── docker/                     # Docker Compose & Configuration
├── docs/                       # Documentation
│   ├── MICROSERVICES_ARCHITECTURE.md
│   ├── Product_Concept_Document.md
│   └── Research_Notes_and_Sources.md
├── impresourcesfortesting/     # Firebase & Test Resources
│   └── firebase-service-account-test.json
└── README.md
```

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17+**: [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+**: [Download](https://maven.apache.org/download.cgi)
- **Node.js 18+** and npm: [Download](https://nodejs.org/)
- **Docker** and **Docker Compose**: [Download](https://www.docker.com/products/docker-desktop/)
- **Git**: [Download](https://git-scm.com/downloads)

---

## Getting Started

### Quick Start with Universal Launcher (Recommended)

The easiest way to start the entire application with the latest code:

```bash
# Start all services (builds with --no-cache to ensure latest code)
python run_app.py start

# Access the application at http://localhost:3000
```

This launcher script:
- ✅ Automatically kills processes on required ports
- ✅ Rebuilds Docker images with `--no-cache` (ensures latest code is used)
- ✅ Starts all services in the correct dependency order
- ✅ Provides service health monitoring
- ✅ Shows all service endpoints

For more details, see [RUN_APP_GUIDE.md](RUN_APP_GUIDE.md)

### Manual Setup (Alternative)

If you prefer to set up manually:

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SEV
```

### 3. Set Up Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing project
3. Enable Authentication (Email/Password, Google, etc.)
4. Download service account key JSON file
5. Place it in `impresourcesfortesting/firebase-service-account-test.json`

### 4. Configure Environment Variables

Create a `.env` file in the root directory:

```env
# Firebase Configuration
FIREBASE_API_KEY=your_firebase_api_key
FIREBASE_AUTH_DOMAIN=your_project_id.firebaseapp.com
FIREBASE_PROJECT_ID=your_project_id

# Database Configuration
POSTGRES_USER=evfleet
POSTGRES_PASSWORD=evfleet123

# RabbitMQ Configuration
RABBITMQ_USER=evfleet
RABBITMQ_PASSWORD=evfleet123
```

### 5. Start Infrastructure Services with Docker Compose

```bash
cd docker
docker-compose up -d postgres redis rabbitmq
```

Wait for all services to be healthy:

```bash
docker-compose ps
```

### 6. Build All Microservices

Navigate to each backend service and build:

```bash
# Build Eureka Server
cd backend/eureka-server
mvn clean package

# Build Config Server
cd ../config-server
mvn clean package

# Build API Gateway
cd ../api-gateway
mvn clean package

# Build Auth Service
cd ../auth-service
mvn clean package

# ... repeat for all services
```

### 7. Start All Services with Docker Compose

```bash
cd docker
docker-compose up -d
```

### 8. Access the Applications

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (user: evfleet, pass: evfleet123)
- **React Frontend**: http://localhost:3000

---

## Development Workflow

### Running Services Locally (without Docker)

1. **Start Infrastructure**:
```bash
cd docker
docker-compose up -d postgres redis rabbitmq
```

2. **Start Eureka Server**:
```bash
cd backend/eureka-server
mvn spring-boot:run
```

3. **Start Config Server**:
```bash
cd backend/config-server
mvn spring-boot:run
```

4. **Start API Gateway**:
```bash
cd backend/api-gateway
mvn spring-boot:run
```

5. **Start Individual Microservices**:
```bash
cd backend/auth-service
mvn spring-boot:run
```

6. **Start React Frontend**:
```bash
cd frontend
npm install
npm start
```

### Making Code Changes

1. Make changes to the service code
2. Rebuild the service: `mvn clean package`
3. Restart the Docker container: `docker-compose restart <service-name>`

---

## Testing

### Running Unit Tests

```bash
cd backend/<service-name>
mvn test
```

### Running Integration Tests

```bash
mvn verify
```

### API Testing with Postman/Insomnia

Import the API collection from `docs/api-collection.json`

---

## API Documentation

Once services are running, access Swagger UI:

- **Auth Service**: http://localhost:8081/swagger-ui.html
- **Fleet Service**: http://localhost:8082/swagger-ui.html
- **Charging Service**: http://localhost:8083/swagger-ui.html
- (And so on for each service)

---

## Database Management

### Database Reset and Initialization

⚠️ **IMPORTANT**: Backend database sync is now **required** for authentication. Users must be synced from Firebase to the backend database.

To reset and initialize all databases with proper schema and seed data:

```bash
python reset_database.py
```

This script will:
- Drop all existing databases
- Create fresh databases for all microservices
- Run Flyway migrations to create tables
- Seed default roles (SUPER_ADMIN, ADMIN, FLEET_MANAGER, DRIVER, VIEWER)

For detailed documentation, see [Database Reset Guide](DATABASE_RESET_GUIDE.md) and [Authentication Changes](AUTHENTICATION_CHANGES.md).

### Accessing PostgreSQL

```bash
docker exec -it evfleet-postgres psql -U evfleet -d auth_db
```

### Database Migrations

The auth-service now uses **Flyway** for database migrations. Migration files are located in:
- `backend/auth-service/src/main/resources/db/migration/`

Migrations run automatically when the service starts. For manual migration:

```bash
cd backend/auth-service
mvn flyway:migrate
```

---

## Monitoring and Logging

### View Logs

```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f auth-service
```

### Health Checks

- All services expose `/actuator/health` endpoint
- Check service health: http://localhost:8081/actuator/health

---

## Deployment

### Building for Production

1. Set profile to `prod` in application.yml
2. Build Docker images:

```bash
docker-compose build
```

3. Push to container registry:

```bash
docker tag evfleet-auth-service:latest your-registry/evfleet-auth-service:latest
docker push your-registry/evfleet-auth-service:latest
```

4. Deploy to Kubernetes/ECS/Azure Container Apps

---

## Troubleshooting

### Common Issues

1. **Service not registering with Eureka**
   - Check Eureka Server is running
   - Verify `eureka.client.service-url.defaultZone` in application.yml

2. **Database connection errors**
   - Ensure PostgreSQL is running: `docker-compose ps postgres`
   - Check database credentials in application.yml

3. **Port conflicts**
   - Ensure no other services are running on ports 8080-8088, 5432, 6379, 5672

4. **Firebase authentication failing**
   - Verify firebase-service-account-test.json is in the correct location
   - Check Firebase project settings

### Getting Help

- Check logs: `docker-compose logs <service-name>`
- Review documentation in `docs/` folder
- Open an issue on GitHub

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add some feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit a pull request

---

## License

This project is proprietary software for EV Fleet Management Platform.

---

## Contact

For questions or support, contact the development team.

---

## Roadmap

### Phase 1 (Current - MVP)
- [x] Microservices architecture design
- [x] Infrastructure services (Eureka, Config, Gateway)
- [ ] Core services (Auth, Fleet, Charging)
- [ ] Basic React frontend
- [ ] Docker deployment setup

### Phase 2 (Beta)
- [ ] Advanced analytics
- [ ] Predictive maintenance ML models
- [ ] Mobile app (React Native)
- [ ] Integration with charging networks
- [ ] Payment gateway integration

### Phase 3 (GA)
- [ ] Advanced security (SOC 2)
- [ ] Multi-tenancy enhancements
- [ ] API marketplace
- [ ] International expansion
- [ ] Kubernetes deployment

---

**Version**: 2.1.0
**Last Updated**: November 11, 2025
**Status**: Active Development - Route Optimization & Customer Management Complete
