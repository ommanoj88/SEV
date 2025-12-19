# Customizing Initial Users

This guide shows you how to customize the initial users that are seeded by `db_connect.py`.

## Default Users

The `db_connect.py` script seeds 20 initial users by default. You can customize these users by editing the `INITIAL_USERS` list in `db_connect.py`.

## User Data Structure

Each user in the `INITIAL_USERS` list has the following structure:

```python
{
    'firebase_uid': 'unique_firebase_uid',  # Placeholder - will be replaced when user registers
    'email': 'user@example.com',            # Must be unique
    'name': 'Full Name',                     # Display name
    'first_name': 'First',                   # First name
    'last_name': 'Last',                     # Last name
    'phone': '+91-1234567890',              # Phone number
    'company_name': 'Company Name',          # Company/Organization
    'role_names': ['ROLE_DRIVER']           # List of roles (can have multiple)
}
```

## Available Roles

The following roles are available:

- `ROLE_ADMIN` - Administrator with full system access
- `ROLE_SUPER_ADMIN` - Super Administrator with all permissions
- `ROLE_FLEET_MANAGER` - Fleet Manager for managing vehicles and drivers
- `ROLE_DRIVER` - Driver role for drivers
- `ROLE_MAINTENANCE_MANAGER` - Maintenance Manager for vehicle maintenance
- `ROLE_ANALYST` - Analyst for data analysis and reporting
- `ROLE_SUPPORT` - Support team member for customer support
- `ROLE_USER` - Regular user with basic access

## Example: Adding a New User

To add a new user, add an entry to the `INITIAL_USERS` list in `db_connect.py`:

```python
{
    'firebase_uid': 'user_021_firebase_uid',  # Increment the number
    'email': 'newuser@evfleet.com',
    'name': 'New User',
    'first_name': 'New',
    'last_name': 'User',
    'phone': '+91-9876543221',
    'company_name': 'EV Fleet HQ',
    'role_names': ['ROLE_USER']
}
```

## Example: Changing User Roles

To give a user multiple roles:

```python
{
    'firebase_uid': 'user_001_firebase_uid',
    'email': 'admin@evfleet.com',
    'name': 'Admin User',
    'first_name': 'Admin',
    'last_name': 'User',
    'phone': '+91-9876543201',
    'company_name': 'EV Fleet HQ',
    'role_names': ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_ANALYST']  # Multiple roles
}
```

## Example: Changing Company Names

To organize users by different companies:

```python
# Mumbai office users
{
    'firebase_uid': 'user_001_firebase_uid',
    'email': 'driver.mumbai@evfleet.com',
    'name': 'Mumbai Driver',
    'first_name': 'Mumbai',
    'last_name': 'Driver',
    'phone': '+91-9876543201',
    'company_name': 'Mumbai Fleet Operations',
    'role_names': ['ROLE_DRIVER']
},

# Delhi office users
{
    'firebase_uid': 'user_002_firebase_uid',
    'email': 'driver.delhi@evfleet.com',
    'name': 'Delhi Driver',
    'first_name': 'Delhi',
    'last_name': 'Driver',
    'phone': '+91-9876543202',
    'company_name': 'Delhi Fleet Operations',
    'role_names': ['ROLE_DRIVER']
}
```

## Example: Creating a Template for Your Organization

Here's a template you can use to create users for your organization:

```python
# Template: Add these to INITIAL_USERS in db_connect.py

# Company: Your Company Name
# Department: Management
{
    'firebase_uid': 'user_XXX_firebase_uid',  # Replace XXX with unique number
    'email': 'manager@yourcompany.com',
    'name': 'Manager Name',
    'first_name': 'Manager',
    'last_name': 'Name',
    'phone': '+91-XXXXXXXXXX',
    'company_name': 'Your Company Name',
    'role_names': ['ROLE_FLEET_MANAGER']
},

# Department: Operations - Drivers
{
    'firebase_uid': 'user_XXX_firebase_uid',
    'email': 'driver1@yourcompany.com',
    'name': 'Driver One',
    'first_name': 'Driver',
    'last_name': 'One',
    'phone': '+91-XXXXXXXXXX',
    'company_name': 'Your Company Name',
    'role_names': ['ROLE_DRIVER']
},

# Department: Maintenance
{
    'firebase_uid': 'user_XXX_firebase_uid',
    'email': 'maintenance@yourcompany.com',
    'name': 'Maintenance Manager',
    'first_name': 'Maintenance',
    'last_name': 'Manager',
    'phone': '+91-XXXXXXXXXX',
    'company_name': 'Your Company Name',
    'role_names': ['ROLE_MAINTENANCE_MANAGER']
}
```

## Tips

1. **Email Uniqueness**: Each email must be unique across all users
2. **Firebase UID**: Use placeholder UIDs for initial seeding (format: `user_XXX_firebase_uid`)
3. **Phone Format**: Use international format with country code (e.g., `+91-XXXXXXXXXX`)
4. **Role Names**: Must match exactly (case-sensitive) with the available roles
5. **Company Names**: Group users by company/department for better organization

## After Customization

After customizing the users:

1. Save the changes to `db_connect.py`
2. Run the script:
   ```bash
   python db_connect.py
   ```
3. Or if you've already run it once:
   ```bash
   # Delete existing users from database first
   psql -h localhost -U postgres -d evfleet_auth -c "TRUNCATE users CASCADE;"
   
   # Then re-seed
   python db_connect.py --seed-only
   ```

## Production Considerations

⚠️ **Important**: The seeded users are for development and testing only.

For production:
- Don't seed test users
- Have real users register through Firebase authentication
- Use environment variables for all configuration
- Update Firebase UIDs with real values from your Firebase console

## Example: Minimal Production Setup

For production, you might only want to seed a single admin user:

```python
INITIAL_USERS = [
    {
        'firebase_uid': 'your_actual_firebase_uid_from_firebase_console',
        'email': 'admin@yourcompany.com',
        'name': 'System Administrator',
        'first_name': 'System',
        'last_name': 'Administrator',
        'phone': '+91-XXXXXXXXXX',
        'company_name': 'Your Company Name',
        'role_names': ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
    }
]
```

Then have all other users register through the application.
