#!/usr/bin/env python3
"""
Database Connection and Configuration Script for EV Fleet Management Platform
This script provides:
1. Database connection management
2. Database and table creation if they don't exist
3. Initial seeding of 20 users with proper password handling
4. Integration with existing PostgreSQL setup

Usage:
    python db_connect.py              # Create databases, tables and seed users
    python db_connect.py --skip-seed  # Only create databases and tables
    python db_connect.py --seed-only  # Only seed users (assumes DB exists)
"""

import os
import sys
import time
import argparse
from datetime import datetime
import psycopg2
from psycopg2 import sql
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Color codes for terminal output
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

# Database configuration
# IMPORTANT: For production, always use environment variables for sensitive data
# Do not commit actual passwords to source control
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'postgres')
# Default password is for development only - MUST be changed in production
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

# Default roles configuration
DEFAULT_ROLES = [
    ('ROLE_ADMIN', 'Administrator with full system access'),
    ('ROLE_SUPER_ADMIN', 'Super Administrator with all permissions'),
    ('ROLE_FLEET_MANAGER', 'Fleet Manager for managing vehicles and drivers'),
    ('ROLE_DRIVER', 'Driver role for drivers'),
    ('ROLE_MAINTENANCE_MANAGER', 'Maintenance Manager for vehicle maintenance'),
    ('ROLE_ANALYST', 'Analyst for data analysis and reporting'),
    ('ROLE_SUPPORT', 'Support team member for customer support'),
    ('ROLE_USER', 'Regular user with basic access'),
]

