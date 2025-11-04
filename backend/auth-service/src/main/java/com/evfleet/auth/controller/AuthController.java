package com.evfleet.auth.controller;

import com.evfleet.auth.dto.AuthResponse;
import com.evfleet.auth.dto.LoginRequest;
import com.evfleet.auth.dto.RegisterRequest;
import com.evfleet.auth.dto.UserResponse;
import com.evfleet.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Authentication and User Management
 * Provides endpoints for user registration, login, and CRUD operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and User Management API")
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with Firebase authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Registering user: {}", request.getEmail());

        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login an existing user
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates user with Firebase token and returns user details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Login attempt for Firebase UID: {}", request.getFirebaseUid());

        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user details by database ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        log.info("GET /api/auth/users/{} - Fetching user by ID", id);

        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/auth/users - Fetching all users");

        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * Update user
     */
    @PutMapping("/users/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates user details by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserResponse userResponse) {
        log.info("PUT /api/auth/users/{} - Updating user", id);

        UserResponse response = userService.updateUser(id, userResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/users/{id}")
    @Operation(
            summary = "Delete user",
            description = "Soft deletes user by marking as inactive"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/auth/users/{} - Deleting user", id);

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get users by company ID
     */
    @GetMapping("/users/company/{companyId}")
    @Operation(
            summary = "Get users by company",
            description = "Retrieves all users belonging to a specific company"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserResponse>> getUsersByCompany(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId) {
        log.info("GET /api/auth/users/company/{} - Fetching users by company", companyId);

        List<UserResponse> response = userService.getUsersByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by Firebase UID
     */
    @GetMapping("/users/firebase/{firebaseUid}")
    @Operation(
            summary = "Get user by Firebase UID",
            description = "Retrieves user details by Firebase unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponse> getUserByFirebaseUid(
            @Parameter(description = "Firebase UID", required = true)
            @PathVariable String firebaseUid) {
        log.info("GET /api/auth/users/firebase/{} - Fetching user by Firebase UID", firebaseUid);

        UserResponse response = userService.getUserByFirebaseUid(firebaseUid);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user from Authorization Bearer token
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Get current user information from Firebase ID token in Authorization header"
    )
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/auth/me - Getting current user from Authorization header");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            // Verify Firebase token and extract UID
            com.google.firebase.auth.FirebaseToken decodedToken =
                com.google.firebase.auth.FirebaseAuth.getInstance().verifyIdToken(token);
            String firebaseUid = decodedToken.getUid();

            log.info("Verified Firebase token for UID: {}", firebaseUid);

            UserResponse user = userService.getUserByFirebaseUid(firebaseUid);
            return ResponseEntity.ok(user);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            log.warn("Invalid Firebase token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error extracting user from token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Sync Firebase user to database
     */
    @PostMapping("/sync")
    @Operation(
            summary = "Sync Firebase user",
            description = "Sync Firebase user with backend database"
    )
    public ResponseEntity<AuthResponse> syncUser(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/sync - Syncing user: {}", request.getEmail());

        AuthResponse response = userService.syncUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if the auth service is running"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
