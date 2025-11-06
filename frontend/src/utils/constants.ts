// API Configuration
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';
export const WS_URL = process.env.REACT_APP_WEBSOCKET_URL || 'ws://localhost:8080/ws';

// Firebase Configuration
export const FIREBASE_CONFIG = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY || "AIzaSyCZAd5WmJE7SrUYj4yNZDG5sltaEnrUQgk",
  authDomain: process.env.REACT_APP_FIREBASE_AUTH_DOMAIN || "rentvat.firebaseapp.com",
  projectId: process.env.REACT_APP_FIREBASE_PROJECT_ID || "rentvat",
  storageBucket: process.env.REACT_APP_FIREBASE_STORAGE_BUCKET || "rentvat.firebasestorage.app",
  messagingSenderId: process.env.REACT_APP_FIREBASE_MESSAGING_SENDER_ID || "59823572862",
  appId: process.env.REACT_APP_FIREBASE_APP_ID || "1:59823572862:web:0e8e7f53f82ac3b2a7152f",
  measurementId: process.env.REACT_APP_FIREBASE_MEASUREMENT_ID || "G-6QHKKBTFYN",
};

// Mapbox Configuration
export const MAPBOX_TOKEN = process.env.REACT_APP_MAPBOX_TOKEN || '';
export const DEFAULT_MAP_CENTER: [number, number] = [-122.4194, 37.7749]; // San Francisco
export const DEFAULT_MAP_ZOOM = 12;

// App Configuration
export const APP_NAME = process.env.REACT_APP_NAME || 'EV Fleet Management';
export const APP_VERSION = process.env.REACT_APP_VERSION || '1.0.0';

// Pagination
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [5, 10, 25, 50, 100];

// Chart Colors
export const CHART_COLORS = {
  primary: '#1976d2',
  secondary: '#4caf50',
  error: '#f44336',
  warning: '#ff9800',
  info: '#2196f3',
  success: '#4caf50',
  purple: '#9c27b0',
  pink: '#e91e63',
  orange: '#ff5722',
  teal: '#009688',
  indigo: '#3f51b5',
  cyan: '#00bcd4',
};

// Vehicle Status Colors
export const VEHICLE_STATUS_COLORS = {
  ACTIVE: '#4caf50',
  INACTIVE: '#ff9800',
  CHARGING: '#2196f3',
  MAINTENANCE: '#f44336',
  IN_TRIP: '#9c27b0',
};

// Battery Thresholds
export const BATTERY_LOW_THRESHOLD = 20;
export const BATTERY_CRITICAL_THRESHOLD = 10;
export const BATTERY_OPTIMAL_MIN = 20;
export const BATTERY_OPTIMAL_MAX = 80;

// Distance and Energy
export const KM_TO_MILES = 0.621371;
export const MILES_TO_KM = 1.60934;
export const AVERAGE_ENERGY_CONSUMPTION = 0.2; // kWh per km

// Date Formats
export const DATE_FORMAT = 'MMM dd, yyyy';
export const TIME_FORMAT = 'HH:mm';
export const DATETIME_FORMAT = 'MMM dd, yyyy HH:mm';
export const DATE_INPUT_FORMAT = 'yyyy-MM-dd';

// Validation
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const PHONE_REGEX = /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/;
export const VIN_LENGTH = 17;
export const LICENSE_PLATE_REGEX = /^[A-Z0-9-]{2,10}$/i;

// Local Storage Keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'ev_fleet_auth_token',
  USER_PREFERENCES: 'ev_fleet_user_preferences',
  THEME_MODE: 'ev_fleet_theme_mode',
  SIDEBAR_STATE: 'ev_fleet_sidebar_state',
};

// WebSocket Events
export const WS_EVENTS = {
  CONNECT: 'connect',
  DISCONNECT: 'disconnect',
  LOCATION_UPDATE: 'location_update',
  BATTERY_UPDATE: 'battery_update',
  VEHICLE_STATUS_UPDATE: 'vehicle_status_update',
  ALERT: 'alert',
  NOTIFICATION: 'notification',
  CHARGING_UPDATE: 'charging_update',
};

// Error Messages
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Network error. Please check your connection.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  SESSION_EXPIRED: 'Your session has expired. Please log in again.',
  SERVER_ERROR: 'Server error. Please try again later.',
  VALIDATION_ERROR: 'Please check your input and try again.',
  NOT_FOUND: 'The requested resource was not found.',
};

// Success Messages
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Welcome back!',
  LOGOUT_SUCCESS: 'You have been logged out successfully.',
  SAVE_SUCCESS: 'Changes saved successfully.',
  DELETE_SUCCESS: 'Item deleted successfully.',
  UPDATE_SUCCESS: 'Updated successfully.',
  CREATE_SUCCESS: 'Created successfully.',
};

// Routes
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',
  DASHBOARD: '/dashboard',
  FLEET: '/fleet',
  FLEET_DETAILS: '/fleet/:vehicleId',
  CHARGING: '/charging',
  DRIVERS: '/drivers',
  DRIVER_DETAILS: '/drivers/:driverId',
  MAINTENANCE: '/maintenance',
  ANALYTICS: '/analytics',
  BILLING: '/billing',
  SETTINGS: '/settings',
  PROFILE: '/profile',
};

// Subscription Features
export const SUBSCRIPTION_FEATURES = {
  FREE: {
    vehicles: 5,
    drivers: 5,
    features: ['Basic Dashboard', 'Vehicle Tracking', 'Trip History'],
  },
  STARTER: {
    vehicles: 25,
    drivers: 25,
    features: [
      'Everything in Free',
      'Charging Management',
      'Driver Performance',
      'Basic Analytics',
      'Email Support',
    ],
  },
  PROFESSIONAL: {
    vehicles: 100,
    drivers: 100,
    features: [
      'Everything in Starter',
      'Maintenance Scheduling',
      'Advanced Analytics',
      'TCO Analysis',
      'Route Optimization',
      'Priority Support',
      'API Access',
    ],
  },
  ENTERPRISE: {
    vehicles: -1, // unlimited
    drivers: -1,
    features: [
      'Everything in Professional',
      'Custom Integrations',
      'White Label',
      'Dedicated Support',
      'Custom Reports',
      'SLA Guarantee',
      'Training',
    ],
  },
};

// Refresh Intervals (in milliseconds)
export const REFRESH_INTERVALS = {
  LOCATION: 10000, // 10 seconds
  BATTERY: 30000, // 30 seconds
  DASHBOARD: 60000, // 1 minute
  NOTIFICATIONS: 30000, // 30 seconds
};

// Map Settings
export const MAP_STYLES = {
  STREETS: 'mapbox://styles/mapbox/streets-v12',
  SATELLITE: 'mapbox://styles/mapbox/satellite-streets-v12',
  DARK: 'mapbox://styles/mapbox/dark-v11',
  LIGHT: 'mapbox://styles/mapbox/light-v11',
};

// Notification Settings
export const NOTIFICATION_DURATION = 5000; // 5 seconds
export const MAX_NOTIFICATIONS = 5;
