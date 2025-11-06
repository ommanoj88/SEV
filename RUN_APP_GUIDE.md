# EV Fleet Management Platform - Application Launcher Guide

## Overview

The `run_app.py` script is a comprehensive launcher that manages the entire application stack, ensuring that:
- Latest code is always used (no Docker cache issues)
- All required ports are available
- Services start in the correct order
- Health status is monitored

## Prerequisites

- Python 3.6 or higher
- Docker and Docker Compose installed
- Sufficient permissions to kill processes on ports

## Quick Start

### Start the Application

```bash
python run_app.py start
```

This will:
1. Kill any processes using required ports (3000, 8080-8088, 5432, 6379, 5672, 15672)
2. Build all Docker images with `--no-cache` to ensure latest code
3. Start all services in the correct order:
   - Infrastructure (PostgreSQL, Redis, RabbitMQ)
   - Service Discovery (Eureka)
   - API Gateway
   - Business Microservices (Auth, Fleet, Charging, etc.)
   - Frontend Application

### Stop the Application

```bash
python run_app.py stop
```

This will:
1. Stop all Docker containers
2. Kill processes on all required ports

### Restart the Application

```bash
python run_app.py restart
```

This performs a full stop and start cycle.

### Check Application Status

```bash
python run_app.py status
```

Shows the status of all Docker containers and service endpoints.

### Full Clean and Rebuild

```bash
python run_app.py clean
```

This will:
1. Stop all containers
2. Remove Docker volumes
3. Remove dangling images
4. Rebuild all images from scratch

## Advanced Usage

### Skip Docker Build (Faster Start)

If you haven't made code changes and want to start faster:

```bash
python run_app.py start --skip-build
```

**Warning:** This may use cached code, not the latest changes.

## Service Endpoints

After starting the application, access:

- **Frontend Application**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672
  - Username: `evfleet`
  - Password: `evfleet123`

### Individual Microservices

- **Auth Service**: http://localhost:8081/actuator/health
- **Fleet Service**: http://localhost:8082/actuator/health
- **Charging Service**: http://localhost:8083/actuator/health
- **Maintenance Service**: http://localhost:8084/actuator/health
- **Driver Service**: http://localhost:8085/actuator/health
- **Analytics Service**: http://localhost:8086/actuator/health
- **Notification Service**: http://localhost:8087/actuator/health
- **Billing Service**: http://localhost:8088/actuator/health

## Troubleshooting

### Port Already in Use

The script automatically kills processes on required ports. If you see errors:

1. Run `python run_app.py stop` first
2. Manually check for processes: `lsof -i :3000` (replace 3000 with the port)
3. Kill the process: `kill -9 <PID>`

### Docker Build Failures

If builds fail:

1. Ensure Docker daemon is running
2. Check you have enough disk space
3. Try cleaning first: `python run_app.py clean`
4. Check individual service logs in `docker/` directory

### Services Not Starting

If services fail to start:

1. Check Docker container logs: `docker-compose logs <service-name>`
2. Ensure all prerequisites are met (Java 17+, Node 18+)
3. Verify Firebase credentials are in `impresourcesfortesting/firebase-service-account-test.json`

### Old Code Being Used

The script uses `--no-cache` flag by default to ensure latest code is used. If you still see old code:

1. Run `python run_app.py clean` for a full rebuild
2. Check your local changes are committed/saved
3. Verify Dockerfiles are copying the correct source files

## Port Usage Reference

| Port(s)       | Service              | Purpose                    |
|---------------|---------------------|----------------------------|
| 3000          | Frontend            | React Application          |
| 8080          | API Gateway         | Single Entry Point         |
| 8761          | Eureka Server       | Service Discovery          |
| 8081          | Auth Service        | Authentication             |
| 8082          | Fleet Service       | Fleet Management           |
| 8083          | Charging Service    | Charging Management        |
| 8084          | Maintenance Service | Maintenance Operations     |
| 8085          | Driver Service      | Driver Management          |
| 8086          | Analytics Service   | Analytics & Reporting      |
| 8087          | Notification Service| Notifications & Alerts     |
| 8088          | Billing Service     | Billing & Invoicing        |
| 5432          | PostgreSQL          | Database                   |
| 6379          | Redis               | Cache & Session            |
| 5672          | RabbitMQ            | Message Queue (AMQP)       |
| 15672         | RabbitMQ            | Management UI              |

## Best Practices

1. **Always use `run_app.py start`** instead of manual `docker-compose up` to ensure latest code
2. **Run `clean` periodically** to remove old images and free disk space
3. **Check status** after starting to verify all services are healthy
4. **Use `stop`** before system shutdown to gracefully stop services
5. **Keep the script updated** when adding new services or ports

## Development Workflow

### Making Code Changes

1. Make your changes in the source code
2. Run `python run_app.py restart` to see changes
3. The script will rebuild Docker images with your latest code

### Testing Changes Quickly

For frontend-only changes, you can run the frontend locally:

```bash
cd frontend
npm start
```

For backend changes, restart just that service:

```bash
cd docker
docker-compose restart <service-name>
```

But remember: The `run_app.py` script ensures consistency and is recommended for full testing.

## Support

For issues or questions:
1. Check the main [README.md](README.md)
2. Review [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md)
3. Check Docker logs: `docker-compose logs -f <service-name>`
4. Open an issue on GitHub

---

**Version**: 1.0.0  
**Last Updated**: November 6, 2025  
**Maintainer**: EV Fleet Management Team
