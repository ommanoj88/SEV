import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Trip, TripStatus } from '../../types';
import tripService from '../../services/tripService';
import { RootState } from '../store';

interface TripState {
  trips: Trip[];
  ongoingTrips: Trip[];
  selectedTrip: Trip | null;
  loading: boolean;
  error: string | null;
  filters: {
    vehicleId?: number;
    driverId?: number;
    companyId?: number;
    status?: TripStatus;
  };
}

const initialState: TripState = {
  trips: [],
  ongoingTrips: [],
  selectedTrip: null,
  loading: false,
  error: null,
  filters: {},
};

// Async thunks
export const fetchAllTrips = createAsyncThunk(
  'trips/fetchAllTrips',
  async (params?: any) => {
    return await tripService.getAllTrips(params);
  }
);

export const fetchTripsByVehicle = createAsyncThunk(
  'trips/fetchByVehicle',
  async (vehicleId: number) => {
    return await tripService.getTripsByVehicle(vehicleId);
  }
);

export const fetchTripsByDriver = createAsyncThunk(
  'trips/fetchByDriver',
  async (driverId: number) => {
    return await tripService.getTripsByDriver(driverId);
  }
);

export const fetchOngoingTrips = createAsyncThunk(
  'trips/fetchOngoing',
  async () => {
    return await tripService.getOngoingTrips();
  }
);

export const fetchTripById = createAsyncThunk(
  'trips/fetchById',
  async (tripId: number) => {
    return await tripService.getTripById(tripId);
  }
);

export const startTrip = createAsyncThunk(
  'trips/start',
  async (data: {
    vehicleId: number;
    driverId?: number;
    startLocation: { latitude: number; longitude: number };
    startBatterySoc?: number;
  }) => {
    return await tripService.startTrip(data);
  }
);

export const endTrip = createAsyncThunk(
  'trips/end',
  async ({
    tripId,
    data,
  }: {
    tripId: number;
    data: {
      endLocation: { latitude: number; longitude: number };
      endBatterySoc?: number;
      distance?: number;
      energyConsumed?: number;
    };
  }) => {
    return await tripService.endTrip(tripId, data);
  }
);

export const pauseTrip = createAsyncThunk(
  'trips/pause',
  async (tripId: number) => {
    return await tripService.pauseTrip(tripId);
  }
);

export const resumeTrip = createAsyncThunk(
  'trips/resume',
  async (tripId: number) => {
    return await tripService.resumeTrip(tripId);
  }
);

export const cancelTrip = createAsyncThunk(
  'trips/cancel',
  async ({ tripId, reason }: { tripId: number; reason?: string }) => {
    return await tripService.cancelTrip(tripId, reason);
  }
);

const tripSlice = createSlice({
  name: 'trips',
  initialState,
  reducers: {
    setFilters: (
      state,
      action: PayloadAction<{
        vehicleId?: number;
        driverId?: number;
        companyId?: number;
        status?: TripStatus;
      }>
    ) => {
      state.filters = action.payload;
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    clearError: (state) => {
      state.error = null;
    },
    selectTrip: (state, action: PayloadAction<Trip | null>) => {
      state.selectedTrip = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch all trips
    builder
      .addCase(fetchAllTrips.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllTrips.fulfilled, (state, action) => {
        state.loading = false;
        state.trips = action.payload;
      })
      .addCase(fetchAllTrips.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch trips';
      });

    // Fetch by vehicle
    builder
      .addCase(fetchTripsByVehicle.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchTripsByVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.trips = action.payload;
      })
      .addCase(fetchTripsByVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicle trips';
      });

    // Fetch by driver
    builder
      .addCase(fetchTripsByDriver.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchTripsByDriver.fulfilled, (state, action) => {
        state.loading = false;
        state.trips = action.payload;
      })
      .addCase(fetchTripsByDriver.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch driver trips';
      });

    // Fetch ongoing
    builder
      .addCase(fetchOngoingTrips.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchOngoingTrips.fulfilled, (state, action) => {
        state.loading = false;
        state.ongoingTrips = action.payload;
      })
      .addCase(fetchOngoingTrips.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch ongoing trips';
      });

    // Fetch by ID
    builder
      .addCase(fetchTripById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchTripById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedTrip = action.payload;
      })
      .addCase(fetchTripById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch trip';
      });

    // Start trip
    builder
      .addCase(startTrip.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(startTrip.fulfilled, (state, action) => {
        state.loading = false;
        state.ongoingTrips.push(action.payload);
        state.selectedTrip = action.payload;
      })
      .addCase(startTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to start trip';
      });

    // End trip
    builder
      .addCase(endTrip.pending, (state) => {
        state.loading = true;
      })
      .addCase(endTrip.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.ongoingTrips.findIndex(
          (trip) => trip.id === action.payload.id
        );
        if (index !== -1) {
          state.ongoingTrips.splice(index, 1);
        }
        state.trips.push(action.payload);
      })
      .addCase(endTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to end trip';
      });

    // Pause trip
    builder
      .addCase(pauseTrip.pending, (state) => {
        state.loading = true;
      })
      .addCase(pauseTrip.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.ongoingTrips.findIndex(
          (trip) => trip.id === action.payload.id
        );
        if (index !== -1) {
          state.ongoingTrips[index] = action.payload;
        }
      })
      .addCase(pauseTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to pause trip';
      });

    // Resume trip
    builder
      .addCase(resumeTrip.pending, (state) => {
        state.loading = true;
      })
      .addCase(resumeTrip.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.ongoingTrips.findIndex(
          (trip) => trip.id === action.payload.id
        );
        if (index !== -1) {
          state.ongoingTrips[index] = action.payload;
        }
      })
      .addCase(resumeTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to resume trip';
      });

    // Cancel trip
    builder
      .addCase(cancelTrip.pending, (state) => {
        state.loading = true;
      })
      .addCase(cancelTrip.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.ongoingTrips.findIndex(
          (trip) => trip.id === action.payload.id
        );
        if (index !== -1) {
          state.ongoingTrips.splice(index, 1);
        }
      })
      .addCase(cancelTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to cancel trip';
      });
  },
});

// Selectors
export const selectAllTrips = (state: RootState) => state.trips.trips;
export const selectOngoingTrips = (state: RootState) => state.trips.ongoingTrips;
export const selectSelectedTrip = (state: RootState) => state.trips.selectedTrip;
export const selectTripLoading = (state: RootState) => state.trips.loading;
export const selectTripError = (state: RootState) => state.trips.error;
export const selectTripFilters = (state: RootState) => state.trips.filters;

export const { setFilters, clearFilters, clearError, selectTrip } =
  tripSlice.actions;

export default tripSlice.reducer;
