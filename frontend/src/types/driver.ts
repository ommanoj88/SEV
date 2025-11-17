export enum DriverStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  ON_TRIP = 'ON_TRIP',
  ON_LEAVE = 'ON_LEAVE',
  SUSPENDED = 'SUSPENDED',
  TERMINATED = 'TERMINATED',
}

export enum DriverAssignmentStatus {
  SCHEDULED = 'SCHEDULED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface Driver {
  id: string;
  fleetId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  licenseExpiry: string;
  status: DriverStatus;
  profileImageUrl?: string;
  dateOfBirth?: string;
  address?: string;
  emergencyContact?: {
    name: string;
    phone: string;
    relationship: string;
  };
  performanceScore: number; // 0-100
  totalTrips: number;
  totalDistance: number; // in km
  totalDrivingHours: number;
  averageRating?: number;
  createdAt: string;
  updatedAt: string;
}

export interface DriverBehavior {
  id: string;
  driverId: string;
  tripId: string;
  date: string;
  harshAccelerations: number;
  harshBraking: number;
  harshCornering: number;
  speeding: number;
  idling: number; // in minutes
  score: number; // 0-100
  fuelEfficiencyScore?: number;
  safetyScore?: number;
}

export interface DriverAttendance {
  id: string;
  driverId: string;
  date: string;
  checkIn?: string;
  checkOut?: string;
  status: 'PRESENT' | 'ABSENT' | 'LEAVE' | 'HOLIDAY';
  hoursWorked?: number;
  notes?: string;
}

export interface DriverLeaderboard {
  driverId: string;
  driverName: string;
  profileImageUrl?: string;
  performanceScore: number;
  totalTrips: number;
  totalDistance: number;
  fuelEfficiency: number;
  safetyScore: number;
  rank: number;
}

export interface DriverFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  licenseExpiry: string;
  dateOfBirth?: string;
  address?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;
}
