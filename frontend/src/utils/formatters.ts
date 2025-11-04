import { format, formatDistance as formatDistanceDate, formatRelative, parseISO } from 'date-fns';
import {
  DATE_FORMAT,
  TIME_FORMAT,
  DATETIME_FORMAT,
  KM_TO_MILES,
} from './constants';

// Date Formatters
export const formatDate = (date: string | Date, formatStr: string = DATE_FORMAT): string => {
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;
    return format(dateObj, formatStr);
  } catch (error) {
    return 'Invalid date';
  }
};

export const formatTime = (date: string | Date): string => {
  return formatDate(date, TIME_FORMAT);
};

export const formatDateTime = (date: string | Date): string => {
  return formatDate(date, DATETIME_FORMAT);
};

export const formatRelativeTime = (date: string | Date): string => {
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;
    return formatRelative(dateObj, new Date());
  } catch (error) {
    return 'Unknown';
  }
};

export const formatTimeAgo = (date: string | Date): string => {
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;
    return formatDistanceDate(dateObj, new Date(), { addSuffix: true });
  } catch (error) {
    return 'Unknown';
  }
};

// Number Formatters
export const formatNumber = (num: number, decimals: number = 0): string => {
  return num.toLocaleString('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
};

export const formatCurrency = (
  amount: number,
  currency: string = 'USD',
  decimals: number = 2
): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(amount);
};

export const formatPercentage = (value: number, decimals: number = 1): string => {
  return `${value.toFixed(decimals)}%`;
};

// Distance Formatters
export const formatDistance = (km: number, unit: 'km' | 'mi' = 'km'): string => {
  const value = unit === 'mi' ? km * KM_TO_MILES : km;
  return `${formatNumber(value, 1)} ${unit}`;
};

export const formatSpeed = (kmh: number, unit: 'km/h' | 'mph' = 'km/h'): string => {
  const value = unit === 'mph' ? kmh * KM_TO_MILES : kmh;
  return `${formatNumber(value, 0)} ${unit}`;
};

// Battery Formatters
export const formatBatteryLevel = (level: number): string => {
  return `${Math.round(level)}%`;
};

export const formatBatteryHealth = (health: number): string => {
  return `${Math.round(health)}%`;
};

export const formatRange = (km: number, unit: 'km' | 'mi' = 'km'): string => {
  return formatDistance(km, unit);
};

// Energy Formatters
export const formatEnergy = (kwh: number): string => {
  return `${formatNumber(kwh, 2)} kWh`;
};

export const formatEnergyEfficiency = (kwhPer100km: number): string => {
  return `${formatNumber(kwhPer100km, 1)} kWh/100km`;
};

export const formatPower = (kw: number): string => {
  return `${formatNumber(kw, 1)} kW`;
};

// Duration Formatters
export const formatDuration = (minutes: number): string => {
  if (minutes < 60) {
    return `${Math.round(minutes)} min`;
  }
  const hours = Math.floor(minutes / 60);
  const mins = Math.round(minutes % 60);
  return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
};

export const formatDurationHours = (hours: number): string => {
  if (hours < 1) {
    return `${Math.round(hours * 60)} min`;
  }
  return `${formatNumber(hours, 1)}h`;
};

// Address Formatter
export const formatAddress = (location: {
  address?: string;
  latitude: number;
  longitude: number;
}): string => {
  if (location.address) {
    return location.address;
  }
  return `${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)}`;
};

// Phone Formatter
export const formatPhone = (phone: string): string => {
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  return phone;
};

// VIN Formatter
export const formatVIN = (vin: string): string => {
  return vin.toUpperCase();
};

// License Plate Formatter
export const formatLicensePlate = (plate: string): string => {
  return plate.toUpperCase();
};

// File Size Formatter
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
};

// CO2 Formatter
export const formatCO2 = (kg: number): string => {
  if (kg < 1000) {
    return `${formatNumber(kg, 1)} kg`;
  }
  return `${formatNumber(kg / 1000, 2)} tonnes`;
};

// Status Formatter
export const formatStatus = (status: string): string => {
  return status
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

// Name Formatter
export const formatFullName = (firstName: string, lastName: string): string => {
  return `${firstName} ${lastName}`;
};

export const formatInitials = (firstName: string, lastName: string): string => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
};

// Truncate Text
export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return `${text.slice(0, maxLength)}...`;
};

// Score Formatter
export const formatScore = (score: number, max: number = 100): string => {
  return `${Math.round(score)}/${max}`;
};

// Ordinal Formatter (1st, 2nd, 3rd, etc.)
export const formatOrdinal = (num: number): string => {
  const suffixes = ['th', 'st', 'nd', 'rd'];
  const v = num % 100;
  return num + (suffixes[(v - 20) % 10] || suffixes[v] || suffixes[0]);
};
