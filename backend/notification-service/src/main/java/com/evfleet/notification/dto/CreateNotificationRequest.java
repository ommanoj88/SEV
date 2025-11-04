package com.evfleet.notification.dto;

import com.evfleet.notification.entity.NotificationChannel;
import com.evfleet.notification.entity.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    private String userId;
    private String type;
    private String title;
    private String message;
    private NotificationChannel channel;
    private NotificationPriority priority;
    private String metadata;
}
