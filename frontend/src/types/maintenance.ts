export enum MaintenanceType {
  ROUTINE = 'ROUTINE',
  PREVENTIVE = 'PREVENTIVE',
  REPAIR = 'REPAIR',
  INSPECTION = 'INSPECTION',
  TIRE_SERVICE = 'TIRE_SERVICE',
  BATTERY_SERVICE = 'BATTERY_SERVICE',
  SOFTWARE_UPDATE = 'SOFTWARE_UPDATE',
}

export enum MaintenanceStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  OVERDUE = 'OVERDUE',
}

export enum MaintenancePriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

export interface MaintenanceRecord {
  id: string;
  vehicleId: string;
  vehicleName?: string;
  type: MaintenanceType;
  status: MaintenanceStatus;
  priority: MaintenancePriority;
  scheduledDate: string;
  completedDate?: string;
  description: string;
  odometer?: number; // odometer reading at service
  cost?: number;
  serviceProvider?: string;
  technician?: string;
  parts?: {
    name: string;
    quantity: number;
    cost: number;
  }[];
  labor?: {
    description: string;
    hours: number;
    cost: number;
  };
  notes?: string;
  attachments?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface BatteryHealthRecord {
  id: string;
  vehicleId: string;
  date: string;
  stateOfHealth: number; // 0-100
  capacity: number; // in kWh
  degradation: number; // percentage
  cycleCount: number;
  temperature: number;
  internalResistance?: number;
  cellVoltageMin?: number;
  cellVoltageMax?: number;
  notes?: string;
}

export interface MaintenanceSchedule {
  id: string;
  vehicleId: string;
  type: MaintenanceType;
  intervalType: 'MILEAGE' | 'TIME' | 'BATTERY_CYCLE';
  intervalValue: number; // km, days, or cycles
  lastServiceDate?: string;
  lastServiceOdometer?: number;
  nextServiceDate?: string;
  nextServiceOdometer?: number;
  isActive: boolean;
  description: string;
}

export interface ServiceReminder {
  id: string;
  vehicleId: string;
  vehicleName: string;
  type: MaintenanceType;
  dueDate: string;
  dueOdometer?: number;
  currentOdometer?: number;
  daysUntilDue: number;
  kmUntilDue?: number;
  priority: MaintenancePriority;
  status: 'UPCOMING' | 'DUE' | 'OVERDUE';
}

export interface MaintenanceAnalytics {
  totalCost: number;
  totalServices: number;
  averageCostPerService: number;
  costByType: {
    type: MaintenanceType;
    cost: number;
    count: number;
  }[];
  costTrend: {
    month: string;
    cost: number;
  }[];
  upcomingServices: number;
  overdueServices: number;
}

export interface MaintenanceFormData {
  vehicleId: string;
  type: MaintenanceType;
  priority: MaintenancePriority;
  scheduledDate: string;
  description: string;
  estimatedCost?: number;
  serviceProvider?: string;
  notes?: string;
}
