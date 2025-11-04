import { apiClient } from './api';
import { Notification, Alert, NotificationPreferences } from '../types';

/**
 * Notification Service
 * Handles all notification-related API calls
 * API Base: /api/v1/notifications
 */
export const notificationService = {
  /**
   * Get all notifications for current user
   */
  getAllNotifications: async (params?: any): Promise<Notification[]> => {
    return apiClient.get('/v1/notifications', params);
  },

  /**
   * Get notification by ID
   */
  getNotificationById: async (notificationId: number): Promise<Notification> => {
    return apiClient.get(`/v1/notifications/${notificationId}`);
  },

  /**
   * Get unread notification count
   */
  getUnreadCount: async (): Promise<{ count: number }> => {
    return apiClient.get('/v1/notifications/unread-count');
  },

  /**
   * Get high and critical priority alerts
   */
  getHighPriorityAlerts: async (): Promise<Alert[]> => {
    return apiClient.get('/v1/notifications/alerts');
  },

  /**
   * Get alerts by priority
   */
  getAlertsByPriority: async (priority: string): Promise<Alert[]> => {
    return apiClient.get(`/v1/notifications/alerts/${priority}`);
  },

  /**
   * Create notification
   */
  createNotification: async (data: Partial<Notification>): Promise<Notification> => {
    return apiClient.post('/v1/notifications', data);
  },

  /**
   * Mark notification as read
   */
  markAsRead: async (notificationId: number): Promise<Notification> => {
    return apiClient.patch(`/v1/notifications/${notificationId}/read`, {});
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async (): Promise<void> => {
    return apiClient.post('/v1/notifications/read-all', {});
  },

  /**
   * Delete notification
   */
  deleteNotification: async (notificationId: number): Promise<void> => {
    return apiClient.delete(`/v1/notifications/${notificationId}`);
  },

  /**
   * Delete all notifications
   */
  deleteAllNotifications: async (): Promise<void> => {
    return apiClient.delete('/v1/notifications/all');
  },

  /**
   * Get notification preferences
   */
  getPreferences: async (): Promise<NotificationPreferences> => {
    return apiClient.get('/v1/notifications/preferences');
  },

  /**
   * Update notification preferences
   */
  updatePreferences: async (data: Partial<NotificationPreferences>): Promise<NotificationPreferences> => {
    return apiClient.put('/v1/notifications/preferences', data);
  },
};

export default notificationService;
