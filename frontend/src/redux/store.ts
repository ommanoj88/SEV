import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import vehicleReducer from './slices/vehicleSlice';
import chargingReducer from './slices/chargingSlice';
import driverReducer from './slices/driverSlice';
import maintenanceReducer from './slices/maintenanceSlice';
import analyticsReducer from './slices/analyticsSlice';
import notificationReducer from './slices/notificationSlice';
import billingReducer from './slices/billingSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    vehicles: vehicleReducer,
    charging: chargingReducer,
    drivers: driverReducer,
    maintenance: maintenanceReducer,
    analytics: analyticsReducer,
    notifications: notificationReducer,
    billing: billingReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore these action types
        ignoredActions: ['auth/setFirebaseUser'],
        // Ignore these field paths in all actions
        ignoredActionPaths: ['payload.timestamp', 'meta.arg'],
        // Ignore these paths in the state
        ignoredPaths: ['auth.firebaseUser'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
