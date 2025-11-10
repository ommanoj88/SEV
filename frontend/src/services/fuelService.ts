import { apiClient } from './api';
import { FuelStation, FuelStationType } from '../types';

/**
 * Fuel Service
 * Handles all fuel station-related API calls for ICE vehicles
 * API Base: /api/v1/fuel
 */
export const fuelService = {
  /**
   * Get all fuel stations
   */
  getAllStations: async (params?: any): Promise<FuelStation[]> => {
    return apiClient.get('/v1/fuel/stations', params);
  },

  /**
   * Get fuel station by ID
   */
  getStationById: async (stationId: number): Promise<FuelStation> => {
    return apiClient.get(`/v1/fuel/stations/${stationId}`);
  },

  /**
   * Get available fuel stations
   */
  getAvailableStations: async (): Promise<FuelStation[]> => {
    return apiClient.get('/v1/fuel/stations/available');
  },

  /**
   * Find nearest fuel stations
   */
  getNearestStations: async (
    latitude: number,
    longitude: number,
    limit: number = 10
  ): Promise<FuelStation[]> => {
    return apiClient.get('/v1/fuel/stations/nearest', {
      latitude,
      longitude,
      limit,
    });
  },

  /**
   * Get fuel stations by type
   */
  getStationsByFuelType: async (fuelType: FuelStationType): Promise<FuelStation[]> => {
    return apiClient.get(`/v1/fuel/stations/type/${fuelType}`);
  },

  /**
   * Get stations by brand
   */
  getStationsByBrand: async (brand: string): Promise<FuelStation[]> => {
    return apiClient.get(`/v1/fuel/stations/brand/${brand}`);
  },

  /**
   * Create fuel station
   */
  createStation: async (data: Partial<FuelStation>): Promise<FuelStation> => {
    return apiClient.post('/v1/fuel/stations', data);
  },

  /**
   * Update fuel station
   */
  updateStation: async (stationId: number, data: Partial<FuelStation>): Promise<FuelStation> => {
    return apiClient.put(`/v1/fuel/stations/${stationId}`, data);
  },

  /**
   * Delete fuel station
   */
  deleteStation: async (stationId: number): Promise<void> => {
    return apiClient.delete(`/v1/fuel/stations/${stationId}`);
  },
};

export default fuelService;
