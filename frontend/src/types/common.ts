export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  errors?: Record<string, string[]>;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface FilterOptions {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  search?: string;
}

export interface Location {
  latitude: number;
  longitude: number;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  zipCode?: string;
}

export interface TimeRange {
  startDate: Date | string;
  endDate: Date | string;
}

export enum UserRole {
  ADMIN = 'admin',
  FLEET_MANAGER = 'fleet_manager',
  DRIVER = 'driver',
  MAINTENANCE = 'maintenance',
  VIEWER = 'viewer'
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  organization?: string;
  avatar?: string;
  phoneNumber?: string;
  createdAt: Date | string;
  updatedAt: Date | string;
}

export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  data?: Record<string, any>;
  read: boolean;
  createdAt: Date | string;
}

export enum NotificationType {
  INFO = 'info',
  SUCCESS = 'success',
  WARNING = 'warning',
  ERROR = 'error',
  ALERT = 'alert'
}

export enum AlertSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface Alert {
  id: string;
  vehicleId?: string;
  driverId?: string;
  type: AlertType;
  severity: AlertSeverity;
  title: string;
  description: string;
  acknowledged: boolean;
  acknowledgedBy?: string;
  acknowledgedAt?: Date | string;
  createdAt: Date | string;
  resolvedAt?: Date | string;
}

export enum AlertType {
  LOW_BATTERY = 'low_battery',
  MAINTENANCE_DUE = 'maintenance_due',
  CHARGING_ISSUE = 'charging_issue',
  ACCIDENT = 'accident',
  SPEEDING = 'speeding',
  HARSH_BRAKING = 'harsh_braking',
  IDLE_TIME = 'idle_time',
  GEOFENCE_VIOLATION = 'geofence_violation',
  BATTERY_HEALTH = 'battery_health',
  SYSTEM_ERROR = 'system_error'
}

export interface ChartDataPoint {
  date: string;
  value: number;
  label?: string;
}

export interface DashboardMetrics {
  totalVehicles: number;
  activeVehicles: number;
  chargingVehicles: number;
  maintenanceVehicles: number;
  averageBatteryLevel: number;
  totalDistance: number;
  totalDrivers: number;
  fleetUtilization: number;
  activeAlerts: number;
}
