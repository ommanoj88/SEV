package com.evfleet.notification.dto;

import com.evfleet.notification.entity.NotificationChannel;
import com.evfleet.notification.entity.NotificationPriority;
import com.evfleet.notification.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private NotificationStatus status;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String metadata;
}
