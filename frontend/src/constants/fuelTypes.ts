/**
 * FuelType Constants
 * Defines the types of fuel/power sources supported by vehicles
 */

export enum FuelType {
  ICE = 'ICE',
  EV = 'EV',
  HYBRID = 'HYBRID',
}

export interface FuelTypeOption {
  value: FuelType;
  label: string;
  description: string;
  icon: string;
  color: string;
}

export const FUEL_TYPE_OPTIONS: FuelTypeOption[] = [
  {
    value: FuelType.EV,
    label: 'Electric Vehicle',
    description: 'Battery-powered vehicles',
    icon: '⚡',
    color: '#4caf50',
  },
  {
    value: FuelType.ICE,
    label: 'Internal Combustion Engine',
    description: 'Traditional fuel-based vehicles',
    icon: '⛽',
    color: '#ff9800',
  },
  {
    value: FuelType.HYBRID,
    label: 'Hybrid',
    description: 'Both electric and fuel capabilities',
    icon: '🔋',
    color: '#2196f3',
  },
];

export const getFuelTypeOption = (fuelType: FuelType): FuelTypeOption | undefined => {
  return FUEL_TYPE_OPTIONS.find(option => option.value === fuelType);
};

export const getFuelTypeLabel = (fuelType: FuelType): string => {
  const option = getFuelTypeOption(fuelType);
  return option ? option.label : fuelType;
};

export const getFuelTypeColor = (fuelType: FuelType): string => {
  const option = getFuelTypeOption(fuelType);
  return option ? option.color : '#666666';
};
