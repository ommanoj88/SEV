# 🚀 EVFleet Monolith Deployment Guide

## Prerequisites

### Required Software
- **Java**: 17 or higher
- **Maven**: 3.9 or higher
- **Docker**: 20.10 or higher
- **Docker Compose**: 2.0 or higher
- **PostgreSQL**: 15 or higher (if not using Docker)
- **Redis**: 7 or higher (if not using Docker)

### System Requirements
**Minimum**:
- 2 GB RAM
- 2 CPU cores
- 10 GB disk space

**Recommended**:
- 4 GB RAM
- 4 CPU cores
- 20 GB disk space

---

## 🐳 Option 1: Docker Deployment (Recommended)

### Step 1: Clone Repository
```bash
cd backend/evfleet-monolith
```

### Step 2: Configure Environment
Create `.env` file:
```bash
DB_PASSWORD=Shobharain11@
REDIS_HOST=redis
REDIS_PORT=6379
FIREBASE_CONFIG_PATH=/app/config/firebase-service-account.json
```

### Step 3: Add Firebase Config
Place your `firebase-service-account.json` in:
```
backend/evfleet-monolith/src/main/resources/firebase-service-account.json
```

### Step 4: Build & Deploy
```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f evfleet-monolith

# Check status
docker-compose ps
```

### Step 5: Verify Deployment
```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html

# Auth health
curl http://localhost:8080/api/v1/auth/health
```

### Step 6: Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (careful - deletes data!)
docker-compose down -v
```

---

## 💻 Option 2: Local Development

### Step 1: Start Dependencies
```bash
# Start PostgreSQL
docker run -d --name evfleet-postgres \
  -e POSTGRES_PASSWORD=Shobharain11@ \
  -p 5432:5432 \
  postgres:15

# Start Redis
docker run -d --name evfleet-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Step 2: Create Databases
```bash
# Connect to PostgreSQL
docker exec -it evfleet-postgres psql -U postgres

# Create databases
CREATE DATABASE evfleet_auth;
CREATE DATABASE evfleet_fleet;
CREATE DATABASE evfleet_charging;
CREATE DATABASE evfleet_maintenance;
CREATE DATABASE evfleet_driver;
CREATE DATABASE evfleet_analytics;
CREATE DATABASE evfleet_notification;
CREATE DATABASE evfleet_billing;

\q
```

### Step 3: Configure Application
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    auth:
      jdbc-url: jdbc:postgresql://localhost:5432/evfleet_auth
      username: postgres
      password: ${DB_PASSWORD:Shobharain11@}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

firebase:
  config:
    path: ${FIREBASE_CONFIG_PATH:firebase-service-account.json}
```

### Step 4: Build & Run
```bash
# Clean build
mvn clean package -DskipTests

# Run application
mvn spring-boot:run

# Or run JAR
java -jar target/evfleet-monolith-1.0.0.jar
```

---

## 🌐 Production Deployment

### Option A: Hetzner/DigitalOcean VPS

#### 1. Provision Server
- Choose: 4GB RAM, 2 vCPUs, 80GB SSD
- OS: Ubuntu 22.04 LTS
- Cost: ~₹2,400/month

#### 2. Install Dependencies
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Nginx
sudo apt install nginx -y
```

#### 3. Setup SSL with Let's Encrypt
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d api.yourdomain.com
```

#### 4. Configure Nginx Reverse Proxy
```nginx
# /etc/nginx/sites-available/evfleet
server {
    listen 80;
    server_name api.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/evfleet /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### 5. Deploy Application
```bash
# Clone repository
git clone <your-repo> evfleet
cd evfleet/backend/evfleet-monolith

# Configure .env
nano .env

# Deploy
docker-compose up -d

# Setup auto-restart
sudo systemctl enable docker
```

### Option B: AWS/GCP/Azure

#### AWS Elastic Beanstalk
```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init -p docker evfleet-monolith

# Deploy
eb create evfleet-prod
eb deploy
```

#### Google Cloud Run
```bash
# Build image
docker build -t gcr.io/PROJECT_ID/evfleet:latest .

# Push to registry
docker push gcr.io/PROJECT_ID/evfleet:latest

# Deploy
gcloud run deploy evfleet \
  --image gcr.io/PROJECT_ID/evfleet:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

---

## 📊 Monitoring & Maintenance

### Health Checks
```bash
# Application health
curl https://api.yourdomain.com/actuator/health

# Module health checks
curl https://api.yourdomain.com/api/v1/auth/health
curl https://api.yourdomain.com/api/v1/fleet/health
curl https://api.yourdomain.com/api/v1/charging/health
```

### Logs
```bash
# Docker logs
docker logs -f evfleet-monolith

# Application logs (if running locally)
tail -f logs/application.log
```

### Database Backup
```bash
# Backup all databases
docker exec evfleet-postgres pg_dumpall -U postgres > backup_$(date +%Y%m%d).sql

# Restore
docker exec -i evfleet-postgres psql -U postgres < backup_20250115.sql
```

### Performance Monitoring
```bash
# Check container stats
docker stats evfleet-monolith

# Memory usage
docker exec evfleet-monolith java -XX:+PrintFlagsFinal -version | grep MaxHeapSize
```

---

## 🔧 Troubleshooting

### Issue: Application won't start
**Check**:
```bash
docker logs evfleet-monolith
# Look for database connection errors
```

**Fix**:
- Ensure PostgreSQL is running
- Verify database passwords
- Check firewall rules

### Issue: 503 Service Unavailable
**Check**:
```bash
curl http://localhost:8080/actuator/health
```

**Fix**:
- Restart application: `docker-compose restart evfleet-monolith`
- Check database connectivity
- Increase memory: Edit docker-compose.yml, add `mem_limit: 4g`

### Issue: Out of Memory
**Fix**:
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xms512m -Xmx2048m"
```

---

## 🔐 Security Checklist

- [ ] Change default database password
- [ ] Restrict CORS origins in production
- [ ] Enable HTTPS/SSL
- [ ] Setup firewall rules (allow only 80, 443, 22)
- [ ] Disable Swagger UI in production
- [ ] Enable rate limiting
- [ ] Setup backup automation
- [ ] Configure log rotation
- [ ] Enable database encryption
- [ ] Setup monitoring alerts

---

## 📈 Scaling

### Vertical Scaling (Easier)
- Increase RAM/CPU on current server
- Upgrade to 8GB RAM, 4 vCPUs: ~₹4,800/month

### Horizontal Scaling (Advanced)
When you exceed 10,000 vehicles or 100,000 trips/day:
1. Setup load balancer
2. Deploy multiple instances
3. Use managed PostgreSQL (RDS/Cloud SQL)
4. Extract high-load modules (Fleet, Charging) to separate services

---

## 💰 Cost Estimates

| Deployment | Monthly Cost | Notes |
|------------|-------------|-------|
| Hetzner VPS (4GB) | ₹2,400 | Recommended for MVP |
| DigitalOcean (4GB) | ₹3,200 | Good alternative |
| AWS t3.medium | ₹5,600 | Higher cost, better support |
| GCP e2-medium | ₹4,800 | Good for Google ecosystem |

---

**Deployment Checklist**: ✅ Dependencies installed ✅ Databases created ✅ Environment configured ✅ Firebase setup ✅ Application deployed ✅ Health checks passing ✅ SSL configured ✅ Monitoring setup

---

**Last Updated**: 2025-11-15
**Maintainer**: SEV Platform Team
