# Test User Setup Guide

## Overview
This guide explains how to create and use test user credentials for the EV Fleet Management System chaos testing.

## Test User Credentials

### Pre-configured Test User
```
Email: test@evfleet.com
Password: Test@1234567
Company ID: 1
Role: TEST_ADMIN
```

### Test Service Account (for API testing)
```
Email: testapi@evfleet.com
Password: Api@1234567
Company ID: 1
Role: SERVICE_ACCOUNT
```

## Option 1: Using Firebase (Current Setup)

### Step 1: Register Test User in Firebase
1. Go to Firebase Console: https://console.firebase.google.com/
2. Select your project
3. Go to Authentication → Users
4. Click "Add User"
5. Enter:
   - Email: test@evfleet.com
   - Password: Test@1234567
6. Click "Create User"

### Step 2: Get Firebase ID Token
Run this command in your terminal:

```bash
curl -X POST "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_FIREBASE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@evfleet.com",
    "password": "Test@1234567",
    "returnSecureToken": true
  }'
```

This will return:
```json
{
  "idToken": "YOUR_ID_TOKEN",
  "email": "test@evfleet.com",
  ...
}
```

### Step 3: Use Token in Chaos Tests
Copy the `idToken` and use it in tests:

```python
auth_header = {"Authorization": f"Bearer {idToken}"}
response = requests.get(
    "http://localhost:8080/api/v1/vehicles",
    headers=auth_header
)
```

---

## Option 2: Direct Database Insert (For Testing Only)

If Firebase is not configured, you can create a test user directly in the database:

### Step 1: Connect to PostgreSQL
```bash
psql -h localhost -p 5432 -U evfleet -d fleet_db
```

### Step 2: Insert Test User
```sql
-- In fleet_db
INSERT INTO users (id, email, password_hash, name, role, status, created_at)
VALUES (
  'test-user-001',
  'test@evfleet.com',
  '$2a$10$YOUR_HASHED_PASSWORD',  -- Use bcrypt hash
  'Test User',
  'ADMIN',
  'ACTIVE',
  NOW()
);

INSERT INTO companies (id, name, created_at)
VALUES (1, 'Test Company', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO company_users (user_id, company_id, role)
VALUES ('test-user-001', 1, 'ADMIN');
```

---

## Option 3: Create Test Token Manually

If you have a local token generation service:

```bash
curl -X POST http://localhost:8081/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@evfleet.com",
    "password": "Test@1234567"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "refreshToken": "..."
}
```

---

## Using Test User in Chaos Tests

### Manual Test
```bash
# Get token
TOKEN=$(curl -X POST http://localhost:8081/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"email":"test@evfleet.com","password":"Test@1234567"}' | jq -r '.token')

# Use token
curl -X GET http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer $TOKEN"
```

### Python Script
```python
import requests

# Get token
auth_response = requests.post(
    "http://localhost:8081/api/auth/token",
    json={
        "email": "test@evfleet.com",
        "password": "Test@1234567"
    }
)

token = auth_response.json()['token']

# Make authenticated request
headers = {"Authorization": f"Bearer {token}"}
response = requests.get(
    "http://localhost:8080/api/v1/vehicles",
    headers=headers
)
```

---

## Test User Roles

| Role | Permissions | Use Case |
|------|-------------|----------|
| TEST_ADMIN | All operations | Complete testing |
| SERVICE_ACCOUNT | API operations only | CI/CD testing |
| TEST_USER | Read operations only | Basic functionality tests |

---

## Automatic Token Refresh

For long-running tests, implement token refresh:

```python
def get_valid_token():
    """Get or refresh token if expired"""
    global token, token_expires_at

    if token and time.time() < token_expires_at - 60:
        return token

    response = requests.post(
        "http://localhost:8081/api/auth/token",
        json={
            "email": "test@evfleet.com",
            "password": "Test@1234567"
        }
    )

    data = response.json()
    token = data['token']
    token_expires_at = time.time() + data['expiresIn']
    return token
```

---

## Test User Cleanup

After testing, you can delete the test user:

### Firebase
```bash
# Get user UID first
firebase auth:export users.json

# Delete user
firebase auth:delete [UID]
```

### Database
```sql
DELETE FROM company_users WHERE user_id = 'test-user-001';
DELETE FROM users WHERE id = 'test-user-001';
```

---

## Troubleshooting

### 401 Unauthorized
- Token is invalid or expired
- User doesn't exist in database
- User is not assigned to a company
- Authorization header is malformed (must be `Bearer TOKEN`)

### 403 Forbidden
- User doesn't have permission for this operation
- User's role doesn't match endpoint requirements

### Token Expired
- Token has expired (check token_expires_at)
- Refresh token to get new one

---

## Quick Start for Chaos Testing

1. **Create test user** (Option 1, 2, or 3 above)
2. **Get token** for test@evfleet.com
3. **Update chaos test scripts** with token
4. **Run chaos tests**:
   ```bash
   cd tests/chaos-testing
   python run_all_chaos_tests.py
   ```

---

## Configuration for CI/CD

For automated testing in CI/CD pipeline:

```yaml
# .github/workflows/chaos-test.yml
env:
  TEST_EMAIL: test@evfleet.com
  TEST_PASSWORD: Test@1234567
  API_GATEWAY_URL: http://localhost:8080

jobs:
  chaos-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Get Auth Token
        run: |
          TOKEN=$(curl -X POST http://localhost:8081/api/auth/token \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}" | jq -r '.token')
          echo "AUTH_TOKEN=$TOKEN" >> $GITHUB_ENV

      - name: Run Chaos Tests
        run: |
          cd tests/chaos-testing
          python run_all_chaos_tests.py --token $AUTH_TOKEN
```

---

## Security Notes

⚠️ **Never commit real passwords to version control**
- Use environment variables for sensitive data
- Rotate test credentials regularly
- Keep test credentials different from production
- Delete test users after testing in production-like environments

---

## Next Steps

1. Create test user using Option 1, 2, or 3
2. Update `tests/chaos-testing/run_all_chaos_tests.py` to use test credentials
3. Run chaos tests with authentication
4. Monitor test results
5. Fix any remaining issues

For detailed testing instructions, see `CHAOS_TESTING_DOCUMENTATION.md`
