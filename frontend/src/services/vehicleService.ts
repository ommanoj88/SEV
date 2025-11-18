import { apiClient } from './api';
import { Vehicle, VehicleFormData, VehicleFilters } from '../types';

/**
 * Map backend vehicle response to frontend Vehicle type
 * Backend uses currentDriverId, frontend prefers assignedDriverId
 */
const mapVehicleResponse = (vehicle: any): Vehicle => {
  return {
    ...vehicle,
    assignedDriverId: vehicle.currentDriverId ? String(vehicle.currentDriverId) : vehicle.assignedDriverId,
  };
};

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
    const vehicles = await apiClient.get('/v1/vehicles', filters);
    return Array.isArray(vehicles) ? vehicles.map(mapVehicleResponse) : [];
  },

  /**
   * Get vehicle by ID
   */
  getVehicle: async (id: string | number): Promise<Vehicle> => {
    const vehicle = await apiClient.get(`/v1/vehicles/${id}`);
    return mapVehicleResponse(vehicle);
  },

  /**
   * Get vehicle by vehicle number
   */
  getVehicleByNumber: async (vehicleNumber: string): Promise<Vehicle> => {
    const vehicle = await apiClient.get(`/v1/vehicles/number/${vehicleNumber}`);
    return mapVehicleResponse(vehicle);
  },

  /**
   * Get vehicles by company
   */
  getVehiclesByCompany: async (companyId: number): Promise<Vehicle[]> => {
    const vehicles = await apiClient.get(`/v1/vehicles/company/${companyId}`);
    return Array.isArray(vehicles) ? vehicles.map(mapVehicleResponse) : [];
  },

  /**
   * Get vehicles by status
   */
  getVehiclesByStatus: async (status: string): Promise<Vehicle[]> => {
    const vehicles = await apiClient.get(`/v1/vehicles/status/${status}`);
    return Array.isArray(vehicles) ? vehicles.map(mapVehicleResponse) : [];
  },

  /**
   * Get low battery vehicles
   */
  getLowBatteryVehicles: async (threshold: number = 20): Promise<Vehicle[]> => {
    const vehicles = await apiClient.get('/v1/vehicles/low-battery', { threshold });
    return Array.isArray(vehicles) ? vehicles.map(mapVehicleResponse) : [];
  },

  /**
   * Create vehicle
   */
  createVehicle: async (data: VehicleFormData): Promise<Vehicle> => {
    const vehicle = await apiClient.post('/v1/vehicles', data);
    return mapVehicleResponse(vehicle);
  },

  /**
   * Update vehicle
   */
  updateVehicle: async (id: string | number, data: Partial<VehicleFormData>): Promise<Vehicle> => {
    const vehicle = await apiClient.put(`/v1/vehicles/${id}`, data);
    return mapVehicleResponse(vehicle);
  },

  /**
   * Delete vehicle
   */
  deleteVehicle: async (id: string | number): Promise<void> => {
    return apiClient.delete(`/v1/vehicles/${id}`);
  },

  /**
   * Update vehicle location
   */
  updateVehicleLocation: async (
    id: string | number,
    latitude: number,
    longitude: number
  ): Promise<Vehicle> => {
    const vehicle = await apiClient.patch(`/v1/vehicles/${id}/location`, {
      latitude,
      longitude,
    });
    return mapVehicleResponse(vehicle);
  },

  /**
   * Update vehicle battery level
   */
  updateVehicleBattery: async (id: string | number, currentBatterySoc: number): Promise<Vehicle> => {
    const vehicle = await apiClient.patch(`/v1/vehicles/${id}/battery`, {
      currentBatterySoc,
    });
    return mapVehicleResponse(vehicle);
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
