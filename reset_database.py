#!/usr/bin/env python3
"""
Database Reset Script for EV Fleet Management Platform
This script will:
1. Drop all databases
2. Create all databases
3. Run migrations to create tables
4. Seed initial data (roles)
"""

import os
import sys
import subprocess
import time
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Database configuration
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'postgres')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'postgres')

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

def print_error(message):
    """Print an error message"""
    print(f"✗ ERROR: {message}", file=sys.stderr)

def connect_to_postgres():
    """Connect to PostgreSQL server (postgres database)"""
    try:
        print_step("Connecting to PostgreSQL server...")
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database='postgres'
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        print_success("Connected to PostgreSQL server")
        return conn
    except Exception as e:
        print_error(f"Failed to connect to PostgreSQL: {e}")
        sys.exit(1)

def drop_database(cursor, db_name):
    """Drop a database if it exists"""
    try:
        print_step(f"Dropping database '{db_name}'...")
        # Terminate all connections to the database first
        cursor.execute(f"""
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = '{db_name}'
              AND pid <> pg_backend_pid();
        """)
        cursor.execute(f"DROP DATABASE IF EXISTS {db_name}")
        print_success(f"Database '{db_name}' dropped")
    except Exception as e:
        print_error(f"Failed to drop database '{db_name}': {e}")

def create_database(cursor, db_name):
    """Create a database"""
    try:
        print_step(f"Creating database '{db_name}'...")
        cursor.execute(f"CREATE DATABASE {db_name}")
        print_success(f"Database '{db_name}' created")
    except Exception as e:
        print_error(f"Failed to create database '{db_name}': {e}")
        raise

def reset_databases():
    """Drop and recreate all databases"""
    print_header("STEP 1: Resetting Databases")
    
    conn = connect_to_postgres()
    cursor = conn.cursor()
    
    # Drop all databases
    print_step("Dropping all existing databases...")
    for db_name in DATABASES:
        drop_database(cursor, db_name)
    
    print("\n")
    
    # Create all databases
    print_step("Creating all databases...")
    for db_name in DATABASES:
        create_database(cursor, db_name)
    
    cursor.close()
    conn.close()
    print_success("All databases reset successfully")

def wait_for_postgres():
    """Wait for PostgreSQL to be ready"""
    print_step("Checking PostgreSQL availability...")
    max_retries = 30
    retry_count = 0
    
    while retry_count < max_retries:
        try:
            conn = psycopg2.connect(
                host=DB_HOST,
                port=DB_PORT,
                user=DB_USER,
                password=DB_PASSWORD,
                database='postgres'
            )
            conn.close()
            print_success("PostgreSQL is ready")
            return True
        except Exception:
            retry_count += 1
            if retry_count < max_retries:
                print(f"  Waiting for PostgreSQL... (attempt {retry_count}/{max_retries})")
                time.sleep(2)
    
    print_error("PostgreSQL is not available")
    return False

def build_auth_service():
    """Build the auth-service to ensure migrations are packaged"""
    print_header("STEP 2: Building Auth Service")
    
    auth_service_path = os.path.join(os.path.dirname(__file__), 'backend', 'auth-service')
    
    if not os.path.exists(auth_service_path):
        print_error(f"Auth service directory not found: {auth_service_path}")
        return False
    
    try:
        print_step("Building auth-service with Maven...")
        os.chdir(auth_service_path)
        result = subprocess.run(
            ['mvn', 'clean', 'install', '-DskipTests'],
            capture_output=True,
            text=True
        )
        
        if result.returncode == 0:
            print_success("Auth service built successfully")
            return True
        else:
            print_error(f"Failed to build auth service:\n{result.stderr}")
            return False
    except Exception as e:
        print_error(f"Error building auth service: {e}")
        return False

