import { apiClient } from './api';
import { Driver, DriverBehavior, DriverLeaderboard } from '../types';

/**
 * Map backend driver response (single name field) to frontend Driver type (firstName/lastName)
 */
const mapDriverResponse = (backendDriver: any): Driver => {
  const nameParts = (backendDriver.name || '').split(' ');
  const firstName = nameParts[0] || '';
  const lastName = nameParts.slice(1).join(' ') || '';

  return {
    ...backendDriver,
    firstName,
    lastName,
    // Keep other fields as-is
    performanceScore: backendDriver.performanceScore || 0,
    totalTrips: backendDriver.totalTrips || 0,
    totalDistance: backendDriver.totalDistance || 0,
    totalDrivingHours: backendDriver.totalDrivingHours || 0,
  };
};

/**
 * Driver Service
 * Handles all driver-related API calls
 * API Base: /api/v1/drivers
 */
export const driverService = {
  /**
   * Get all drivers
   * Backend requires companyId as a query parameter
   */
  getAllDrivers: async (params?: { companyId?: number }): Promise<Driver[]> => {
    const drivers = await apiClient.get('/v1/drivers', params);
    return Array.isArray(drivers) ? drivers.map(mapDriverResponse) : [];
  },

  /**
   * Get driver by ID
   */
  getDriverById: async (driverId: number): Promise<Driver> => {
    const driver = await apiClient.get(`/v1/drivers/${driverId}`);
    return mapDriverResponse(driver);
  },

  /**
   * Get drivers by company
   * Uses query parameter since backend doesn't have /company/{id} endpoint
   */
  getDriversByCompany: async (companyId: number): Promise<Driver[]> => {
    const drivers = await apiClient.get('/v1/drivers', { companyId });
    return Array.isArray(drivers) ? drivers.map(mapDriverResponse) : [];
  },

  /**
   * Get drivers by status
   */
  getDriversByStatus: async (status: string): Promise<Driver[]> => {
    const drivers = await apiClient.get(`/v1/drivers/status/${status}`);
    return Array.isArray(drivers) ? drivers.map(mapDriverResponse) : [];
  },

  /**
   * Create driver
   * Backend expects: name (single field), phone, email, licenseNumber, licenseExpiry, status
   */
  createDriver: async (companyId: number, data: any): Promise<Driver> => {
    const driver = await apiClient.post(`/v1/drivers?companyId=${companyId}`, data);
    return mapDriverResponse(driver);
  },

  /**
   * Update driver
   * Backend expects: name (single field), phone, email, licenseNumber, licenseExpiry, status
   */
  updateDriver: async (driverId: number, data: any): Promise<Driver> => {
    const driver = await apiClient.put(`/v1/drivers/${driverId}`, data);
    return mapDriverResponse(driver);
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
  assignToVehicle: async (driverId: number, vehicleId: number): Promise<Driver> => {
    const driver = await apiClient.post(`/v1/drivers/${driverId}/assign?vehicleId=${vehicleId}`);
    return mapDriverResponse(driver);
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