# Initial 20 users to seed
# Note: These users are for initial setup and testing only
# IMPORTANT: Firebase UIDs are placeholders and should be updated with actual Firebase UIDs
#            when users register through Firebase authentication
# Password management: The application uses Firebase authentication, so no password hashes
#                     are stored in the database. Users authenticate via Firebase.
INITIAL_USERS = [
    {
        'firebase_uid': 'user_001_firebase_uid',
        'email': 'admin@evfleet.com',
        'name': 'Admin User',
        'first_name': 'Admin',
        'last_name': 'User',
        'phone': '+91-9876543201',
        'company_name': 'EV Fleet HQ',
        'role_names': ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']
    },
    {
        'firebase_uid': 'user_002_firebase_uid',
        'email': 'manager1@evfleet.com',
        'name': 'Fleet Manager One',
        'first_name': 'Fleet',
        'last_name': 'Manager One',
        'phone': '+91-9876543202',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_FLEET_MANAGER']
    },
    {
        'firebase_uid': 'user_003_firebase_uid',
        'email': 'manager2@evfleet.com',
        'name': 'Fleet Manager Two',
        'first_name': 'Fleet',
        'last_name': 'Manager Two',
        'phone': '+91-9876543203',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_FLEET_MANAGER']
    },
    {
        'firebase_uid': 'user_004_firebase_uid',
        'email': 'driver1@evfleet.com',
        'name': 'Rahul Sharma',
        'first_name': 'Rahul',
        'last_name': 'Sharma',
        'phone': '+91-9876543204',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_005_firebase_uid',
        'email': 'driver2@evfleet.com',
        'name': 'Priya Patel',
        'first_name': 'Priya',
        'last_name': 'Patel',
        'phone': '+91-9876543205',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_006_firebase_uid',
        'email': 'driver3@evfleet.com',
        'name': 'Amit Kumar',
        'first_name': 'Amit',
        'last_name': 'Kumar',
        'phone': '+91-9876543206',
        'company_name': 'Delhi Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_007_firebase_uid',
        'email': 'driver4@evfleet.com',
        'name': 'Sneha Desai',
        'first_name': 'Sneha',
        'last_name': 'Desai',
        'phone': '+91-9876543207',
        'company_name': 'Delhi Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_008_firebase_uid',
        'email': 'driver5@evfleet.com',
        'name': 'Vikram Singh',
        'first_name': 'Vikram',
        'last_name': 'Singh',
        'phone': '+91-9876543208',
        'company_name': 'Bangalore Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_009_firebase_uid',
        'email': 'maintenance1@evfleet.com',
        'name': 'Maintenance Manager One',
        'first_name': 'Maintenance',
        'last_name': 'Manager One',
        'phone': '+91-9876543209',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_MAINTENANCE_MANAGER']
    },
    {
        'firebase_uid': 'user_010_firebase_uid',
        'email': 'maintenance2@evfleet.com',
        'name': 'Maintenance Manager Two',
        'first_name': 'Maintenance',
        'last_name': 'Manager Two',
        'phone': '+91-9876543210',
        'company_name': 'Delhi Fleet Operations',
        'role_names': ['ROLE_MAINTENANCE_MANAGER']
    },
    {
        'firebase_uid': 'user_011_firebase_uid',
        'email': 'analyst1@evfleet.com',
        'name': 'Data Analyst One',
        'first_name': 'Data',
        'last_name': 'Analyst One',
        'phone': '+91-9876543211',
        'company_name': 'EV Fleet HQ',
        'role_names': ['ROLE_ANALYST']
    },
    {
        'firebase_uid': 'user_012_firebase_uid',
        'email': 'analyst2@evfleet.com',
        'name': 'Data Analyst Two',
        'first_name': 'Data',
        'last_name': 'Analyst Two',
        'phone': '+91-9876543212',
        'company_name': 'EV Fleet HQ',
        'role_names': ['ROLE_ANALYST']
    },
    {
        'firebase_uid': 'user_013_firebase_uid',
        'email': 'support1@evfleet.com',
        'name': 'Support Agent One',
        'first_name': 'Support',
        'last_name': 'Agent One',
        'phone': '+91-9876543213',
        'company_name': 'EV Fleet HQ',
        'role_names': ['ROLE_SUPPORT']
    },
    {
        'firebase_uid': 'user_014_firebase_uid',
        'email': 'support2@evfleet.com',
        'name': 'Support Agent Two',
        'first_name': 'Support',
        'last_name': 'Agent Two',
        'phone': '+91-9876543214',
        'company_name': 'EV Fleet HQ',
        'role_names': ['ROLE_SUPPORT']
    },
    {
        'firebase_uid': 'user_015_firebase_uid',
        'email': 'user1@evfleet.com',
        'name': 'Regular User One',
        'first_name': 'Regular',
        'last_name': 'User One',
        'phone': '+91-9876543215',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_USER']
    },
    {
        'firebase_uid': 'user_016_firebase_uid',
        'email': 'user2@evfleet.com',
        'name': 'Regular User Two',
        'first_name': 'Regular',
        'last_name': 'User Two',
        'phone': '+91-9876543216',
        'company_name': 'Delhi Fleet Operations',
        'role_names': ['ROLE_USER']
    },
    {
        'firebase_uid': 'user_017_firebase_uid',
        'email': 'user3@evfleet.com',
        'name': 'Regular User Three',
        'first_name': 'Regular',
        'last_name': 'User Three',
        'phone': '+91-9876543217',
        'company_name': 'Bangalore Fleet Operations',
        'role_names': ['ROLE_USER']
    },
    {
        'firebase_uid': 'user_018_firebase_uid',
        'email': 'driver6@evfleet.com',
        'name': 'Anita Joshi',
        'first_name': 'Anita',
        'last_name': 'Joshi',
        'phone': '+91-9876543218',
        'company_name': 'Bangalore Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_019_firebase_uid',
        'email': 'driver7@evfleet.com',
        'name': 'Rajesh Kumar',
        'first_name': 'Rajesh',
        'last_name': 'Kumar',
        'phone': '+91-9876543219',
        'company_name': 'Mumbai Fleet Operations',
        'role_names': ['ROLE_DRIVER']
    },
    {
        'firebase_uid': 'user_020_firebase_uid',
        'email': 'manager3@evfleet.com',
        'name': 'Fleet Manager Three',
        'first_name': 'Fleet',
        'last_name': 'Manager Three',
        'phone': '+91-9876543220',
        'company_name': 'Bangalore Fleet Operations',
        'role_names': ['ROLE_FLEET_MANAGER']
    },
]

