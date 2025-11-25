# Database Management Guide

## 🎯 Overview

This guide explains how databases are managed in the EV Fleet Management Platform.

---

## 📊 Database Structure

### 8 Microservice Databases

| Database | Service | Tables | PR Implementations |
|----------|---------|--------|-------------------|
| **evfleet_auth** | Auth Service | users, roles, permissions | Authentication & authorization |
| **evfleet_fleet** | Fleet Service | vehicles, telemetry, fuel_consumption, feature_toggles | PRs 1-8 (fuel types, telemetry, analytics) |
| **evfleet_charging** | Charging Service | charging_stations, charging_sessions | PRs 9-10 (charging validation, analytics) |
| **evfleet_maintenance** | Maintenance Service | maintenance_records, schedules | PRs 11-12 (ICE maintenance, cost tracking) |
| **evfleet_driver** | Driver Service | drivers, assignments | Driver management |
| **evfleet_analytics** | Analytics Service | analytics_data, reports | Analytics & reporting |
| **evfleet_notification** | Notification Service | notifications, templates | Notifications |
| **evfleet_billing** | Billing Service | invoices, payments, subscriptions, pricing_tiers | PRs 17-18 (pricing, invoicing) |

---

## 🔧 Database Scripts

### ✅ **init_database.py** - SAFE (Use This!)

**Purpose:** Initialize databases for FIRST TIME or ensure they exist

**What it does:**
- ✅ Checks if databases exist
- ✅ Creates ONLY missing databases
- ✅ Does NOT drop existing databases
- ✅ Safe to run multiple times
- ✅ Your data is preserved

**When to use:**
- First-time setup
- After cloning the repository
- When you want to ensure all databases exist
- Part of automatic startup (run_app_fixed.py uses this)

**How to run:**
```bash
python init_database.py
```

**Output:**
```
NEW databases created: 2
  • evfleet_analytics
  • evfleet_notification

EXISTING databases skipped: 6
  • evfleet_auth
  • evfleet_fleet
  • evfleet_charging
  • evfleet_maintenance
  • evfleet_driver
  • evfleet_billing

✅ Database initialization completed successfully!
```

---

### 🔴 **reset_database.py** - DANGEROUS (Development Only!)

**Purpose:** Complete database reset for development

**What it does:**
- 🔴 DROPS all 8 databases
- 🔴 Recreates all databases from scratch
- 🔴 Deletes ALL your data
- 🔴 Runs Flyway migrations
- 🔴 Seeds sample data

**When to use:**
- ⚠️  ONLY in development when you want fresh start
- ⚠️  When testing migrations from scratch
- ⚠️  When you have corrupted data and want to reset
- ⚠️  NEVER in production!

**How to run:**
```bash
# WARNING: This will DELETE everything!
python reset_database.py
```

**Use Cases:**
```bash
# Development: Testing fresh database state
python reset_database.py

# Development: After major schema changes
python reset_database.py

# Development: Clean slate for testing
python reset_database.py
```

---

## 🚀 Database Initialization Flow

### Automatic (Recommended)

When you run `python run_app_fixed.py start`:

1. ✅ PostgreSQL starts in Docker
2. ✅ Waits 10 seconds for PostgreSQL to be ready
3. ✅ Runs `init_database.py` (SAFE)
4. ✅ Creates missing databases
5. ✅ Preserves existing databases
6. ✅ Services start
7. ✅ Flyway runs migrations automatically

**Your data is SAFE!**

### Manual

If you want to initialize databases manually:

```bash
# Option 1: Safe initialization (preserves data)
python init_database.py

# Option 2: Complete reset (DELETES data)
python reset_database.py
```

---

## 📂 Flyway Migrations

### How Migrations Work

Flyway handles schema versioning automatically:

1. When a service starts, Flyway checks the database
2. It looks for migration files in `src/main/resources/db/migration/`
3. It runs migrations that haven't been applied yet
4. Migration history is tracked in `flyway_schema_history` table

### Migration Files

#### Fleet Service (PRs 1-4)
```
V1__initial_schema.sql                    - Base tables (vehicles, telemetry)
V2__add_fuel_type_support.sql            - PR 1: Fuel type enum (ICE/EV/HYBRID)
V3__create_fuel_consumption_table.sql    - PR 1: Fuel consumption tracking
V4__create_feature_toggles.sql           - PR 2: Feature flag system
V5__extend_telemetry_for_multifuel.sql   - PR 3: ICE telemetry fields
```

#### Maintenance Service (PRs 11-12)
```
V1__initial_schema.sql                   - Base maintenance tables
V2__add_ice_maintenance_support.sql      - PR 11: ICE maintenance types (oil, filters, etc.)
```

