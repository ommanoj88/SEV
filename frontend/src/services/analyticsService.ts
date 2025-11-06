import { apiClient } from './api';
import {
  FleetAnalytics,
  UtilizationData,
  EnergyConsumption,
  CostAnalysis,
  TCOAnalysis,
  CarbonFootprint,
  BatteryAnalytics,
} from '../types';

/**
 * Analytics Service
 * Handles all analytics-related API calls
 * API Base: /api/v1/analytics
 */
export const analyticsService = {
  /**
   * Get fleet summary and analytics
   */
  getFleetSummary: async (params?: any): Promise<any> => {
    return apiClient.get('/v1/analytics/fleet', params);
  },

  /**
   * Get fleet analytics by company
   */
  getFleetAnalyticsByCompany: async (companyId: number, params?: any): Promise<FleetAnalytics> => {
    return apiClient.get<FleetAnalytics>(`/v1/analytics/fleet/company/${companyId}`, params);
  },

  /**
   * Get utilization reports
   */
  getUtilizationReports: async (params?: any): Promise<UtilizationData[]> => {
    return apiClient.get('/v1/analytics/utilization-reports', params);
  },

  /**
   * Get utilization report by vehicle
   */
  getUtilizationByVehicle: async (vehicleId: number, startDate?: string, endDate?: string): Promise<any> => {
    return apiClient.get(`/v1/analytics/utilization/${vehicleId}`, { startDate, endDate });
  },

  /**
   * Get cost analytics
   */
  getCostAnalytics: async (params?: any): Promise<CostAnalysis[]> => {
    return apiClient.get('/v1/analytics/cost-analytics', params);
  },

  /**
   * Get cost analytics by vehicle
   */
  getCostAnalyticsByVehicle: async (vehicleId: number): Promise<any> => {
    return apiClient.get(`/v1/analytics/cost-analytics/${vehicleId}`);
  },

  /**
   * Get total cost of ownership (TCO) analysis
   */
  getTCOAnalysis: async (vehicleId: number): Promise<TCOAnalysis> => {
    return apiClient.get(`/v1/analytics/tco-analysis/${vehicleId}`);
  },

  /**
   * Get energy consumption analytics
   */
  getEnergyConsumption: async (params?: any): Promise<EnergyConsumption[]> => {
    return apiClient.get('/v1/analytics/energy-consumption', params);
  },

  /**
   * Get carbon footprint data
   */
  getCarbonFootprint: async (params?: any): Promise<CarbonFootprint[]> => {
    return apiClient.get('/v1/analytics/carbon-footprint', params);
  },

  /**
   * Get battery analytics
   */
  getBatteryAnalytics: async (): Promise<BatteryAnalytics> => {
    return apiClient.get('/v1/analytics/battery');
  },

  /**
   * Export analytics report
   */
  exportReport: async (type: string, format: string, params?: any): Promise<Blob> => {
    return apiClient.post('/v1/analytics/export', { type, format, ...params });
  },
};

export default analyticsService;
