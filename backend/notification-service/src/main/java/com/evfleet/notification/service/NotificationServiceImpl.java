package com.evfleet.notification.service;

import com.evfleet.notification.dto.CreateNotificationRequest;
import com.evfleet.notification.dto.NotificationResponse;
import com.evfleet.notification.entity.Notification;
import com.evfleet.notification.entity.NotificationPriority;
import com.evfleet.notification.entity.NotificationStatus;
import com.evfleet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponse> getAllAlerts() {
        List<NotificationPriority> alertPriorities = Arrays.asList(
            NotificationPriority.HIGH,
            NotificationPriority.CRITICAL
        );

        List<Notification> alerts = notificationRepository
            .findByReadAtIsNullAndPriorityInOrderByCreatedAtDesc(alertPriorities);

        return alerts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getNotificationsByUserId(String userId) {
        List<Notification> notifications = notificationRepository
            .findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setChannel(request.getChannel());
        notification.setPriority(request.getPriority() != null ? request.getPriority() : NotificationPriority.MEDIUM);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setMetadata(request.getMetadata());
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        return mapToResponse(savedNotification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.READ);

        Notification updatedNotification = notificationRepository.save(notification);

        return mapToResponse(updatedNotification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setStatus(notification.getStatus());
        response.setChannel(notification.getChannel());
        response.setPriority(notification.getPriority());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSentAt(notification.getSentAt());
        response.setReadAt(notification.getReadAt());
        response.setMetadata(notification.getMetadata());
        return response;
    }
}
