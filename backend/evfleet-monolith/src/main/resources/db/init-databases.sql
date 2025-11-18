-- EVFleet Monolith - Database Initialization Script
-- This script creates all 8 databases required by the monolith
-- Run this script as PostgreSQL superuser before starting the application

-- Create databases
CREATE DATABASE evfleet_auth;
CREATE DATABASE evfleet_fleet;
CREATE DATABASE evfleet_charging;
CREATE DATABASE evfleet_maintenance;
CREATE DATABASE evfleet_driver;
CREATE DATABASE evfleet_analytics;
CREATE DATABASE evfleet_notification;
CREATE DATABASE evfleet_billing;

-- Create user (optional - if you want a dedicated user)
-- CREATE USER evfleet_user WITH PASSWORD 'Shobharain11@';

-- Grant privileges (if using dedicated user)
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_auth TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_fleet TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_charging TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_maintenance TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_driver TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_analytics TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_notification TO evfleet_user;
-- GRANT ALL PRIVILEGES ON DATABASE evfleet_billing TO evfleet_user;

-- List databases
\l

-- Connect to each database and verify
\c evfleet_auth
SELECT 'Connected to evfleet_auth' AS status;

\c evfleet_fleet
SELECT 'Connected to evfleet_fleet' AS status;

\c evfleet_charging
SELECT 'Connected to evfleet_charging' AS status;

\c evfleet_maintenance
SELECT 'Connected to evfleet_maintenance' AS status;

\c evfleet_driver
SELECT 'Connected to evfleet_driver' AS status;

\c evfleet_analytics
SELECT 'Connected to evfleet_analytics' AS status;

\c evfleet_notification
SELECT 'Connected to evfleet_notification' AS status;

\c evfleet_billing
SELECT 'Connected to evfleet_billing' AS status;

-- All databases created successfully
SELECT 'All 8 databases created successfully!' AS result;
