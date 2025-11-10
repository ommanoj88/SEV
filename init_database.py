#!/usr/bin/env python3
# On Windows, run with: python init_database.py
"""
Database Initialization Script for EV Fleet Management Platform
This script will SAFELY:
1. Check if databases exist
2. Create databases ONLY if they don't exist
3. Run migrations (Flyway will handle schema versioning)
4. Seed initial data if needed

This is SAFE to run multiple times - it will NOT drop existing databases!
Use reset_database.py ONLY for development when you want to start fresh.
"""

import os
import sys
import subprocess
import time

# Database configuration
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'postgres')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'Shobharain11@')

# List of databases for each microservice
DATABASES = [
    'evfleet_auth',
    'evfleet_fleet',
    'evfleet_charging',
    'evfleet_maintenance',
    'evfleet_driver',
    'evfleet_analytics',
    'evfleet_notification',
    'evfleet_billing'
]

def print_header(message):
    """Print a formatted header message"""
    print("\n" + "=" * 80)
    print(f"  {message}")
    print("=" * 80 + "\n")

def print_step(message):
    """Print a step message"""
    print(f"➤ {message}")

def print_success(message):
    """Print a success message"""
    print(f"✓ {message}")

def print_warning(message):
    """Print a warning message"""
    print(f"⚠ {message}")

def print_error(message):
    """Print an error message"""
    print(f"✗ ERROR: {message}", file=sys.stderr)

def run_psql_command(sql_command, database='postgres'):
    """Run a psql command"""
    env = os.environ.copy()
    env['PGPASSWORD'] = DB_PASSWORD

    cmd = [
        'psql',
        '-h', DB_HOST,
        '-p', DB_PORT,
        '-U', DB_USER,
        '-d', database,
        '-c', sql_command
    ]

    result = subprocess.run(cmd, capture_output=True, text=True, env=env)
    return result.returncode == 0, result.stdout, result.stderr

def wait_for_postgres():
    """Wait for PostgreSQL to be ready"""
    print_step("Checking PostgreSQL availability...")
    max_retries = 30
    retry_count = 0

    env = os.environ.copy()
    env['PGPASSWORD'] = DB_PASSWORD

    while retry_count < max_retries:
        result = subprocess.run(
            ['psql', '-h', DB_HOST, '-p', DB_PORT, '-U', DB_USER, '-d', 'postgres', '-c', 'SELECT 1'],
            capture_output=True,
            env=env
        )

        if result.returncode == 0:
            print_success("PostgreSQL is ready")
            return True

        retry_count += 1
        if retry_count < max_retries:
            print(f"  Waiting for PostgreSQL... (attempt {retry_count}/{max_retries})")
            time.sleep(2)

    print_error("PostgreSQL is not available")
    return False

def database_exists(db_name):
    """Check if a database exists"""
    success, stdout, stderr = run_psql_command(
        f"SELECT 1 FROM pg_database WHERE datname = '{db_name}'",
        'postgres'
    )

    if success and '1 row' in stdout:
        return True
    return False

def create_database(db_name):
    """Create a database ONLY if it doesn't exist"""
    try:
        if database_exists(db_name):
            print_warning(f"Database '{db_name}' already exists - SKIPPING")
            return True

        print_step(f"Creating database '{db_name}'...")
        success, stdout, stderr = run_psql_command(f"CREATE DATABASE {db_name}", 'postgres')

        if success:
            print_success(f"Database '{db_name}' created successfully")
            return True
        else:
            print_error(f"Failed to create database '{db_name}': {stderr}")
            return False
    except Exception as e:
        print_error(f"Exception creating database '{db_name}': {e}")
        return False

def main():
    """Main execution"""
    print_header("EV Fleet Management Platform - Database Initialization")

    print_warning("⚠️  This script is SAFE - it will NOT drop existing databases")
    print_warning("⚠️  Flyway will handle schema migrations automatically")
    print("")

    # Wait for PostgreSQL
    if not wait_for_postgres():
        sys.exit(1)

    # Create databases (only if they don't exist)
    print_header("Creating Databases (if they don't exist)")

    all_success = True
    new_databases = []
    existing_databases = []

    for db_name in DATABASES:
        if database_exists(db_name):
            existing_databases.append(db_name)
            print_warning(f"✓ {db_name} - Already exists")
        else:
            if create_database(db_name):
                new_databases.append(db_name)
            else:
                all_success = False

    # Summary
    print_header("Initialization Summary")

    if new_databases:
        print_success(f"NEW databases created: {len(new_databases)}")
        for db in new_databases:
            print(f"  • {db}")

    if existing_databases:
        print_warning(f"EXISTING databases skipped: {len(existing_databases)}")
        for db in existing_databases:
            print(f"  • {db}")

    print("")

    if all_success:
        print_success("✅ Database initialization completed successfully!")
        print("")
        print_step("Next steps:")
        print("  1. Start your application with: python run_app_fixed.py start")
        print("  2. Flyway will automatically run migrations when services start")
        print("  3. Your data is SAFE - no databases were dropped")
        print("")
        print_warning("💡 TIP: To completely reset databases (DANGER!), use: python reset_database.py")
        return 0
    else:
        print_error("❌ Some databases could not be created")
        return 1

if __name__ == '__main__':
    sys.exit(main())
