import { VehicleStatus, BatteryInfo, Location } from '../types';
import {
  BATTERY_LOW_THRESHOLD,
  BATTERY_CRITICAL_THRESHOLD,
  VEHICLE_STATUS_COLORS,
} from './constants';

// Battery Helpers
export const getBatteryStatusColor = (level: number): string => {
  if (level <= BATTERY_CRITICAL_THRESHOLD) return '#f44336'; // red
  if (level <= BATTERY_LOW_THRESHOLD) return '#ff9800'; // orange
  return '#4caf50'; // green
};

export const getBatteryHealthColor = (health: number): string => {
  if (health >= 90) return '#4caf50'; // green
  if (health >= 70) return '#ff9800'; // orange
  return '#f44336'; // red
};

export const isBatteryLow = (level: number): boolean => {
  return level <= BATTERY_LOW_THRESHOLD;
};

export const isBatteryCritical = (level: number): boolean => {
  return level <= BATTERY_CRITICAL_THRESHOLD;
};

export const calculateBatteryRange = (battery: BatteryInfo): number => {
  // Estimate range based on SOC and capacity
  return (battery.stateOfCharge / 100) * battery.range;
};

// Vehicle Helpers
export const getVehicleStatusColor = (status: VehicleStatus): string => {
  return VEHICLE_STATUS_COLORS[status] || '#9e9e9e';
};

export const isVehicleActive = (status: VehicleStatus): boolean => {
  return status === VehicleStatus.ACTIVE;
};

export const isVehicleAvailable = (status: VehicleStatus): boolean => {
  return status === VehicleStatus.ACTIVE || status === VehicleStatus.INACTIVE;
};

export const getVehicleStatusIcon = (status: VehicleStatus): string => {
  const icons: Record<VehicleStatus, string> = {
    [VehicleStatus.ACTIVE]: 'directions_car',
    [VehicleStatus.INACTIVE]: 'local_parking',
    [VehicleStatus.CHARGING]: 'ev_station',
    [VehicleStatus.MAINTENANCE]: 'build',
    [VehicleStatus.IN_TRIP]: 'navigation',
  };
  return icons[status] || 'help';
};

