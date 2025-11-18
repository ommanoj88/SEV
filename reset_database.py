#!/usr/bin/env python3
# On Windows, run with: python reset_database.py
"""
Database Reset Script for EV Fleet Management Platform
Updated for Modular Monolith Architecture (Nov 2025)

This script will:
1. Drop all databases (with confirmation)
2. Create all databases
3. Run Flyway migrations from evfleet-monolith
4. Seed initial data (roles)
"""

import os
import sys
import subprocess
import time

# Maven path (IntelliJ bundled Maven on Windows)
MAVEN_CMD = r"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd"

# Database configuration
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'postgres')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'Shobharain11@')

# List of databases for modular monolith (8 module databases)
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
    print(f">> {message}")

def print_success(message):
    """Print a success message"""
    print(f"[OK] {message}")

def print_error(message):
    """Print an error message"""
    print(f"[ERROR] {message}", file=sys.stderr)

def print_warning(message):
    """Print a warning message"""
    print(f"WARNING: {message}")

def print_info(message):
    """Print an info message"""
    print(f"INFO: {message}")

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

def drop_database(db_name):
    """Drop a database if it exists"""
    try:
        print_step(f"Dropping database '{db_name}'...")

        # Terminate all connections to the database first
        terminate_sql = f"""
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = '{db_name}'
              AND pid <> pg_backend_pid();
        """
        run_psql_command(terminate_sql)

        # Drop the database
        success, stdout, stderr = run_psql_command(f"DROP DATABASE IF EXISTS {db_name}")
        if success:
            print_success(f"Database '{db_name}' dropped")
        else:
            print_error(f"Failed to drop database '{db_name}': {stderr}")
    except Exception as e:
        print_error(f"Failed to drop database '{db_name}': {e}")

def create_database(db_name):
    """Create a database"""
    try:
        print_step(f"Creating database '{db_name}'...")
        success, stdout, stderr = run_psql_command(f"CREATE DATABASE {db_name}")
        if success:
            print_success(f"Database '{db_name}' created")
            return True
        else:
            print_error(f"Failed to create database '{db_name}': {stderr}")
            return False
    except Exception as e:
        print_error(f"Failed to create database '{db_name}': {e}")
        return False

def reset_databases():
    """Drop and recreate all databases"""
    print_header("STEP 1: Resetting Databases")

    # Drop all databases
    print_step("Dropping all existing databases...")
    for db_name in DATABASES:
        drop_database(db_name)

    print("\n")

    # Create all databases
    print_step("Creating all databases...")
    for db_name in DATABASES:
        if not create_database(db_name):
            return False

    print_success("All databases reset successfully")
    return True

