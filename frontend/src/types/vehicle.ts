export enum VehicleStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  MAINTENANCE = 'MAINTENANCE',
  IN_TRIP = 'IN_TRIP',
  CHARGING = 'CHARGING',
}

export enum VehicleType {
  TWO_WHEELER = 'TWO_WHEELER',
  THREE_WHEELER = 'THREE_WHEELER',
  LCV = 'LCV',
}

export enum FuelType {
  ICE = 'ICE',
  EV = 'EV',
  HYBRID = 'HYBRID',
}

export enum TripStatus {
  ONGOING = 'ONGOING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  PAUSED = 'PAUSED',
}

export interface Location {
  latitude: number;
  longitude: number;
  address?: string;
  timestamp?: string;
}

export interface BatteryInfo {
  stateOfCharge: number; // 0-100
  stateOfHealth: number; // 0-100
  range: number; // in km
  capacity: number; // in kWh
  temperature?: number; // in Celsius
  voltage?: number;
  current?: number;
  lastUpdated?: string;
}

export interface Vehicle {
  id: string;
  fleetId: string;
  vin: string;
  make: string;
  model: string;
  year: number;
  type: VehicleType;
  fuelType: FuelType;
  status: VehicleStatus;
  licensePlate: string;
  color?: string;
  battery: BatteryInfo;
  location: Location;
  odometer: number; // in km
  // Backend sends currentDriverId, frontend uses assignedDriverId
  currentDriverId?: string | number;
  assignedDriverId?: string;
  assignedDriverName?: string;
  lastServiceDate?: string;
  nextServiceDate?: string;
  imageUrl?: string;
  // ICE-specific fields
  fuelTankCapacity?: number; // in liters
  fuelLevel?: number; // in liters
  createdAt: string;
  updatedAt: string;
}

export interface Trip {
  id: string;
  vehicleId: string;
  driverId: string;
  driverName?: string;
  startLocation: Location;
  endLocation?: Location;
  startTime: string;
  endTime?: string;
  distance: number; // in km
  duration?: number; // in minutes
  startBatteryLevel: number;
  endBatteryLevel?: number;
  energyConsumed?: number; // in kWh
  averageSpeed?: number; // in km/h
  maxSpeed?: number; // in km/h
  cost?: number;
  route?: Location[];
  status: TripStatus;
}

// Note: Geofence type is defined in geofence.ts
// This is kept here for backward compatibility but should use the geofence.ts definition

export interface VehicleFilters {
  companyId?: number;
  status?: VehicleStatus[];
  type?: VehicleType[];
  batteryMin?: number;
  batteryMax?: number;
  search?: string;
  assignedDriver?: string;
}

export interface VehicleFormData {
  vehicleNumber: string;
  vin?: string;
  make: string;
  model: string;
  year: number;
  type: VehicleType;
  fuelType: FuelType;
  licensePlate?: string;
  color?: string;
  // EV/Hybrid fields
  batteryCapacity?: number;
  currentBatterySoc?: number;
  range?: number;
  // ICE/Hybrid fields
  fuelTankCapacity?: number;
  fuelLevel?: number;
  status: VehicleStatus;
  companyId: number;
}