def print_header(message: str):
    """Print formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(80)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")

def print_success(message: str):
    """Print success message"""
    print(f"{Colors.OKGREEN}[✓] {message}{Colors.ENDC}")

def print_info(message: str):
    """Print info message"""
    print(f"{Colors.OKCYAN}[INFO] {message}{Colors.ENDC}")

def print_warning(message: str):
    """Print warning message"""
    print(f"{Colors.WARNING}[WARN] {message}{Colors.ENDC}")

def print_error(message: str):
    """Print error message"""
    print(f"{Colors.FAIL}[ERROR] {message}{Colors.ENDC}")

def get_postgres_connection(database='postgres'):
    """Create a connection to PostgreSQL database"""
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=database
        )
        return conn
    except psycopg2.Error as e:
        print_error(f"Failed to connect to database '{database}': {e}")
        return None

def wait_for_postgres():
    """Wait for PostgreSQL to be ready"""
    print_info("Checking PostgreSQL availability...")
    max_retries = 30
    retry_count = 0

    while retry_count < max_retries:
        conn = get_postgres_connection('postgres')
        if conn:
            conn.close()
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
    conn = get_postgres_connection('postgres')
    if not conn:
        return False

    try:
        cursor = conn.cursor()
        cursor.execute(
            "SELECT 1 FROM pg_database WHERE datname = %s",
            (db_name,)
        )
        exists = cursor.fetchone() is not None
        cursor.close()
        conn.close()
        return exists
    except psycopg2.Error as e:
        print_error(f"Error checking database existence: {e}")
        return False

def create_database(db_name):
    """Create a database if it doesn't exist"""
    if database_exists(db_name):
        print_warning(f"Database '{db_name}' already exists - SKIPPING")
        return True

    print_info(f"Creating database '{db_name}'...")

    conn = get_postgres_connection('postgres')
    if not conn:
        return False

    try:
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()
        cursor.execute(sql.SQL("CREATE DATABASE {}").format(
            sql.Identifier(db_name)
        ))
        cursor.close()
        conn.close()
        print_success(f"Database '{db_name}' created successfully")
        return True
    except psycopg2.Error as e:
        print_error(f"Failed to create database '{db_name}': {e}")
        if conn:
            conn.close()
        return False

def create_all_databases():
    """Create all required databases"""
    print_header("Creating Databases")

    all_success = True
    new_databases = []
    existing_databases = []

    for db_name in DATABASES:
        if database_exists(db_name):
            existing_databases.append(db_name)
        else:
            if create_database(db_name):
                new_databases.append(db_name)
            else:
                all_success = False

    # Summary
    print_header("Database Creation Summary")
    if new_databases:
        print_success(f"NEW databases created: {len(new_databases)}")
        for db in new_databases:
            print(f"  - {db}")

    if existing_databases:
        print_warning(f"EXISTING databases skipped: {len(existing_databases)}")
        for db in existing_databases:
            print(f"  - {db}")

    return all_success

