import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {
  FleetAnalytics,
  UtilizationData,
  EnergyConsumption,
  CostAnalysis,
  TCOAnalysis,
  CarbonFootprint,
  BatteryAnalytics,
  TimeRangeFilter,
} from '../../types';
import analyticsService from '../../services/analyticsService';
import { RootState } from '../store';

interface AnalyticsState {
  fleetSummary: any | null;
  fleetAnalytics: FleetAnalytics | null;
  utilizationReports: UtilizationData[];
  utilizationByVehicle: any | null;
  costAnalytics: CostAnalysis[];
  costAnalyticsByVehicle: any | null;
  tcoAnalysis: TCOAnalysis | null;
  energyConsumption: EnergyConsumption[];
  carbonFootprint: CarbonFootprint[];
  batteryAnalytics: BatteryAnalytics | null;
  exportedReport: Blob | null;
  loading: boolean;
  error: string | null;
  filters: {
    companyId?: number;
    vehicleId?: number;
    timeRange?: TimeRangeFilter;
  };
}

// Mock analytics data for development/fallback
const MOCK_FLEET_ANALYTICS: FleetAnalytics = {
  totalVehicles: 3,
  activeVehicles: 2,
  inactiveVehicles: 0,
  chargingVehicles: 1,
  maintenanceVehicles: 0,
  inTripVehicles: 0,
  averageBatteryLevel: 62,
  averageBatteryHealth: 92,
  totalDistance: 42300,
  totalTrips: 634,
  totalEnergyConsumed: 8500,
  utilizationRate: 68,
  averageUtilization: 8.5,
  summary: {
    totalVehicles: 3,
    totalTrips: 634,
    totalDistance: 42300,
    averageUtilization: 8.5,
  },
};

const MOCK_ENERGY_CONSUMPTION: EnergyConsumption[] = [
  {
    date: '2025-10-28',
    energyConsumed: 120.5,
    distance: 450,
    efficiency: 26.8,
    chargingCost: 1446,
  },
  {
    date: '2025-10-29',
    energyConsumed: 98.2,
    distance: 380,
    efficiency: 25.8,
    chargingCost: 1178.4,
  },
  {
    date: '2025-10-30',
    energyConsumed: 112.3,
    distance: 420,
    efficiency: 26.7,
    chargingCost: 1347.6,
  },
];

const MOCK_COST_ANALYSIS: CostAnalysis[] = [
  {
    period: 'October 2025',
    energyCost: 12850,
    maintenanceCost: 2100,
    insuranceCost: 3500,
    otherCosts: 800,
    totalCost: 19250,
    costPerKm: 0.45,
    costPerVehicle: 6417,
  },
];

const MOCK_CARBON_FOOTPRINT: CarbonFootprint[] = [
  {
    date: '2025-10-28',
    energyConsumed: 120.5,
    co2Avoided: 36.15,
    equivalentTrees: 2,
  },
  {
    date: '2025-10-29',
    energyConsumed: 98.2,
    co2Avoided: 29.46,
    equivalentTrees: 2,
  },
  {
    date: '2025-10-30',
    energyConsumed: 112.3,
    co2Avoided: 33.69,
    equivalentTrees: 2,
  },
];

const MOCK_BATTERY_ANALYTICS: BatteryAnalytics = {
  averageSOC: 62,
  averageSOH: 92,
  totalCapacity: 159.5,
  degradationRate: 0.08,
  chargingCycles: 450,
  batteryHealthDistribution: [
    { range: '90-100%', count: 2 },
    { range: '80-90%', count: 1 },
  ],
  healthTrend: [
    { date: '2025-10-01', avgHealth: 93.2 },
    { date: '2025-10-15', avgHealth: 92.8 },
    { date: '2025-10-30', avgHealth: 92.0 },
  ],
};

