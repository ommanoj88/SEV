package com.evfleet.auth.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity
 *
 * Represents a user in the EV Fleet Management system.
 * Integrates with Firebase Authentication via firebaseUid.
 * Extends BaseEntity for standard audit fields.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_firebase_uid", columnList = "firebase_uid"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_company_id", columnList = "company_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Firebase UID for authentication integration
     */
    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    private String firebaseUid;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    /**
     * Company/Organization ID this user belongs to
     */
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", length = 255)
    private String companyName;

    /**
     * Whether the user account is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether the user's email is verified
     */
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * URL to user's profile image
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * Roles assigned to this user
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Last login timestamp
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ========== Helper Methods ==========

    /**
     * Add a role to this user
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Remove a role from this user
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Check if user is an admin (ADMIN or SUPER_ADMIN)
     */
    public boolean isAdmin() {
        return hasRole(Role.ROLE_ADMIN) || hasRole(Role.ROLE_SUPER_ADMIN);
    }

    /**
     * Check if user is a fleet manager
     */
    public boolean isFleetManager() {
        return hasRole(Role.ROLE_FLEET_MANAGER);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
