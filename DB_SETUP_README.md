# Database Setup and Configuration Guide

## Overview

This guide explains how to set up and configure the PostgreSQL databases for the EV Fleet Management Platform using the `db_connect.py` script.

## Purpose

The `db_connect.py` script provides an automated way to:

1. **Connect to PostgreSQL** - Establish a connection to your PostgreSQL server
2. **Create Databases** - Create all 8 required module databases
3. **Create Tables** - Set up the authentication tables (roles, users, user_roles)
4. **Seed Initial Data** - Populate the database with 20 initial users

## Prerequisites

Before running the script, ensure you have:

- **PostgreSQL** installed and running on `localhost:5432`
- **Python 3.7+** installed
- **psycopg2-binary** Python package (automatically installed or run: `pip install psycopg2-binary`)

## Database Configuration

The script uses the following default configuration (can be overridden with environment variables):

```bash
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=Shobharain11@  # Development default - CHANGE for production
```

⚠️ **SECURITY WARNING**: The default password shown above is for development purposes only. 
**NEVER use this password in production environments.**

### Environment Variables

You can customize the database connection by setting these environment variables:

```bash
export DB_HOST=your_host
export DB_PORT=your_port
export DB_USER=your_username
export DB_PASSWORD=your_secure_password  # Use a strong password for production
```

**Best Practice**: Store these in a `.env` file (excluded from git) or use a secrets management system.

## Usage

### 1. Full Setup (Recommended for first-time setup)

Creates databases, tables, and seeds initial 20 users:

```bash
python db_connect.py
```

### 2. Create Databases and Tables Only

Skip user seeding:

```bash
python db_connect.py --skip-seed
```

### 3. Seed Users Only

Assumes databases and tables already exist:

```bash
python db_connect.py --seed-only
```

## What Gets Created

### Databases (8 module databases)

1. `evfleet_auth` - Authentication and user management
2. `evfleet_fleet` - Vehicle and fleet management
3. `evfleet_charging` - Charging stations and sessions
4. `evfleet_maintenance` - Maintenance records
5. `evfleet_driver` - Driver management
6. `evfleet_analytics` - Analytics and reporting
7. `evfleet_notification` - Notifications
8. `evfleet_billing` - Billing and subscriptions

### Tables (in evfleet_auth database)

1. **roles** - User roles (ADMIN, FLEET_MANAGER, DRIVER, etc.)
2. **users** - User accounts with Firebase integration
3. **user_roles** - Junction table for user-role assignments

### Initial Data

#### Default Roles (8 roles)

- `ROLE_ADMIN` - Administrator with full system access
- `ROLE_SUPER_ADMIN` - Super Administrator with all permissions
- `ROLE_FLEET_MANAGER` - Fleet Manager for managing vehicles and drivers
- `ROLE_DRIVER` - Driver role for drivers
- `ROLE_MAINTENANCE_MANAGER` - Maintenance Manager for vehicle maintenance
- `ROLE_ANALYST` - Analyst for data analysis and reporting
- `ROLE_SUPPORT` - Support team member for customer support
- `ROLE_USER` - Regular user with basic access

#### Initial Users (20 users)

The script seeds 20 initial users across different roles:

| Email | Role(s) | Company |
|-------|---------|---------|
| admin@evfleet.com | ADMIN, SUPER_ADMIN | EV Fleet HQ |
| manager1@evfleet.com | FLEET_MANAGER | Mumbai Fleet Operations |
| manager2@evfleet.com | FLEET_MANAGER | Mumbai Fleet Operations |
| driver1@evfleet.com | DRIVER | Mumbai Fleet Operations |
| driver2@evfleet.com | DRIVER | Mumbai Fleet Operations |
| driver3@evfleet.com | DRIVER | Delhi Fleet Operations |
| driver4@evfleet.com | DRIVER | Delhi Fleet Operations |
| driver5@evfleet.com | DRIVER | Bangalore Fleet Operations |
| maintenance1@evfleet.com | MAINTENANCE_MANAGER | Mumbai Fleet Operations |
| maintenance2@evfleet.com | MAINTENANCE_MANAGER | Delhi Fleet Operations |
| analyst1@evfleet.com | ANALYST | EV Fleet HQ |
| analyst2@evfleet.com | ANALYST | EV Fleet HQ |
| support1@evfleet.com | SUPPORT | EV Fleet HQ |
| support2@evfleet.com | SUPPORT | EV Fleet HQ |
| user1@evfleet.com | USER | Mumbai Fleet Operations |
| user2@evfleet.com | USER | Delhi Fleet Operations |
| user3@evfleet.com | USER | Bangalore Fleet Operations |
| driver6@evfleet.com | DRIVER | Bangalore Fleet Operations |
| driver7@evfleet.com | DRIVER | Mumbai Fleet Operations |
| manager3@evfleet.com | FLEET_MANAGER | Bangalore Fleet Operations |

