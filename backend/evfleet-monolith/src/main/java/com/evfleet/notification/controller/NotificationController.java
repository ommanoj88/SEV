package com.evfleet.notification.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.notification.dto.NotificationResponse;
import com.evfleet.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification Controller
 * Handles all notification-related REST endpoints
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification Management API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/alerts")
    @Operation(summary = "Get all alerts (high-priority notifications)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAlerts() {
        log.info("GET /api/v1/notifications/alerts");
        // For now, return empty list - in real implementation, filter by priority
        List<NotificationResponse> alerts = List.of();
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @GetMapping("/alerts/{priority}")
    @Operation(summary = "Get alerts by priority")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAlertsByPriority(
            @PathVariable String priority) {
        log.info("GET /api/v1/notifications/alerts/{}", priority);
        // For now, return empty list
        List<NotificationResponse> alerts = List.of();
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @GetMapping
    @Operation(summary = "Get all notifications for user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam Long userId) {
        log.info("GET /api/v1/notifications - userId: {}", userId);
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @RequestParam Long userId) {
        log.info("GET /api/v1/notifications/unread - userId: {}", userId);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestParam Long userId) {
        log.info("GET /api/v1/notifications/unread/count - userId: {}", userId);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @PathVariable Long id) {
        log.info("GET /api/v1/notifications/{}", id);
        NotificationResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id) {
        log.info("PUT /api/v1/notifications/{}/read", id);
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam Long userId) {
        log.info("PUT /api/v1/notifications/read-all - userId: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        log.info("DELETE /api/v1/notifications/{}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @DeleteMapping("/read")
    @Operation(summary = "Delete all read notifications")
    public ResponseEntity<ApiResponse<Void>> deleteAllRead(
            @RequestParam Long userId) {
        log.info("DELETE /api/v1/notifications/read - userId: {}", userId);
        notificationService.deleteAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All read notifications deleted", null));
    }
}
