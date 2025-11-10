import { Location } from './vehicle';

/**
 * Fuel Station Types
 * Types for ICE vehicle fuel stations
 */

export enum FuelStationType {
  PETROL = 'PETROL',
  DIESEL = 'DIESEL',
  CNG = 'CNG',
  LPG = 'LPG',
}

export enum FuelStationStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  MAINTENANCE = 'MAINTENANCE',
  FULL = 'FULL',
}

export interface FuelStation {
  id: string;
  name: string;
  location: Location;
  fuelTypes: FuelStationType[]; // A station can have multiple fuel types
  status: FuelStationStatus;
  totalPumps: number;
  availablePumps: number;
  pricePerLiter: {
    PETROL?: number;
    DIESEL?: number;
    CNG?: number;
    LPG?: number;
  };
  amenities?: string[];
  operatingHours?: string;
  brand?: string;
  phoneNumber?: string;
  distance?: number; // distance from current location in km
  estimatedWaitTime?: number; // in minutes
  rating?: number;
  createdAt: string;
  updatedAt: string;
}

export interface FuelPrice {
  fuelType: FuelStationType;
  price: number;
  currency: string;
  lastUpdated: string;
}

/**
 * Union type for all station types (charging or fuel)
 */
export type Station = import('./charging').ChargingStation | FuelStation;

/**
 * Type guard to check if a station is a charging station
 */
export function isChargingStation(station: Station): station is import('./charging').ChargingStation {
  return 'costPerKwh' in station;
}

/**
 * Type guard to check if a station is a fuel station
 */
export function isFuelStation(station: Station): station is FuelStation {
  return 'pricePerLiter' in station;
}