**⚠️ IMPORTANT: Firebase Integration**

These seeded users have **placeholder Firebase UIDs** (e.g., `user_001_firebase_uid`). They are for initial database setup and testing only.

**For production use:**
1. Users must register via the application's Firebase authentication system
2. Their actual Firebase UID will be synced with the database automatically
3. OR manually update these placeholder UIDs with real Firebase UIDs from your Firebase console

**Authentication:** The application uses Firebase authentication. Users authenticate through Firebase, not with database passwords. The database only stores user profile information and links to their Firebase UID.

## Integration with Application

### Option 1: Run db_connect.py before starting the application

```bash
# Step 1: Setup databases and seed users
python db_connect.py

# Step 2: Start the application
python run_app_fixed.py start
```

### Option 2: Use with run_app_fixed.py

The `run_app_fixed.py` script already calls `init_database.py` which creates databases. You can:

1. Use `db_connect.py` for initial setup with user seeding
2. Use `run_app_fixed.py start` for normal application startup

## Verification

After running the script, you can verify the setup:

```bash
# Check databases exist
psql -h localhost -U postgres -c "\l"

# Check tables in auth database
psql -h localhost -U postgres -d evfleet_auth -c "\dt"

# Check roles
psql -h localhost -U postgres -d evfleet_auth -c "SELECT * FROM roles;"

# Check users
psql -h localhost -U postgres -d evfleet_auth -c "SELECT email, name FROM users;"
```

## Troubleshooting

### PostgreSQL Not Running

**Error:** `PostgreSQL is not available`

**Solution:** Start PostgreSQL:
- Windows: Start `postgresql-x64-15` service
- Linux: `sudo systemctl start postgresql`
- Mac: `brew services start postgresql`

### Connection Refused

**Error:** `Failed to connect to database`

**Solution:** 
- Check PostgreSQL is running on the correct port (5432)
- Verify the password is correct
- Check firewall settings

### Permission Denied

**Error:** `permission denied to create database`

**Solution:** Ensure the database user has CREATEDB privilege:
```sql
ALTER USER postgres CREATEDB;
```

### Users Already Exist

**Warning:** `Users already exist - SKIPPING initial seeding`

**Solution:** This is normal if you've run the script before. To re-seed:
1. Delete existing users from the database
2. Run `python db_connect.py --seed-only`

## Security Best Practices

1. **Database Credentials**: 
   - Never commit actual passwords to source control
   - Use environment variables for all sensitive configuration
   - Use strong, unique passwords for production databases
   - Rotate database credentials regularly

2. **Firebase Integration**: 
   - Ensure proper Firebase configuration for authentication
   - Update placeholder Firebase UIDs with actual UIDs when users register
   - Use Firebase security rules to protect user data

3. **Initial Users**:
   - Seeded users are for development and initial setup only
   - In production, disable or remove these test accounts
   - Require actual Firebase registration for all real users

4. **SSL/TLS**: Use SSL/TLS for PostgreSQL connections in production

5. **Network Security**: Restrict database access to trusted IP addresses only

6. **Audit Logging**: Enable PostgreSQL audit logging for production environments

## Next Steps

After setting up the database:

1. Start the application: `python run_app_fixed.py start`
2. Access the frontend at: http://localhost:3000
3. Access the API at: http://localhost:8080
4. View API docs at: http://localhost:8080/swagger-ui.html
5. Test login with seeded users (remember to set up Firebase authentication first)

## Additional Scripts

- `init_database.py` - Creates databases only (no seeding)
- `reset_database.py` - Drops and recreates all databases (⚠️ DANGER: Deletes all data!)
- `seed_default_roles.py` - Seeds only roles
- `run_app_fixed.py` - Main application launcher

## Support

For issues or questions:
1. Check the logs in the console output
2. Verify PostgreSQL is running and accessible
3. Check database configuration in `application.yml`
4. Review Firebase configuration
