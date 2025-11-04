# Fleet Management Service

The Fleet Management Service is a critical core microservice of the EV Fleet Management Platform that handles:
- Vehicle tracking and management
- Real-time telemetry data processing
- Trip management and analytics
- Geofencing capabilities

## Features

### Vehicle Management
- CRUD operations for electric vehicles
- Real-time location tracking
- Battery state of charge (SOC) monitoring
- Vehicle status management (Active, Inactive, Maintenance, In-Trip, Charging)
- Support for multiple vehicle types (2-Wheeler, 3-Wheeler, LCV)

### Telemetry Data Processing
- Real-time telemetry data ingestion
- Historical telemetry data querying
- Battery health monitoring
- Error code tracking
- Performance metrics collection

### Trip Management
- Trip initiation and completion
- Real-time trip tracking
- Trip pause/resume functionality
- Efficiency scoring based on driving behavior
- Energy consumption tracking
- Trip analytics and reporting

### Geofencing
- Multiple geofence types (Charging Zones, Depots, Restricted Areas, etc.)
- Support for circular and polygon geofences
- Entry/exit alerts
- Point-in-geofence verification
- Schedule-based geofence activation

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **Message Broker**: RabbitMQ
- **Cache**: Redis
- **Service Discovery**: Eureka Client
- **API Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- RabbitMQ 3.11+
- Redis 7+
- Eureka Server (for service discovery)

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE evfleet_fleet;
```

The application will automatically create the required tables using JPA/Hibernate.

## Configuration

The application can be configured through environment variables or `application.yml`:

### Database
- `DB_USERNAME`: Database username (default: postgres)
- `DB_PASSWORD`: Database password (default: postgres)

### RabbitMQ
- `RABBITMQ_HOST`: RabbitMQ host (default: localhost)
- `RABBITMQ_PORT`: RabbitMQ port (default: 5672)
- `RABBITMQ_USERNAME`: RabbitMQ username (default: guest)
- `RABBITMQ_PASSWORD`: RabbitMQ password (default: guest)

### Redis
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `REDIS_PASSWORD`: Redis password (optional)

### Eureka
- `EUREKA_SERVER`: Eureka server URL (default: http://localhost:8761/eureka/)

### Server
- `PORT`: Application port (default: 8082)

## Running the Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Docker

```bash
docker build -t fleet-service:latest .
docker run -p 8082:8082 fleet-service:latest
```

### Using Docker Compose

```bash
docker-compose up -d
```

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8082/swagger-ui.html
```

API documentation is available at:
```
http://localhost:8082/api-docs
```

## API Endpoints

### Vehicle Management
- `POST /api/fleet/vehicles` - Create a new vehicle
- `GET /api/fleet/vehicles/{id}` - Get vehicle by ID
- `PUT /api/fleet/vehicles/{id}` - Update vehicle
- `DELETE /api/fleet/vehicles/{id}` - Delete vehicle
- `GET /api/fleet/vehicles/company/{companyId}` - Get vehicles by company
- `PATCH /api/fleet/vehicles/{id}/location` - Update vehicle location
- `PATCH /api/fleet/vehicles/{id}/battery` - Update battery SOC

### Telemetry
- `POST /api/fleet/telemetry` - Submit telemetry data
- `POST /api/fleet/telemetry/batch` - Submit batch telemetry
- `GET /api/fleet/telemetry/vehicle/{vehicleId}` - Get vehicle telemetry
- `GET /api/fleet/telemetry/vehicle/{vehicleId}/latest` - Get latest telemetry

### Trip Management
- `POST /api/fleet/trips` - Start a new trip
- `PUT /api/fleet/trips/{id}/end` - End a trip
- `PUT /api/fleet/trips/{id}/pause` - Pause a trip
- `PUT /api/fleet/trips/{id}/resume` - Resume a trip
- `GET /api/fleet/trips/{id}` - Get trip by ID
- `GET /api/fleet/trips/vehicle/{vehicleId}` - Get trips by vehicle
- `GET /api/fleet/trips/company/{companyId}/ongoing` - Get ongoing trips

### Geofencing
- `POST /api/fleet/geofences` - Create a geofence
- `GET /api/fleet/geofences/{id}` - Get geofence by ID
- `PUT /api/fleet/geofences/{id}` - Update geofence
- `DELETE /api/fleet/geofences/{id}` - Delete geofence
- `GET /api/fleet/geofences/company/{companyId}` - Get company geofences
- `GET /api/fleet/geofences/{id}/check` - Check if point is in geofence

## Events Published

The service publishes the following events to RabbitMQ:

1. **Vehicle Location Updated** (`vehicle.location.updated`)
   - Published when vehicle location is updated
   - Contains: vehicleId, companyId, latitude, longitude, batterySoc, timestamp

2. **Trip Completed** (`trip.completed`)
   - Published when a trip is completed
   - Contains: tripId, vehicleId, driverId, distance, energyConsumed, efficiencyScore

3. **Battery Low** (`battery.low`)
   - Published when battery SOC falls below threshold
   - Contains: vehicleId, companyId, batterySoc, severity

## Health Check

Check application health:
```
http://localhost:8082/actuator/health
```

## Monitoring

Prometheus metrics are available at:
```
http://localhost:8082/actuator/prometheus
```

## Development

### Project Structure
```
fleet-service/
├── src/
│   ├── main/
│   │   ├── java/com/evfleet/fleet/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── event/           # Event publishers
│   │   │   ├── config/          # Configuration classes
│   │   │   └── exception/       # Exception handlers
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/                    # Test cases
├── Dockerfile                   # Docker build file
├── pom.xml                      # Maven dependencies
└── README.md                    # This file
```

## License

Copyright (c) 2024 EV Fleet Management Platform
