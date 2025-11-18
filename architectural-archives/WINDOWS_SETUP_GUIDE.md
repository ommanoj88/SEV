# Windows Setup Guide - EV Fleet Management Platform

## Quick Start for Windows Users

### Method 1: Using Batch File (Easiest)
```cmd
start_app.bat
```

### Method 2: Using PowerShell
```powershell
python run_app_fixed.py start
```

### Method 3: Using Command Prompt
```cmd
python run_app_fixed.py start
```

---

## Common Windows Issues and Fixes

### Issue 1: `/usr/bin/env` Error

**Error:**
```
/usr/bin/env : The term '/usr/bin/env' is not recognized...
```

**Solution:**
Don't run scripts with `/usr/bin/env`. Use `python` command instead:

```powershell
# ❌ DON'T DO THIS (Linux/Mac syntax)
/usr/bin/env python3 reset_database.py

# ✅ DO THIS (Windows syntax)
python reset_database.py
```

---

### Issue 2: PowerShell Execution Policy

**Error:**
```
... cannot be loaded because running scripts is disabled on this system.
```

**Solution:**
```powershell
# Check current policy
Get-ExecutionPolicy

# Allow scripts (run PowerShell as Administrator)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

### Issue 3: Docker Not Running

**Error:**
```
Cannot connect to the Docker daemon
```

**Solution:**
1. Open **Docker Desktop**
2. Wait for Docker to start (whale icon in system tray)
3. Run the application again

---

### Issue 4: Port Already in Use

**Error:**
```
Port 8080 is already in use
```

**Solution - PowerShell:**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual number)
taskkill /F /PID <PID>
```

**Solution - Command Prompt:**
```cmd
# Find and kill process on port 8080
FOR /F "tokens=5" %P IN ('netstat -ano ^| findstr :8080') DO taskkill /F /PID %P
```

---

## Database Management on Windows

### Safe Database Initialization (Preserves Data)
```powershell
python init_database.py
```

### Complete Database Reset (DELETES ALL DATA)
```powershell
python reset_database.py
```

### Connecting to PostgreSQL
```powershell
# Wait for Docker PostgreSQL to start
docker ps | findstr postgres

# Connect to PostgreSQL
psql -h localhost -U postgres -d postgres

# When prompted, enter password: Shobharain11@
```

---

## Prerequisites for Windows

### Required Software

1. **Docker Desktop for Windows**
   - Download: https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`
   - Verify: `docker-compose --version`

2. **Python 3.7+**
   - Download: https://www.python.org/downloads/
   - Verify: `python --version`
   - Make sure "Add Python to PATH" was checked during installation

3. **Git for Windows** (if not already installed)
   - Download: https://git-scm.com/download/win
   - Verify: `git --version`

### Optional Software

4. **PostgreSQL Client Tools** (for database management)
   - Download: https://www.postgresql.org/download/windows/
   - Select only "Command Line Tools" if you don't want full PostgreSQL server
   - Verify: `psql --version`

5. **Node.js** (for frontend development)
   - Download: https://nodejs.org/
   - Verify: `node --version`

6. **Java Development Kit 17+** (for backend development)
   - Download: https://adoptium.net/
   - Verify: `java -version`

---

## Step-by-Step Startup Process

### First Time Setup

1. **Open PowerShell or Command Prompt in the project directory:**
   ```powershell
   cd C:\Users\omman\Desktop\SEV
   ```

2. **Make sure Docker Desktop is running:**
   - Look for Docker whale icon in system tray
   - Should show "Docker Desktop is running"

3. **Start the application:**
   ```powershell
   python run_app_fixed.py start
   ```

4. **What happens automatically:**
   - ✅ Checks prerequisites (Docker, Python, etc.)
   - ✅ Starts PostgreSQL, Redis, RabbitMQ in Docker
   - ✅ Creates 8 databases (safe, preserves existing data)
   - ✅ Starts all 11 microservices
   - ✅ Waits for services to be healthy
   - ✅ Shows you all service URLs

5. **Access the application:**
   - Frontend: http://localhost:3000
   - Eureka Dashboard: http://localhost:8761
   - API Gateway: http://localhost:8080

### Subsequent Runs

```powershell
# Quick start (skips Docker rebuild)
python run_app_fixed.py start --skip-build

