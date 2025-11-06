# Authentication Flow Documentation

## Overview

The SEV EV Fleet Management Platform uses a **hybrid authentication system** combining Firebase Authentication (frontend) with a PostgreSQL database (backend) for user management.

## How It Works

### 1. **Firebase Authentication** (Frontend)
- Users sign in using Firebase (Email/Password or Google OAuth)
- Firebase returns a Firebase User object with a unique `firebaseUid`
- Firebase also provides an authentication token (JWT)

### 2. **Database Synchronization** (Backend)
- The frontend calls `/api/auth/sync` endpoint with Firebase user details
- The backend **automatically checks** if the user exists in the database
- If user doesn't exist, it **creates a new user record**
- If user exists, it returns the existing user data

### 3. **User Data Flow**

```
┌─────────────────┐
│  User Login     │
│  (Firebase)     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Frontend calls:                    │
│  authService.syncUser(firebaseUser) │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  POST /api/auth/sync                │
│  {                                  │
│    firebaseUid: "abc123...",        │
│    email: "user@example.com",       │
│    name: "John Doe",                │
│    phone: "+1234567890"             │
│  }                                  │
└────────┬────────────────────────────┘
         │
         ▼
┌──────────────────────────────────────┐
│  Backend checks:                     │
│  1. User exists by firebaseUid?      │
│     ✓ Yes → Return existing user     │
│     ✗ No  → Continue to step 2       │
│                                      │
│  2. User exists by email?            │
│     ✓ Yes → Link Firebase UID        │
│     ✗ No  → Continue to step 3       │
│                                      │
│  3. Create new user                  │
│     - Save to database               │
│     - Assign default role            │
│     - Return new user                │
└──────────────────────────────────────┘
```

## Key Files

### Frontend
- **`src/redux/slices/authSlice.ts`** - Redux actions for login/register
  - `loginWithEmail()` - Email/password login
  - `loginWithGoogle()` - Google OAuth login
  - Both call `authService.syncUser()` after Firebase authentication

- **`src/services/authService.ts`** - API service layer
  - `syncUser()` - Calls `/api/auth/sync` endpoint
  - Automatically creates user if doesn't exist

- **`src/services/firebase.ts`** - Firebase SDK wrapper
  - `signIn()` - Email/password authentication
  - `signInWithGoogle()` - Google OAuth authentication

### Backend
- **`AuthController.java`** - REST endpoints
  - `POST /api/auth/sync` - Sync Firebase user to database
  - `POST /api/auth/register` - Manual registration
  - `POST /api/auth/login` - Token verification + user lookup

- **`UserServiceImpl.java`** - Business logic
  - `syncUser()` - **Smart sync logic**:
    1. Check if user exists by Firebase UID
    2. Check if user exists by email
    3. Create new user if neither exists
  
- **`User.java`** - Entity model
  - `firebaseUid` - Unique Firebase identifier (required, indexed)
  - `email` - User email (required, unique, indexed)
  - `name`, `phone`, `companyId`, `companyName` - Optional fields

## Authentication Scenarios

### Scenario 1: New User Signs Up with Google
```
1. User clicks "Sign in with Google"
2. Firebase authenticates → Returns firebaseUid="xyz789"
3. Frontend calls /api/auth/sync
4. Backend: User not found by firebaseUid OR email
5. Backend creates NEW user in database
6. User is logged in ✓
```

### Scenario 2: Existing User Signs In with Google
```
1. User clicks "Sign in with Google"
2. Firebase authenticates → Returns firebaseUid="xyz789"
3. Frontend calls /api/auth/sync
4. Backend: User FOUND by firebaseUid
5. Backend returns existing user data
6. User is logged in ✓
```

### Scenario 3: User Created via Email/Password, Now Uses Google
```
1. User previously registered with email/password (no firebaseUid)
2. User clicks "Sign in with Google" with SAME email
3. Firebase authenticates → Returns NEW firebaseUid="abc123"
4. Frontend calls /api/auth/sync
5. Backend: User not found by firebaseUid, but FOUND by email
6. Backend LINKS firebaseUid to existing user record
7. User is logged in with same account ✓
```

## API Endpoints

### POST /api/auth/sync
**Purpose:** Sync Firebase user with database (create or update)

**Request Body:**
```json
{
  "firebaseUid": "abc123xyz789",
  "email": "user@example.com",
  "name": "John Doe",
  "phone": "+1234567890",
  "companyId": null,
  "companyName": null
}
```