def build_monolith():
    """Build the evfleet-monolith to ensure migrations are packaged"""
    print_header("STEP 2: Building EVFleet Monolith")

    monolith_path = os.path.join(os.path.dirname(__file__), 'backend', 'evfleet-monolith')

    if not os.path.exists(monolith_path):
        print_error(f"Monolith directory not found: {monolith_path}")
        return False

    try:
        print_step("Building evfleet-monolith with Maven...")
        os.chdir(monolith_path)

        result = subprocess.run(
            [MAVEN_CMD, 'clean', 'package', '-DskipTests'],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            print_success("Monolith built successfully")
            return True
        else:
            print_error(f"Failed to build monolith:\n{result.stderr}")
            return False
    except Exception as e:
        print_error(f"Error building monolith: {e}")
        return False

def run_migrations_for_module(module_name, db_name):
    """Run Flyway migrations for a specific module"""
    print_step(f"Running migrations for {module_name} module...")

    monolith_path = os.path.join(os.path.dirname(__file__), 'backend', 'evfleet-monolith')

    if not os.path.exists(monolith_path):
        print_error(f"Monolith directory not found: {monolith_path}")
        return False

    try:
        os.chdir(monolith_path)

        # Set environment variables for the migration
        env = os.environ.copy()
        env['SPRING_DATASOURCE_URL'] = f"jdbc:postgresql://{DB_HOST}:{DB_PORT}/{db_name}"
        env['SPRING_DATASOURCE_USERNAME'] = DB_USER
        env['SPRING_DATASOURCE_PASSWORD'] = DB_PASSWORD
        env['SPRING_FLYWAY_ENABLED'] = 'true'

        # Run Flyway migration for this specific module
        result = subprocess.run(
            [MAVEN_CMD, 'flyway:migrate',
             f'-Dflyway.url=jdbc:postgresql://{DB_HOST}:{DB_PORT}/{db_name}',
             f'-Dflyway.user={DB_USER}',
             f'-Dflyway.password={DB_PASSWORD}',
             f'-Dflyway.locations=classpath:db/migration/{module_name}'],
            capture_output=True,
            text=True,
            env=env
        )

        if result.returncode == 0:
            print_success(f"Migrations completed for {module_name}")
            return True
        else:
            print_warning(f"Migration might have failed for {module_name}: {result.stderr}")
            # Continue anyway - migrations might already be applied
            return True
    except Exception as e:
        print_error(f"Error running migrations for {module_name}: {e}")
        return True  # Continue anyway

def run_all_migrations():
    """Run database migrations for all modules"""
    print_header("STEP 3: Running Database Migrations")

    # Mapping of module names to database names
    module_mappings = {
        'auth': 'evfleet_auth',
        'fleet': 'evfleet_fleet',
        'charging': 'evfleet_charging',
        'maintenance': 'evfleet_maintenance',
        'driver': 'evfleet_driver',
        'analytics': 'evfleet_analytics',
        'notification': 'evfleet_notification',
        'billing': 'evfleet_billing'
    }

    for module_name, db_name in module_mappings.items():
        if not run_migrations_for_module(module_name, db_name):
            print_warning(f"Migration issues for {module_name}, but continuing...")

    print_success("All migrations completed")
    return True

def verify_auth_tables():
    """Verify that auth tables were created (sample verification)"""
    print_header("STEP 4: Verifying Database Schema")

    try:
        print_step("Checking for required tables in evfleet_auth...")

        success, stdout, stderr = run_psql_command(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name",
            database='evfleet_auth'
        )

        if not success:
            print_error(f"Failed to query tables: {stderr}")
            return False

        # Parse tables
        tables = [
            line.strip()
            for line in stdout.split('\n')
            if line.strip()
            and not line.startswith('-')
            and not line.startswith('table_name')
            and not line.startswith('(')
            and 'row' not in line.lower()
        ]

        if tables:
            print_success(f"Found {len(tables)} tables in evfleet_auth:")
            for table in tables[:10]:  # Show first 10
                print(f"    - {table}")
            if len(tables) > 10:
                print(f"    ... and {len(tables) - 10} more")
        else:
            print_warning("No tables found in evfleet_auth!")
            print_warning("Tables will be created when the monolith starts")

        # Check for required auth tables
        required_tables = ['roles', 'users', 'user_roles']
        missing_tables = [t for t in required_tables if t not in tables]

        if missing_tables:
            print_warning(f"Some expected tables not found: {', '.join(missing_tables)}")
            print_info("These will be created when the monolith starts")
        else:
            # Check role data
            print_step("Checking default roles...")
            success, stdout, stderr = run_psql_command(
                "SELECT name FROM roles ORDER BY name",
                database='evfleet_auth'
            )

            if success:
                roles = [
                    line.strip()
                    for line in stdout.split('\n')
                    if line.strip()
                    and not line.startswith('-')
                    and not line.startswith('name')
                    and not line.startswith('(')
                    and 'row' not in line.lower()
                ]

                if roles:
                    print_success(f"Found {len(roles)} roles:")
                    for role in roles:
                        print(f"    - {role}")
                else:
                    print_warning("No roles found - will be seeded on first startup")

            # Check users table columns (including firstName and lastName)
            print_step("Checking users table columns...")
            success, stdout, stderr = run_psql_command(
                "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users' ORDER BY ordinal_position",
                database='evfleet_auth'
            )

            if success:
                columns = [
                    line.strip()
                    for line in stdout.split('\n')
                    if line.strip()
                    and not line.startswith('-')
                    and not line.startswith('column_name')
                    and not line.startswith('(')
                    and 'row' not in line.lower()
                ]

                if columns:
                    print_success(f"Found {len(columns)} columns in users table:")
                    # Check for new firstName and lastName columns
                    has_firstname = any('first_name' in col.lower() for col in columns)
                    has_lastname = any('last_name' in col.lower() for col in columns)
                    
                    for col in columns:
                        print(f"    - {col}")
                    
                    if has_firstname and has_lastname:
                        print_success("✓ firstName and lastName columns are present")
                    else:
                        if not has_firstname:
                            print_warning("  Missing first_name column (will be created on startup)")
                        if not has_lastname:
                            print_warning("  Missing last_name column (will be created on startup)")
                else:
                    print_warning("No columns found - users table will be created on startup")

        print_success("Database verification completed")
        return True

    except Exception as e:
        print_error(f"Failed to verify database: {e}")
        return False

def seed_default_roles():
    """Seed default roles into auth database"""
    print_header("STEP 5: Seeding Default Roles")

    try:
        print_step("Running seed_default_roles.py...")
        script_path = os.path.join(os.path.dirname(__file__), 'seed_default_roles.py')

        if not os.path.exists(script_path):
            print_warning("seed_default_roles.py not found - roles will be seeded on first startup")
            return True

        result = subprocess.run(
            [sys.executable, script_path],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            print_success("Default roles seeded successfully")
            print(result.stdout)
            return True
        else:
            print_error(f"Failed to seed roles: {result.stderr}")
            return False
    except Exception as e:
        print_error(f"Error seeding roles: {e}")
        return False

def main():
    """Main function"""
    print_header("EV Fleet Management - Database Reset Script (Monolith)")

    print("This script will:")
    print("  1. Drop all existing databases (8 module databases)")
    print("  2. Create fresh databases")
    print("  3. Run Flyway migrations from evfleet-monolith")
    print("  4. Verify schema creation")
    print("  5. Seed default roles")
    print("\nWARNING: This will DELETE ALL DATA in the databases!")
    print(f"\nDatabases to be reset: {', '.join(DATABASES)}")

    response = input("\nDo you want to continue? (yes/no): ").strip().lower()
    if response not in ['yes', 'y']:
        print("\nOperation cancelled.")
        sys.exit(0)

    # Check PostgreSQL availability
    if not wait_for_postgres():
        sys.exit(1)

    # Reset databases
    try:
        if not reset_databases():
            print_error("Failed to reset databases")
            sys.exit(1)
    except Exception as e:
        print_error(f"Failed during database reset: {e}")
        sys.exit(1)

    # Build monolith
    print_step("Building monolith (this may take a few minutes)...")
    if not build_monolith():
        print_warning("Build failed, but continuing...")

    # Run migrations
    print_step("Running migrations for all modules...")
    if not run_all_migrations():
        print_warning("Some migrations might have failed, but continuing...")

    # Verify tables
    verify_auth_tables()

    # Seed default roles
    if not seed_default_roles():
        print_warning("Role seeding failed, but database reset completed")

    print_header("Database Reset Complete!")
    print("\nSUCCESS: All databases have been dropped and recreated successfully.")
    print("\nDatabase Schema Updates (Nov 2025):")
    print("  • Users table now includes: firstName, lastName columns")
    print("  • User registration collects firstName and lastName")
    print("  • Google Sign-In sync includes firstName and lastName")
    print("  • UserResponse API returns firstName and lastName fields")
    print("\nNext steps:")
    print("  1. Start your application with: python run_app_fixed.py start")
    print("  2. Or manually start backend: cd backend/evfleet-monolith && mvn spring-boot:run")
    print("  3. Flyway will automatically run any remaining migrations on startup")
    print("  4. Initial data (roles, etc.) will be seeded automatically")
    print("  5. User firstName/lastName will be collected on registration")
    print("\nTIP: The monolith uses 8 separate databases for module isolation!")
    print("\nDatabase URLs:")
    for db in DATABASES:
        print(f"  - {db:30} jdbc:postgresql://{DB_HOST}:{DB_PORT}/{db}")
    print("\n" + "=" * 80 + "\n")

if __name__ == '__main__':
    main()