#### Billing Service (PRs 17-18)
```
V1__create_billing_tables.sql            - Base invoices, payments, subscriptions
V2__add_pricing_tiers.sql                - PR 17: 3-tier pricing model
V20__create_invoice_payment_enhancements.sql  - PR 18: Multi-fuel surcharges
```

### Migration Naming Convention

- **Format:** `V{version}__{description}.sql`
- **Version:** Sequential number (V1, V2, V3, ...)
- **Description:** Snake_case description
- **Example:** `V5__extend_telemetry_for_multifuel.sql`

---

## ✅ Database Schema Verification

### Are Tables Created?

**Yes!** All migrations create tables properly:

#### Billing Service (PR 18 Example)

**V1** creates:
- ✅ `invoices` table (id, company_id, subscription_id, invoice_number, amount, tax, total_amount, due_date, paid_date, status, items, created_at, updated_at)
- ✅ `payments` table (id, invoice_id, amount, payment_method, transaction_id, payment_date, status, gateway_response, created_at)
- ✅ `subscriptions` table
- ✅ `pricing_plans` table
- ✅ `payment_methods` table
- ✅ Other billing tables

**V20** (PR 18) adds:
- ✅ `vehicle_count` column to invoices
- ✅ `invoice_month` column to invoices
- ✅ `charges_by_tier` column to invoices (JSONB)
- ✅ `processed_at` column to payments
- ✅ `failure_reason` column to payments
- ✅ Indexes for performance
- ✅ Column comments for documentation

#### Fleet Service (PRs 1-4 Example)

**V1** creates:
- ✅ `vehicles` table
- ✅ `telemetry_data` table
- ✅ Base indexes

**V2** (PR 1) adds:
- ✅ `fuel_type` column (ICE, EV, HYBRID enum)
- ✅ `fuel_tank_capacity` column
- ✅ `fuel_level` column
- ✅ `engine_type` column

**V3** (PR 1) creates:
- ✅ `fuel_consumption` table (for ICE vehicles)
- ✅ Tracks liters consumed, cost, distance, CO2 emissions

**V4** (PR 2) creates:
- ✅ `feature_toggles` table
- ✅ Seed data: 12 features (BATTERY_TRACKING, FUEL_CONSUMPTION, etc.)

**V5** (PR 3) adds:
- ✅ `fuel_level` column to telemetry
- ✅ `engine_rpm` column to telemetry
- ✅ `engine_temperature` column to telemetry
- ✅ `engine_load` column to telemetry
- ✅ `engine_hours` column to telemetry
- ✅ Check constraints for valid ranges
- ✅ Indexes for ICE metrics

---

## 🔍 How to Verify Database State

### Check if Databases Exist

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres

# List all databases
\l

# You should see:
#   evfleet_auth
#   evfleet_fleet
#   evfleet_charging
#   evfleet_maintenance
#   evfleet_driver
#   evfleet_analytics
#   evfleet_notification
#   evfleet_billing
```

### Check Tables in a Database

```bash
# Connect to fleet database
\c evfleet_fleet

# List all tables
\dt

# You should see:
#   vehicles
#   telemetry_data
#   fuel_consumption
#   feature_toggles
#   flyway_schema_history
```

### Check Columns Added by PR 18

```bash
# Connect to billing database
\c evfleet_billing

# Describe invoices table
\d invoices

# Look for PR 18 columns:
#   vehicle_count | integer
#   invoice_month | character varying(7)
#   charges_by_tier | jsonb
```

### Check Flyway Migration History

```bash
# See which migrations have been applied
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;

# Example output:
# installed_rank | version | description                         | installed_on        | success
# ---------------+---------+-------------------------------------+---------------------+---------
#              1 | 1       | create billing tables              | 2025-11-10 10:15:30 | t
#              2 | 2       | add pricing tiers                  | 2025-11-10 10:15:32 | t
#              3 | 20      | create invoice payment enhancements| 2025-11-10 10:15:35 | t
```

---

## 🎯 Common Scenarios

### Scenario 1: First Time Setup

```bash
# Just run the fixed launcher
python run_app_fixed.py start

# It will:
# 1. Start PostgreSQL
# 2. Create all 8 databases (init_database.py)
# 3. Services start and Flyway runs migrations
# 4. Tables are created automatically
```

### Scenario 2: Already Have Databases

```bash
# Run the launcher - your data is safe
python run_app_fixed.py start

# It will:
# 1. Check databases exist (they do)
# 2. Skip database creation
# 3. Services start
# 4. Flyway runs ONLY new migrations
# 5. Your existing data is preserved
```

### Scenario 3: Want Fresh Start (Development)

```bash
# WARNING: This deletes everything!
python reset_database.py

