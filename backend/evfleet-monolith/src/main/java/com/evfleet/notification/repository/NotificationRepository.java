package com.evfleet.notification.repository;

import com.evfleet.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
    boolean existsByUserIdAndReferenceIdAndCreatedAtAfter(Long userId, String referenceId, LocalDateTime createdAfter);
}
