package com.evfleet.notification.repository;

import com.evfleet.notification.entity.Notification;
import com.evfleet.notification.entity.NotificationPriority;
import com.evfleet.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByStatusAndPriorityInOrderByCreatedAtDesc(
        NotificationStatus status,
        List<NotificationPriority> priorities
    );

    List<Notification> findByReadAtIsNullAndPriorityInOrderByCreatedAtDesc(
        List<NotificationPriority> priorities
    );
}
