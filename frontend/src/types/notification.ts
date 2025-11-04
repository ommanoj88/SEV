export enum NotificationType {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL',
  SUCCESS = 'SUCCESS',
}

export enum NotificationCategory {
  BATTERY = 'BATTERY',
  MAINTENANCE = 'MAINTENANCE',
  DRIVER = 'DRIVER',
  VEHICLE = 'VEHICLE',
  CHARGING = 'CHARGING',
  TRIP = 'TRIP',
  SYSTEM = 'SYSTEM',
  BILLING = 'BILLING',
}

export enum NotificationChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  IN_APP = 'IN_APP',
  PUSH = 'PUSH',
}

export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

export enum NotificationStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  FAILED = 'FAILED',
  READ = 'READ',
}

export interface Notification {
  id: string;
  fleetId: string;
  type: NotificationType;
  category: NotificationCategory;
  title: string;
  message: string;
  vehicleId?: string;
  vehicleName?: string;
  driverId?: string;
  driverName?: string;
  isRead: boolean;
  actionUrl?: string;
  metadata?: Record<string, any>;
  createdAt: string;
  readAt?: string;
}

export interface Alert extends Notification {
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  resolved: boolean;
  resolvedAt?: string;
  resolvedBy?: string;
  autoResolved?: boolean;
}

export interface NotificationPreferences {
  userId: string;
  emailNotifications: boolean;
  pushNotifications: boolean;
  smsNotifications: boolean;
  categories: {
    [key in NotificationCategory]: {
      enabled: boolean;
      email: boolean;
      push: boolean;
      sms: boolean;
    };
  };
}
