import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Geofence, GeofenceType } from '../../types';
import geofenceService from '../../services/geofenceService';
import { RootState } from '../store';

interface GeofenceState {
  geofences: Geofence[];
  activeGeofences: Geofence[];
  selectedGeofence: Geofence | null;
  loading: boolean;
  error: string | null;
  filters: {
    companyId?: number;
    type?: GeofenceType;
    searchTerm?: string;
  };
}

const initialState: GeofenceState = {
  geofences: [],
  activeGeofences: [],
  selectedGeofence: null,
  loading: false,
  error: null,
  filters: {},
};

// Async thunks
export const fetchAllGeofences = createAsyncThunk(
  'geofences/fetchAll',
  async (params?: any) => {
    return await geofenceService.getAllGeofences(params);
  }
);

export const fetchGeofenceById = createAsyncThunk(
  'geofences/fetchById',
  async (geofenceId: number) => {
    return await geofenceService.getGeofenceById(geofenceId);
  }
);

export const fetchGeofencesByCompany = createAsyncThunk(
  'geofences/fetchByCompany',
  async (companyId: number) => {
    return await geofenceService.getGeofencesByCompany(companyId);
  }
);

export const fetchGeofencesByType = createAsyncThunk(
  'geofences/fetchByType',
  async (type: GeofenceType) => {
    return await geofenceService.getGeofencesByType(type);
  }
);

export const fetchActiveGeofences = createAsyncThunk(
  'geofences/fetchActive',
  async () => {
    return await geofenceService.getActiveGeofences();
  }
);

export const createGeofence = createAsyncThunk(
  'geofences/create',
  async (data: Partial<Geofence>) => {
    return await geofenceService.createGeofence(data as any);
  }
);

export const updateGeofence = createAsyncThunk(
  'geofences/update',
  async ({ geofenceId, data }: { geofenceId: number; data: Partial<Geofence> }) => {
    return await geofenceService.updateGeofence(geofenceId, data);
  }
);

export const deleteGeofence = createAsyncThunk(
  'geofences/delete',
  async (geofenceId: number) => {
    await geofenceService.deleteGeofence(geofenceId);
    return geofenceId;
  }
);

export const checkPointInGeofence = createAsyncThunk(
  'geofences/checkPoint',
  async (data: { latitude: number; longitude: number; geofenceId?: number }) => {
    if (!data.geofenceId) throw new Error('geofenceId is required');
    return await geofenceService.checkPointInGeofence(data.geofenceId, data.latitude, data.longitude);
  }
);

export const getGeofencesForVehicle = createAsyncThunk(
  'geofences/fetchForVehicle',
  async (vehicleId: number) => {
    return await geofenceService.getGeofencesForVehicle(vehicleId);
  }
);

const geofenceSlice = createSlice({
  name: 'geofences',
  initialState,
  reducers: {
    setFilters: (
      state,
      action: PayloadAction<{
        companyId?: number;
        type?: GeofenceType;
        searchTerm?: string;
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
    selectGeofence: (state, action: PayloadAction<Geofence | null>) => {
      state.selectedGeofence = action.payload;
    },
  },
  extraReducers: (builder) => {
    // Fetch all geofences
    builder
      .addCase(fetchAllGeofences.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllGeofences.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences = action.payload;
      })
      .addCase(fetchAllGeofences.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch geofences';
      });

    // Fetch by ID
    builder
      .addCase(fetchGeofenceById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchGeofenceById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedGeofence = action.payload;
      })
      .addCase(fetchGeofenceById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch geofence';
      });

    // Fetch by company
    builder
      .addCase(fetchGeofencesByCompany.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchGeofencesByCompany.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences = action.payload;
      })
      .addCase(fetchGeofencesByCompany.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch company geofences';
      });

    // Fetch by type
    builder
      .addCase(fetchGeofencesByType.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchGeofencesByType.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences = action.payload;
      })
      .addCase(fetchGeofencesByType.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch geofences by type';
      });

    // Fetch active geofences
    builder
      .addCase(fetchActiveGeofences.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchActiveGeofences.fulfilled, (state, action) => {
        state.loading = false;
        state.activeGeofences = action.payload;
      })
      .addCase(fetchActiveGeofences.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch active geofences';
      });

    // Create geofence
    builder
      .addCase(createGeofence.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createGeofence.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences.push(action.payload);
        state.selectedGeofence = action.payload;
      })
      .addCase(createGeofence.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create geofence';
      });

    // Update geofence
    builder
      .addCase(updateGeofence.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateGeofence.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.geofences.findIndex(
          (g) => g.id === action.payload.id
        );
        if (index !== -1) {
          state.geofences[index] = action.payload;
        }
        if (state.selectedGeofence?.id === action.payload.id) {
          state.selectedGeofence = action.payload;
        }
      })
      .addCase(updateGeofence.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update geofence';
      });

    // Delete geofence
    builder
      .addCase(deleteGeofence.pending, (state) => {
        state.loading = true;
      })
      .addCase(deleteGeofence.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences = state.geofences.filter(
          (g) => g.id !== action.payload
        );
        if (state.selectedGeofence?.id === action.payload) {
          state.selectedGeofence = null;
        }
      })
      .addCase(deleteGeofence.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete geofence';
      });

    // Check point in geofence
    builder
      .addCase(checkPointInGeofence.pending, (state) => {
        state.loading = true;
      })
      .addCase(checkPointInGeofence.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(checkPointInGeofence.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to check point location';
      });

    // Get geofences for vehicle
    builder
      .addCase(getGeofencesForVehicle.pending, (state) => {
        state.loading = true;
      })
      .addCase(getGeofencesForVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.geofences = action.payload;
      })
      .addCase(getGeofencesForVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicle geofences';
      });
  },
});

// Selectors
export const selectAllGeofences = (state: RootState) => state.geofences.geofences;
export const selectActiveGeofences = (state: RootState) => state.geofences.activeGeofences;
export const selectSelectedGeofence = (state: RootState) => state.geofences.selectedGeofence;
export const selectGeofenceLoading = (state: RootState) => state.geofences.loading;
export const selectGeofenceError = (state: RootState) => state.geofences.error;
export const selectGeofenceFilters = (state: RootState) => state.geofences.filters;

export const { setFilters, clearFilters, clearError, selectGeofence } =
  geofenceSlice.actions;

export default geofenceSlice.reducer;