# Or just use the batch file
start_app.bat
```

---

## Stopping the Application

```powershell
python run_app_fixed.py stop
```

Or press `Ctrl+C` if running in foreground.

---

## Checking Application Status

```powershell
python run_app_fixed.py status
```

---

## Troubleshooting Commands

### Check Docker Containers
```powershell
docker ps
```

### Check Docker Logs
```powershell
docker logs ev-fleet-postgres
docker logs ev-fleet-redis
docker logs ev-fleet-rabbitmq
```

### Check Service Logs
```powershell
docker-compose logs fleet-service
docker-compose logs billing-service
```

### Restart Everything
```powershell
python run_app_fixed.py restart
```

### Clean Rebuild
```powershell
python run_app_fixed.py clean
```

### Remove All Docker Data (NUCLEAR OPTION)
```powershell
docker-compose down -v
docker system prune -a
```

---

## Database Verification on Windows

### Check Databases Exist
```powershell
# Connect to PostgreSQL
psql -h localhost -U postgres

# List databases
\l

# Should see all 8 databases:
# evfleet_auth, evfleet_fleet, evfleet_charging,
# evfleet_maintenance, evfleet_driver, evfleet_analytics,
# evfleet_notification, evfleet_billing
```

### Check Tables in a Database
```powershell
# Connect to fleet database
psql -h localhost -U postgres -d evfleet_fleet

# List tables
\dt

# You should see:
# vehicles, telemetry_data, fuel_consumption, feature_toggles
```

### Check Migration History
```sql
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

---

## Environment Variables (Optional)

If you want to override defaults, create a `.env` file:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=Shobharain11@

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=admin123

# Firebase (for Auth Service)
FIREBASE_API_KEY=your-api-key
FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
```

---

## Performance Tips for Windows

1. **Disable Antivirus Scanning for Project Folder**
   - Docker volumes can be slow if antivirus scans them
   - Add exclusion for `C:\Users\omman\Desktop\SEV`

2. **Use WSL2 Backend for Docker**
   - Docker Desktop → Settings → General
   - Enable "Use the WSL 2 based engine"

3. **Allocate More Resources to Docker**
   - Docker Desktop → Settings → Resources
   - Increase CPU: 4+ cores
   - Increase Memory: 8+ GB
   - Increase Disk: 60+ GB

4. **Enable Fast Startup**
   ```powershell
   # Skip Docker build after first time
   python run_app_fixed.py start --skip-build
   ```

---

## File Paths on Windows

### Using Backslashes (Windows Style)
```powershell
cd C:\Users\omman\Desktop\SEV
python run_app_fixed.py start
```

### Using Forward Slashes (Also Works)
```powershell
cd C:/Users/omman/Desktop/SEV
python run_app_fixed.py start
```

---

## Quick Command Reference

| Task | Command |
|------|---------|
| Start app (first time) | `python run_app_fixed.py start` |
| Start app (skip build) | `python run_app_fixed.py start --skip-build` |
| Stop app | `python run_app_fixed.py stop` |
| Check status | `python run_app_fixed.py status` |
| Restart app | `python run_app_fixed.py restart` |
| Clean rebuild | `python run_app_fixed.py clean` |
| Init databases (safe) | `python init_database.py` |
| Reset databases (danger) | `python reset_database.py` |
| Connect to PostgreSQL | `psql -h localhost -U postgres` |

---

## Support

If you encounter issues:
1. Check Docker Desktop is running
2. Check [DATABASE_GUIDE.md](DATABASE_GUIDE.md) for database issues
3. Check [RUN_APP_FIXED_README.md](RUN_APP_FIXED_README.md) for detailed documentation
4. Run with verbose output: `python run_app_fixed.py start`

---

**Ready to start? Run this:**

```powershell
python run_app_fixed.py start
```

Or double-click: **start_app.bat**

Good luck! 🚀
