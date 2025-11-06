# Authentication Flow Changes - Backend Sync Required

## Overview

The EV Fleet Management Platform now **requires backend database synchronization** for user authentication. Users can no longer access the application with Firebase authentication alone - they must be synced to the backend database.

## What Changed

### 1. Database Migrations (Flyway)

- **Added Flyway** to `auth-service` for database schema management
- **Migration Files Created**:
  - `V1__create_auth_tables.sql` - Creates `users`, `roles`, and `user_roles` tables
  - `V2__seed_default_roles.sql` - Seeds default roles (SUPER_ADMIN, ADMIN, FLEET_MANAGER, DRIVER, VIEWER)

### 2. Database Reset Script

- **Created `reset_database.py`** - A comprehensive script to:
  - Drop all existing databases
  - Create fresh databases
  - Run Flyway migrations
  - Seed default roles
  - Verify schema creation

### 3. Frontend Authentication Enforcement

- **Updated `useAuth` hook** to:
  - Require successful backend sync before allowing access
  - Log out users automatically if backend sync fails
  - Show clear error messages when backend is unavailable
  - Retry with exponential backoff when backend is temporarily unavailable

- **Updated `App.tsx`** to:
  - Only show authenticated UI when BOTH Firebase user AND backend user exist
  - Check for `user !== null` in addition to `isAuthenticated`

## How It Works Now

### Authentication Flow

1. **User signs in** via Firebase (email/password or Google)
2. **Frontend attempts to sync** user to backend database
3. **If sync succeeds** → User can access the application
4. **If sync fails** → User is logged out with error message

### First Time User Registration

```
User → Firebase Sign Up → Frontend → Backend Sync → Database User Created → Access Granted
```

### Existing User Login

```
User → Firebase Sign In → Frontend → Backend User Lookup → Access Granted (if found)
```

### Failed Sync Scenarios

- **Backend unavailable (503)**: Shows error, logs out user
- **User not found (404)**: Attempts to sync, then grants access
- **Sync fails**: Shows error, logs out user

## Database Schema

### Tables Created

#### `roles`
- `id` - Primary key
- `name` - Role name (SUPER_ADMIN, ADMIN, FLEET_MANAGER, DRIVER, VIEWER)
- `description` - Role description
- `permissions` - JSON permissions

#### `users`
- `id` - Primary key
- `firebase_uid` - Unique Firebase UID (indexed)
- `email` - Unique email (indexed)
- `name` - User full name
- `phone` - Phone number
- `company_id` - Company identifier (indexed)
- `company_name` - Company name
- `active` - Active status
- `email_verified` - Email verification status
- `profile_image_url` - Profile image URL
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp
- `last_login` - Last login timestamp

#### `user_roles`
- `user_id` - Foreign key to users
- `role_id` - Foreign key to roles

### Default Roles

- **SUPER_ADMIN** - Full system access
- **ADMIN** - Administrative access
- **FLEET_MANAGER** - Fleet management access (default for new users)
- **DRIVER** - Driver access
- **VIEWER** - Read-only access

## Usage

### Resetting Database

```bash
# Run the reset script
python reset_database.py

# Or with custom PostgreSQL settings
export DB_HOST=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=your_password
python reset_database.py
```

### Starting the Application

1. **Reset the database** (first time or when needed):
   ```bash
   python reset_database.py
   ```

2. **Start backend services**:
   ```bash
   python run_app.py start
   ```

3. **Register a new user** through the frontend
4. **User is automatically synced** to backend database

### Verifying Database

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d evfleet_auth

# Check tables
\dt

# Check roles
SELECT * FROM roles;

# Check users
SELECT id, email, name, firebase_uid FROM users;
```

## Breaking Changes

⚠️ **BREAKING CHANGE**: Applications that were relying on Firebase-only authentication will no longer work. Backend database sync is now **mandatory**.

### Migration Path for Existing Users

If you have existing Firebase users but no backend database:

1. Run `python reset_database.py` to create the schema
2. Existing users will be automatically synced when they log in
3. Their Firebase data will be used to create backend user records

## Configuration

### Backend (application.yml)

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # Changed from 'none'

flyway:
  enabled: true
  baseline-on-migrate: true
  locations: classpath:db/migration
  schemas: public
```

### Frontend (useAuth.ts)

- Enforces backend sync before allowing access
- Logs out users if sync fails
- Retries with exponential backoff (2s, 4s, 8s)
- Max 3 retry attempts

## Troubleshooting

### "Backend sync required but unavailable"

**Cause**: Backend services are not running or database is not accessible

**Solution**:
1. Ensure PostgreSQL is running
2. Ensure backend services are running
3. Check database connection settings
4. Run `python reset_database.py` if tables don't exist

### "User not found. Please register first."

**Cause**: User exists in Firebase but not in backend database

**Solution**:
- Frontend will automatically attempt to sync the user
- If sync fails, user will be logged out
- Ensure backend is running and database is accessible

### Migration Failures

**Cause**: Database schema conflicts or migration errors

**Solution**:
1. Drop and recreate the database:
   ```bash
   python reset_database.py
   ```
2. If that fails, manually drop the database:
   ```bash
   psql -h localhost -U postgres -c "DROP DATABASE evfleet_auth"
   psql -h localhost -U postgres -c "CREATE DATABASE evfleet_auth"
   ```
3. Run migrations manually:
   ```bash
   cd backend/auth-service
   mvn flyway:migrate
   ```

## Benefits

✅ **Data Integrity**: User data is consistent between Firebase and backend
✅ **RBAC Support**: Role-based access control is properly enforced
✅ **Audit Trail**: All user activities are tracked in backend database
✅ **Scalability**: Backend can manage complex business logic and relationships
✅ **Security**: Backend validates all operations, not just Firebase authentication

## Future Enhancements

- [ ] Add database migration rollback support
- [ ] Add user import/export functionality
- [ ] Add bulk user management tools
- [ ] Add audit logging for authentication events
- [ ] Add support for other database providers (MySQL, etc.)

## See Also

- [Database Reset Guide](DATABASE_RESET_GUIDE.md)
- [Firebase Setup Guide](backend/auth-service/FIREBASE-SETUP.md)
- [Running the Application](RUN_APP_GUIDE.md)
