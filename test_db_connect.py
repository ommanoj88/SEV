#!/usr/bin/env python3
"""
Test script for db_connect.py to verify the structure and logic
This test can run without a PostgreSQL connection
"""

import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Import the db_connect module
import db_connect

def test_constants():
    """Test that all constants are properly defined"""
    print("Testing constants...")
    
    # Check database list
    assert len(db_connect.DATABASES) == 8, f"Expected 8 databases, got {len(db_connect.DATABASES)}"
    assert 'evfleet_auth' in db_connect.DATABASES
    assert 'evfleet_fleet' in db_connect.DATABASES
    print(f"✓ Database list contains {len(db_connect.DATABASES)} databases")
    
    # Check roles
    assert len(db_connect.DEFAULT_ROLES) == 8, f"Expected 8 roles, got {len(db_connect.DEFAULT_ROLES)}"
    role_names = [role[0] for role in db_connect.DEFAULT_ROLES]
    assert 'ROLE_ADMIN' in role_names
    assert 'ROLE_DRIVER' in role_names
    print(f"✓ Default roles contains {len(db_connect.DEFAULT_ROLES)} roles")
    
    # Check initial users
    assert len(db_connect.INITIAL_USERS) == 20, f"Expected 20 users, got {len(db_connect.INITIAL_USERS)}"
    print(f"✓ Initial users list contains {len(db_connect.INITIAL_USERS)} users")
    
    print("\n[PASS] All constants are correctly defined\n")

def test_user_data_structure():
    """Test that user data has the required structure"""
    print("Testing user data structure...")
    
    required_fields = ['firebase_uid', 'email', 'name', 'first_name', 'last_name', 
                       'phone', 'company_name', 'role_names']
    
    for i, user in enumerate(db_connect.INITIAL_USERS):
        for field in required_fields:
            assert field in user, f"User {i} missing field: {field}"
        
        # Check email format
        assert '@' in user['email'], f"Invalid email format: {user['email']}"
        
        # Check role_names is a list
        assert isinstance(user['role_names'], list), f"role_names should be a list for user {user['email']}"
        assert len(user['role_names']) > 0, f"User {user['email']} has no roles"
        
        # Verify all role names are valid
        valid_role_names = [role[0] for role in db_connect.DEFAULT_ROLES]
        for role_name in user['role_names']:
            assert role_name in valid_role_names, f"Invalid role {role_name} for user {user['email']}"
    
    print(f"✓ All {len(db_connect.INITIAL_USERS)} users have valid data structure")
    print("\n[PASS] User data structure is correct\n")

def test_user_diversity():
    """Test that users have diverse roles and companies"""
    print("Testing user diversity...")
    
    # Collect unique emails
    emails = [user['email'] for user in db_connect.INITIAL_USERS]
    assert len(emails) == len(set(emails)), "Duplicate email addresses found"
    print(f"✓ All {len(emails)} emails are unique")
    
    # Collect unique companies
    companies = set(user['company_name'] for user in db_connect.INITIAL_USERS)
    assert len(companies) >= 3, f"Expected at least 3 different companies, got {len(companies)}"
    print(f"✓ Users spread across {len(companies)} different companies: {', '.join(companies)}")
    
    # Collect role distribution
    role_distribution = {}
    for user in db_connect.INITIAL_USERS:
        for role in user['role_names']:
            role_distribution[role] = role_distribution.get(role, 0) + 1
    
    print(f"✓ Role distribution:")
    for role, count in sorted(role_distribution.items()):
        print(f"  - {role}: {count} users")
    
    # Verify we have at least one user for each major role
    assert role_distribution.get('ROLE_ADMIN', 0) > 0, "No ADMIN users"
    assert role_distribution.get('ROLE_DRIVER', 0) > 0, "No DRIVER users"
    assert role_distribution.get('ROLE_FLEET_MANAGER', 0) > 0, "No FLEET_MANAGER users"
    
    print("\n[PASS] User diversity is good\n")

def test_environment_variables():
    """Test environment variable handling"""
    print("Testing environment variable handling...")
    
    # Check defaults
    assert db_connect.DB_HOST == os.getenv('DB_HOST', 'localhost')
    assert db_connect.DB_PORT == os.getenv('DB_PORT', '5432')
    assert db_connect.DB_USER == os.getenv('DB_USER', 'postgres')
    
    print("✓ Environment variables are properly handled with defaults")
    print(f"  - DB_HOST: {db_connect.DB_HOST}")
    print(f"  - DB_PORT: {db_connect.DB_PORT}")
    print(f"  - DB_USER: {db_connect.DB_USER}")
    
    print("\n[PASS] Environment variables are correctly configured\n")

def test_print_functions():
    """Test that print functions work"""
    print("Testing print functions...")
    
    db_connect.print_header("Test Header")
    db_connect.print_success("Test Success")
    db_connect.print_info("Test Info")
    db_connect.print_warning("Test Warning")
    db_connect.print_error("Test Error")
    
    print("\n[PASS] All print functions work\n")

def main():
    """Run all tests"""
    print("="*80)
    print("Running db_connect.py validation tests")
    print("="*80)
    print()
    
    try:
        test_constants()
        test_user_data_structure()
        test_user_diversity()
        test_environment_variables()
        test_print_functions()
        
        print("="*80)
        print("✅ ALL TESTS PASSED")
        print("="*80)
        print()
        print("Summary:")
        print(f"  - {len(db_connect.DATABASES)} databases configured")
        print(f"  - {len(db_connect.DEFAULT_ROLES)} default roles defined")
        print(f"  - {len(db_connect.INITIAL_USERS)} initial users ready for seeding")
        print(f"  - Default password: User@123")
        print()
        print("The db_connect.py script is ready to use!")
        print("Run 'python db_connect.py' when PostgreSQL is available")
        return 0
        
    except AssertionError as e:
        print("="*80)
        print(f"❌ TEST FAILED: {e}")
        print("="*80)
        return 1
    except Exception as e:
        print("="*80)
        print(f"❌ ERROR: {e}")
        print("="*80)
        import traceback
        traceback.print_exc()
        return 1

if __name__ == '__main__':
    sys.exit(main())
