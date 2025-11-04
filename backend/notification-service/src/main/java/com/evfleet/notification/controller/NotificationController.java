package com.evfleet.notification.controller;

import com.evfleet.notification.dto.CreateNotificationRequest;
import com.evfleet.notification.dto.NotificationResponse;
import com.evfleet.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/alerts")
    @Operation(summary = "Get all alerts", description = "Retrieve all unread notifications with HIGH or CRITICAL priority")
    public ResponseEntity<List<NotificationResponse>> getAllAlerts() {
        List<NotificationResponse> alerts = notificationService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for user", description = "Retrieve all notifications for a specific user")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable String userId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody CreateNotificationRequest request) {
        NotificationResponse notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable String id) {
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }
}
