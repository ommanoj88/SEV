import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { MaintenanceRecord, ServiceReminder, MaintenanceType, MaintenanceStatus, MaintenancePriority } from '../../types';
import maintenanceService from '../../services/maintenanceService';
import { RootState } from '../store';

interface MaintenanceState {
  records: MaintenanceRecord[];
  reminders: ServiceReminder[];
  loading: boolean;
  error: string | null;
}

// Mock maintenance data for development/fallback
const MOCK_RECORDS: MaintenanceRecord[] = [
  {
    id: 'MR1',
    vehicleId: '1',
    vehicleName: 'Tesla Model 3',
    type: MaintenanceType.ROUTINE,
    status: MaintenanceStatus.COMPLETED,
    priority: MaintenancePriority.MEDIUM,
    scheduledDate: '2025-10-15',
    completedDate: '2025-10-15',
    description: 'Routine maintenance - Tire rotation and alignment',
    odometer: 12500,
    cost: 2500,
    serviceProvider: 'EV Service Center',
    technician: 'Rajesh Kumar',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'MR2',
    vehicleId: '2',
    vehicleName: 'Tata Nexon EV',
    type: MaintenanceType.BATTERY_SERVICE,
    status: MaintenanceStatus.SCHEDULED,
    priority: MaintenancePriority.HIGH,
    scheduledDate: '2025-11-10',
    description: 'Battery health check and diagnostics',
    cost: 5000,
    serviceProvider: 'Authorized Tata Service',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

const MOCK_REMINDERS: ServiceReminder[] = [
  {
    id: 'SR1',
    vehicleId: '1',
    vehicleName: 'Tesla Model 3',
    type: MaintenanceType.ROUTINE,
    dueDate: '2025-11-15',
    dueOdometer: 13500,
    currentOdometer: 12500,
    daysUntilDue: 43,
    kmUntilDue: 1000,
    priority: MaintenancePriority.LOW,
    status: 'UPCOMING',
  },
  {
    id: 'SR2',
    vehicleId: '3',
    vehicleName: 'MG ZS EV',
    type: MaintenanceType.INSPECTION,
    dueDate: '2025-11-05',
    dueOdometer: 16000,
    currentOdometer: 15200,
    daysUntilDue: 3,
    kmUntilDue: 800,
    priority: MaintenancePriority.MEDIUM,
    status: 'DUE',
  },
];

const initialState: MaintenanceState = {
  records: MOCK_RECORDS,
  reminders: MOCK_REMINDERS,
  loading: false,
  error: null,
};

export const fetchRecords = createAsyncThunk('maintenance/fetchRecords', async () => {
  return await maintenanceService.getAllRecords();
});

export const fetchReminders = createAsyncThunk('maintenance/fetchReminders', async () => {
  return await maintenanceService.getReminders();
});

export const createRecord = createAsyncThunk(
  'maintenance/createRecord',
  async (data: any) => {
    return await maintenanceService.createRecord(data);
  }
);

export const updateRecord = createAsyncThunk(
  'maintenance/updateRecord',
  async ({ id, data }: { id: string; data: any }) => {
    return await maintenanceService.updateRecord(Number(id), data);
  }
);

export const fetchMaintenanceSchedule = createAsyncThunk(
  'maintenance/fetchMaintenanceSchedule',
  async () => {
    return await maintenanceService.getAllSchedules();
  }
);

export const scheduleMaintenance = createAsyncThunk(
  'maintenance/scheduleMaintenance',
  async (data: any) => {
    return await maintenanceService.createRecord(data);
  }
);

const maintenanceSlice = createSlice({
  name: 'maintenance',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchRecords.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchRecords.fulfilled, (state, action) => {
        state.loading = false;
        state.records = action.payload;
      })
      .addCase(fetchRecords.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch records';
        // Use mock data as fallback
        state.records = MOCK_RECORDS;
      })
      .addCase(fetchReminders.fulfilled, (state, action) => {
        state.reminders = action.payload;
      })
      .addCase(fetchReminders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch reminders';
        // Use mock data as fallback
        state.reminders = MOCK_REMINDERS;
      })
      .addCase(createRecord.fulfilled, (state, action) => {
        state.records.unshift(action.payload);
      })
      .addCase(updateRecord.fulfilled, (state, action) => {
        const index = state.records.findIndex(r => r.id === action.payload.id);
        if (index !== -1) {
          state.records[index] = action.payload;
        }
      });
  },
});

export const { clearError } = maintenanceSlice.actions;
export const selectRecords = (state: RootState) => state.maintenance.records;
export const selectReminders = (state: RootState) => state.maintenance.reminders;

export default maintenanceSlice.reducer;
