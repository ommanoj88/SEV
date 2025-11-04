package com.evfleet.notification.service;

import com.evfleet.notification.dto.CreateNotificationRequest;
import com.evfleet.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getAllAlerts();

    List<NotificationResponse> getNotificationsByUserId(String userId);

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse markAsRead(String notificationId);
}
