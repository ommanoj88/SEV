package com.evfleet.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base Entity for all domain entities
 *
 * Provides common auditing fields (createdAt, updatedAt) to all entities.
 * Entities should extend this class to inherit standard audit fields.
 *
 * Usage:
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "my_entity")
 * public class MyEntity extends BaseEntity {
 *     // entity-specific fields
 * }
 * }
 * </pre>
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp when the entity was created
     * Automatically populated by Spring Data JPA
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last updated
     * Automatically updated by Spring Data JPA on every save
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Pre-persist callback to ensure timestamps are set
     * Fallback if JPA auditing is not enabled
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Pre-update callback to update the updatedAt timestamp
     * Fallback if JPA auditing is not enabled
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
