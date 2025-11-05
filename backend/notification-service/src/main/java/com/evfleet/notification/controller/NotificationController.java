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

    @GetMapping
    @Operation(summary = "Get all notifications for current user", description = "Retrieve all notifications for authenticated user")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(@RequestParam(required = false) java.util.Map<String, String> params) {
        // Note: Should get user from security context
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable String id) {
        // Note: Should be implemented in service layer
        // For now, returning empty response as placeholder
        return ResponseEntity.ok(new NotificationResponse());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<java.util.Map<String, Integer>> getUnreadCount() {
        // Note: Should be implemented in service layer
        // For now, returning placeholder count
        return ResponseEntity.ok(java.util.Map.of("count", 0));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get all alerts", description = "Retrieve all unread notifications with HIGH or CRITICAL priority")
    public ResponseEntity<List<NotificationResponse>> getAllAlerts() {
        List<NotificationResponse> alerts = notificationService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/{priority}")
    @Operation(summary = "Get alerts by priority", description = "Retrieve alerts filtered by priority level")
    public ResponseEntity<List<NotificationResponse>> getAlertsByPriority(@PathVariable String priority) {
        // Note: Should be implemented in service layer to filter by specific priority
        // For now, returning all alerts
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

    @PostMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for current user")
    public ResponseEntity<Void> markAllAsRead() {
        // Note: Should be implemented in service layer
        // For now, returning success
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        // Note: Should be implemented in service layer
        // For now, returning success
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all notifications", description = "Delete all notifications for current user")
    public ResponseEntity<Void> deleteAllNotifications() {
        // Note: Should be implemented in service layer
        // For now, returning success
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences", description = "Retrieve notification preferences for current user")
    public ResponseEntity<java.util.Map<String, Object>> getPreferences() {
        // Note: Should be implemented in service layer
        // For now, returning empty preferences
        return ResponseEntity.ok(java.util.Map.of());
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences", description = "Update notification preferences for current user")
    public ResponseEntity<java.util.Map<String, Object>> updatePreferences(@RequestBody java.util.Map<String, Object> preferences) {
        // Note: Should be implemented in service layer
        // For now, returning the same preferences
        return ResponseEntity.ok(preferences);
    }
}
