package com.evfleet.auth.repository;

import com.evfleet.auth.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Firebase UID (optimized with EntityGraph to avoid N+1 query)
     */
    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u WHERE u.firebaseUid = :firebaseUid")
    Optional<User> findByFirebaseUid(@Param("firebaseUid") String firebaseUid);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users belonging to a company
     */
    List<User> findByCompanyId(Long companyId);

    /**
     * Find all active/inactive users
     */
    List<User> findByActive(Boolean active);

    /**
     * Check if user exists by Firebase UID
     */
    boolean existsByFirebaseUid(String firebaseUid);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(String roleName);

    /**
     * Find fleet managers by company ID
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.companyId = :companyId AND r.name = 'FLEET_MANAGER'")
    List<User> findFleetManagersByCompanyId(@Param("companyId") Long companyId);
}
