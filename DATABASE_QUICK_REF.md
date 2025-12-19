# Database Scripts Quick Reference

## Quick Start

### First Time Setup (Development)
```bash
python db_connect.py && python run_app_fixed.py start
```

### Daily Development
```bash
python run_app_fixed.py start
```

### Fresh Start (Reset Everything)
```bash
python reset_database.py && python run_app_fixed.py start
```

## All Scripts

| Script | Purpose | Safe? | Creates DBs | Creates Tables | Seeds Users |
|--------|---------|-------|-------------|----------------|-------------|
| `db_connect.py` | Full setup with users | ✅ Yes | ✅ | ✅ | ✅ 20 users |
| `init_database.py` | Basic DB creation | ✅ Yes | ✅ | ❌ | ❌ |
| `reset_database.py` | Complete reset | ⚠️ **NO** | ✅ | ✅ | ❌ |
| `run_app_fixed.py` | App launcher | ✅ Yes | Via init_database.py | Via Flyway | ❌ |

## Common Commands

### Setup Commands
```bash
# Full setup with test users (development)
python db_connect.py

# Create databases only (production)
python init_database.py

# Reset everything (development only)
python reset_database.py
```

### Database Management
```bash
# Connect to auth database
psql -h localhost -U postgres -d evfleet_auth

# List all databases
psql -h localhost -U postgres -c "\l"

# View users
psql -h localhost -U postgres -d evfleet_auth -c "SELECT email, name FROM users;"

# View roles
psql -h localhost -U postgres -d evfleet_auth -c "SELECT * FROM roles;"
```

### Application Commands
```bash
# Start everything
python run_app_fixed.py start

# Stop everything
python run_app_fixed.py stop

# Check status
python run_app_fixed.py status

# Restart
python run_app_fixed.py restart
```

## Environment Variables
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=your_secure_password
```

## Seeded Users (20)

| Role | Count | Example Email |
|------|-------|---------------|
| Super Admin | 1 | admin@evfleet.com |
| Admin | 1 | admin@evfleet.com |
| Fleet Manager | 3 | manager1@evfleet.com |
| Driver | 7 | driver1@evfleet.com |
| Maintenance Manager | 2 | maintenance1@evfleet.com |
| Analyst | 2 | analyst1@evfleet.com |
| Support | 2 | support1@evfleet.com |
| User | 3 | user1@evfleet.com |

**Note**: All users have placeholder Firebase UIDs. Real authentication happens through Firebase.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "PostgreSQL not running" | Start PostgreSQL service |
| "Database already exists" | Normal - script is safe to re-run |
| "Permission denied" | Check DB_USER has CREATEDB privilege |
| "Connection refused" | Verify DB_HOST and DB_PORT |
| "Table does not exist" | Run `python run_app_fixed.py start` (Flyway creates tables) |

## Guides

- 📖 [DB_SETUP_README.md](DB_SETUP_README.md) - Detailed setup guide
- 📖 [DATABASE_SCRIPTS_GUIDE.md](DATABASE_SCRIPTS_GUIDE.md) - All scripts explained
- 📖 [CUSTOMIZE_USERS_GUIDE.md](CUSTOMIZE_USERS_GUIDE.md) - Customize initial users
- 📖 [DATABASE_RESET_GUIDE.md](DATABASE_RESET_GUIDE.md) - Reset database guide

## URLs After Setup

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health
