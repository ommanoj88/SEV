import { apiClient } from './api';
import { Vehicle, VehicleFormData, VehicleFilters } from '../types';

/**
 * Vehicle Service
 * Handles all vehicle-related API calls
 * API Base: /api/v1/vehicles
 */
export const vehicleService = {
  /**
   * Get all vehicles
   */
  getVehicles: async (filters?: VehicleFilters): Promise<Vehicle[]> => {
    return apiClient.get('/v1/vehicles', filters);
  },

  /**
   * Get vehicle by ID
   */
  getVehicle: async (id: number): Promise<Vehicle> => {
    return apiClient.get(`/v1/vehicles/${id}`);
  },

  /**
   * Get vehicle by vehicle number
   */
  getVehicleByNumber: async (vehicleNumber: string): Promise<Vehicle> => {
    return apiClient.get(`/v1/vehicles/number/${vehicleNumber}`);
  },

  /**
   * Get vehicles by company
   */
  getVehiclesByCompany: async (companyId: number): Promise<Vehicle[]> => {
    return apiClient.get(`/v1/vehicles/company/${companyId}`);
  },

  /**
   * Get vehicles by status
   */
  getVehiclesByStatus: async (status: string): Promise<Vehicle[]> => {
    return apiClient.get(`/v1/vehicles/status/${status}`);
  },

  /**
   * Get low battery vehicles
   */
  getLowBatteryVehicles: async (threshold: number = 20): Promise<Vehicle[]> => {
    return apiClient.get('/v1/vehicles/low-battery', { threshold });
  },

  /**
   * Create vehicle
   */
  createVehicle: async (data: VehicleFormData): Promise<Vehicle> => {
    return apiClient.post('/v1/vehicles', data);
  },

  /**
   * Update vehicle
   */
  updateVehicle: async (id: number, data: Partial<VehicleFormData>): Promise<Vehicle> => {
    return apiClient.put(`/v1/vehicles/${id}`, data);
  },

  /**
   * Delete vehicle
   */
  deleteVehicle: async (id: number): Promise<void> => {
    return apiClient.delete(`/v1/vehicles/${id}`);
  },

  /**
   * Update vehicle location
   */
  updateVehicleLocation: async (
    id: number,
    latitude: number,
    longitude: number
  ): Promise<Vehicle> => {
    return apiClient.patch(`/v1/vehicles/${id}/location`, {
      latitude,
      longitude,
    });
  },

  /**
   * Update vehicle battery level
   */
  updateVehicleBattery: async (id: number, currentBatterySoc: number): Promise<Vehicle> => {
    return apiClient.patch(`/v1/vehicles/${id}/battery`, {
      currentBatterySoc,
    });
  },

  /**
   * Get fleet composition by fuel type
   */
  getFleetComposition: async (companyId: number): Promise<any> => {
    return apiClient.get(`/v1/vehicles/company/${companyId}/fleet-composition`);
  },

  /**
   * Get low battery vehicles for a company
   */
  getLowBatteryVehiclesByCompany: async (companyId: number, threshold: number = 20): Promise<Vehicle[]> => {
    return apiClient.get(`/v1/vehicles/company/${companyId}/low-battery-vehicles`, { threshold });
  },

  /**
   * Get low fuel vehicles for a company
   */
  getLowFuelVehicles: async (companyId: number, threshold: number = 20): Promise<Vehicle[]> => {
    return apiClient.get(`/v1/vehicles/company/${companyId}/low-fuel-vehicles`, { threshold });
  },
};

export default vehicleService;
