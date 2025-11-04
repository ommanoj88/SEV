# Firebase Setup Guide for Auth Service

## Overview
The Auth Service uses Firebase Admin SDK for user authentication and token verification.

## Setup Instructions

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Enable Authentication in the Firebase Console

### 2. Generate Service Account Key

1. In Firebase Console, go to **Project Settings** (gear icon)
2. Navigate to **Service Accounts** tab
3. Click **Generate New Private Key**
4. Download the JSON file

### 3. Configure the Service

#### Option 1: Local Development (File-based)
1. Save the downloaded JSON file as `firebase-service-account.json`
2. Place it in `src/main/resources/` directory
3. Add to `.gitignore` to prevent committing sensitive data:
   ```
   **/firebase-service-account.json
   ```

#### Option 2: Environment Variable
1. Set the Firebase config as an environment variable:
   ```bash
   export FIREBASE_CONFIG_FILE=/path/to/firebase-service-account.json
   ```

#### Option 3: Google Cloud (Application Default Credentials)
If running on Google Cloud Platform, the service will automatically use Application Default Credentials.

### 4. Environment Variables

The following environment variables can be configured:

```bash
# Firebase Configuration
FIREBASE_CONFIG_FILE=firebase-service-account.json
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=evfleet_auth
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Eureka Configuration
EUREKA_ENABLED=true
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# Logging
LOG_LEVEL=DEBUG
```

### 5. Docker Configuration

When running in Docker, you can:

1. **Mount the file as a volume:**
   ```bash
   docker run -v /path/to/firebase-service-account.json:/app/firebase-service-account.json auth-service
   ```

2. **Build with the file:**
   Uncomment the COPY line in Dockerfile:
   ```dockerfile
   COPY firebase-service-account.json /app/firebase-service-account.json
   ```

3. **Use environment variable:**
   ```bash
   docker run -e FIREBASE_CONFIG_FILE=/app/config/firebase.json auth-service
   ```

### 6. Testing Without Firebase

The service will start even if Firebase is not configured, but authentication features will not work. You'll see a warning in the logs:

```
Firebase Admin SDK not initialized. Authentication features will not work.
```

## Security Best Practices

1. **Never commit** the Firebase service account key to version control
2. **Use environment variables** or secrets management in production
3. **Restrict permissions** on the service account key file (chmod 600)
4. **Rotate keys** regularly
5. **Use different Firebase projects** for development, staging, and production

## Troubleshooting

### Error: "Firebase config file not found"
- Verify the file path is correct
- Check file permissions
- Ensure the file is in the classpath or specified path

### Error: "Failed to initialize Firebase"
- Verify the JSON file is valid
- Check that the service account has proper permissions
- Ensure the Firebase project exists

### Error: "Invalid Firebase token"
- Verify the token is from the correct Firebase project
- Check that the token hasn't expired
- Ensure the client app is using the same Firebase project

## Development vs Production

### Development
- Use local file configuration
- Enable debug logging
- Use test Firebase project

### Production
- Use secrets management (AWS Secrets Manager, Google Secret Manager, etc.)
- Use Application Default Credentials on cloud platforms
- Disable debug logging
- Use separate Firebase project

## References

- [Firebase Admin SDK Setup](https://firebase.google.com/docs/admin/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Service Account Documentation](https://cloud.google.com/iam/docs/service-accounts)
