import { Location } from './vehicle';

export enum ChargingStationType {
  LEVEL_1 = 'LEVEL_1', // 120V AC - Slow
  LEVEL_2 = 'LEVEL_2', // 240V AC - Medium
  DC_FAST = 'DC_FAST', // DC - Fast
  SUPERCHARGER = 'SUPERCHARGER', // Tesla Supercharger
}

export enum ChargingStationStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  MAINTENANCE = 'MAINTENANCE',
  FULL = 'FULL',
}

export interface ChargingStation {
  id: string;
  name: string;
  location: Location;
  type: ChargingStationType;
  status: ChargingStationStatus;
  totalPorts: number;
  availablePorts: number;
  powerOutput: number; // in kW
  costPerKwh: number;
  amenities?: string[];
  operatingHours?: string;
  provider?: string;
  phoneNumber?: string;
  distance?: number; // distance from current location in km
  estimatedWaitTime?: number; // in minutes
  rating?: number;
  createdAt: string;
  updatedAt: string;
}

export enum ChargingSessionStatus {
  INITIATED = 'INITIATED',
  CHARGING = 'CHARGING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export interface ChargingSession {
  id: string;
  vehicleId: string;
  vehicleName?: string;
  stationId: string;
  stationName?: string;
  driverId?: string;
  driverName?: string;
  startTime: string;
  endTime?: string;
  startBatteryLevel: number;
  endBatteryLevel?: number;
  energyDelivered: number; // in kWh
  duration?: number; // in minutes
  cost: number;
  status: ChargingSessionStatus;
  paymentStatus?: 'PENDING' | 'PAID' | 'FAILED';
  notes?: string;
}

export interface ChargingSchedule {
  id: string;
  vehicleId: string;
  stationId: string;
  scheduledTime: string;
  targetBatteryLevel: number;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}

export interface RouteOptimization {
  origin: Location;
  destination: Location;
  vehicleId: string;
  currentBatteryLevel: number;
  batteryCapacity: number;
  preferredStationType?: ChargingStationType;
  maxDetourDistance?: number; // in km
}

export interface OptimizedRoute {
  totalDistance: number; // in km
  totalDuration: number; // in minutes
  totalChargingTime: number; // in minutes
  totalCost: number;
  route: Location[];
  chargingStops: {
    station: ChargingStation;
    arrivalBattery: number;
    departureBattery: number;
    chargingDuration: number; // in minutes
    cost: number;
  }[];
  energyConsumption: number; // in kWh
}

export interface ChargingAnalytics {
  totalSessions: number;
  totalEnergyDelivered: number; // in kWh
  totalCost: number;
  averageChargingTime: number; // in minutes
  averageCostPerSession: number;
  mostUsedStation?: ChargingStation;
  costSavings?: number; // compared to gas
  co2Savings?: number; // in kg
}
