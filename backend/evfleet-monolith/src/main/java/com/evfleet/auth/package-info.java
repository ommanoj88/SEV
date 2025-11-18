/**
 * Authentication and Authorization Module
 *
 * Handles user authentication, registration, and authorization.
 * Integrates with Firebase Authentication for token-based auth.
 *
 * <h2>Module Responsibilities</h2>
 * - User registration and login
 * - Firebase token verification
 * - Role-based access control (RBAC)
 * - User profile management
 * - Session management
 *
 * <h2>API Endpoints</h2>
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login user
 * - POST /api/auth/refresh - Refresh authentication token
 * - GET /api/auth/me - Get current user profile
 * - PUT /api/auth/profile - Update user profile
 *
 * <h2>Events Published</h2>
 * - UserRegisteredEvent - When a new user registers
 * - UserLoggedInEvent - When a user logs in
 * - UserProfileUpdatedEvent - When user profile is updated
 *
 * <h2>Module Dependencies</h2>
 * - Common module (for exceptions, DTOs, events)
 * - Firebase Admin SDK (for authentication)
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Authentication Module",
        allowedDependencies = {"common"}
)
package com.evfleet.auth;
