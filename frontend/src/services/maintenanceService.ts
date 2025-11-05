import { apiClient } from './api';
import {
  MaintenanceRecord,
  BatteryHealthRecord,
  MaintenanceSchedule,
  ServiceReminder,
} from '../types';

/**
 * Maintenance Service
 * Handles all maintenance-related API calls
 * API Base: /api/v1/maintenance
 */
export const maintenanceService = {
  // ========== MAINTENANCE RECORDS ==========

  /**
   * Get all maintenance records
   */
  getAllRecords: async (params?: any): Promise<MaintenanceRecord[]> => {
    return apiClient.get('/v1/maintenance/records', params);
  },

  /**
   * Get maintenance record by ID
   */
  getRecordById: async (recordId: number): Promise<MaintenanceRecord> => {
    return apiClient.get(`/v1/maintenance/records/${recordId}`);
  },

  /**
   * Get maintenance records by vehicle
   */
  getRecordsByVehicle: async (vehicleId: number): Promise<MaintenanceRecord[]> => {
    return apiClient.get(`/v1/maintenance/records/vehicle/${vehicleId}`);
  },

  /**
   * Create maintenance record
   */
  createRecord: async (data: Partial<MaintenanceRecord>): Promise<MaintenanceRecord> => {
    return apiClient.post('/v1/maintenance/records', data);
  },

  /**
   * Update maintenance record
   */
  updateRecord: async (recordId: number, data: Partial<MaintenanceRecord>): Promise<MaintenanceRecord> => {
    return apiClient.put(`/v1/maintenance/records/${recordId}`, data);
  },

  /**
   * Delete maintenance record
   */
  deleteRecord: async (recordId: number): Promise<void> => {
    return apiClient.delete(`/v1/maintenance/records/${recordId}`);
  },

  // ========== MAINTENANCE SCHEDULES ==========

  /**
   * Get all maintenance schedules
   */
  getAllSchedules: async (params?: any): Promise<MaintenanceSchedule[]> => {
    return apiClient.get('/v1/maintenance/schedules', params);
  },

  /**
   * Get maintenance schedule by ID
   */
  getScheduleById: async (scheduleId: number): Promise<MaintenanceSchedule> => {
    return apiClient.get(`/v1/maintenance/schedules/${scheduleId}`);
  },

  /**
   * Get maintenance schedules by vehicle
   */
  getSchedulesByVehicle: async (vehicleId: number): Promise<MaintenanceSchedule[]> => {
    return apiClient.get(`/v1/maintenance/schedules/vehicle/${vehicleId}`);
  },

  /**
   * Create maintenance schedule
   */
  createSchedule: async (data: Partial<MaintenanceSchedule>): Promise<MaintenanceSchedule> => {
    return apiClient.post('/v1/maintenance/schedules', data);
  },

  /**
   * Update maintenance schedule
   */
  updateSchedule: async (scheduleId: number, data: Partial<MaintenanceSchedule>): Promise<MaintenanceSchedule> => {
    return apiClient.put(`/v1/maintenance/schedules/${scheduleId}`, data);
  },

  /**
   * Delete maintenance schedule
   */
  deleteSchedule: async (scheduleId: number): Promise<void> => {
    return apiClient.delete(`/v1/maintenance/schedules/${scheduleId}`);
  },

  // ========== BATTERY HEALTH ==========

  /**
   * Get battery health records for a vehicle
   */
  getBatteryHealth: async (vehicleId: number): Promise<BatteryHealthRecord[]> => {
    return apiClient.get(`/v1/maintenance/battery-health/${vehicleId}`);
  },

  /**
   * Create battery health record
   */
  createBatteryHealthRecord: async (data: Partial<BatteryHealthRecord>): Promise<BatteryHealthRecord> => {
    return apiClient.post('/v1/maintenance/battery-health', data);
  },

  /**
   * Get service history for a vehicle
   */
  getServiceHistory: async (vehicleId: number): Promise<MaintenanceRecord[]> => {
    return apiClient.get(`/v1/maintenance/service-history/${vehicleId}`);
  },

  /**
   * Get service reminders
   */
  getReminders: async (): Promise<ServiceReminder[]> => {
    return apiClient.get('/v1/maintenance/reminders');
  },
};

export default maintenanceService;
