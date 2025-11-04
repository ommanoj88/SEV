import { apiClient } from './api';
import { Geofence, GeofenceFormData } from '../types';

/**
 * Geofence Service
 * Handles all geofence-related API calls
 * API Base: /api/v1/fleet/geofences
 */
export const geofenceService = {
  /**
   * Get all geofences
   */
  getAllGeofences: async (params?: any): Promise<Geofence[]> => {
    return apiClient.get('/v1/fleet/geofences', params);
  },

  /**
   * Get geofence by ID
   */
  getGeofenceById: async (geofenceId: number): Promise<Geofence> => {
    return apiClient.get(`/v1/fleet/geofences/${geofenceId}`);
  },

  /**
   * Get geofences by company
   */
  getGeofencesByCompany: async (companyId: number): Promise<Geofence[]> => {
    return apiClient.get(`/v1/fleet/geofences/company/${companyId}`);
  },

  /**
   * Get geofences by type
   */
  getGeofencesByType: async (type: string): Promise<Geofence[]> => {
    return apiClient.get(`/v1/fleet/geofences/type/${type}`);
  },

  /**
   * Get active geofences
   */
  getActiveGeofences: async (): Promise<Geofence[]> => {
    return apiClient.get('/v1/fleet/geofences/active');
  },

  /**
   * Create a new geofence
   */
  createGeofence: async (data: GeofenceFormData): Promise<Geofence> => {
    return apiClient.post('/v1/fleet/geofences', data);
  },

  /**
   * Update geofence
   */
  updateGeofence: async (geofenceId: number, data: Partial<GeofenceFormData>): Promise<Geofence> => {
    return apiClient.put(`/v1/fleet/geofences/${geofenceId}`, data);
  },

  /**
   * Delete geofence
   */
  deleteGeofence: async (geofenceId: number): Promise<void> => {
    return apiClient.delete(`/v1/fleet/geofences/${geofenceId}`);
  },

  /**
   * Check if a point is inside a geofence
   */
  checkPointInGeofence: async (
    geofenceId: number,
    latitude: number,
    longitude: number
  ): Promise<{ inside: boolean }> => {
    return apiClient.post(`/v1/fleet/geofences/${geofenceId}/point-check`, {
      latitude,
      longitude,
    });
  },

  /**
   * Get geofences for a specific vehicle
   */
  getGeofencesForVehicle: async (vehicleId: number): Promise<Geofence[]> => {
    return apiClient.get(`/v1/fleet/geofences/vehicle/${vehicleId}`);
  },
};

export default geofenceService;
