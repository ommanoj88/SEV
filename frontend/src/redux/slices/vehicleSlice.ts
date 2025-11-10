import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Vehicle, Trip, VehicleFormData, VehicleFilters, VehicleStatus, VehicleType, FuelType } from '../../types';
import vehicleService from '../../services/vehicleService';
import tripService from '../../services/tripService';
import { RootState } from '../store';

interface VehicleState {
  vehicles: Vehicle[];
  selectedVehicle: Vehicle | null;
  trips: Trip[];
  loading: boolean;
  error: string | null;
  filters: VehicleFilters;
}

// Mock vehicle data for development/fallback
const MOCK_VEHICLES: Vehicle[] = [
  {
    id: '1',
    fleetId: '1',
    vin: 'ABCD1234567890123',
    make: 'Tesla',
    model: 'Model 3',
    year: 2023,
    type: VehicleType.TWO_WHEELER,
    fuelType: FuelType.EV,
    status: VehicleStatus.ACTIVE,
    licensePlate: 'TS-001',
    color: 'White',
    battery: {
      stateOfCharge: 85,
      stateOfHealth: 95,
      range: 420,
      capacity: 75,
      temperature: 25,
    },
    location: { latitude: 28.6139, longitude: 77.2090, address: 'New Delhi, India' },
    odometer: 12500,
    assignedDriverId: '1',
    assignedDriverName: 'John Doe',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: '2',
    fleetId: '1',
    vin: 'EFGH5678901234567',
    make: 'Tata',
    model: 'Nexon EV',
    year: 2023,
    type: VehicleType.THREE_WHEELER,
    fuelType: FuelType.EV,
    status: VehicleStatus.CHARGING,
    licensePlate: 'TS-002',
    color: 'Blue',
    battery: {
      stateOfCharge: 45,
      stateOfHealth: 92,
      range: 280,
      capacity: 40.5,
      temperature: 28,
    },
    location: { latitude: 28.5721, longitude: 77.1884, address: 'Connaught Place, Delhi' },
    odometer: 8900,
    assignedDriverId: '2',
    assignedDriverName: 'Jane Smith',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: '3',
    fleetId: '1',
    vin: 'IJKL9012345678901',
    make: 'MG',
    model: 'ZS EV',
    year: 2022,
    type: VehicleType.LCV,
    fuelType: FuelType.EV,
    status: VehicleStatus.INACTIVE,
    licensePlate: 'TS-003',
    color: 'Red',
    battery: {
      stateOfCharge: 20,
      stateOfHealth: 88,
      range: 150,
      capacity: 44.5,
      temperature: 22,
    },
    location: { latitude: 28.6162, longitude: 77.2137, address: 'Lodhi Road, Delhi' },
    odometer: 15200,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

const initialState: VehicleState = {
  vehicles: [],
  selectedVehicle: null,
  trips: [],
  loading: false,
  error: null,
  filters: {},
};

export const fetchVehicles = createAsyncThunk(
  'vehicles/fetchVehicles',
  async (filters?: VehicleFilters) => {
    return await vehicleService.getVehicles(filters);
  }
);

export const fetchVehicle = createAsyncThunk(
  'vehicles/fetchVehicle',
  async (id: string) => {
    return await vehicleService.getVehicle(Number(id));
  }
);

export const createVehicle = createAsyncThunk(
  'vehicles/createVehicle',
  async (data: VehicleFormData) => {
    return await vehicleService.createVehicle(data);
  }
);

export const updateVehicle = createAsyncThunk(
  'vehicles/updateVehicle',
  async ({ id, data }: { id: string; data: Partial<VehicleFormData> }) => {
    return await vehicleService.updateVehicle(Number(id), data);
  }
);

export const deleteVehicle = createAsyncThunk(
  'vehicles/deleteVehicle',
  async (id: string) => {
    await vehicleService.deleteVehicle(Number(id));
    return id;
  }
);

export const fetchVehicleTrips = createAsyncThunk(
  'vehicles/fetchTrips',
  async (vehicleId: string) => {
    return await tripService.getTripsByVehicle(Number(vehicleId));
  }
);

const vehicleSlice = createSlice({
  name: 'vehicles',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<VehicleFilters>) => {
      state.filters = action.payload;
    },
    clearFilters: (state) => {
      state.filters = {};
    },
    updateVehicleRealtime: (state, action: PayloadAction<Vehicle>) => {
      const index = state.vehicles.findIndex(v => v.id === action.payload.id);
      if (index !== -1) {
        state.vehicles[index] = action.payload;
      }
      if (state.selectedVehicle?.id === action.payload.id) {
        state.selectedVehicle = action.payload;
      }
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchVehicles.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchVehicles.fulfilled, (state, action) => {
        state.loading = false;
        state.vehicles = action.payload;
      })
      .addCase(fetchVehicles.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch vehicles';
        // Use mock data as fallback when API fails
        state.vehicles = MOCK_VEHICLES;
      })
      .addCase(fetchVehicle.fulfilled, (state, action) => {
        state.selectedVehicle = action.payload;
      })
      .addCase(createVehicle.fulfilled, (state, action) => {
        state.vehicles.push(action.payload);
      })
      .addCase(updateVehicle.fulfilled, (state, action) => {
        const index = state.vehicles.findIndex(v => v.id === action.payload.id);
        if (index !== -1) {
          state.vehicles[index] = action.payload;
        }
        if (state.selectedVehicle?.id === action.payload.id) {
          state.selectedVehicle = action.payload;
        }
      })
      .addCase(deleteVehicle.fulfilled, (state, action) => {
        state.vehicles = state.vehicles.filter(v => v.id !== action.payload);
      })
      .addCase(fetchVehicleTrips.fulfilled, (state, action) => {
        state.trips = action.payload;
      });
  },
});

export const { setFilters, clearFilters, updateVehicleRealtime, clearError } = vehicleSlice.actions;

export const selectVehicles = (state: RootState) => state.vehicles.vehicles;
export const selectSelectedVehicle = (state: RootState) => state.vehicles.selectedVehicle;
export const selectVehicleTrips = (state: RootState) => state.vehicles.trips;
export const selectVehicleLoading = (state: RootState) => state.vehicles.loading;
export const selectVehicleFilters = (state: RootState) => state.vehicles.filters;

export default vehicleSlice.reducer;
