import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Notification, Alert, NotificationType, NotificationCategory } from '../../types';
import notificationService from '../../services/notificationService';
import { RootState } from '../store';

interface NotificationState {
  notifications: Notification[];
  alerts: Alert[];
  unreadCount: number;
  loading: boolean;
  error: string | null;
}

// Mock notification and alert data for development/fallback
const MOCK_NOTIFICATIONS: Notification[] = [
  {
    id: 'N1',
    fleetId: '1',
    type: NotificationType.WARNING,
    category: NotificationCategory.BATTERY,
    title: 'Low Battery Alert',
    message: 'Tesla Model 3 (TS-001) battery at 20%, please charge soon',
    vehicleId: '1',
    vehicleName: 'Tesla Model 3',
    isRead: false,
    createdAt: new Date(Date.now() - 600000).toISOString(),
  },
  {
    id: 'N2',
    fleetId: '1',
    type: NotificationType.SUCCESS,
    category: NotificationCategory.CHARGING,
    title: 'Charging Completed',
    message: 'Tata Nexon EV (TS-002) completed charging session at 10:30 AM',
    vehicleId: '2',
    vehicleName: 'Tata Nexon EV',
    isRead: true,
    readAt: new Date(Date.now() - 1800000).toISOString(),
    createdAt: new Date(Date.now() - 1800000).toISOString(),
  },
];

const MOCK_ALERTS: Alert[] = [
  {
    id: 'A1',
    fleetId: '1',
    type: NotificationType.CRITICAL,
    category: NotificationCategory.BATTERY,
    title: 'Critical: Low Battery',
    message: 'Tesla Model 3 battery critically low at 15%',
    vehicleId: '1',
    vehicleName: 'Tesla Model 3',
    isRead: false,
    severity: 'CRITICAL',
    resolved: false,
    createdAt: new Date(Date.now() - 300000).toISOString(),
  },
];

const initialState: NotificationState = {
  notifications: MOCK_NOTIFICATIONS,
  alerts: MOCK_ALERTS,
  unreadCount: 1,
  loading: false,
  error: null,
};

export const fetchNotifications = createAsyncThunk(
  'notifications/fetchNotifications',
  async () => {
    return await notificationService.getAllNotifications();
  }
);

export const fetchAlerts = createAsyncThunk('notifications/fetchAlerts', async () => {
  return await notificationService.getHighPriorityAlerts();
});

export const fetchUnreadCount = createAsyncThunk(
  'notifications/fetchUnreadCount',
  async () => {
    return await notificationService.getUnreadCount();
  }
);

export const markAsRead = createAsyncThunk(
  'notifications/markAsRead',
  async (id: string) => {
    await notificationService.markAsRead(Number(id));
    return id;
  }
);

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    addNotification: (state, action: PayloadAction<Notification>) => {
      state.notifications.unshift(action.payload);
      if (!action.payload.isRead) {
        state.unreadCount++;
      }
    },
    addAlert: (state, action: PayloadAction<Alert>) => {
      state.alerts.unshift(action.payload);
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.loading = false;
        state.notifications = action.payload;
      })
      .addCase(fetchNotifications.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch notifications';
        // Only log warning, don't show error to user - use mock data as fallback
        console.warn('[notificationSlice] Failed to fetch notifications - using mock data:', action.error.message);
        // Use mock data as fallback
        state.notifications = MOCK_NOTIFICATIONS;
      })
      .addCase(fetchAlerts.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchAlerts.fulfilled, (state, action) => {
        state.loading = false;
        state.alerts = action.payload;
      })
      .addCase(fetchAlerts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch alerts';
        // Only log warning, don't show error to user - use mock data as fallback
        console.warn('[notificationSlice] Failed to fetch alerts - using mock data:', action.error.message);
        // Use mock data as fallback
        state.alerts = MOCK_ALERTS;
      })
      .addCase(fetchUnreadCount.fulfilled, (state, action) => {
        state.unreadCount = action.payload.count;
      })
      .addCase(fetchUnreadCount.rejected, (state, action) => {
        // Silently fail and use default count
        console.warn('[notificationSlice] Failed to fetch unread count - using default:', action.error.message);
        state.unreadCount = 0;
      })
      .addCase(markAsRead.fulfilled, (state, action) => {
        const notification = state.notifications.find(n => n.id === action.payload);
        if (notification && !notification.isRead) {
          notification.isRead = true;
          state.unreadCount = Math.max(0, state.unreadCount - 1);
        }
      })
      .addCase(markAsRead.rejected, (state, action) => {
        console.warn('[notificationSlice] Failed to mark as read:', action.error.message);
      });
  },
});

export const { addNotification, addAlert, clearError } = notificationSlice.actions;
export const selectNotifications = (state: RootState) => state.notifications.notifications;
export const selectAlerts = (state: RootState) => state.notifications.alerts;
export const selectUnreadCount = (state: RootState) => state.notifications.unreadCount;

export default notificationSlice.reducer;