def run_migrations():
    """Run database migrations for auth-service"""
    print_header("STEP 3: Running Database Migrations")
    
    auth_service_path = os.path.join(os.path.dirname(__file__), 'backend', 'auth-service')
    
    if not os.path.exists(auth_service_path):
        print_error(f"Auth service directory not found: {auth_service_path}")
        return False
    
    try:
        print_step("Running Flyway migrations for auth-service...")
        os.chdir(auth_service_path)
        
        # Set environment variables for the migration
        env = os.environ.copy()
        env['SPRING_DATASOURCE_URL'] = f"jdbc:postgresql://{DB_HOST}:{DB_PORT}/evfleet_auth"
        env['SPRING_DATASOURCE_USERNAME'] = DB_USER
        env['SPRING_DATASOURCE_PASSWORD'] = DB_PASSWORD
        env['SPRING_FLYWAY_ENABLED'] = 'true'
        
        result = subprocess.run(
            ['mvn', 'flyway:migrate', 
             f'-Dflyway.url=jdbc:postgresql://{DB_HOST}:{DB_PORT}/evfleet_auth',
             f'-Dflyway.user={DB_USER}',
             f'-Dflyway.password={DB_PASSWORD}',
             '-Dflyway.locations=classpath:db/migration'],
            capture_output=True,
            text=True,
            env=env
        )
        
        if result.returncode == 0:
            print_success("Migrations completed successfully")
            print(result.stdout)
            return True
        else:
            print_error(f"Failed to run migrations:\n{result.stderr}")
            return False
    except Exception as e:
        print_error(f"Error running migrations: {e}")
        return False

def verify_tables():
    """Verify that tables were created"""
    print_header("STEP 4: Verifying Database Schema")
    
    try:
        print_step("Connecting to evfleet_auth database...")
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database='evfleet_auth'
        )
        cursor = conn.cursor()
        
        # Check for tables
        print_step("Checking for required tables...")
        cursor.execute("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
        """)
        
        tables = [row[0] for row in cursor.fetchall()]
        
        if tables:
            print_success(f"Found {len(tables)} tables:")
            for table in tables:
                print(f"    - {table}")
        else:
            print_error("No tables found!")
            return False
        
        # Check for roles
        required_tables = ['roles', 'users', 'user_roles']
        missing_tables = [t for t in required_tables if t not in tables]
        
        if missing_tables:
            print_error(f"Missing required tables: {', '.join(missing_tables)}")
            return False
        
        # Check role data
        print_step("Checking default roles...")
        cursor.execute("SELECT name FROM roles ORDER BY name")
        roles = [row[0] for row in cursor.fetchall()]
        
        if roles:
            print_success(f"Found {len(roles)} roles:")
            for role in roles:
                print(f"    - {role}")
        else:
            print_error("No roles found!")
            return False
        
        cursor.close()
        conn.close()
        
        print_success("Database schema verified successfully")
        return True
        
    except Exception as e:
        print_error(f"Failed to verify database: {e}")
        return False

def main():
    """Main function"""
    print_header("EV Fleet Management - Database Reset Script")
    
    print("This script will:")
    print("  1. Drop all existing databases")
    print("  2. Create fresh databases")
    print("  3. Run migrations to create tables")
    print("  4. Seed initial data (roles)")
    print("\n⚠️  WARNING: This will DELETE ALL DATA in the databases!")
    
    response = input("\nDo you want to continue? (yes/no): ").strip().lower()
    if response not in ['yes', 'y']:
        print("\nOperation cancelled.")
        sys.exit(0)
    
    # Check PostgreSQL availability
    if not wait_for_postgres():
        sys.exit(1)
    
    # Reset databases
    try:
        reset_databases()
    except Exception as e:
        print_error(f"Failed during database reset: {e}")
        sys.exit(1)
    
    # Build auth service
    if not build_auth_service():
        print_error("Build failed. Please fix the errors and try again.")
        sys.exit(1)
    
    # Run migrations
    if not run_migrations():
        print_error("Migration failed. Please fix the errors and try again.")
        sys.exit(1)
    
    # Verify setup
    if not verify_tables():
        print_error("Verification failed. Please check the database.")
        sys.exit(1)
    
    print_header("Database Reset Complete!")
    print("\n✓ All databases have been reset and initialized successfully.")
    print("\nNext steps:")
    print("  1. Start your microservices")
    print("  2. Register a new user through Firebase")
    print("  3. The user will be synced to the backend database automatically")
    print("\nNote: Firebase authentication must sync with backend database for access.")
    print("\n" + "=" * 80 + "\n")

if __name__ == '__main__':
    main()
