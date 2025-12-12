// ============================================================================
// COMPREHENSIVE FRONTEND PAGES DATA
// Based on actual code analysis of all 26 pages in src/pages/
// ============================================================================

export interface PageFeature {
  name: string;
  description: string;
}

export interface PageAPI {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  endpoint: string;
  description: string;
}

export interface PageRedux {
  slice: string;
  selectors: string[];
  actions: string[];
}

export interface PageData {
  id: string;
  name: string;
  path: string;
  file: string;
  description: string;
  category: 'dashboard' | 'fleet' | 'charging' | 'driver' | 'maintenance' | 'analytics' | 'billing' | 'settings' | 'auth' | 'docs';
  features: PageFeature[];
  apiCalls: PageAPI[];
  redux?: PageRedux;
  childComponents: string[];
  tabs?: string[];
  hasSearch?: boolean;
  hasCRUD?: boolean;
  hasExport?: boolean;
  mockData?: boolean; // true if page uses mock data instead of real API
}

export const frontendPagesData: PageData[] = [
  // ============ DASHBOARD ============
  {
    id: 'dashboard',
    name: 'Dashboard',
    path: '/dashboard',
    file: 'DashboardPage.tsx',
    description: 'Main fleet overview with real-time KPIs for multi-fuel fleet (EV, ICE, Hybrid)',
    category: 'dashboard',
    features: [
      { name: 'Fleet Summary Cards', description: 'Total vehicles, active trips, charging sessions, alerts' },
      { name: 'Battery Health KPI', description: 'Average battery SOC across EV fleet' },
      { name: 'Utilization Rate', description: 'Fleet utilization percentage' },
      { name: 'Vehicle Statistics', description: 'Count by fuel type (EV/ICE/Hybrid)' },
      { name: 'Recent Alerts', description: 'Latest system alerts and notifications' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/analytics/fleet-summary', description: 'Fetch fleet summary stats' },
      { method: 'GET', endpoint: '/api/notifications/alerts', description: 'Fetch recent alerts' },
    ],
    redux: {
      slice: 'analyticsSlice',
      selectors: ['selectFleetAnalytics'],
      actions: ['fetchFleetSummary'],
    },
    childComponents: ['FleetSummaryCard'],
  },

  // ============ FLEET MANAGEMENT ============
  {
    id: 'fleet-management',
    name: 'Fleet Management',
    path: '/fleet/*',
    file: 'FleetManagementPage.tsx',
    description: 'Vehicle fleet management with card-based grid, CRUD operations, real-time location',
    category: 'fleet',
    features: [
      { name: 'Vehicle Cards Grid', description: 'Visual cards showing vehicle info with battery/fuel indicators' },
      { name: 'Add Vehicle', description: 'Create new vehicle with form dialog' },
      { name: 'Edit Vehicle', description: 'Update vehicle details' },
      { name: 'Delete Vehicle', description: 'Remove vehicle from fleet' },
      { name: 'Vehicle Details', description: 'Detailed vehicle information dialog' },
      { name: 'Real-time Location', description: 'Live GPS tracking via useRealTimeLocation hook' },
      { name: 'Multi-fuel Support', description: 'EV (battery), ICE (fuel tank), Hybrid vehicles' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/fleet/vehicles', description: 'List all vehicles' },
      { method: 'POST', endpoint: '/api/fleet/vehicles', description: 'Create vehicle' },
      { method: 'PUT', endpoint: '/api/fleet/vehicles/{id}', description: 'Update vehicle' },
      { method: 'DELETE', endpoint: '/api/fleet/vehicles/{id}', description: 'Delete vehicle' },
    ],
    redux: {
      slice: 'vehicleSlice',
      selectors: ['selectAllVehicles', 'selectVehicleLoading'],
      actions: ['fetchVehicles', 'createVehicle', 'updateVehicle', 'deleteVehicle'],
    },
    childComponents: ['VehicleFormDialog', 'VehicleDetailDialog'],
    hasSearch: true,
    hasCRUD: true,
  },

  {
    id: 'trip-management',
    name: 'Trip Management',
    path: '/trips',
    file: 'TripManagementPage.tsx',
    description: 'Complete trip lifecycle management with start/end/pause/resume/cancel operations',
    category: 'fleet',
    features: [
      { name: 'Trip List', description: 'Table view of all trips with status badges' },
      { name: 'Trip Status Filter', description: 'Filter by SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED' },
      { name: 'Start Trip', description: 'Begin a scheduled trip' },
      { name: 'End Trip', description: 'Complete an in-progress trip' },
      { name: 'Pause/Resume', description: 'Pause and resume active trips' },
      { name: 'Cancel Trip', description: 'Cancel scheduled/in-progress trips' },
      { name: 'Trip Details', description: 'View trip information in dialog' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/fleet/trips', description: 'List all trips' },
      { method: 'POST', endpoint: '/api/fleet/trips/{id}/start', description: 'Start trip' },
      { method: 'POST', endpoint: '/api/fleet/trips/{id}/end', description: 'End trip' },
      { method: 'POST', endpoint: '/api/fleet/trips/{id}/pause', description: 'Pause trip' },
      { method: 'POST', endpoint: '/api/fleet/trips/{id}/resume', description: 'Resume trip' },
      { method: 'POST', endpoint: '/api/fleet/trips/{id}/cancel', description: 'Cancel trip' },
    ],
    redux: {
      slice: 'tripSlice',
      selectors: ['selectAllTrips', 'selectTripLoading', 'selectTripError'],
      actions: ['fetchAllTrips', 'startTrip', 'endTrip', 'pauseTrip', 'resumeTrip', 'cancelTrip'],
    },
    childComponents: [],
    hasSearch: true,
  },

  // ============ CHARGING ============
  {
    id: 'charging',
    name: 'Charging Overview',
    path: '/charging',
    file: 'ChargingPage.tsx',
    description: 'Simplified charging overview with active sessions and available stations',
    category: 'charging',
    features: [
      { name: 'Active Sessions', description: 'Currently charging vehicles list' },
      { name: 'Available Stations', description: 'Stations with free slots' },
      { name: 'Quick Start', description: 'Start charging session button' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/charging/stations', description: 'List charging stations' },
      { method: 'GET', endpoint: '/api/charging/sessions', description: 'List charging sessions' },
    ],
    redux: {
      slice: 'chargingSlice',
      selectors: ['selectStations', 'selectSessions', 'selectChargingLoading'],
      actions: ['fetchAllStations', 'fetchAllSessions'],
    },
    childComponents: [],
  },

  {
    id: 'charging-management',
    name: 'Charging Management',
    path: '/charging/manage',
    file: 'ChargingManagementPage.tsx',
    description: 'Comprehensive charging management with station monitoring and session tracking',
    category: 'charging',
    features: [
      { name: 'Station List', description: 'All charging stations with status' },
      { name: 'Session Tracking', description: 'Track energy consumed, cost, duration' },
      { name: 'Start Session', description: 'Initiate new charging session' },
      { name: 'End Session', description: 'Complete charging session' },
      { name: 'Summary Cards', description: 'Total/active stations and sessions count' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/charging/stations', description: 'List stations' },
      { method: 'GET', endpoint: '/api/charging/sessions', description: 'List sessions' },
      { method: 'POST', endpoint: '/api/charging/sessions', description: 'Start session' },
      { method: 'PUT', endpoint: '/api/charging/sessions/{id}/end', description: 'End session' },
    ],
    redux: {
      slice: 'chargingSlice',
      selectors: ['selectStations', 'selectSessions', 'selectChargingLoading', 'selectChargingError'],
      actions: ['fetchAllStations', 'fetchAllSessions', 'startChargingSession', 'endChargingSession'],
    },
    childComponents: [],
    tabs: ['Charging Stations', 'Charging Sessions'],
  },

  {
    id: 'station-discovery',
    name: 'Station Discovery',
    path: '/stations',
    file: 'StationDiscoveryPage.tsx',
    description: 'Find and navigate to charging/fuel stations based on vehicle type',
    category: 'charging',
    features: [
      { name: 'Map View', description: 'Interactive map with station markers' },
      { name: 'Station Search', description: 'Search by location/name' },
      { name: 'Filter by Type', description: 'EV chargers, gas stations' },
      { name: 'Navigation', description: 'Get directions to station' },
    ],
    apiCalls: [],
    childComponents: ['StationDiscovery'],
  },

  // ============ DRIVERS ============
  {
    id: 'drivers',
    name: 'Drivers',
    path: '/drivers/*',
    file: 'DriversPage.tsx',
    description: 'Enhanced driver management with cards, KPIs, leaderboard, and assignments',
    category: 'driver',
    features: [
      { name: 'Driver Cards', description: 'Visual cards with driver info and metrics' },
      { name: 'KPI Dashboard', description: 'Safety score, total trips, efficiency' },
      { name: 'Add/Edit Driver', description: 'CRUD operations with form dialog' },
      { name: 'Driver Leaderboard', description: 'Ranked drivers by performance' },
      { name: 'Driver Assignment', description: 'Assign drivers to vehicles' },
      { name: 'Driver Details', description: 'Detailed driver profile dialog' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/drivers', description: 'List all drivers' },
      { method: 'POST', endpoint: '/api/drivers', description: 'Create driver' },
      { method: 'PUT', endpoint: '/api/drivers/{id}', description: 'Update driver' },
      { method: 'DELETE', endpoint: '/api/drivers/{id}', description: 'Delete driver' },
      { method: 'GET', endpoint: '/api/drivers/leaderboard', description: 'Get leaderboard' },
    ],
    redux: {
      slice: 'driverSlice',
      selectors: ['selectAllDrivers', 'selectDriverLoading'],
      actions: ['fetchAllDrivers', 'deleteDriver'],
    },
    childComponents: ['DriverLeaderboard', 'AssignDriver', 'DriverFormDialog'],
    tabs: ['All Drivers', 'Leaderboard', 'Assign Driver'],
    hasCRUD: true,
  },

  {
    id: 'driver-management',
    name: 'Driver Management',
    path: '/drivers/manage',
    file: 'DriverManagementPage.tsx',
    description: 'Table-based driver management with search and status filtering',
    category: 'driver',
    features: [
      { name: 'Driver Table', description: 'Tabular view of all drivers' },
      { name: 'Status Filter', description: 'Filter by ACTIVE/INACTIVE/ON_TRIP/ON_LEAVE' },
      { name: 'Search', description: 'Search drivers by name/phone/license' },
      { name: 'Leaderboard Tab', description: 'Performance ranking' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/drivers', description: 'List drivers' },
      { method: 'GET', endpoint: '/api/drivers/leaderboard', description: 'Get leaderboard' },
    ],
    redux: {
      slice: 'driverSlice',
      selectors: ['selectAllDrivers', 'selectLeaderboard', 'selectDriverLoading', 'selectDriverError'],
      actions: ['fetchAllDrivers', 'fetchLeaderboard'],
    },
    childComponents: [],
    tabs: ['All Drivers', 'Leaderboard'],
    hasSearch: true,
  },

  // ============ MAINTENANCE ============
  {
    id: 'maintenance',
    name: 'Maintenance',
    path: '/maintenance',
    file: 'MaintenancePage.tsx',
    description: 'Vehicle maintenance hub with schedule, history, battery health, and scheduling',
    category: 'maintenance',
    features: [
      { name: 'Maintenance Schedule', description: 'Upcoming maintenance tasks' },
      { name: 'Service History', description: 'Past maintenance records' },
      { name: 'Battery Health', description: 'EV battery health monitoring' },
      { name: 'Schedule New', description: 'Create new maintenance task' },
    ],
    apiCalls: [],
    childComponents: ['MaintenanceSchedule', 'ServiceHistory', 'BatteryHealth', 'ScheduleMaintenance'],
    tabs: ['Schedule', 'Service History', 'Battery Health', 'New Schedule'],
  },

  {
    id: 'maintenance-scheduling',
    name: 'Maintenance Scheduling',
    path: '/maintenance/schedule',
    file: 'MaintenanceSchedulingPage.tsx',
    description: 'Detailed maintenance scheduling with service records tracking',
    category: 'maintenance',
    features: [
      { name: 'Schedule Management', description: 'View/manage scheduled maintenance' },
      { name: 'Service Records', description: 'Historical service records' },
      { name: 'Status Summary', description: 'Pending/overdue counts' },
      { name: 'Search/Filter', description: 'Find maintenance tasks' },
    ],
    apiCalls: [],
    childComponents: [],
    tabs: ['Schedules', 'Service Records'],
    mockData: true,
  },

  // ============ ANALYTICS ============
  {
    id: 'analytics',
    name: 'Analytics',
    path: '/analytics',
    file: 'AnalyticsPage.tsx',
    description: 'Analytics dashboard with fleet, TCO, carbon footprint, and utilization',
    category: 'analytics',
    features: [
      { name: 'Fleet Analytics', description: 'Overall fleet performance metrics' },
      { name: 'TCO Analysis', description: 'Total Cost of Ownership calculations' },
      { name: 'Carbon Footprint', description: 'Emissions tracking and reduction' },
      { name: 'Utilization Report', description: 'Vehicle usage efficiency' },
    ],
    apiCalls: [],
    childComponents: ['FleetAnalytics', 'TCOAnalysis', 'CarbonFootprint', 'UtilizationReport'],
    tabs: ['Fleet Analytics', 'TCO Analysis', 'Carbon Footprint', 'Utilization'],
  },

  {
    id: 'detailed-analytics',
    name: 'Detailed Analytics Dashboard',
    path: '/analytics/detailed',
    file: 'DetailedAnalyticsDashboardPage.tsx',
    description: 'Advanced analytics with interactive charts (Line, Bar, Pie)',
    category: 'analytics',
    features: [
      { name: 'Fleet Utilization Charts', description: 'Line charts showing usage trends' },
      { name: 'Cost Breakdown', description: 'Pie chart of expense categories' },
      { name: 'Energy Consumption', description: 'Bar chart of energy usage' },
      { name: 'Carbon Emissions', description: 'Environmental impact visualization' },
      { name: 'Time Range Filter', description: 'Select date range for data' },
    ],
    apiCalls: [],
    childComponents: [],
    mockData: true,
  },

  {
    id: 'vehicle-report',
    name: 'Vehicle Report',
    path: '/reports/vehicle',
    file: 'VehicleReportPage.tsx',
    description: 'Generate comprehensive vehicle reports with PDF download',
    category: 'analytics',
    features: [
      { name: 'Report Sections', description: 'Vehicle info, events, trips, maintenance, charging' },
      { name: 'Date Range', description: 'Select report period' },
      { name: 'PDF Download', description: 'Export report as PDF' },
      { name: 'Performance Metrics', description: 'Include performance data' },
      { name: 'Cost Analysis', description: 'Include cost breakdown' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/fleet/vehicles', description: 'Get vehicles list' },
      { method: 'POST', endpoint: '/api/analytics/vehicle-report', description: 'Generate report' },
      { method: 'POST', endpoint: '/api/analytics/genealogy-report', description: 'Generate genealogy' },
    ],
    childComponents: [],
    hasExport: true,
  },

  // ============ BILLING ============
  {
    id: 'billing',
    name: 'Billing',
    path: '/billing',
    file: 'BillingPage.tsx',
    description: 'Billing and subscription management with invoices and payments',
    category: 'billing',
    features: [
      { name: 'Subscriptions', description: 'Manage subscription plans' },
      { name: 'Invoices', description: 'View and download invoices' },
      { name: 'Payments', description: 'Payment history and receipts' },
      { name: 'Pricing Plans', description: 'Available pricing tiers' },
    ],
    apiCalls: [],
    childComponents: ['Subscriptions', 'Invoices', 'Payments', 'PricingPlans'],
    tabs: ['Subscriptions', 'Invoices', 'Payments', 'Pricing Plans'],
  },

  {
    id: 'invoicing',
    name: 'Invoicing',
    path: '/billing/invoices',
    file: 'InvoicingPage.tsx',
    description: 'Invoice and payment management with revenue tracking',
    category: 'billing',
    features: [
      { name: 'Revenue Summary', description: 'Total, paid, pending amounts' },
      { name: 'Invoice List', description: 'Searchable invoice table' },
      { name: 'Payment History', description: 'Track all payments' },
      { name: 'Invoice Details', description: 'View invoice breakdown' },
    ],
    apiCalls: [],
    childComponents: [],
    tabs: ['Invoices', 'Payments'],
    mockData: true,
    hasSearch: true,
  },

  // ============ CUSTOMERS ============
  {
    id: 'customers',
    name: 'Customer Management',
    path: '/customers',
    file: 'CustomerManagementPage.tsx',
    description: 'Full CRM with customer CRUD, feedback management, and delivery statistics',
    category: 'fleet',
    features: [
      { name: 'Customer List', description: 'All customers with filtering' },
      { name: 'Add Customer', description: 'Create new customer record' },
      { name: 'Edit Customer', description: 'Update customer details' },
      { name: 'Customer Feedback', description: 'Manage customer feedback' },
      { name: 'Status Filter', description: 'Active/Business/Top Rated filters' },
      { name: 'Delivery Stats', description: 'Customer delivery statistics' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/customers', description: 'List customers' },
      { method: 'POST', endpoint: '/api/customers', description: 'Create customer' },
      { method: 'PUT', endpoint: '/api/customers/{id}', description: 'Update customer' },
      { method: 'DELETE', endpoint: '/api/customers/{id}', description: 'Delete customer' },
      { method: 'GET', endpoint: '/api/customers/feedback/unaddressed', description: 'Get feedback' },
      { method: 'POST', endpoint: '/api/customers/feedback', description: 'Add feedback' },
    ],
    childComponents: [],
    tabs: ['All', 'Active', 'Business', 'Top Rated'],
    hasCRUD: true,
    hasSearch: true,
  },

  // ============ ROUTES ============
  {
    id: 'route-optimization',
    name: 'Route Optimization',
    path: '/routes',
    file: 'RouteOptimizationPage.tsx',
    description: 'Route planning with waypoints, optimization, and status tracking',
    category: 'fleet',
    features: [
      { name: 'Route List', description: 'All routes with status badges' },
      { name: 'Create Route', description: 'Plan new route' },
      { name: 'Waypoint Management', description: 'Add/edit waypoints' },
      { name: 'Distance/Duration', description: 'Estimated route metrics' },
      { name: 'Optimization Criteria', description: 'Shortest/Fastest/Cheapest' },
      { name: 'Status Tracking', description: 'Planned/In Progress/Completed' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/routes', description: 'List routes' },
      { method: 'POST', endpoint: '/api/routes', description: 'Create route' },
      { method: 'PUT', endpoint: '/api/routes/{id}', description: 'Update route' },
      { method: 'DELETE', endpoint: '/api/routes/{id}', description: 'Delete route' },
      { method: 'GET', endpoint: '/api/routes/{id}/waypoints', description: 'Get waypoints' },
      { method: 'POST', endpoint: '/api/routes/{id}/waypoints', description: 'Add waypoint' },
    ],
    childComponents: [],
    tabs: ['All', 'Planned', 'In Progress', 'Completed'],
    hasCRUD: true,
  },

  // ============ GEOFENCING ============
  {
    id: 'geofence',
    name: 'Geofence Management',
    path: '/geofences',
    file: 'GeofenceManagementPage.tsx',
    description: 'Define zones for charging, depots, restricted areas, service areas',
    category: 'fleet',
    features: [
      { name: 'Geofence List', description: 'All defined zones' },
      { name: 'Create Zone', description: 'Define new geofence' },
      { name: 'Type Filter', description: 'Filter by zone type' },
      { name: 'Coordinate Setup', description: 'Set center point and radius' },
      { name: 'Alert Settings', description: 'Entry/exit notifications' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/geofences', description: 'List geofences' },
      { method: 'POST', endpoint: '/api/geofences', description: 'Create geofence' },
      { method: 'PUT', endpoint: '/api/geofences/{id}', description: 'Update geofence' },
      { method: 'DELETE', endpoint: '/api/geofences/{id}', description: 'Delete geofence' },
    ],
    redux: {
      slice: 'geofenceSlice',
      selectors: ['selectAllGeofences', 'selectGeofenceLoading', 'selectGeofenceError'],
      actions: ['fetchAllGeofences', 'createGeofence', 'updateGeofence', 'deleteGeofence'],
    },
    childComponents: [],
    hasCRUD: true,
    hasSearch: true,
  },

  // ============ DOCUMENTS ============
  {
    id: 'documents',
    name: 'Document Management',
    path: '/documents',
    file: 'DocumentManagementPage.tsx',
    description: 'Vehicle/driver documents with upload, verification, expiry tracking',
    category: 'fleet',
    features: [
      { name: 'Document Upload', description: 'Upload RC, insurance, permits, licenses' },
      { name: 'Verification', description: 'Mark documents as verified' },
      { name: 'Expiry Tracking', description: 'Track document expiration dates' },
      { name: 'Status Filters', description: 'All/Expiring/Expired/Unverified' },
      { name: 'Action Required', description: 'Documents needing attention' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/documents/action-required', description: 'Get action items' },
      { method: 'POST', endpoint: '/api/documents', description: 'Upload document' },
      { method: 'PUT', endpoint: '/api/documents/{id}/verify', description: 'Verify document' },
      { method: 'DELETE', endpoint: '/api/documents/{id}', description: 'Delete document' },
    ],
    childComponents: [],
    tabs: ['All', 'Expiring Soon', 'Expired', 'Unverified'],
  },

  // ============ EXPENSES ============
  {
    id: 'expenses',
    name: 'Expense Management',
    path: '/expenses',
    file: 'ExpenseManagementPage.tsx',
    description: 'Fleet expense tracking with approval workflow and analytics',
    category: 'billing',
    features: [
      { name: 'Expense List', description: 'All expenses with status' },
      { name: 'Create Expense', description: 'Submit new expense' },
      { name: 'Approval Workflow', description: 'Approve/reject expenses' },
      { name: 'Category Breakdown', description: 'Expenses by category (pie chart)' },
      { name: 'Reimbursement', description: 'Track reimbursement status' },
    ],
    apiCalls: [
      { method: 'GET', endpoint: '/api/expenses', description: 'List expenses' },
      { method: 'POST', endpoint: '/api/expenses', description: 'Create expense' },
      { method: 'PUT', endpoint: '/api/expenses/{id}/approve', description: 'Approve expense' },
      { method: 'PUT', endpoint: '/api/expenses/{id}/reject', description: 'Reject expense' },
    ],
    childComponents: [],
    tabs: ['All', 'Pending', 'Approved', 'Rejected'],
    hasCRUD: true,
  },

  // ============ NOTIFICATIONS ============
  {
    id: 'notifications',
    name: 'Notification Center',
    path: '/notifications',
    file: 'NotificationCenterPage.tsx',
    description: 'Centralized notification and alert management',
    category: 'settings',
    features: [
      { name: 'Notification List', description: 'All notifications with unread badge' },
      { name: 'Alert Management', description: 'Critical/warning alerts' },
      { name: 'Severity Levels', description: 'Filter by severity' },
      { name: 'Notification Details', description: 'View full notification' },
    ],
    apiCalls: [],
    childComponents: [],
    tabs: ['Notifications', 'Alerts'],
    mockData: true,
  },

  // ============ SETTINGS ============
  {
    id: 'settings',
    name: 'Settings',
    path: '/settings',
    file: 'SettingsPage.tsx',
    description: 'App settings with notifications, alerts, privacy, and theme',
    category: 'settings',
    features: [
      { name: 'Notification Preferences', description: 'Enable/disable notification types' },
      { name: 'Alert Settings', description: 'Battery/fuel/maintenance thresholds' },
      { name: 'Privacy Options', description: 'Data sharing preferences' },
      { name: 'Theme Toggle', description: 'Light/dark mode' },
      { name: 'Password Reset', description: 'Change password via Firebase' },
    ],
    apiCalls: [
      { method: 'POST', endpoint: 'firebase:resetPassword', description: 'Reset password' },
    ],
    childComponents: ['LoadingSpinner'],
  },

  {
    id: 'profile',
    name: 'Profile',
    path: '/profile',
    file: 'ProfilePage.tsx',
    description: 'User profile management with view/edit mode',
    category: 'settings',
    features: [
      { name: 'Profile View', description: 'Display user information' },
      { name: 'Edit Mode', description: 'Update personal details' },
      { name: 'Avatar', description: 'Profile picture display' },
      { name: 'Company Info', description: 'Company details' },
    ],
    apiCalls: [
      { method: 'PUT', endpoint: '/api/auth/profile', description: 'Update profile' },
    ],
    childComponents: ['LoadingSpinner'],
  },

  // ============ AUTH ============
  {
    id: 'login',
    name: 'Login',
    path: '/login',
    file: 'LoginPage.tsx',
    description: 'User login page',
    category: 'auth',
    features: [
      { name: 'Email/Password', description: 'Traditional login' },
      { name: 'Google Sign-in', description: 'OAuth with Google' },
      { name: 'Forgot Password', description: 'Password reset link' },
    ],
    apiCalls: [
      { method: 'POST', endpoint: '/api/auth/login', description: 'Authenticate user' },
    ],
    childComponents: ['Login'],
  },

  {
    id: 'company-onboarding',
    name: 'Company Onboarding',
    path: '/onboarding/company',
    file: 'CompanyOnboardingPage.tsx',
    description: 'New user company setup during registration',
    category: 'auth',
    features: [
      { name: 'Company Name', description: 'Set organization name' },
      { name: 'Phone Number', description: 'Optional contact number' },
    ],
    apiCalls: [
      { method: 'PUT', endpoint: '/api/auth/profile', description: 'Update company info' },
    ],
    redux: {
      slice: 'authSlice',
      selectors: ['selectUser'],
      actions: ['updateProfile'],
    },
    childComponents: ['LoadingSpinner'],
  },

  // ============ DEVELOPER DOCS ============
  {
    id: 'developer-docs',
    name: 'Developer Docs',
    path: '/developer-docs',
    file: 'DeveloperDocsPage.tsx',
    description: 'Interactive developer documentation with visual diagrams (NO AUTH)',
    category: 'docs',
    features: [
      { name: 'Architecture Diagram', description: 'System architecture visualization' },
      { name: 'Database Schema', description: 'ER diagram with relationships' },
      { name: 'Event Flow', description: 'Event-driven architecture' },
      { name: 'Frontend Structure', description: 'Page and component map' },
      { name: 'Clickable Nodes', description: 'Interactive node details' },
    ],
    apiCalls: [],
    childComponents: ['ArchitectureFlow', 'DatabaseSchema', 'EventFlow', 'FrontendFlow'],
    tabs: ['Architecture', 'Database', 'Events', 'Frontend'],
  },
];

// Group pages by category
export const pagesByCategory = frontendPagesData.reduce((acc, page) => {
  if (!acc[page.category]) {
    acc[page.category] = [];
  }
  acc[page.category].push(page);
  return acc;
}, {} as Record<string, PageData[]>);

// Count statistics
export const pageStats = {
  total: frontendPagesData.length,
  withRedux: frontendPagesData.filter(p => p.redux).length,
  withCRUD: frontendPagesData.filter(p => p.hasCRUD).length,
  withMockData: frontendPagesData.filter(p => p.mockData).length,
  withTabs: frontendPagesData.filter(p => p.tabs).length,
};
