import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {
  Driver,
  DriverFormData,
  DriverBehavior,
  DriverLeaderboard,
  DriverAttendance,
  DriverStatus,
} from '../../types';
import driverService from '../../services/driverService';
import { RootState } from '../store';

interface DriverState {
  drivers: Driver[];
  selectedDriver: Driver | null;
  driverBehavior: DriverBehavior[];
  driverAssignments: any[];
  driverAttendance: DriverAttendance[];
  performanceScore: { score: number; rating: number } | null;
  leaderboard: DriverLeaderboard[];
  loading: boolean;
  error: string | null;
  filters: {
    companyId?: number;
    status?: string;
  };
}

// Mock driver data for development/fallback
const MOCK_DRIVERS: Driver[] = [
  {
    id: '1',
    fleetId: '1',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@fleet.com',
    phone: '+91-9999-1111',
    licenseNumber: 'DL-001',
    licenseExpiry: '2025-12-31',
    status: DriverStatus.ACTIVE,
    dateOfBirth: '1990-05-15',
    address: 'New Delhi, India',
    emergencyContact: {
      name: 'Jane Doe',
      phone: '+91-9999-1112',
      relationship: 'Spouse',
    },
    performanceScore: 92,
    totalTrips: 245,
    totalDistance: 12500,
    totalDrivingHours: 750,
    averageRating: 4.8,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: '2',
    fleetId: '1',
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'jane.smith@fleet.com',
    phone: '+91-9999-2222',
    licenseNumber: 'DL-002',
    licenseExpiry: '2025-09-30',
    status: DriverStatus.ACTIVE,
    dateOfBirth: '1992-08-22',
    address: 'Gurugram, Haryana',
    emergencyContact: {
      name: 'Robert Smith',
      phone: '+91-9999-2223',
      relationship: 'Brother',
    },
    performanceScore: 88,
    totalTrips: 189,
    totalDistance: 9800,
    totalDrivingHours: 650,
    averageRating: 4.6,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: '3',
    fleetId: '1',
    firstName: 'Rajesh',
    lastName: 'Kumar',
    email: 'rajesh.kumar@fleet.com',
    phone: '+91-9999-3333',
    licenseNumber: 'DL-003',
    licenseExpiry: '2024-06-15',
    status: DriverStatus.SUSPENDED,
    dateOfBirth: '1988-03-10',
    address: 'Noida, Uttar Pradesh',
    performanceScore: 65,
    totalTrips: 412,
    totalDistance: 18900,
    totalDrivingHours: 1200,
    averageRating: 3.9,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

const MOCK_LEADERBOARD: DriverLeaderboard[] = [
  {
    driverId: '1',
    driverName: 'John Doe',
    performanceScore: 92,
    totalTrips: 245,
    totalDistance: 12500,
    fuelEfficiency: 95,
    safetyScore: 94,
    rank: 1,
  },
  {
    driverId: '2',
    driverName: 'Jane Smith',
    performanceScore: 88,
    totalTrips: 189,
    totalDistance: 9800,
    fuelEfficiency: 91,
    safetyScore: 89,
    rank: 2,
  },
];

const initialState: DriverState = {
  drivers: [],
  selectedDriver: null,
  driverBehavior: [],
  driverAssignments: [],
  driverAttendance: [],
  performanceScore: null,
  leaderboard: [],
  loading: false,
  error: null,
  filters: {},
};

// Fetching Drivers
export const fetchAllDrivers = createAsyncThunk(
  'drivers/fetchAll',
  async (params?: any) => {
    return await driverService.getAllDrivers(params);
  }
);

export const fetchDriverById = createAsyncThunk(
  'drivers/fetchById',
  async (driverId: number) => {
    return await driverService.getDriverById(driverId);
  }
);

export const fetchDriversByCompany = createAsyncThunk(
  'drivers/fetchByCompany',
  async (companyId: number) => {
    return await driverService.getDriversByCompany(companyId);
  }
);

export const fetchDriversByStatus = createAsyncThunk(
  'drivers/fetchByStatus',
  async (status: string) => {
    return await driverService.getDriversByStatus(status);
  }
);

// CRUD Operations
export const createDriver = createAsyncThunk(
  'drivers/create',
  async ({ companyId, data }: { companyId: number; data: DriverFormData }) => {
    return await driverService.createDriver(companyId, data);
  }
);

export const updateDriver = createAsyncThunk(
  'drivers/update',
  async ({ driverId, data }: { driverId: number; data: Partial<DriverFormData> }) => {
    return await driverService.updateDriver(driverId, data);
  }
);

export const deleteDriver = createAsyncThunk(
  'drivers/delete',
  async (driverId: number) => {
    await driverService.deleteDriver(driverId);
    return driverId;
  }
);

// Behavior and Assignments
export const fetchDriverBehavior = createAsyncThunk(
  'drivers/fetchBehavior',
  async (driverId: number) => {
    return await driverService.getDriverBehavior(driverId);
  }
);

export const fetchDriverAssignments = createAsyncThunk(
  'drivers/fetchAssignments',
  async (driverId: number) => {
    return await driverService.getDriverAssignments(driverId);
  }
);

export const assignDriverToVehicle = createAsyncThunk(
  'drivers/assignToVehicle',
  async ({ driverId, vehicleId }: { driverId: number; vehicleId: number }) => {
    return await driverService.assignToVehicle(driverId, vehicleId);
  }
);

export const updateAssignment = createAsyncThunk(
  'drivers/updateAssignment',
  async ({ assignmentId, data }: { assignmentId: number; data: any }) => {
    return await driverService.updateAssignment(assignmentId, data);
  }
);

// Performance and Scoring
export const fetchPerformanceScore = createAsyncThunk(
  'drivers/fetchPerformanceScore',
  async (driverId: number) => {
    return await driverService.getPerformanceScore(driverId);
  }
);

export const fetchLeaderboard = createAsyncThunk(
  'drivers/fetchLeaderboard',
  async (limit?: number) => {
    return await driverService.getLeaderboard(limit);
  }
);

const driverSlice = createSlice({
  name: 'drivers',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    selectDriver: (state, action: PayloadAction<Driver | null>) => {
      state.selectedDriver = action.payload;
    },
    setFilters: (
      state,
      action: PayloadAction<{
        companyId?: number;
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
    // Fetch all drivers
    builder
      .addCase(fetchAllDrivers.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllDrivers.fulfilled, (state, action) => {
        state.loading = false;
        state.drivers = action.payload;
      })
      .addCase(fetchAllDrivers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch drivers';
        // Use mock data as fallback
        state.drivers = MOCK_DRIVERS;
      });

    // Fetch driver by ID
    builder
      .addCase(fetchDriverById.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchDriverById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedDriver = action.payload;
      })
      .addCase(fetchDriverById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch driver';
      });

    // Fetch drivers by company
    builder
      .addCase(fetchDriversByCompany.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchDriversByCompany.fulfilled, (state, action) => {
        state.loading = false;
        state.drivers = action.payload;
      })
      .addCase(fetchDriversByCompany.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch company drivers';
      });

    // Fetch drivers by status
    builder
      .addCase(fetchDriversByStatus.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchDriversByStatus.fulfilled, (state, action) => {
        state.loading = false;
        state.drivers = action.payload;
      })
      .addCase(fetchDriversByStatus.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch drivers by status';
      });

    // Create driver
    builder
      .addCase(createDriver.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createDriver.fulfilled, (state, action) => {
        state.loading = false;
        state.drivers.push(action.payload);
        state.selectedDriver = action.payload;
      })
      .addCase(createDriver.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create driver';
      });

    // Update driver
    builder
      .addCase(updateDriver.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateDriver.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.drivers.findIndex(d => d.id === action.payload.id);
        if (index !== -1) {
          state.drivers[index] = action.payload;
        }
        if (state.selectedDriver?.id === action.payload.id) {
          state.selectedDriver = action.payload;
        }
      })
      .addCase(updateDriver.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update driver';
      });

    // Delete driver
    builder
      .addCase(deleteDriver.pending, (state) => {
        state.loading = true;
      })
      .addCase(deleteDriver.fulfilled, (state, action) => {
        state.loading = false;
        state.drivers = state.drivers.filter(d => d.id !== String(action.payload));
        if (state.selectedDriver?.id === String(action.payload)) {
          state.selectedDriver = null;
        }
      })
      .addCase(deleteDriver.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete driver';
      });

    // Fetch driver behavior
    builder
      .addCase(fetchDriverBehavior.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchDriverBehavior.fulfilled, (state, action) => {
        state.loading = false;
        state.driverBehavior = action.payload;
      })
      .addCase(fetchDriverBehavior.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch driver behavior';
      });

    // Fetch driver assignments
    builder
      .addCase(fetchDriverAssignments.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchDriverAssignments.fulfilled, (state, action) => {
        state.loading = false;
        state.driverAssignments = action.payload;
      })
      .addCase(fetchDriverAssignments.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch driver assignments';
      });

    // Assign driver to vehicle
    builder
      .addCase(assignDriverToVehicle.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(assignDriverToVehicle.fulfilled, (state, action) => {
        state.loading = false;
        state.driverAssignments.push(action.payload);
      })
      .addCase(assignDriverToVehicle.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to assign driver to vehicle';
      });

    // Update assignment
    builder
      .addCase(updateAssignment.pending, (state) => {
        state.loading = true;
      })
      .addCase(updateAssignment.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.driverAssignments.findIndex(a => a.id === action.payload.id);
        if (index !== -1) {
          state.driverAssignments[index] = action.payload;
        }
      })
      .addCase(updateAssignment.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update assignment';
      });

    // Fetch performance score
    builder
      .addCase(fetchPerformanceScore.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchPerformanceScore.fulfilled, (state, action) => {
        state.loading = false;
        state.performanceScore = action.payload;
      })
      .addCase(fetchPerformanceScore.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch performance score';
      });

    // Fetch leaderboard
    builder
      .addCase(fetchLeaderboard.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchLeaderboard.fulfilled, (state, action) => {
        state.loading = false;
        state.leaderboard = action.payload;
      })
      .addCase(fetchLeaderboard.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch leaderboard';
        // Use mock data as fallback
        state.leaderboard = MOCK_LEADERBOARD;
      });
  },
});

// Selectors
export const selectAllDrivers = (state: RootState) => state.drivers.drivers;
export const selectSelectedDriver = (state: RootState) => state.drivers.selectedDriver;
export const selectDriverBehavior = (state: RootState) => state.drivers.driverBehavior;
export const selectDriverAssignments = (state: RootState) => state.drivers.driverAssignments;
export const selectDriverAttendance = (state: RootState) => state.drivers.driverAttendance;
export const selectPerformanceScore = (state: RootState) => state.drivers.performanceScore;
export const selectLeaderboard = (state: RootState) => state.drivers.leaderboard;
export const selectDriverLoading = (state: RootState) => state.drivers.loading;
export const selectDriverError = (state: RootState) => state.drivers.error;
export const selectDriverFilters = (state: RootState) => state.drivers.filters;

export const { clearError, selectDriver, setFilters, clearFilters } = driverSlice.actions;

export default driverSlice.reducer;
