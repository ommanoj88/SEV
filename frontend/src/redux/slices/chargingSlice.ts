import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { ChargingStation, ChargingSession, ChargingStationType, ChargingStationStatus, ChargingSessionStatus } from '../../types';
import chargingService from '../../services/chargingService';
import { RootState } from '../store';

interface ChargingState {
  stations: ChargingStation[];
  sessions: ChargingSession[];
  selectedStation: ChargingStation | null;
  selectedSession: ChargingSession | null;
  nearestStations: ChargingStation[];
  routeOptimization: any | null;
  loading: boolean;
  error: string | null;
  filters: {
    providerId?: number;
    vehicleId?: number;
    status?: string;
  };
}

// Mock charging data for development/fallback
const MOCK_STATIONS: ChargingStation[] = [
  {
    id: '1',
    name: 'Delhi Central Charging Hub',
    location: { latitude: 28.6139, longitude: 77.2090, address: 'New Delhi, India' },
    type: ChargingStationType.DC_FAST,
    status: ChargingStationStatus.ACTIVE,
    totalPorts: 12,
    availablePorts: 4,
    powerOutput: 150,
    costPerKwh: 12,
    amenities: ['WiFi', 'Coffee', 'Restroom'],
    operatingHours: '24/7',
    provider: 'EV Power India',
    distance: 2.5,
    rating: 4.5,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: '2',
    name: 'Noida EV Station',
    location: { latitude: 28.5721, longitude: 77.1884, address: 'Noida, Uttar Pradesh' },
    type: ChargingStationType.LEVEL_2,
    status: ChargingStationStatus.ACTIVE,
    totalPorts: 8,
    availablePorts: 3,
    powerOutput: 50,
    costPerKwh: 10,
    amenities: ['WiFi', 'Parking'],
    operatingHours: '6AM - 10PM',
    provider: 'ChargeGrid',
    distance: 15.3,
    rating: 4.2,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

const MOCK_SESSIONS: ChargingSession[] = [
  {
    id: 'S1',
    vehicleId: '1',
    vehicleName: 'Tesla Model 3',
    stationId: '1',
    stationName: 'Delhi Central Charging Hub',
    driverId: '1',
    driverName: 'John Doe',
    startTime: new Date(Date.now() - 3600000).toISOString(),
    endTime: new Date().toISOString(),
    startBatteryLevel: 20,
    endBatteryLevel: 85,
    energyDelivered: 48,
    duration: 60,
    cost: 576,
    status: ChargingSessionStatus.COMPLETED,
    paymentStatus: 'PAID',
  },
  {
    id: 'S2',
    vehicleId: '2',
    vehicleName: 'Tata Nexon EV',
    stationId: '2',
    stationName: 'Noida EV Station',
    driverId: '2',
    driverName: 'Jane Smith',
    startTime: new Date(Date.now() - 1800000).toISOString(),
    startBatteryLevel: 35,
    endBatteryLevel: 95,
    energyDelivered: 24,
    status: ChargingSessionStatus.CHARGING,
    paymentStatus: 'PENDING',
  },
];

const initialState: ChargingState = {
  stations: [],
  sessions: [],
  selectedStation: null,
  selectedSession: null,
  nearestStations: [],
  routeOptimization: null,
  loading: false,
  error: null,
  filters: {},
};

// Stations
export const fetchAllStations = createAsyncThunk(
  'charging/fetchAllStations',
  async (params?: any) => {
    return await chargingService.getAllStations(params);
  }
);

export const fetchStationById = createAsyncThunk(
  'charging/fetchStationById',
  async (stationId: number) => {
    return await chargingService.getStationById(stationId);
  }
);

export const fetchAvailableStations = createAsyncThunk(
  'charging/fetchAvailableStations',
  async () => {
    return await chargingService.getAvailableStations();
  }
);

export const fetchNearestStations = createAsyncThunk(
  'charging/fetchNearestStations',
  async (data: { latitude: number; longitude: number; limit?: number }) => {
    return await chargingService.getNearestStations(data.latitude, data.longitude, data.limit);
  }
);

export const fetchStationsByProvider = createAsyncThunk(
  'charging/fetchStationsByProvider',
  async (providerId: number) => {
    return await chargingService.getStationsByProvider(providerId);
  }
);

export const createChargingStation = createAsyncThunk(
  'charging/createStation',
  async (data: Partial<ChargingStation>) => {
    return await chargingService.createStation(data);
  }
);

export const updateChargingStation = createAsyncThunk(
  'charging/updateStation',
  async ({ stationId, data }: { stationId: number; data: Partial<ChargingStation> }) => {
    return await chargingService.updateStation(stationId, data);
  }
);

export const deleteChargingStation = createAsyncThunk(
  'charging/deleteStation',
  async (stationId: number) => {
    await chargingService.deleteStation(stationId);
    return stationId;
  }
);

// Sessions
export const fetchAllSessions = createAsyncThunk(
  'charging/fetchAllSessions',
  async (params?: any) => {
    return await chargingService.getAllSessions(params);
  }
);

export const fetchSessionById = createAsyncThunk(
  'charging/fetchSessionById',
  async (sessionId: number) => {
    return await chargingService.getSessionById(sessionId);
  }
);

export const fetchSessionsByVehicle = createAsyncThunk(
  'charging/fetchSessionsByVehicle',
  async (vehicleId: number) => {
    return await chargingService.getSessionsByVehicle(vehicleId);
  }
);

export const fetchSessionsByStation = createAsyncThunk(
  'charging/fetchSessionsByStation',
  async (stationId: number) => {
    return await chargingService.getSessionsByStation(stationId);
  }
);

export const startChargingSession = createAsyncThunk(
  'charging/startSession',
  async (data: {
    vehicleId: number;
    stationId: number;
    targetBatteryLevel?: number;
  }) => {
    return await chargingService.startSession(data);
  }
);

export const endChargingSession = createAsyncThunk(
  'charging/endSession',
  async (sessionId: number) => {
    return await chargingService.endSession(sessionId);
  }
);

export const cancelChargingSession = createAsyncThunk(
  'charging/cancelSession',
  async (sessionId: number) => {
    return await chargingService.cancelSession(sessionId);
  }
);

// Slot Management
export const reserveChargingSlot = createAsyncThunk(
  'charging/reserveSlot',
  async (data: { stationId: number; vehicleId: number; duration?: number }) => {
    return await chargingService.reserveSlot(data.stationId, data.vehicleId);
  }
);

export const releaseChargingSlot = createAsyncThunk(
  'charging/releaseSlot',
  async (data: { stationId: number; vehicleId: number }) => {
    return await chargingService.releaseSlot(data.stationId, data.vehicleId);
  }
);

// Route Optimization
export const optimizeChargingRoute = createAsyncThunk(
  'charging/optimizeRoute',
  async (data: {
    origin: { latitude: number; longitude: number };
    destination: { latitude: number; longitude: number };
    vehicleId: number;
  }) => {
    return await chargingService.optimizeRoute(data);
  }
);

const chargingSlice = createSlice({
  name: 'charging',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    selectStation: (state, action: PayloadAction<ChargingStation | null>) => {
      state.selectedStation = action.payload;
    },
    selectSession: (state, action: PayloadAction<ChargingSession | null>) => {
      state.selectedSession = action.payload;
    },
    setFilters: (
      state,
      action: PayloadAction<{
        providerId?: number;
        vehicleId?: number;
        status?: string;
      }>
    ) => {
      state.filters = action.payload;
    },
    clearFilters: (state) => {
      state.filters = {};
    },
  },
  extraReducers: (builder) => {
    // Fetch all stations
    builder
      .addCase(fetchAllStations.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllStations.fulfilled, (state, action) => {
        state.loading = false;
        state.stations = action.payload;
      })
      .addCase(fetchAllStations.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch stations';
        // Use mock data as fallback
        state.stations = MOCK_STATIONS;
      });

    // Fetch station by ID
    builder
      .addCase(fetchStationById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchStationById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedStation = action.payload;
      })
      .addCase(fetchStationById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch station';
      });

    // Fetch available stations
    builder
      .addCase(fetchAvailableStations.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchAvailableStations.fulfilled, (state, action) => {
        state.loading = false;
        state.stations = action.payload;
      })
      .addCase(fetchAvailableStations.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch available stations';
      });

    // Fetch nearest stations
    builder
      .addCase(fetchNearestStations.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchNearestStations.fulfilled, (state, action) => {
        state.loading = false;
        state.nearestStations = action.payload;
      })
      .addCase(fetchNearestStations.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch nearest stations';
      });

    // Fetch stations by provider
    builder
      .addCase(fetchStationsByProvider.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchStationsByProvider.fulfilled, (state, action) => {
        state.loading = false;
        state.stations = action.payload;
      })
      .addCase(fetchStationsByProvider.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch provider stations';
      });

    // Create station
    builder
      .addCase(createChargingStation.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createChargingStation.fulfilled, (state, action) => {
        state.loading = false;
        state.stations.push(action.payload);
        state.selectedStation = action.payload;
      })
      .addCase(createChargingStation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create station';
      });

    // Update station
    builder
      .addCase(updateChargingStation.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateChargingStation.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.stations.findIndex(s => s.id === action.payload.id);
        if (index !== -1) {
          state.stations[index] = action.payload;
        }
        if (state.selectedStation?.id === action.payload.id) {
          state.selectedStation = action.payload;
        }
      })
      .addCase(updateChargingStation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update station';
      });

    // Delete station
    builder
      .addCase(deleteChargingStation.pending, (state) => {
        state.loading = true;
      })
      .addCase(deleteChargingStation.fulfilled, (state, action) => {
        state.loading = false;
        state.stations = state.stations.filter(s => s.id !== action.payload);
        if (state.selectedStation?.id === action.payload) {
          state.selectedStation = null;
        }
      })
      .addCase(deleteChargingStation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete station';
      });

    // Fetch all sessions
    builder
      .addCase(fetchAllSessions.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchAllSessions.fulfilled, (state, action) => {
        state.loading = false;
        state.sessions = action.payload;
      })
      .addCase(fetchAllSessions.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch sessions';
        // Use mock data as fallback
        state.sessions = MOCK_SESSIONS;
      });

    // Fetch session by ID
    builder
      .addCase(fetchSessionById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchSessionById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedSession = action.payload;
      })
      .addCase(fetchSessionById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch session';
      });

    // Fetch sessions by vehicle
    builder
      .addCase(fetchSessionsByVehicle.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchSessionsByVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.sessions = action.payload;
      })
      .addCase(fetchSessionsByVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicle sessions';
      });

    // Fetch sessions by station
    builder
      .addCase(fetchSessionsByStation.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchSessionsByStation.fulfilled, (state, action) => {
        state.loading = false;
        state.sessions = action.payload;
      })
      .addCase(fetchSessionsByStation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch station sessions';
      });

    // Start session
    builder
      .addCase(startChargingSession.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(startChargingSession.fulfilled, (state, action) => {
        state.loading = false;
        state.sessions.unshift(action.payload);
        state.selectedSession = action.payload;
      })
      .addCase(startChargingSession.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to start session';
      });

    // End session
    builder
      .addCase(endChargingSession.pending, (state) => {
        state.loading = true;
      })
      .addCase(endChargingSession.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.sessions.findIndex(s => s.id === action.payload.id);
        if (index !== -1) {
          state.sessions[index] = action.payload;
        }
      })
      .addCase(endChargingSession.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to end session';
      });

    // Cancel session
    builder
      .addCase(cancelChargingSession.pending, (state) => {
        state.loading = true;
      })
      .addCase(cancelChargingSession.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.sessions.findIndex(s => s.id === action.payload.id);
        if (index !== -1) {
          state.sessions.splice(index, 1);
        }
      })
      .addCase(cancelChargingSession.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to cancel session';
      });

    // Reserve slot
    builder
      .addCase(reserveChargingSlot.pending, (state) => {
        state.loading = true;
      })
      .addCase(reserveChargingSlot.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(reserveChargingSlot.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to reserve slot';
      });

    // Release slot
    builder
      .addCase(releaseChargingSlot.pending, (state) => {
        state.loading = true;
      })
      .addCase(releaseChargingSlot.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(releaseChargingSlot.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to release slot';
      });

    // Optimize route
    builder
      .addCase(optimizeChargingRoute.pending, (state) => {
        state.loading = true;
      })
      .addCase(optimizeChargingRoute.fulfilled, (state, action) => {
        state.loading = false;
        state.routeOptimization = action.payload;
      })
      .addCase(optimizeChargingRoute.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to optimize route';
      });
  },
});

// Selectors
export const selectStations = (state: RootState) => state.charging.stations;
export const selectSessions = (state: RootState) => state.charging.sessions;
export const selectSelectedStation = (state: RootState) => state.charging.selectedStation;
export const selectSelectedSession = (state: RootState) => state.charging.selectedSession;
export const selectNearestStations = (state: RootState) => state.charging.nearestStations;
export const selectRouteOptimization = (state: RootState) => state.charging.routeOptimization;
export const selectChargingLoading = (state: RootState) => state.charging.loading;
export const selectChargingError = (state: RootState) => state.charging.error;
export const selectChargingFilters = (state: RootState) => state.charging.filters;

export const { clearError, selectStation, selectSession, setFilters, clearFilters } = chargingSlice.actions;

export default chargingSlice.reducer;
