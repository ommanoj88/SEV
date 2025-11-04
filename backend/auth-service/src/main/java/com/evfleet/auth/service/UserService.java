package com.evfleet.auth.service;

import com.evfleet.auth.dto.AuthResponse;
import com.evfleet.auth.dto.LoginRequest;
import com.evfleet.auth.dto.RegisterRequest;
import com.evfleet.auth.dto.UserResponse;

import java.util.List;

/**
 * Service interface for User management
 * Handles user registration, authentication, and CRUD operations
 */
public interface UserService {

    /**
     * Register a new user in the system
     * @param request Registration request containing user details
     * @return AuthResponse with user details and token
     */
    AuthResponse registerUser(RegisterRequest request);

    /**
     * Authenticate a user with Firebase token
     * @param request Login request containing Firebase UID and token
     * @return AuthResponse with user details and validated token
     */
    AuthResponse loginUser(LoginRequest request);

    /**
     * Get user by Firebase UID
     * @param firebaseUid Firebase unique identifier
     * @return UserResponse containing user details
     */
    UserResponse getUserByFirebaseUid(String firebaseUid);

    /**
     * Get user by database ID
     * @param id User database ID
     * @return UserResponse containing user details
     */
    UserResponse getUserById(Long id);

    /**
     * Update user details
     * @param id User database ID
     * @param userResponse UserResponse containing updated details
     * @return Updated UserResponse
     */
    UserResponse updateUser(Long id, UserResponse userResponse);

    /**
     * Delete user by ID (soft delete - marks as inactive)
     * @param id User database ID
     */
    void deleteUser(Long id);

    /**
     * Get all users in the system
     * @return List of all UserResponse objects
     */
    List<UserResponse> getAllUsers();

    /**
     * Get all users belonging to a specific company
     * @param companyId Company ID
     * @return List of UserResponse objects for the company
     */
    List<UserResponse> getUsersByCompanyId(Long companyId);

    /**
     * Sync Firebase user with database (create or update)
     * @param request Registration request containing user details and Firebase UID
     * @return AuthResponse with user details
     */
    AuthResponse syncUser(RegisterRequest request);
}
