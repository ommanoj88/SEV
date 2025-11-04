// Re-export all types from a central location
export * from './vehicle';
export * from './driver';
export * from './charging';
export * from './maintenance';
export * from './analytics';
export * from './auth';
export * from './notification';
export * from './billing';
export * from './geofence';

// Common types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface ApiError {
  message: string;
  code?: string;
  details?: any;
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface DateRange {
  start: Date | string;
  end: Date | string;
}

export interface TableColumn {
  id: string;
  label: string;
  sortable?: boolean;
  align?: 'left' | 'center' | 'right';
  format?: (value: any) => string;
}

export interface ChartData {
  name: string;
  value: number;
  [key: string]: any;
}
