package com.evfleet.auth.repository;

import com.evfleet.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    List<User> findByCompanyId(Long companyId);

    List<User> findByActive(Boolean active);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);
}
