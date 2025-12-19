# Database Scripts Integration Guide

This guide explains how the different database initialization scripts work together and when to use each one.

## Available Scripts

### 1. `db_connect.py` (NEW - Recommended for Initial Setup)

**Purpose**: Complete database setup with initial user seeding

**What it does**:
- Creates all 8 module databases
- Creates authentication tables (roles, users, user_roles)
- Seeds default roles (8 roles)
- **Seeds 20 initial users** across different roles and companies
- Verifies the setup

**When to use**:
- First-time setup when you need test users
- Development environments
- Demo/testing environments
- When you want a complete ready-to-use database

**Usage**:
```bash
# Full setup with users
python db_connect.py

# Only create databases and tables (no users)
python db_connect.py --skip-seed

# Only seed users (if DB already exists)
python db_connect.py --seed-only
```

### 2. `init_database.py` (Original)

**Purpose**: Basic database creation without user seeding

**What it does**:
- Creates all 8 module databases
- Does NOT create tables (relies on Flyway migrations)
- Does NOT seed any data

**When to use**:
- Production environments where you don't want test users
- When you want tables created by the application's Flyway migrations
- When you only need the databases created

**Usage**:
```bash
python init_database.py
```

### 3. `reset_database.py` (Destructive)

**Purpose**: Complete database reset (⚠️ DANGER - deletes all data)

**What it does**:
- Drops all existing databases
- Creates fresh databases
- Runs Flyway migrations from evfleet-monolith
- Seeds default roles

**When to use**:
- Development only
- When you want to start completely fresh
- **NEVER use in production**

**Usage**:
```bash
python reset_database.py
```

### 4. `seed_default_roles.py` (Legacy)

**Purpose**: Only seed roles into existing auth database

**What it does**:
- Seeds default roles into evfleet_auth database

**When to use**:
- Rarely needed (db_connect.py and reset_database.py both handle this)
- When you only need to add/update roles

**Usage**:
```bash
python seed_default_roles.py
```

## Integration with run_app_fixed.py

The `run_app_fixed.py` application launcher automatically calls `init_database.py` during startup. This ensures databases exist before the application starts.

### Workflow Options

#### Option A: First-Time Setup with Test Users (Recommended for Development)

```bash
# Step 1: Setup databases and seed users
python db_connect.py

# Step 2: Start the application
python run_app_fixed.py start
```

The application will detect that databases already exist and skip the initialization.

#### Option B: Production Setup (No Test Users)

```bash
# Start the application (will create databases automatically)
python run_app_fixed.py start
```

The `run_app_fixed.py` will:
1. Call `init_database.py` to create databases
2. Start the backend (Flyway will create tables)
3. Start the frontend

#### Option C: Development Reset

```bash
# Complete reset and fresh start
python reset_database.py

# Then start the application
python run_app_fixed.py start
```

## Comparison Table

| Feature | db_connect.py | init_database.py | reset_database.py |
|---------|--------------|------------------|-------------------|
| Create databases | ✅ Yes | ✅ Yes | ✅ Yes |
| Create tables | ✅ Yes | ❌ No (Flyway) | ✅ Yes (Flyway) |
| Seed roles | ✅ Yes | ❌ No | ✅ Yes |
| Seed users | ✅ Yes (20 users) | ❌ No | ❌ No |
| Drop existing DBs | ❌ No (safe) | ❌ No (safe) | ⚠️ YES (dangerous) |
| Safe to re-run | ✅ Yes | ✅ Yes | ⚠️ No (deletes data) |
| Production use | ⚠️ No (test users) | ✅ Yes | ❌ NEVER |
| Development use | ✅ Recommended | ✅ Yes | ✅ Yes (careful) |

## Best Practices

### For Development Environments

1. **First-time setup**:
   ```bash
   python db_connect.py
   python run_app_fixed.py start
   ```

2. **Daily work**:
   ```bash
   python run_app_fixed.py start
   ```

3. **When things are broken**:
   ```bash
   python reset_database.py
   python run_app_fixed.py start
   ```

### For Production Environments

1. **Initial deployment**:
   ```bash
   # Set environment variables for production credentials
   export DB_PASSWORD="your_secure_production_password"
   
   # Create databases only (no test users)
   python init_database.py
   
   # Start application (Flyway will create tables)
   python run_app_fixed.py start
   ```

2. **Never**:
   - Don't use `db_connect.py` (it seeds test users)
   - Don't use `reset_database.py` (it deletes all data)
   - Don't use default passwords

### For CI/CD Pipelines

```bash
# In GitHub Actions, Docker builds, etc.
# Create databases without users
python init_database.py

# Or use db_connect.py for integration tests
python db_connect.py  # Creates test users for automated tests
```

## Environment Variables

All scripts respect these environment variables:

```bash
export DB_HOST=localhost        # PostgreSQL host
export DB_PORT=5432            # PostgreSQL port
export DB_USER=postgres        # PostgreSQL user
export DB_PASSWORD=your_password  # PostgreSQL password (CHANGE in production)
```

## Troubleshooting

### "Database already exists" warnings

This is normal and safe. The scripts detect existing databases and skip creation.

### Users not appearing after seeding

Check:
1. The script completed successfully
2. You're querying the correct database (evfleet_auth)
3. PostgreSQL is running and accessible

### "Table does not exist" errors

You may need to run the application once to let Flyway create tables:
```bash
python run_app_fixed.py start
```

Or use `db_connect.py` which creates tables directly.

## Migration Path

### From init_database.py to db_connect.py

If you've been using `init_database.py`:

```bash
# Option 1: Just add users to existing setup
python db_connect.py --seed-only

# Option 2: Complete fresh setup with users
python reset_database.py
python db_connect.py
```

### From manual setup to automated setup

```bash
# If you have existing databases and want to add test users
python db_connect.py --seed-only

# If you want a complete fresh start
python reset_database.py
python db_connect.py
```

## Summary

- **Use `db_connect.py`** for development setup with test users
- **Use `init_database.py`** for production (no test users, Flyway creates tables)
- **Use `reset_database.py`** only in development when you need a fresh start
- **Use `run_app_fixed.py start`** for normal application startup (calls init_database.py automatically)