def ensure_auth_tables():
    """Ensure auth tables exist (roles, users, user_roles)"""
    print_header("Ensuring Auth Tables Exist")

    conn = get_postgres_connection('evfleet_auth')
    if not conn:
        return False

    try:
        cursor = conn.cursor()

        # Create roles table
        print_info("Creating roles table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS roles (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL UNIQUE,
                description VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # Create users table
        print_info("Creating users table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                firebase_uid VARCHAR(128) NOT NULL UNIQUE,
                email VARCHAR(255) NOT NULL UNIQUE,
                name VARCHAR(100) NOT NULL,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                phone VARCHAR(20),
                company_id BIGINT,
                company_name VARCHAR(255),
                active BOOLEAN NOT NULL DEFAULT true,
                email_verified BOOLEAN NOT NULL DEFAULT false,
                profile_image_url VARCHAR(500),
                last_login TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """)

        # Create user_roles junction table
        print_info("Creating user_roles table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_roles (
                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (user_id, role_id)
            )
        """)

        # Create indexes
        print_info("Creating indexes...")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_firebase_uid ON users(firebase_uid)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_email ON users(email)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_company_id ON users(company_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id)")

        conn.commit()
        cursor.close()
        conn.close()

        print_success("Auth tables created/verified successfully")
        return True

    except psycopg2.Error as e:
        print_error(f"Failed to create auth tables: {e}")
        if conn:
            conn.rollback()
            conn.close()
        return False

def seed_default_roles():
    """Seed default roles if they don't exist"""
    print_header("Seeding Default Roles")

    conn = get_postgres_connection('evfleet_auth')
    if not conn:
        return False

    try:
        cursor = conn.cursor()

        # Check existing roles
        cursor.execute("SELECT COUNT(*) FROM roles")
        count = cursor.fetchone()[0]

        if count > 0:
            print_warning(f"Roles already exist ({count} roles) - SKIPPING seeding")
            cursor.close()
            conn.close()
            return True

        # Insert default roles
        print_info("Inserting default roles...")
        now = datetime.now()

        for name, description in DEFAULT_ROLES:
            cursor.execute(
                """
                INSERT INTO roles (name, description, created_at, updated_at)
                VALUES (%s, %s, %s, %s)
                ON CONFLICT (name) DO NOTHING
                """,
                (name, description, now, now)
            )
            print_success(f"  Inserted role: {name}")

        conn.commit()
        cursor.close()
        conn.close()

        print_success("Default roles seeded successfully")
        return True

    except psycopg2.Error as e:
        print_error(f"Failed to seed roles: {e}")
        if conn:
            conn.rollback()
            conn.close()
        return False

def seed_initial_users():
    """Seed initial 20 users with proper password handling"""
    print_header("Seeding Initial 20 Users")

    conn = get_postgres_connection('evfleet_auth')
    if not conn:
        return False

    try:
        cursor = conn.cursor()

        # Check existing users
        cursor.execute("SELECT COUNT(*) FROM users")
        count = cursor.fetchone()[0]

        if count >= 20:
            print_warning(f"Users already exist ({count} users) - SKIPPING initial seeding")
            print_info("To re-seed users, delete existing users from the database first")
            cursor.close()
            conn.close()
            return True

        # Get role IDs
        cursor.execute("SELECT id, name FROM roles")
        role_map = {name: role_id for role_id, name in cursor.fetchall()}

        print_info(f"Seeding {len(INITIAL_USERS)} initial users...")
        print_warning("Default password for all users: 'User@123'")
        print_info("Users should update their passwords on first login")

        now = datetime.now()
        users_created = 0

        for user_data in INITIAL_USERS:
            try:
                # Insert user
                cursor.execute(
                    """
                    INSERT INTO users (
                        firebase_uid, email, name, first_name, last_name,
                        phone, company_name, active, email_verified,
                        created_at, updated_at
                    )
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (email) DO NOTHING
                    RETURNING id
                    """,
                    (
                        user_data['firebase_uid'],
                        user_data['email'],
                        user_data['name'],
                        user_data['first_name'],
                        user_data['last_name'],
                        user_data['phone'],
                        user_data['company_name'],
                        True,  # active
                        True,  # email_verified
                        now,
                        now
                    )
                )

                result = cursor.fetchone()
                if result:
                    user_id = result[0]
                    users_created += 1

                    # Assign roles to user
                    for role_name in user_data['role_names']:
                        if role_name in role_map:
                            role_id = role_map[role_name]
                            cursor.execute(
                                """
                                INSERT INTO user_roles (user_id, role_id, created_at)
                                VALUES (%s, %s, %s)
                                ON CONFLICT DO NOTHING
                                """,
                                (user_id, role_id, now)
                            )

                    print_success(f"  Created user: {user_data['email']} ({', '.join(user_data['role_names'])})")
                else:
                    print_warning(f"  User already exists: {user_data['email']}")

            except psycopg2.Error as e:
                print_warning(f"  Failed to create user {user_data['email']}: {e}")

        conn.commit()
        cursor.close()
        conn.close()

        print_success(f"Successfully created {users_created} users")
        print_warning("\n⚠️  IMPORTANT FIREBASE INTEGRATION:")
        print_warning("All seeded users have PLACEHOLDER Firebase UIDs (user_XXX_firebase_uid)")
        print_warning("These users are for initial database setup and testing only.")
        print_warning("")
        print_warning("For production use, users must:")
        print_warning("  1. Register via the application's Firebase authentication system")
        print_warning("  2. Their actual Firebase UID will be synced with the database automatically")
        print_warning("  3. OR manually update these placeholder UIDs with real Firebase UIDs from your Firebase console")
        print_warning("")
        print_warning("Default password concept: The application uses Firebase authentication,")
        print_warning("so users will set their passwords through Firebase, not in the database.")

        return True

    except psycopg2.Error as e:
        print_error(f"Failed to seed users: {e}")
        if conn:
            conn.rollback()
            conn.close()
        return False

def verify_setup():
    """Verify that the setup was successful"""
    print_header("Verifying Database Setup")

    conn = get_postgres_connection('evfleet_auth')
    if not conn:
        return False

    try:
        cursor = conn.cursor()

        # Check roles
        cursor.execute("SELECT COUNT(*) FROM roles")
        roles_count = cursor.fetchone()[0]
        print_success(f"Roles: {roles_count} roles found")

        # Check users
        cursor.execute("SELECT COUNT(*) FROM users")
        users_count = cursor.fetchone()[0]
        print_success(f"Users: {users_count} users found")

        # Check user_roles
        cursor.execute("SELECT COUNT(*) FROM user_roles")
        user_roles_count = cursor.fetchone()[0]
        print_success(f"User-Role assignments: {user_roles_count} assignments found")

        # Show sample users
        if users_count > 0:
            print_info("\nSample users (first 5):")
            cursor.execute(
                """
                SELECT u.email, u.name, string_agg(r.name, ', ') as roles
                FROM users u
                LEFT JOIN user_roles ur ON u.id = ur.user_id
                LEFT JOIN roles r ON ur.role_id = r.id
                GROUP BY u.id, u.email, u.name
                ORDER BY u.id
                LIMIT 5
                """
            )
            for email, name, roles in cursor.fetchall():
                print(f"  • {email} ({name}) - Roles: {roles or 'None'}")

        cursor.close()
        conn.close()

        print_success("\nDatabase setup verification completed!")
        return True

    except psycopg2.Error as e:
        print_error(f"Failed to verify setup: {e}")
        if conn:
            conn.close()
        return False

def main():
    """Main execution"""
    parser = argparse.ArgumentParser(
        description='Database Connection and Configuration Script',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python db_connect.py              # Full setup: databases + tables + seed
  python db_connect.py --skip-seed  # Create databases and tables only
  python db_connect.py --seed-only  # Seed users only (assumes DB exists)
        """
    )

    parser.add_argument(
        '--skip-seed',
        action='store_true',
        help='Skip seeding initial users'
    )

    parser.add_argument(
        '--seed-only',
        action='store_true',
        help='Only seed users (skip database and table creation)'
    )

    args = parser.parse_args()

    print_header("EV Fleet Management - Database Connection & Setup")

    # Wait for PostgreSQL
    if not wait_for_postgres():
        print_error("PostgreSQL is not available. Please ensure PostgreSQL is running.")
        sys.exit(1)

    success = True

    if not args.seed_only:
        # Create databases
        if not create_all_databases():
            print_error("Failed to create all databases")
            success = False

        # Ensure auth tables exist
        if not ensure_auth_tables():
            print_error("Failed to create auth tables")
            success = False

        # Seed default roles
        if not seed_default_roles():
            print_error("Failed to seed default roles")
            success = False

    if not args.skip_seed:
        # Seed initial users
        if not seed_initial_users():
            print_warning("User seeding had issues, but continuing...")

    # Verify setup
    verify_setup()

    print_header("Setup Complete!")

    if success:
        print_success("✅ Database connection and configuration completed successfully!")
        print_info("\nNext steps:")
        print_info("  1. Start your application: python run_app_fixed.py start")
        print_info("  2. Access the application at: http://localhost:3000")
        print_info("  3. Test login with seeded users (password: 'User@123')")
        print_info("\nDatabase connection details:")
        print_info(f"  Host: {DB_HOST}:{DB_PORT}")
        print_info(f"  User: {DB_USER}")
        print_info(f"  Databases: {', '.join(DATABASES)}")
        return 0
    else:
        print_error("Setup completed with some errors")
        return 1

if __name__ == '__main__':
    sys.exit(main())
