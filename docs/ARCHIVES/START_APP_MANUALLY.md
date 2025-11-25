# Manual Application Startup Guide

## ✅ Status
- Databases: **RESET AND READY** ✅
- PostgreSQL: **RUNNING** ✅
- Redis: Not running (optional)
- Backend: **NEEDS MANUAL START**
- Frontend: **NEEDS MANUAL START**

---

## 🚀 Quick Start (2 Terminals)

### Terminal 1: Start Backend

```bash
cd backend/evfleet-monolith
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

**Wait for**: "Started EvFleetMonolithApplication in X seconds"
**URL**: http://localhost:8080

### Terminal 2: Start Frontend

```bash
cd frontend
npm install
npm run dev
```

**Wait for**: "Local: http://localhost:3000"
**URL**: http://localhost:3000

---

## 🔍 Alternative: Use IntelliJ IDEA

### Option 1: Run from IDE

1. Open IntelliJ IDEA
2. Open project: `backend/evfleet-monolith`
3. Find: `EvFleetMonolithApplication.java`
4. Right-click → Run 'EvFleetMonolithApplication'

### Option 2: Maven from IDE

1. Open Maven tool window (right sidebar)
2. Navigate to: `evfleet-monolith` → `Plugins` → `spring-boot`
3. Double-click: `spring-boot:run`

---

## 📊 Service URLs

Once started:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (if enabled): http://localhost:8080/h2-console

---

## 🔧 Troubleshooting

### Backend Won't Start

**Check Java Version**:
```bash
java -version
```
Should be Java 17 or higher.

**Build First**:
```bash
cd backend/evfleet-monolith
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean install -DskipTests
```

**Check Port 8080**:
```bash
netstat -ano | findstr :8080
```
If occupied, kill the process or change port.

### Frontend Won't Start

**Check Node Version**:
```bash
node --version
npm --version
```

**Clean Install**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

**Check Port 3000**:
```bash
netstat -ano | findstr :3000
```

### Database Connection Issues

**Check PostgreSQL**:
```bash
psql -h localhost -U postgres -d evfleet_auth
```

**List Databases**:
```sql
\l
```

Should see all 8 databases:
- evfleet_auth
- evfleet_fleet
- evfleet_charging
- evfleet_maintenance
- evfleet_driver
- evfleet_analytics
- evfleet_notification
- evfleet_billing

---

## 🎯 Test Credentials

**Email**: testuser1@gmail.com
**Password**: Password@123

---

## 📝 Notes

1. **Databases are already reset** - Fresh and ready ✅
2. **Maven location**: IntelliJ IDEA bundled Maven
3. **No need to run** `python run_app_fixed.py` - manual start is easier
4. **Flyway migrations** will run automatically on first backend startup
5. **Initial roles** will be seeded automatically

---

## ⚡ Quick Test

Once both are running:

1. **Health Check**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **Frontend**:
   Open browser: http://localhost:3000

3. **Login**:
   - Email: testuser1@gmail.com
   - Password: Password@123

---

## 🛑 Stop Services

**Backend**: `Ctrl+C` in Terminal 1
**Frontend**: `Ctrl+C` in Terminal 2

Or kill ports:
```bash
# Backend (port 8080)
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Frontend (port 3000)
netstat -ano | findstr :3000
taskkill /F /PID <PID>
```

---

**Status**: READY TO START ✅

Just run the 2 commands above in 2 terminals and you're good to go!
