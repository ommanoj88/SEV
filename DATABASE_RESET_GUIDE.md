# Database Reset Script

This script will reset and initialize the EV Fleet Management Platform databases with all required tables and seed data.

## What It Does

1. **Drops all existing databases** (evfleet_auth, evfleet_fleet, evfleet_charging, etc.)
2. **Creates fresh databases** for all microservices
3. **Runs Flyway migrations** to create database tables
4. **Seeds initial data** (default roles: SUPER_ADMIN, ADMIN, FLEET_MANAGER, DRIVER, VIEWER)

## Prerequisites

- PostgreSQL server running (default: localhost:5432)
- Python 3.6+ installed
- `psycopg2` Python package installed
- Maven installed (for running migrations)
- Backend services built (or will be built by the script)

## Installation

Install required Python dependencies:

```bash
pip install psycopg2-binary
```

## Usage

### Basic Usage (Default PostgreSQL settings)

```bash
python reset_database.py
```

This assumes:
- PostgreSQL host: `localhost`
- PostgreSQL port: `5432`
- PostgreSQL user: `postgres`
- PostgreSQL password: `postgres`

### Custom PostgreSQL Configuration

Set environment variables before running:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=your_password

python reset_database.py
```

## What Gets Created

### Tables in evfleet_auth Database

1. **roles** - User roles for RBAC
   - id (primary key)
   - name (unique)
   - description
   - permissions (JSON)

2. **users** - User accounts
   - id (primary key)
   - firebase_uid (unique)
   - email (unique)
   - name
   - phone
   - company_id
   - company_name
   - active
   - email_verified
   - profile_image_url
   - created_at
   - updated_at
   - last_login

3. **user_roles** - Many-to-many relationship
   - user_id (foreign key to users)
   - role_id (foreign key to roles)

### Default Roles Seeded

- **SUPER_ADMIN** - Full system access
- **ADMIN** - Administrative access
- **FLEET_MANAGER** - Fleet management access (default for new users)
- **DRIVER** - Driver access
- **VIEWER** - Read-only access

## Important Notes

⚠️ **WARNING: This script will DELETE ALL DATA in your databases!**

- Always backup your data before running this script
- You will be prompted to confirm before proceeding
- All database connections will be terminated before dropping databases
- This is a destructive operation and cannot be undone

## After Running the Script

1. **Start your microservices** (auth-service, fleet-service, etc.)
2. **Register a new user** through the frontend application
3. **User will be synced** from Firebase to the backend database automatically
4. **Only users synced to backend** can access the application

## Authentication Flow

The application now enforces backend database synchronization:

1. User signs up/logs in via Firebase (frontend)
2. Frontend attempts to sync user to backend database
3. If backend sync succeeds → User can access the application
4. If backend sync fails → User is logged out with error message

**Backend database sync is now REQUIRED for authentication.**

## Troubleshooting

### Script fails to connect to PostgreSQL

- Ensure PostgreSQL is running: `pg_isready -h localhost -p 5432`
- Check your credentials (user/password)
- Verify PostgreSQL is accepting connections

### Maven build fails

- Ensure Maven is installed: `mvn --version`
- Check that you're in the correct directory
- Try building manually: `cd backend/auth-service && mvn clean install`

### Flyway migration fails

- Check that migration files exist in `backend/auth-service/src/main/resources/db/migration/`
- Verify database connection settings
- Check migration file syntax (SQL)

### Tables not created

- Run the verification step manually:
  ```bash
  psql -h localhost -U postgres -d evfleet_auth -c "\dt"
  ```
- Check Flyway migration history:
  ```bash
  psql -h localhost -U postgres -d evfleet_auth -c "SELECT * FROM flyway_schema_history"
  ```

## Manual Alternative

If the script doesn't work, you can manually reset the database:

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres

# Drop and create database
DROP DATABASE IF EXISTS evfleet_auth;
CREATE DATABASE evfleet_auth;

# Exit psql
\q

# Run migrations manually
cd backend/auth-service
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/evfleet_auth \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres
```

## See Also

- [Authentication Implementation Guide](backend/auth-service/FIREBASE-SETUP.md)
- [Docker Setup Guide](DOCKER_BUILD_GUIDE.md)
- [Running the Application](RUN_APP_GUIDE.md)
