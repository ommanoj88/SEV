#!/usr/bin/env python3
"""
Seed default roles for EVFleet Auth database
"""

import psycopg2
from datetime import datetime

DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'user': 'postgres',
    'password': 'Shobharain11@',
    'database': 'evfleet_auth'
}

ROLES = [
    (1, 'SUPER_ADMIN', 'Super administrator with system-level access'),
    (2, 'ADMIN', 'Administrator with full access'),
    (3, 'FLEET_MANAGER', 'Fleet manager with vehicle management access'),
    (4, 'DRIVER', 'Driver with limited access to assigned vehicles'),
    (5, 'VIEWER', 'Viewer with read-only access'),
]

def seed_roles():
    try:
        # Connect to database
        print(f"Connecting to database: {DB_CONFIG['database']}...")
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()

        # Check if roles already exist
        cursor.execute("SELECT COUNT(*) FROM roles;")
        count = cursor.fetchone()[0]
        print(f"Current roles count: {count}")

        if count > 0:
            print("Roles already exist. Clearing...")
            cursor.execute("TRUNCATE TABLE roles CASCADE;")
            print("Roles table cleared.")

        # Insert default roles
        print("\nInserting default roles...")
        now = datetime.now()

        for role_id, name, description in ROLES:
            cursor.execute(
                """
                INSERT INTO roles (id, name, description, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s)
                """,
                (role_id, name, description, now, now)
            )
            print(f"  [OK] Inserted: {name}")

        # Reset sequence
        cursor.execute("SELECT setval('roles_id_seq', 5, true);")

        # Commit changes
        conn.commit()
        print("\n[SUCCESS] Default roles seeded successfully!")

        # Verify
        cursor.execute("SELECT id, name, description FROM roles ORDER BY id;")
        roles = cursor.fetchall()
        print("\nVerification - Roles in database:")
        for role in roles:
            print(f"  {role[0]}: {role[1]} - {role[2]}")

        cursor.close()
        conn.close()

    except Exception as e:
        print(f"[ERROR] {e}")
        return False

    return True

if __name__ == '__main__':
    success = seed_roles()
    exit(0 if success else 1)
