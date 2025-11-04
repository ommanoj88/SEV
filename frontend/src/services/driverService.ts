import { apiClient } from './api';
import { Driver, DriverFormData, DriverBehavior, DriverAttendance, DriverLeaderboard } from '../types';

/**
 * Driver Service
 * Handles all driver-related API calls
 * API Base: /api/v1/drivers
 */
export const driverService = {
  /**
   * Get all drivers
   */
  getAllDrivers: async (params?: any): Promise<Driver[]> => {
    return apiClient.get('/v1/drivers', params);
  },

  /**
   * Get driver by ID
   */
  getDriverById: async (driverId: number): Promise<Driver> => {
    return apiClient.get(`/v1/drivers/${driverId}`);
  },

  /**
   * Get drivers by company
   */
  getDriversByCompany: async (companyId: number): Promise<Driver[]> => {
    return apiClient.get(`/v1/drivers/company/${companyId}`);
  },

  /**
   * Get drivers by status
   */
  getDriversByStatus: async (status: string): Promise<Driver[]> => {
    return apiClient.get(`/v1/drivers/status/${status}`);
  },

  /**
   * Create driver
   */
  createDriver: async (data: DriverFormData): Promise<Driver> => {
    return apiClient.post('/v1/drivers', data);
  },

  /**
   * Update driver
   */
  updateDriver: async (driverId: number, data: Partial<DriverFormData>): Promise<Driver> => {
    return apiClient.put(`/v1/drivers/${driverId}`, data);
  },

  /**
   * Delete driver
   */
  deleteDriver: async (driverId: number): Promise<void> => {
    return apiClient.delete(`/v1/drivers/${driverId}`);
  },

  /**
   * Get driver behavior records
   */
  getDriverBehavior: async (driverId: number): Promise<DriverBehavior[]> => {
    return apiClient.get(`/v1/drivers/${driverId}/behavior`);
  },

  /**
   * Get driver assignments
   */
  getDriverAssignments: async (driverId: number): Promise<any[]> => {
    return apiClient.get(`/v1/drivers/${driverId}/assignments`);
  },

  /**
   * Assign driver to vehicle
   */
  assignToVehicle: async (driverId: number, vehicleId: number): Promise<any> => {
    return apiClient.post(`/v1/drivers/${driverId}/assign-vehicle`, { vehicleId });
  },

  /**
   * Update driver assignment
   */
  updateAssignment: async (assignmentId: number, data: any): Promise<any> => {
    return apiClient.put(`/v1/drivers/assignments/${assignmentId}`, data);
  },

  /**
   * Get driver performance score
   */
  getPerformanceScore: async (driverId: number): Promise<{ score: number; rating: number }> => {
    return apiClient.get(`/v1/drivers/${driverId}/performance-score`);
  },

  /**
   * Get driver leaderboard
   */
  getLeaderboard: async (limit?: number): Promise<DriverLeaderboard[]> => {
    return apiClient.get('/v1/drivers/leaderboard', { limit });
  },
};

export default driverService;