**Response:**
```json
{
  "success": true,
  "message": "User already exists in database",
  "user": {
    "id": 1,
    "firebaseUid": "abc123xyz789",
    "email": "user@example.com",
    "name": "John Doe",
    "phone": "+1234567890",
    "companyId": null,
    "companyName": null,
    "active": true,
    "emailVerified": true,
    "roles": ["FLEET_MANAGER"],
    "createdAt": "2025-11-06T10:30:00",
    "lastLogin": "2025-11-06T15:45:00"
  }
}
```

### POST /api/auth/register
**Purpose:** Manual user registration (alternative to sync)

**Request Body:** Same as /sync

**Response:** Same as /sync

### POST /api/auth/login
**Purpose:** Verify Firebase token and retrieve user data

**Request Body:**
```json
{
  "firebaseUid": "abc123xyz789",
  "firebaseToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** User data with verified token

### GET /api/auth/me
**Purpose:** Get current user from Authorization header

**Headers:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:** User data

## Database Schema

### users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    company_id BIGINT,
    company_name VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    profile_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login TIMESTAMP
);

CREATE INDEX idx_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_company_id ON users(company_id);
```

### user_roles Table (Many-to-Many)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

## Default User Role

All new users are automatically assigned the **FLEET_MANAGER** role.

Roles are defined in the `roles` table:
- `FLEET_MANAGER` - Default role for all users
- `ADMIN` - System administrators
- `DRIVER` - Vehicle drivers
- `MAINTENANCE` - Maintenance staff

## Error Handling

### Frontend
- Invalid credentials → Show error message
- Network errors → Retry with exponential backoff
- Token expiration → Refresh token automatically

### Backend
- Firebase token verification fails → Return 401 Unauthorized
- User not found (login) → Return "User not found. Please register first."
- Duplicate email (register) → Return "User with this email already exists"
- Default role not found → Return "Default role not configured. Please contact administrator."

## Security Features

1. **Firebase Token Verification** - All requests verify Firebase JWT token
2. **Database-Level Uniqueness** - Email and Firebase UID are unique constraints
3. **Soft Delete** - Users are marked inactive instead of deleted
4. **Role-Based Access Control** - Users assigned roles for authorization
5. **Last Login Tracking** - Track user activity
6. **Indexed Queries** - Fast lookups by Firebase UID and email

## Testing the Flow

### Test Case 1: New User Registration
```bash
# 1. Sign up with Google in frontend
# 2. Check backend logs for:
✓ User not found in database - Creating new user for email: test@example.com
✓ User registered successfully with ID: 5

# 3. Verify database:
psql -U postgres -d evfleet_auth
SELECT * FROM users WHERE email = 'test@example.com';
```

### Test Case 2: Existing User Login
```bash
# 1. Sign in with Google (same user)
# 2. Check backend logs for:
✓ User found by Firebase UID - User ID: 5, Email: test@example.com

# 3. Verify last_login updated:
SELECT id, email, last_login FROM users WHERE email = 'test@example.com';
```

## Configuration

### Frontend (.env)
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_FIREBASE_API_KEY=your_api_key
REACT_APP_FIREBASE_AUTH_DOMAIN=your_auth_domain
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
```

### Backend (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/evfleet_auth
    username: postgres
    password: postgres
```

## Troubleshooting

### Issue: "User not found. Please register first."
**Cause:** User logged in with Firebase but not synced to database
**Solution:** Ensure frontend calls `authService.syncUser()` after Firebase authentication

### Issue: "User with this email already exists"
**Cause:** Trying to register when user already exists
**Solution:** Use `/api/auth/sync` instead of `/api/auth/register`

### Issue: "Default role not configured"
**Cause:** FLEET_MANAGER role missing in database
**Solution:** Run database initialization script to create default roles

### Issue: Firebase token verification fails
**Cause:** Invalid or expired Firebase token
**Solution:** Refresh Firebase token in frontend

## Summary

✅ **The system automatically creates users when they sign in with Firebase**
✅ **No manual user creation required**
✅ **Users can switch between email/password and Google OAuth seamlessly**
✅ **All user data is synchronized between Firebase and PostgreSQL**
✅ **Comprehensive logging for debugging authentication issues**

The `/api/auth/sync` endpoint is the **key component** that ensures users are automatically created in the database when they authenticate with Firebase.
