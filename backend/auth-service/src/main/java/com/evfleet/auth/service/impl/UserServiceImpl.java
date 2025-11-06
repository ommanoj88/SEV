package com.evfleet.auth.service.impl;

import com.evfleet.auth.dto.AuthResponse;
import com.evfleet.auth.dto.LoginRequest;
import com.evfleet.auth.dto.RegisterRequest;
import com.evfleet.auth.dto.UserResponse;
import com.evfleet.auth.model.Role;
import com.evfleet.auth.model.User;
import com.evfleet.auth.repository.RoleRepository;
import com.evfleet.auth.repository.UserRepository;
import com.evfleet.auth.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService
 * Handles all user-related business logic
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByFirebaseUid(request.getFirebaseUid())) {
            log.warn("User with Firebase UID {} already exists", request.getFirebaseUid());
            throw new IllegalArgumentException("User with this Firebase UID already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User with email {} already exists", request.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Get default role (FLEET_MANAGER)
        Role defaultRole = roleRepository.findByName(Role.ROLE_FLEET_MANAGER)
                .orElseThrow(() -> {
                    log.error("Default role FLEET_MANAGER not found in database");
                    return new IllegalStateException("Default role not configured. Please contact administrator.");
                });

        // Create new user
        User user = User.builder()
                .firebaseUid(request.getFirebaseUid())
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .companyId(request.getCompanyId())
                .companyName(request.getCompanyName())
                .active(true)
                .emailVerified(false)
                .build();

        user.addRole(defaultRole);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        UserResponse userResponse = UserResponse.fromUser(savedUser);

        return AuthResponse.builder()
                .success(true)
                .message("User registered successfully")
                .user(userResponse)
                .build();
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {
        log.info("User login attempt for Firebase UID: {}", request.getFirebaseUid());

        try {
            // Verify Firebase token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseToken());
            String firebaseUid = decodedToken.getUid();

            // Validate that the UID matches
            if (!firebaseUid.equals(request.getFirebaseUid())) {
                log.warn("Firebase UID mismatch. Expected: {}, Got: {}", request.getFirebaseUid(), firebaseUid);
                throw new IllegalArgumentException("Invalid Firebase token");
            }

            // Find user
            User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> {
                        log.warn("User not found with Firebase UID: {}", firebaseUid);
                        return new IllegalArgumentException("User not found. Please register first.");
                    });

            // Check if user is active
            if (!user.getActive()) {
                log.warn("Inactive user attempted to login: {}", user.getEmail());
                throw new IllegalStateException("User account is inactive. Please contact administrator.");
            }

            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("User logged in successfully: {}", user.getEmail());

            UserResponse userResponse = UserResponse.fromUser(user);

            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .user(userResponse)
                    .token(request.getFirebaseToken())
                    .build();

        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Firebase token: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByFirebaseUid(String firebaseUid) {
        log.debug("Fetching user by Firebase UID: {}", firebaseUid);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> {
                    log.warn("User not found with Firebase UID: {}", firebaseUid);
                    return new IllegalArgumentException("User not found with Firebase UID: " + firebaseUid);
                });

        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });

        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse updateUser(Long id, UserResponse userResponse) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });

        // Update allowed fields
        if (userResponse.getName() != null) {
            user.setName(userResponse.getName());
        }
        if (userResponse.getPhone() != null) {
            user.setPhone(userResponse.getPhone());
        }
        if (userResponse.getCompanyId() != null) {
            user.setCompanyId(userResponse.getCompanyId());
        }
        if (userResponse.getCompanyName() != null) {
            user.setCompanyName(userResponse.getCompanyName());
        }
        if (userResponse.getProfileImageUrl() != null) {
            user.setProfileImageUrl(userResponse.getProfileImageUrl());
        }
        if (userResponse.getActive() != null) {
            user.setActive(userResponse.getActive());
        }
        if (userResponse.getEmailVerified() != null) {
            user.setEmailVerified(userResponse.getEmailVerified());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());

        return UserResponse.fromUser(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });

        // Soft delete - mark as inactive
        user.setActive(false);
        userRepository.save(user);

        log.info("User soft-deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());

        return users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByCompanyId(Long companyId) {
        log.debug("Fetching users for company ID: {}", companyId);

        List<User> users = userRepository.findByCompanyId(companyId);
        log.debug("Found {} users for company {}", users.size(), companyId);

        return users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthResponse syncUser(RegisterRequest request) {
        log.info("=== SYNC USER STARTED === Email: {}, Firebase UID: {}", 
                request.getEmail(), request.getFirebaseUid());

        // Try to find user by Firebase UID first (already synced)
        var existingUserByFirebaseUid = userRepository.findByFirebaseUid(request.getFirebaseUid());
        if (existingUserByFirebaseUid.isPresent()) {
            User user = existingUserByFirebaseUid.get();
            log.info("✓ User found by Firebase UID - User ID: {}, Email: {}", 
                    user.getId(), user.getEmail());
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            UserResponse userResponse = UserResponse.fromUser(user);
            return AuthResponse.builder()
                    .success(true)
                    .message("User already exists in database")
                    .user(userResponse)
                    .build();
        }

        // Check if user exists by email (email/password registration, now trying Google login)
        var existingUserByEmail = userRepository.findByEmail(request.getEmail());
        if (existingUserByEmail.isPresent()) {
            User user = existingUserByEmail.get();
            log.info("✓ User found by email (linking Firebase account) - User ID: {}, Email: {}", 
                    user.getId(), user.getEmail());

            // Update their Firebase UID to link their email/password account with Google
            user.setFirebaseUid(request.getFirebaseUid());
            if (request.getName() != null && !request.getName().isBlank()) {
                user.setName(request.getName());
            }
            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                user.setPhone(request.getPhone());
            }
            user.setLastLogin(LocalDateTime.now());

            User updatedUser = userRepository.save(user);
            log.info("✓ User updated with Firebase UID - User ID: {}", updatedUser.getId());

            UserResponse userResponse = UserResponse.fromUser(updatedUser);
            return AuthResponse.builder()
                    .success(true)
                    .message("User account linked with Firebase successfully")
                    .user(userResponse)
                    .build();
        }

        // User doesn't exist, create new user
        log.info("✗ User not found in database - Creating new user for email: {}", request.getEmail());
        AuthResponse response = registerUser(request);
        log.info("=== SYNC USER COMPLETED === New user created with ID: {}", 
                response.getUser().getId());
        return response;
    }
}
