# Offline Mode and Troubleshooting Guide

## Overview

The EV Fleet Management Platform is designed to handle scenarios where the backend services are unavailable while still providing a functional user experience through Firebase authentication and cached data.

## Authentication Flow

### Normal Operation (Backend Available)

1. User signs in with Firebase (Email/Password or Google)
2. Firebase authenticates the user and provides a token
3. Frontend syncs the Firebase user with the backend database
4. User profile is loaded from the backend
5. All features are fully functional

### Offline Mode (Backend Unavailable)

1. User signs in with Firebase (Email/Password or Google)
2. Firebase authenticates the user successfully
3. Frontend attempts to sync with backend but fails
4. User sees a notification: "You are signed in with Firebase. Backend sync pending..."
5. Profile page displays Firebase user data (limited information)
6. Dashboard shows mock/cached data where available

## Common Issues and Solutions

### Issue 1: Cross-Origin-Opener-Policy Warnings

**Symptoms:**
- Console shows: "Cross-Origin-Opener-Policy policy would block the window.closed call"
- Google Sign-in popup still works correctly

**Explanation:**
- These warnings are expected when using Firebase Google Sign-in with popups
- They don't affect functionality
- The warnings are suppressed in production mode

**Solution:**
- No action needed - this is normal behavior
- The application includes proper COOP headers to allow popups

### Issue 2: Profile Page Not Loading

**Symptoms:**
- Profile page shows loading spinner indefinitely
- Console shows errors about backend unavailability

**Cause:**
- Backend services are not running
- Network connectivity issues

**Solutions:**

1. **Check Backend Services:**
   ```bash
   # Check if backend is running
   python run_app.py status
   
   # Start backend services
   python run_app.py start
   ```

2. **Verify in Offline Mode:**
   - Profile page should show Firebase user data
   - Warning banner should appear: "Backend services are unavailable. Showing Firebase profile data only."
   - Limited profile editing is available

### Issue 3: "ERR_EMPTY_RESPONSE" Errors

**Symptoms:**
- Console shows multiple "ERR_EMPTY_RESPONSE" errors
- API calls to `/api/v1/auth/me`, `/api/v1/auth/sync` fail

**Cause:**
- Backend API Gateway is not responding (usually port 8080)
- Services haven't started yet

**Solutions:**

1. **Wait for Services to Start:**
   - Backend services may take 1-2 minutes to fully start
   - Watch for the toast notification: "Backend services are starting up. Please wait..."

2. **Start Backend Manually:**
   ```bash
   cd /home/runner/work/SEV/SEV
   python run_app.py start
   ```

3. **Check Service Health:**
   - API Gateway: http://localhost:8080/actuator/health
   - Auth Service: http://localhost:8081/actuator/health

### Issue 4: Unable to See Dashboard After Login

**Symptoms:**
- Login appears successful (no error shown)
- Dashboard loads but shows no data or errors
- Cannot determine if login was successful

**Cause:**
- Backend services are starting or unavailable
- Silent failures in data fetching

**Solutions:**

1. **Check Toast Notifications:**
   - Look for notifications indicating backend status
   - Green: "Welcome back!" - Full login successful
   - Yellow/Orange: "Backend services unavailable..." - Firebase-only login
   - Red: Authentication error

2. **Verify Authentication State:**
   - Check browser console for `[useAuth]` logs
   - Look for Firebase user info in Redux DevTools
   - Profile page should be accessible even in offline mode

3. **Refresh After Backend Starts:**
   - Once backend services are running, refresh the page
   - User should be automatically synced with backend

## Understanding Toast Notifications

The application uses toast notifications to provide feedback:

### Success Messages (Green)
- "Welcome back!" - Login successful, backend connected

### Info Messages (Blue)
- "You are signed in with Firebase. Backend sync pending..." - Firebase auth successful, backend unavailable

### Warning Messages (Yellow/Orange)
- "Backend services are unavailable. Running in offline mode..." - Persistent backend unavailability
- "Backend services are starting up. Please wait..." - Services initializing
- "Backend services are temporarily unavailable..." - Temporary connectivity issue

### Error Messages (Red)
- Authentication errors from Firebase
- Validation errors
- Unauthorized access attempts

## Offline Mode Capabilities

### Available Features
- ✅ Firebase authentication (Email/Password, Google)
- ✅ View profile with Firebase data
- ✅ View mock notifications and alerts
- ✅ Basic UI navigation

### Limited Features
- ⚠️ Profile updates (requires backend)
- ⚠️ Real-time vehicle data
- ⚠️ Analytics and reports
- ⚠️ Fleet management operations

### Unavailable Features
- ❌ Creating new users in backend
- ❌ Syncing data across devices
- ❌ Real-time updates via WebSocket
- ❌ Backend-dependent operations

## Developer Tips

### Testing Offline Mode

1. **Start Only Frontend:**
   ```bash
   cd frontend
   npm start
   ```

2. **Verify Offline Behavior:**
   - Sign in with Firebase
   - Check profile page shows Firebase data
   - Verify toast notifications appear
   - Confirm mock data is displayed

### Debugging Authentication Issues

1. **Enable Verbose Logging:**
   - Open browser DevTools Console
   - Look for `[useAuth]` prefix logs
   - Check network tab for failed API calls

2. **Check Redux State:**
   - Install Redux DevTools extension
   - Inspect `auth` state
   - Verify `firebaseUser` and `isAuthenticated` flags

3. **Monitor Toast Notifications:**
   - Each toast has a unique `toastId`
   - Prevents duplicate notifications
   - Can be dismissed programmatically

## Best Practices

1. **Always Start Backend Before Frontend in Production**
   - Use `python run_app.py start` to start all services
   - Wait for health checks to pass

2. **Handle Network Failures Gracefully**
   - Application automatically falls back to mock data
   - Users are notified of offline mode
   - Firebase keeps users authenticated

3. **Monitor Console for Warnings**
   - COOP warnings are expected and harmless
   - Network errors indicate backend issues
   - Authentication errors need attention

4. **Use Environment Variables**
   - Ensure `.env` file is configured with Firebase credentials
   - Verify API base URL matches backend port

## Support

If issues persist:

1. Check the `run_app.py` output for service startup errors
2. Verify all required ports are available
3. Ensure Docker services are running (PostgreSQL, Redis, RabbitMQ)
4. Review browser console for specific error messages
5. Check backend service logs in Docker Compose output

For more information, see:
- [RUN_APP_GUIDE.md](../RUN_APP_GUIDE.md) - Application startup guide
- [README.md](../README.md) - Project overview and architecture
- [BACKEND_INTEGRATION_GUIDE.md](../frontend/BACKEND_INTEGRATION_GUIDE.md) - Backend integration details
