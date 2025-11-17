package com.evfleet.notification.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.notification.dto.NotificationResponse;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification Service
 * Handles all notification-related business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        log.info("GET /api/v1/notifications - userId: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("GET /api/v1/notifications/unread - userId: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        log.info("GET /api/v1/notifications/unread/count - userId: {}", userId);
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        log.info("GET /api/v1/notifications/{} - Fetching notification", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return NotificationResponse.fromEntity(notification);
    }

    public NotificationResponse markAsRead(Long id) {
        log.info("PUT /api/v1/notifications/{}/read - Marking notification as read", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.markAsRead();
        Notification updated = notificationRepository.save(notification);

        log.info("Notification marked as read: {}", id);
        return NotificationResponse.fromEntity(updated);
    }

    public void markAllAsRead(Long userId) {
        log.info("PUT /api/v1/notifications/read-all - Marking all notifications as read for user: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(notifications);

        log.info("All notifications marked as read for user: {}", userId);
    }

    public void deleteNotification(Long id) {
        log.info("DELETE /api/v1/notifications/{} - Deleting notification", id);

        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }

        notificationRepository.deleteById(id);
        log.info("Notification deleted successfully: {}", id);
    }

    public void deleteAllRead(Long userId) {
        log.info("DELETE /api/v1/notifications/read - Deleting all read notifications for user: {}", userId);

        List<Notification> readNotifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, true);
        notificationRepository.deleteAll(readNotifications);

        log.info("All read notifications deleted for user: {}", userId);
    }
}
