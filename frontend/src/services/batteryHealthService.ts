import api from './api';

export interface BatteryHealthData {
  id: number;
  vehicleId: number;
  soh: number;
  cycleCount: number;
  temperature?: number;
  internalResistance?: number;
  voltageDeviation?: number;
  currentSoc?: number;
  recordedAt: string;
  notes?: string;
}

export interface BatteryHealthRequest {
  vehicleId: number;
  soh: number;
  cycleCount: number;
  temperature?: number;
  internalResistance?: number;
  voltageDeviation?: number;
  currentSoc?: number;
  recordedAt?: string;
  notes?: string;
}

class BatteryHealthService {
  /**
   * Record new battery health data
   */
  async recordBatteryHealth(data: BatteryHealthRequest): Promise<BatteryHealthData> {
    const response = await api.post('/api/v1/battery-health', data);
    return response.data;
  }

  /**
   * Get battery health history for a vehicle
   */
  async getBatteryHealthHistory(vehicleId: number): Promise<BatteryHealthData[]> {
    const response = await api.get(`/api/v1/battery-health/vehicle/${vehicleId}`);
    return response.data;
  }

  /**
   * Get battery health history for a date range
   */
  async getBatteryHealthHistoryByDateRange(
    vehicleId: number,
    startTime: string,
    endTime: string
  ): Promise<BatteryHealthData[]> {
    const response = await api.get(`/api/v1/battery-health/vehicle/${vehicleId}/range`, {
      params: { startTime, endTime },
    });
    return response.data;
  }

  /**
   * Get latest battery health for a vehicle
   */
  async getLatestBatteryHealth(vehicleId: number): Promise<BatteryHealthData> {
    const response = await api.get(`/api/v1/battery-health/vehicle/${vehicleId}/latest`);
    return response.data;
  }

  /**
   * Find vehicles with low SOH
   */
  async findVehiclesWithLowSoh(threshold: number = 80): Promise<number[]> {
    const response = await api.get('/api/v1/battery-health/low-soh', {
      params: { threshold },
    });
    return response.data;
  }
}

const batteryHealthService = new BatteryHealthService();
export default batteryHealthService;
