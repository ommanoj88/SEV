package com.evfleet.auth.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Entity
 *
 * Represents user roles for RBAC (Role-Based Access Control).
 * Extends BaseEntity for standard audit fields.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    /**
     * JSON string of permissions associated with this role
     * Example: ["READ_VEHICLES", "WRITE_VEHICLES", "DELETE_VEHICLES"]
     */
    @Column(columnDefinition = "TEXT")
    private String permissions;

    // Role constants
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_FLEET_MANAGER = "FLEET_MANAGER";
    public static final String ROLE_DRIVER = "DRIVER";
    public static final String ROLE_VIEWER = "VIEWER";
}