const initialState: AnalyticsState = {
  fleetSummary: MOCK_FLEET_ANALYTICS,
  fleetAnalytics: MOCK_FLEET_ANALYTICS,
  utilizationReports: [],
  utilizationByVehicle: null,
  costAnalytics: MOCK_COST_ANALYSIS,
  costAnalyticsByVehicle: null,
  tcoAnalysis: null,
  energyConsumption: MOCK_ENERGY_CONSUMPTION,
  carbonFootprint: MOCK_CARBON_FOOTPRINT,
  batteryAnalytics: MOCK_BATTERY_ANALYTICS,
  exportedReport: null,
  loading: false,
  error: null,
  filters: {},
};

// Fleet Analytics
export const fetchFleetSummary = createAsyncThunk(
  'analytics/fetchFleetSummary',
  async (params?: any) => {
    return await analyticsService.getFleetSummary(params);
  }
);

export const fetchFleetAnalyticsByCompany = createAsyncThunk(
  'analytics/fetchFleetAnalyticsByCompany',
  async (companyId: number) => {
    return await analyticsService.getFleetAnalyticsByCompany(companyId);
  }
);

// Utilization
export const fetchUtilizationReports = createAsyncThunk(
  'analytics/fetchUtilizationReports',
  async (params?: any) => {
    return await analyticsService.getUtilizationReports(params);
  }
);

export const fetchUtilizationByVehicle = createAsyncThunk(
  'analytics/fetchUtilizationByVehicle',
  async (data: { vehicleId: number; startDate?: string; endDate?: string }) => {
    return await analyticsService.getUtilizationByVehicle(data.vehicleId, data.startDate, data.endDate);
  }
);

// Cost Analytics
export const fetchCostAnalytics = createAsyncThunk(
  'analytics/fetchCostAnalytics',
  async (params?: any) => {
    return await analyticsService.getCostAnalytics(params);
  }
);

export const fetchCostAnalyticsByVehicle = createAsyncThunk(
  'analytics/fetchCostAnalyticsByVehicle',
  async (vehicleId: number) => {
    return await analyticsService.getCostAnalyticsByVehicle(vehicleId);
  }
);

// TCO Analysis
export const fetchTCOAnalysis = createAsyncThunk(
  'analytics/fetchTCOAnalysis',
  async (vehicleId: number) => {
    return await analyticsService.getTCOAnalysis(vehicleId);
  }
);

// Energy Consumption
export const fetchEnergyConsumption = createAsyncThunk(
  'analytics/fetchEnergyConsumption',
  async (params?: any) => {
    return await analyticsService.getEnergyConsumption(params);
  }
);

// Carbon Footprint
export const fetchCarbonFootprint = createAsyncThunk(
  'analytics/fetchCarbonFootprint',
  async (params?: any) => {
    return await analyticsService.getCarbonFootprint(params);
  }
);

// Battery Analytics
export const fetchBatteryAnalytics = createAsyncThunk(
  'analytics/fetchBatteryAnalytics',
  async () => {
    return await analyticsService.getBatteryAnalytics();
  }
);

// Export Report
export const exportAnalyticsReport = createAsyncThunk(
  'analytics/exportReport',
  async (data: { type: string; format: string; params?: any }) => {
    return await analyticsService.exportReport(data.type, data.format, data.params);
  }
);