// Location Helpers
export const calculateDistance = (
  loc1: Location,
  loc2: Location
): number => {
  // Haversine formula to calculate distance between two coordinates
  const R = 6371; // Earth's radius in km
  const dLat = toRadians(loc2.latitude - loc1.latitude);
  const dLon = toRadians(loc2.longitude - loc1.longitude);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(loc1.latitude)) *
      Math.cos(toRadians(loc2.latitude)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

const toRadians = (degrees: number): number => {
  return (degrees * Math.PI) / 180;
};

export const isLocationValid = (location: Location): boolean => {
  return (
    location.latitude >= -90 &&
    location.latitude <= 90 &&
    location.longitude >= -180 &&
    location.longitude <= 180
  );
};

export const getCenterOfLocations = (locations: Location[]): Location => {
  if (locations.length === 0) {
    return { latitude: 0, longitude: 0 };
  }

  let totalLat = 0;
  let totalLng = 0;

  locations.forEach((loc) => {
    totalLat += loc.latitude;
    totalLng += loc.longitude;
  });

  return {
    latitude: totalLat / locations.length,
    longitude: totalLng / locations.length,
  };
};

// Performance Helpers
export const getPerformanceColor = (score: number): string => {
  if (score >= 90) return '#4caf50'; // green
  if (score >= 70) return '#ff9800'; // orange
  if (score >= 50) return '#ff5722'; // deep orange
  return '#f44336'; // red
};

export const getPerformanceLabel = (score: number): string => {
  if (score >= 90) return 'Excellent';
  if (score >= 70) return 'Good';
  if (score >= 50) return 'Average';
  return 'Poor';
};

// Date Helpers
export const isOverdue = (date: string): boolean => {
  return new Date(date) < new Date();
};

export const getDaysUntil = (date: string): number => {
  const now = new Date();
  const target = new Date(date);
  const diffTime = target.getTime() - now.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

export const isWithinDays = (date: string, days: number): boolean => {
  const daysUntil = getDaysUntil(date);
  return daysUntil >= 0 && daysUntil <= days;
};

// Array Helpers
export const groupBy = <T>(
  array: T[],
  key: keyof T
): Record<string, T[]> => {
  return array.reduce((result, item) => {
    const groupKey = String(item[key]);
    if (!result[groupKey]) {
      result[groupKey] = [];
    }
    result[groupKey].push(item);
    return result;
  }, {} as Record<string, T[]>);
};

export const sortBy = <T>(
  array: T[],
  key: keyof T,
  order: 'asc' | 'desc' = 'asc'
): T[] => {
  return [...array].sort((a, b) => {
    const aVal = a[key];
    const bVal = b[key];
    if (aVal < bVal) return order === 'asc' ? -1 : 1;
    if (aVal > bVal) return order === 'asc' ? 1 : -1;
    return 0;
  });
};

export const uniqueBy = <T>(array: T[], key: keyof T): T[] => {
  const seen = new Set();
  return array.filter((item) => {
    const value = item[key];
    if (seen.has(value)) {
      return false;
    }
    seen.add(value);
    return true;
  });
};

// Statistics Helpers
export const calculateAverage = (numbers: number[]): number => {
  if (numbers.length === 0) return 0;
  return numbers.reduce((sum, num) => sum + num, 0) / numbers.length;
};

export const calculateSum = (numbers: number[]): number => {
  return numbers.reduce((sum, num) => sum + num, 0);
};

export const calculatePercentage = (
  value: number,
  total: number
): number => {
  if (total === 0) return 0;
  return (value / total) * 100;
};

export const calculatePercentageChange = (
  oldValue: number,
  newValue: number
): number => {
  if (oldValue === 0) return 0;
  return ((newValue - oldValue) / oldValue) * 100;
};

// Color Helpers
export const hexToRgba = (hex: string, alpha: number = 1): string => {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

export const getContrastColor = (hexColor: string): string => {
  // Calculate luminance
  const r = parseInt(hexColor.slice(1, 3), 16);
  const g = parseInt(hexColor.slice(3, 5), 16);
  const b = parseInt(hexColor.slice(5, 7), 16);
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance > 0.5 ? '#000000' : '#ffffff';
};

// Debounce
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

// Throttle
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
};

// Download Helper
export const downloadFile = (data: any, filename: string, type: string = 'text/csv') => {
  const blob = new Blob([data], { type });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

// Copy to Clipboard
export const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (error) {
    console.error('Failed to copy to clipboard:', error);
    return false;
  }
};

// Local Storage Helpers
export const getFromStorage = <T>(key: string, defaultValue: T): T => {
  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : defaultValue;
  } catch (error) {
    console.error('Error reading from localStorage:', error);
    return defaultValue;
  }
};

export const setToStorage = <T>(key: string, value: T): void => {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.error('Error writing to localStorage:', error);
  }
};

export const removeFromStorage = (key: string): void => {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error('Error removing from localStorage:', error);
  }
};

// Generate Random ID
export const generateId = (): string => {
  return Math.random().toString(36).substring(2, 15) +
    Math.random().toString(36).substring(2, 15);
};

// Check if online
export const isOnline = (): boolean => {
  return navigator.onLine;
};

// Text Helpers
export const pluralize = (count: number, singular: string, plural?: string): string => {
  return count === 1 ? singular : (plural || `${singular}s`);
};

export const pluralizeWithCount = (count: number, singular: string, plural?: string): string => {
  return `${count} ${pluralize(count, singular, plural)}`;
};

// User Helpers
/**
 * Extract a display name from an email address
 * Takes the part before @ and capitalizes it
 * @param email - The email address
 * @returns A formatted display name
 */
export const getNameFromEmail = (email: string): string => {
  const username = email.split('@')[0];
  // Capitalize first letter and replace dots/underscores with spaces
  return username
    .split(/[._]/)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
};
