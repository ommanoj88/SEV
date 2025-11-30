# SEV Fleet Management - Operations Runbook

**Version:** 2.0.0  
**Last Updated:** November 2025  
**Audience:** DevOps, SRE, On-Call Engineers

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Deployment Procedures](#deployment-procedures)
3. [Rollback Procedures](#rollback-procedures)
4. [Monitoring & Alerting](#monitoring--alerting)
5. [Incident Response](#incident-response)
6. [Backup & Recovery](#backup--recovery)
7. [Troubleshooting Guide](#troubleshooting-guide)
8. [Maintenance Windows](#maintenance-windows)
9. [Contact Information](#contact-information)

---

## System Overview

### Architecture Components

```
┌─────────────────────────────────────────────────────────────┐
│                      Load Balancer (Nginx)                  │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Frontend   │    │   Backend    │    │   Backend    │
│   (React)    │    │  (Spring)    │    │  (Replica)   │
│  Port: 3000  │    │  Port: 8080  │    │  Port: 8081  │
└──────────────┘    └──────────────┘    └──────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │    │    Redis     │    │  Firebase    │
│   (Primary)  │    │   (Cache)    │    │    (Auth)    │
│  Port: 5432  │    │  Port: 6379  │    │   (Cloud)    │
└──────────────┘    └──────────────┘    └──────────────┘
```

### Service Endpoints

| Service | Production URL | Health Check |
|---------|---------------|--------------|
| Frontend | https://app.sevfleet.com | `/` |
| Backend API | https://api.sevfleet.com | `/actuator/health` |
| Admin Portal | https://admin.sevfleet.com | `/health` |

### Key Metrics

| Metric | Normal Range | Alert Threshold |
|--------|--------------|-----------------|
| Response Time (p99) | < 500ms | > 1000ms |
| Error Rate | < 0.1% | > 1% |
| CPU Usage | < 70% | > 85% |
| Memory Usage | < 80% | > 90% |
| Database Connections | < 50 | > 80 |

---

## Deployment Procedures

### Pre-Deployment Checklist

- [ ] Backup current database
- [ ] Notify stakeholders (#ops-notifications)
- [ ] Verify CI/CD pipeline is green
- [ ] Review change log
- [ ] Confirm rollback plan
- [ ] Check maintenance window availability

### Standard Deployment

```bash
# 1. Pull latest changes
git checkout main
git pull origin main

# 2. Build Docker images
docker build -t sevfleet/backend:v2.1.0 ./backend/evfleet-monolith
docker build -t sevfleet/frontend:v2.1.0 ./frontend

# 3. Push to registry
docker push sevfleet/backend:v2.1.0
docker push sevfleet/frontend:v2.1.0

# 4. Deploy with zero downtime
kubectl set image deployment/backend backend=sevfleet/backend:v2.1.0
kubectl set image deployment/frontend frontend=sevfleet/frontend:v2.1.0

# 5. Verify deployment
kubectl rollout status deployment/backend
kubectl rollout status deployment/frontend

# 6. Health check
curl -f https://api.sevfleet.com/actuator/health
```

### Blue-Green Deployment

```bash
# 1. Deploy to green environment
kubectl apply -f kubernetes/green/

# 2. Wait for green to be healthy
kubectl wait --for=condition=available deployment/backend-green

# 3. Switch traffic to green
kubectl apply -f kubernetes/ingress-green.yaml

# 4. Verify green is serving traffic
curl -H "Host: api.sevfleet.com" http://green.internal

# 5. If successful, tear down blue
kubectl delete -f kubernetes/blue/
```

### Database Migration

```bash
# 1. Backup before migration
pg_dump -U postgres evfleet > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Run Flyway migrations
cd backend/evfleet-monolith
./mvnw flyway:migrate

# 3. Verify migration
./mvnw flyway:info

# 4. Test critical queries
psql -U postgres evfleet -c "SELECT count(*) FROM vehicles;"
```

---

## Rollback Procedures

### Immediate Rollback (< 15 minutes since deploy)

```bash
# Kubernetes rollback
kubectl rollout undo deployment/backend
kubectl rollout undo deployment/frontend

# Verify rollback
kubectl rollout status deployment/backend
```

### Database Rollback

```bash
# 1. Stop application
kubectl scale deployment/backend --replicas=0

# 2. Restore from backup
psql -U postgres evfleet < backup_20240115_100000.sql

# 3. Deploy previous version
kubectl set image deployment/backend backend=sevfleet/backend:v2.0.0

# 4. Verify
kubectl rollout status deployment/backend
```

### Rollback Decision Matrix

| Issue | Action | Timeframe |
|-------|--------|-----------|
| High error rate (>5%) | Immediate rollback | < 5 min |
| Performance degradation | Monitor 15 min, then rollback | 15-20 min |
| Non-critical bug | Forward fix | Next release |
| Data corruption | Immediate rollback + DB restore | < 30 min |

---

## Monitoring & Alerting

### Monitoring Stack

- **Metrics:** Prometheus + Grafana
- **Logs:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing:** Jaeger
- **Uptime:** Pingdom / UptimeRobot

### Dashboard URLs

| Dashboard | URL | Purpose |
|-----------|-----|---------|
| Main Overview | grafana.internal/d/main | System health |
| API Metrics | grafana.internal/d/api | Request metrics |
| Database | grafana.internal/d/postgres | DB performance |
| Infrastructure | grafana.internal/d/infra | CPU, memory, disk |

### Alert Runbook

#### 🔴 CRITICAL: API Error Rate > 5%

**Impact:** Users unable to access system
**Response Time:** Immediate

```bash
# 1. Check recent deployments
kubectl rollout history deployment/backend

# 2. Check application logs
kubectl logs -l app=backend --tail=100

# 3. If deployment-related, rollback
kubectl rollout undo deployment/backend

# 4. Check database connectivity
kubectl exec -it backend-pod -- curl postgres:5432
```

#### 🟠 WARNING: Response Time > 1s

**Impact:** Degraded user experience
**Response Time:** 15 minutes

```bash
# 1. Check current load
kubectl top pods

# 2. Check database slow queries
psql -U postgres evfleet -c "SELECT * FROM pg_stat_activity WHERE state = 'active';"

# 3. Scale if needed
kubectl scale deployment/backend --replicas=3

# 4. Check Redis cache
redis-cli info stats
```

#### 🟡 INFO: Disk Usage > 80%

**Impact:** Potential service disruption
**Response Time:** 4 hours

```bash
# 1. Check disk usage
df -h

# 2. Clean up old logs
find /var/log -name "*.log" -mtime +7 -delete

# 3. Clean up Docker
docker system prune -af

# 4. Remove old backups
find /backups -mtime +30 -delete
```

---

## Incident Response

### Severity Levels

| Level | Description | Response Time | Example |
|-------|-------------|---------------|---------|
| SEV-1 | Complete outage | 15 min | API down |
| SEV-2 | Major feature broken | 30 min | Login failing |
| SEV-3 | Minor feature broken | 4 hours | Report export slow |
| SEV-4 | Cosmetic issue | 24 hours | UI alignment |

### Incident Response Process

```
1. DETECT
   └─► Automated alert or user report

2. ACKNOWLEDGE
   └─► Assign incident commander
   └─► Create incident channel (#incident-YYYYMMDD)

3. ASSESS
   └─► Determine severity
   └─► Identify affected systems
   └─► Communicate status

4. MITIGATE
   └─► Apply fix or workaround
   └─► Rollback if necessary

5. RESOLVE
   └─► Verify fix
   └─► Update status page
   └─► Notify stakeholders

6. POSTMORTEM
   └─► Root cause analysis (within 48h)
   └─► Document learnings
   └─► Create follow-up tickets
```

### Communication Templates

**Initial Response:**
```
🚨 INCIDENT: [Brief description]
Status: Investigating
Severity: SEV-[1/2/3]
Impact: [Number of users affected]
Next Update: [Time]
```

**Resolution:**
```
✅ RESOLVED: [Brief description]
Duration: [X hours Y minutes]
Root Cause: [One sentence]
Fix Applied: [What was done]
Postmortem: [Link when available]
```

---

## Backup & Recovery

### Backup Schedule

| Type | Frequency | Retention | Location |
|------|-----------|-----------|----------|
| Full DB | Daily 2:00 AM | 30 days | S3 + local |
| Incremental DB | Hourly | 7 days | S3 |
| File uploads | Daily | 90 days | S3 |
| Config | On change | Forever | Git |

### Manual Backup

```bash
# Full database backup
pg_dump -U postgres -Fc evfleet > evfleet_$(date +%Y%m%d_%H%M%S).dump

# Upload to S3
aws s3 cp evfleet_*.dump s3://sev-backups/db/

# Verify backup
pg_restore --list evfleet_*.dump | head -20
```

### Recovery Procedures

#### Point-in-Time Recovery

```bash
# 1. Stop application
kubectl scale deployment/backend --replicas=0

# 2. Restore to specific point
pg_restore -U postgres -d evfleet_recovery \
  --target-time="2024-01-15 10:00:00" \
  evfleet_20240115.dump

# 3. Verify data
psql -U postgres evfleet_recovery -c "SELECT MAX(created_at) FROM vehicles;"

# 4. Swap databases
ALTER DATABASE evfleet RENAME TO evfleet_old;
ALTER DATABASE evfleet_recovery RENAME TO evfleet;

# 5. Restart application
kubectl scale deployment/backend --replicas=2
```

#### Recovery Time Objectives

| Scenario | RTO | RPO |
|----------|-----|-----|
| Database failure | 1 hour | 1 hour (hourly backups) |
| Application failure | 15 min | 0 (stateless) |
| Complete datacenter failure | 4 hours | 1 hour |

---

## Troubleshooting Guide

### Common Issues

#### Issue: High Memory Usage

```bash
# 1. Check top memory consumers
kubectl top pods --sort-by=memory

# 2. Get heap dump (if Java OOM suspected)
kubectl exec backend-pod -- jmap -dump:format=b,file=/tmp/heap.hprof $(pgrep java)

# 3. Analyze with Eclipse MAT or jhat

# 4. Temporary fix: restart pod
kubectl delete pod backend-pod-xxx
```

#### Issue: Database Connection Pool Exhausted

```bash
# 1. Check active connections
psql -U postgres -c "SELECT count(*) FROM pg_stat_activity WHERE datname='evfleet';"

# 2. Find long-running queries
psql -U postgres -c "SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
FROM pg_stat_activity 
WHERE state != 'idle' AND datname='evfleet' 
ORDER BY duration DESC;"

# 3. Kill stuck queries
psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE duration > interval '5 minutes';"

# 4. Increase pool size (temporary)
kubectl set env deployment/backend SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=30
```

#### Issue: Redis Cache Miss Rate High

```bash
# 1. Check Redis stats
redis-cli info stats | grep -E "(keyspace|hits|misses)"

# 2. Check memory usage
redis-cli info memory

# 3. Flush stale keys
redis-cli KEYS "*:expired:*" | xargs redis-cli DEL

# 4. Increase memory if needed
redis-cli CONFIG SET maxmemory 2gb
```

### Log Analysis

```bash
# Search for errors in last hour
kubectl logs -l app=backend --since=1h | grep -i error

# Count errors by type
kubectl logs -l app=backend --since=1h | grep -i error | sort | uniq -c

# Follow logs in real-time
kubectl logs -f -l app=backend

# Export logs for analysis
kubectl logs -l app=backend --since=24h > logs_$(date +%Y%m%d).txt
```

---

## Maintenance Windows

### Scheduled Maintenance

| Window | Time (IST) | Duration | Type |
|--------|------------|----------|------|
| Weekly | Sunday 2-4 AM | 2 hours | Patches |
| Monthly | 1st Sunday | 4 hours | Major updates |
| Quarterly | As announced | 8 hours | Infrastructure |

### Maintenance Procedure

```bash
# 1. Enable maintenance mode
kubectl apply -f kubernetes/maintenance-page.yaml

# 2. Verify maintenance page
curl https://app.sevfleet.com

# 3. Perform maintenance
# ... maintenance tasks ...

# 4. Verify system health
./scripts/health-check.sh

# 5. Disable maintenance mode
kubectl delete -f kubernetes/maintenance-page.yaml

# 6. Verify normal operation
curl -f https://api.sevfleet.com/actuator/health
```

---

## Contact Information

### On-Call Rotation

| Week | Primary | Secondary |
|------|---------|-----------|
| Current | Check PagerDuty | Check PagerDuty |

### Escalation Path

```
Level 1: On-Call Engineer (15 min response)
    ↓
Level 2: Team Lead (30 min response)
    ↓
Level 3: Engineering Manager (1 hour response)
    ↓
Level 4: CTO (As needed)
```

### External Contacts

| Service | Contact | SLA |
|---------|---------|-----|
| AWS Support | support.aws.amazon.com | 1 hour (Business) |
| Firebase | firebase.google.com/support | 24 hours |
| Postgres RDS | AWS Support | 1 hour |

### Useful Links

- **Status Page:** https://status.sevfleet.com
- **Documentation:** https://docs.sevfleet.com
- **Runbook (this doc):** https://docs.sevfleet.com/runbook
- **Postmortems:** https://wiki.sevfleet.com/postmortems
- **PagerDuty:** https://sevfleet.pagerduty.com

---

*PR #48: Operations Runbook*
