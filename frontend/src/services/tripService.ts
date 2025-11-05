import { apiClient } from './api';
import { Trip } from '../types';

/**
 * Trip Service
 * Handles all trip-related API calls
 * API Base: /api/v1/fleet/trips
 */
export const tripService = {
  /**
   * Get all trips with optional filtering
   */
  getAllTrips: async (params?: any): Promise<Trip[]> => {
    return apiClient.get('/v1/fleet/trips', params);
  },

  /**
   * Get trips by vehicle
   */
  getTripsByVehicle: async (vehicleId: number, params?: any): Promise<Trip[]> => {
    return apiClient.get(`/v1/fleet/trips/vehicle/${vehicleId}`, params);
  },

  /**
   * Get trips by driver
   */
  getTripsByDriver: async (driverId: number, params?: any): Promise<Trip[]> => {
    return apiClient.get(`/v1/fleet/trips/driver/${driverId}`, params);
  },

  /**
   * Get trips by company
   */
  getTripsByCompany: async (companyId: number, params?: any): Promise<Trip[]> => {
    return apiClient.get(`/v1/fleet/trips/company/${companyId}`, params);
  },

  /**
   * Get ongoing trips
   */
  getOngoingTrips: async (): Promise<Trip[]> => {
    return apiClient.get('/v1/fleet/trips/ongoing');
  },

  /**
   * Get trip by ID
   */
  getTripById: async (tripId: number): Promise<Trip> => {
    return apiClient.get(`/v1/fleet/trips/${tripId}`);
  },

  /**
   * Start a new trip
   */
  startTrip: async (data: {
    vehicleId: number;
    driverId?: number;
    startLocation: { latitude: number; longitude: number };
    startBatterySoc?: number;
  }): Promise<Trip> => {
    return apiClient.post('/v1/fleet/trips/start', data);
  },

  /**
   * End an ongoing trip
   */
  endTrip: async (tripId: number, data: {
    endLocation: { latitude: number; longitude: number };
    endBatterySoc?: number;
    distance?: number;
    energyConsumed?: number;
  }): Promise<Trip> => {
    return apiClient.post(`/v1/fleet/trips/${tripId}/end`, data);
  },

  /**
   * Pause a trip
   */
  pauseTrip: async (tripId: number): Promise<Trip> => {
    return apiClient.post(`/v1/fleet/trips/${tripId}/pause`, {});
  },

  /**
   * Resume a paused trip
   */
  resumeTrip: async (tripId: number): Promise<Trip> => {
    return apiClient.post(`/v1/fleet/trips/${tripId}/resume`, {});
  },

  /**
   * Cancel a trip
   */
  cancelTrip: async (tripId: number, reason?: string): Promise<Trip> => {
    return apiClient.post(`/v1/fleet/trips/${tripId}/cancel`, { reason });
  },

  /**
   * Update trip efficiency metrics
   */
  updateTripMetrics: async (tripId: number, metrics: {
    harshAccelerationCount?: number;
    harshBrakingCount?: number;
    overspeedingCount?: number;
    efficiencyScore?: number;
  }): Promise<Trip> => {
    return apiClient.patch(`/v1/fleet/trips/${tripId}/metrics`, metrics);
  },
};

export default tripService;