const analyticsSlice = createSlice({
  name: 'analytics',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setFilters: (
      state,
      action: PayloadAction<{
        companyId?: number;
        vehicleId?: number;
        timeRange?: TimeRangeFilter;
      }>
    ) => {
      state.filters = action.payload;
    },
    clearFilters: (state) => {
      state.filters = {};
    },
  },
  extraReducers: (builder) => {
    // Fleet Summary
    builder
      .addCase(fetchFleetSummary.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFleetSummary.fulfilled, (state, action) => {
        state.loading = false;
        state.fleetSummary = action.payload;
      })
      .addCase(fetchFleetSummary.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch fleet summary';
        // Keep mock data from initialState
        state.fleetSummary = MOCK_FLEET_ANALYTICS;
      });

    // Fleet Analytics by Company
    builder
      .addCase(fetchFleetAnalyticsByCompany.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchFleetAnalyticsByCompany.fulfilled, (state, action) => {
        state.loading = false;
        state.fleetAnalytics = action.payload;
      })
      .addCase(fetchFleetAnalyticsByCompany.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch fleet analytics';
      });

    // Utilization Reports
    builder
      .addCase(fetchUtilizationReports.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchUtilizationReports.fulfilled, (state, action) => {
        state.loading = false;
        state.utilizationReports = action.payload;
      })
      .addCase(fetchUtilizationReports.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch utilization reports';
      });

    // Utilization by Vehicle
    builder
      .addCase(fetchUtilizationByVehicle.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchUtilizationByVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.utilizationByVehicle = action.payload;
      })
      .addCase(fetchUtilizationByVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicle utilization';
      });

    // Cost Analytics
    builder
      .addCase(fetchCostAnalytics.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchCostAnalytics.fulfilled, (state, action) => {
        state.loading = false;
        state.costAnalytics = action.payload;
      })
      .addCase(fetchCostAnalytics.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch cost analytics';
      });

    // Cost Analytics by Vehicle
    builder
      .addCase(fetchCostAnalyticsByVehicle.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchCostAnalyticsByVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.costAnalyticsByVehicle = action.payload;
      })
      .addCase(fetchCostAnalyticsByVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicle cost analytics';
      });

    // TCO Analysis
    builder
      .addCase(fetchTCOAnalysis.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchTCOAnalysis.fulfilled, (state, action) => {
        state.loading = false;
        state.tcoAnalysis = action.payload;
      })
      .addCase(fetchTCOAnalysis.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch TCO analysis';
      });

    // Energy Consumption
    builder
      .addCase(fetchEnergyConsumption.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchEnergyConsumption.fulfilled, (state, action) => {
        state.loading = false;
        state.energyConsumption = action.payload;
      })
      .addCase(fetchEnergyConsumption.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch energy consumption';
      });

    // Carbon Footprint
    builder
      .addCase(fetchCarbonFootprint.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchCarbonFootprint.fulfilled, (state, action) => {
        state.loading = false;
        state.carbonFootprint = action.payload;
      })
      .addCase(fetchCarbonFootprint.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch carbon footprint';
      });

    // Battery Analytics
    builder
      .addCase(fetchBatteryAnalytics.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchBatteryAnalytics.fulfilled, (state, action) => {
        state.loading = false;
        state.batteryAnalytics = action.payload;
      })
      .addCase(fetchBatteryAnalytics.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch battery analytics';
      });

    // Export Report
    builder
      .addCase(exportAnalyticsReport.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(exportAnalyticsReport.fulfilled, (state, action) => {
        state.loading = false;
        state.exportedReport = action.payload;
      })
      .addCase(exportAnalyticsReport.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to export report';
      });
  },
});

// Selectors
export const selectFleetSummary = (state: RootState) => state.analytics.fleetSummary;
export const selectFleetAnalytics = (state: RootState) => state.analytics.fleetAnalytics;
export const selectUtilizationReports = (state: RootState) => state.analytics.utilizationReports;
export const selectUtilizationByVehicle = (state: RootState) => state.analytics.utilizationByVehicle;
export const selectCostAnalytics = (state: RootState) => state.analytics.costAnalytics;
export const selectCostAnalyticsByVehicle = (state: RootState) => state.analytics.costAnalyticsByVehicle;
export const selectTCOAnalysis = (state: RootState) => state.analytics.tcoAnalysis;
export const selectEnergyConsumption = (state: RootState) => state.analytics.energyConsumption;
export const selectCarbonFootprint = (state: RootState) => state.analytics.carbonFootprint;
export const selectBatteryAnalytics = (state: RootState) => state.analytics.batteryAnalytics;
export const selectExportedReport = (state: RootState) => state.analytics.exportedReport;
export const selectAnalyticsLoading = (state: RootState) => state.analytics.loading;
export const selectAnalyticsError = (state: RootState) => state.analytics.error;
export const selectAnalyticsFilters = (state: RootState) => state.analytics.filters;

export const { clearError, setFilters, clearFilters } = analyticsSlice.actions;

export default analyticsSlice.reducer;
