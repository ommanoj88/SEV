package com.evfleet.auth.service;

import com.evfleet.auth.dto.AuthResponse;
import com.evfleet.auth.dto.LoginRequest;
import com.evfleet.auth.dto.RegisterRequest;
import com.evfleet.auth.dto.UserResponse;

import java.util.List;

/**
 * Service interface for User management
 *
 * Handles user registration, authentication, and CRUD operations.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public interface UserService {

    /**
     * Register a new user in the system
     */
    AuthResponse registerUser(RegisterRequest request);

    /**
     * Authenticate a user with Firebase token
     */
    AuthResponse loginUser(LoginRequest request);

    /**
     * Get user by Firebase UID
     */
    UserResponse getUserByFirebaseUid(String firebaseUid);

    /**
     * Get user by database ID
     */
    UserResponse getUserById(Long id);

    /**
     * Update user details
     */
    UserResponse updateUser(Long id, UserResponse userResponse);

    /**
     * Delete user by ID (soft delete - marks as inactive)
     */
    void deleteUser(Long id);

    /**
     * Get all users in the system
     */
    List<UserResponse> getAllUsers();

    /**
     * Get all users belonging to a specific company
     */
    List<UserResponse> getUsersByCompanyId(Long companyId);

    /**
     * Sync Firebase user with database (create or update)
     */
    AuthResponse syncUser(RegisterRequest request);
}