# Then start application
python run_app_fixed.py start
```

### Scenario 4: Check Database Status

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres

# List databases
\l

# Connect to a specific database
\c evfleet_fleet

# List tables
\dt

# Check a specific table
\d vehicles

# Check PR 18 enhancements
\c evfleet_billing
\d invoices
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'invoices';
```

---

## ⚠️ Important Notes

### Data Persistence

**init_database.py (SAFE):**
- ✅ Preserves existing databases
- ✅ Preserves existing tables
- ✅ Preserves existing data
- ✅ Only creates what's missing
- ✅ Safe to run anytime

**reset_database.py (DANGEROUS):**
- 🔴 Drops all databases
- 🔴 Deletes all tables
- 🔴 Deletes all data
- 🔴 Cannot be undone
- 🔴 Development only

### Migrations

**Flyway Migrations:**
- ✅ Run automatically when services start
- ✅ Only new migrations are applied
- ✅ Idempotent (safe to restart services)
- ✅ Versioned and tracked
- ✅ Cannot be edited once applied (use new migration instead)

### Docker Volumes

**PostgreSQL Data:**
- Data is stored in Docker volume: `docker_postgres_data`
- Persists even when containers stop
- To completely remove: `docker-compose down -v`

---

## 🚀 Quick Reference

| Task | Command | Safe? |
|------|---------|-------|
| Initialize databases (first time) | `python init_database.py` | ✅ Safe |
| Reset databases (development) | `python reset_database.py` | 🔴 Deletes data |
| Start application (auto-init) | `python run_app_fixed.py start` | ✅ Safe |
| Check database status | `psql -h localhost -U postgres -c '\l'` | ✅ Safe |
| Connect to database | `psql -h localhost -U postgres -d evfleet_fleet` | ✅ Safe |
| View migration history | `SELECT * FROM flyway_schema_history` | ✅ Safe |
| Drop all data | `docker-compose down -v` | 🔴 Deletes volumes |

---

## ✅ Schema Completeness Checklist

### All PRs Have Database Support

- [x] **PR 1:** V2, V3 migrations (fuel_type, fuel_consumption table)
- [x] **PR 2:** V4 migration (feature_toggles table)
- [x] **PR 3:** V5 migration (ICE telemetry columns)
- [x] **PR 4:** No migration (uses existing tables + query methods)
- [x] **PRs 5-8:** No new migrations (API layer only)
- [x] **PRs 9-10:** Charging service V1 (base tables)
- [x] **PR 11:** Maintenance V2 (ICE maintenance types)
- [x] **PR 12:** No new migration (analytics service layer)
- [x] **PRs 13-16:** Frontend only (no database changes)
- [x] **PR 17:** Billing V2 (pricing_tiers table)
- [x] **PR 18:** Billing V20 (invoice/payment enhancements)

### All Tables Verified

- [x] Invoices table has PR 18 columns (vehicle_count, invoice_month, charges_by_tier)
- [x] Payments table has PR 18 columns (processed_at, failure_reason)
- [x] Vehicles table has PR 1 columns (fuel_type, fuel_tank_capacity, fuel_level)
- [x] Telemetry table has PR 3 columns (fuel_level, engine_rpm, engine_temperature, etc.)
- [x] Feature toggles table exists with 12 pre-seeded features
- [x] Fuel consumption table exists for tracking ICE usage
- [x] Maintenance types include ICE-specific types (oil, filters, etc.)
- [x] Pricing tiers table exists with 3 tiers

---

## 📞 Troubleshooting

### "Database does not exist"

**Solution:** Run `python init_database.py` or `python run_app_fixed.py start`

### "Relation does not exist" (table missing)

**Cause:** Migrations didn't run

**Solution:**
1. Check Flyway logs in service startup
2. Verify migration files exist in `src/main/resources/db/migration/`
3. Check flyway_schema_history table
4. Restart the affected service

### "Column does not exist" (e.g., vehicle_count)

**Cause:** Migration V20 (PR 18) didn't run

**Solution:**
1. Connect to evfleet_billing database
2. Check: `SELECT * FROM flyway_schema_history WHERE version = '20'`
3. If missing, restart billing-service (Flyway will run it)
4. Or manually run: `psql -h localhost -U postgres -d evfleet_billing -f backend/billing-service/src/main/resources/db/migration/V20__create_invoice_payment_enhancements.sql`

### "Want to start completely fresh"

**Solution:**
```bash
# Stop everything
docker-compose down -v

# Reset databases
python reset_database.py

# Start application
python run_app_fixed.py start
```

---

**Last Updated:** November 10, 2025
**All 18 PRs:** ✅ Complete with database support
**Migration Files:** ✅ All present and correct
**Data Safety:** ✅ init_database.py preserves data
