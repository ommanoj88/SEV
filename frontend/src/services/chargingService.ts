import { apiClient } from './api';
import {
  ChargingStation,
  ChargingSession,
  ChargingSchedule,
  RouteOptimization,
  OptimizedRoute,
} from '../types';

/**
 * Charging Service
 * Handles all charging-related API calls
 * API Base: /api/v1/charging
 */
export const chargingService = {
  // ========== CHARGING SESSIONS ==========

  /**
   * Get all charging sessions
   */
  getAllSessions: async (params?: any): Promise<ChargingSession[]> => {
    return apiClient.get('/v1/charging/sessions', params);
  },

  /**
   * Get charging session by ID
   */
  getSessionById: async (sessionId: number): Promise<ChargingSession> => {
    return apiClient.get(`/v1/charging/sessions/${sessionId}`);
  },

  /**
   * Get charging sessions by vehicle
   */
  getSessionsByVehicle: async (vehicleId: number): Promise<ChargingSession[]> => {
    return apiClient.get(`/v1/charging/sessions/vehicle/${vehicleId}`);
  },

  /**
   * Get charging sessions by station
   */
  getSessionsByStation: async (stationId: number): Promise<ChargingSession[]> => {
    return apiClient.get(`/v1/charging/sessions/station/${stationId}`);
  },

  /**
   * Start a charging session
   */
  startSession: async (data: {
    vehicleId: number;
    stationId: number;
    startBatteryLevel: number;
  }): Promise<ChargingSession> => {
    return apiClient.post('/v1/charging/sessions/start', data);
  },

  /**
   * End a charging session
   */
  endSession: async (sessionId: number, data: {
    endBatteryLevel: number;
  }): Promise<ChargingSession> => {
    return apiClient.post(`/v1/charging/sessions/${sessionId}/end`, data);
  },

  /**
   * Cancel a charging session
   */
  cancelSession: async (sessionId: number, reason?: string): Promise<ChargingSession> => {
    return apiClient.post(`/v1/charging/sessions/${sessionId}/cancel`, { reason });
  },

  // ========== CHARGING STATIONS ==========

  /**
   * Get all charging stations
   */
  getAllStations: async (params?: any): Promise<ChargingStation[]> => {
    return apiClient.get('/v1/charging/stations', params);
  },

  /**
   * Get charging station by ID
   */
  getStationById: async (stationId: number): Promise<ChargingStation> => {
    return apiClient.get(`/v1/charging/stations/${stationId}`);
  },

  /**
   * Get available charging stations
   */
  getAvailableStations: async (): Promise<ChargingStation[]> => {
    return apiClient.get('/v1/charging/stations/available');
  },

  /**
   * Find nearest charging stations
   */
  getNearestStations: async (
    latitude: number,
    longitude: number,
    limit: number = 10
  ): Promise<ChargingStation[]> => {
    return apiClient.get('/v1/charging/stations/nearest', {
      latitude,
      longitude,
      limit,
    });
  },

  /**
   * Get stations by provider
   */
  getStationsByProvider: async (provider: string): Promise<ChargingStation[]> => {
    return apiClient.get(`/v1/charging/stations/provider/${provider}`);
  },

  /**
   * Create charging station
   */
  createStation: async (data: Partial<ChargingStation>): Promise<ChargingStation> => {
    return apiClient.post('/v1/charging/stations', data);
  },

  /**
   * Update charging station
   */
  updateStation: async (stationId: number, data: Partial<ChargingStation>): Promise<ChargingStation> => {
    return apiClient.put(`/v1/charging/stations/${stationId}`, data);
  },

  /**
   * Delete charging station
   */
  deleteStation: async (stationId: number): Promise<void> => {
    return apiClient.delete(`/v1/charging/stations/${stationId}`);
  },

  /**
   * Reserve a charging slot at a station
   */
  reserveSlot: async (stationId: number, vehicleId: number): Promise<{ reserved: boolean }> => {
    return apiClient.post(`/v1/charging/stations/${stationId}/reserve`, { vehicleId });
  },

  /**
   * Release a charging slot at a station
   */
  releaseSlot: async (stationId: number, vehicleId: number): Promise<{ released: boolean }> => {
    return apiClient.post(`/v1/charging/stations/${stationId}/release`, { vehicleId });
  },

  // ========== ROUTE OPTIMIZATION ==========

  /**
   * Optimize route with charging stops
   */
  optimizeRoute: async (data: RouteOptimization): Promise<OptimizedRoute> => {
    return apiClient.post('/v1/charging/route-optimization', data);
  },
};

export default chargingService;
