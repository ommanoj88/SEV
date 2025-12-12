// ============================================================================
// COMPREHENSIVE API ENDPOINTS DATA
// Based on actual @RestController analysis from backend/evfleet-monolith
// ============================================================================

export interface ApiParameter {
  name: string;
  type: string;
  location: 'path' | 'query' | 'body' | 'header';
  required: boolean;
  description?: string;
  defaultValue?: string;
}

export interface ApiEndpoint {
  id: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  path: string;
  summary: string;
  description?: string;
  parameters?: ApiParameter[];
  requestBody?: string; // DTO name
  responseBody?: string; // DTO name
  tags?: string[];
}

export interface ApiController {
  id: string;
  name: string;
  basePath: string;
  module: string;
  description: string;
  swaggerTag: string;
  endpoints: ApiEndpoint[];
}

// ============================================================================
// ALL REST CONTROLLERS
// ============================================================================
export const apiControllers: ApiController[] = [
  // ========================
  // AUTH MODULE
  // ========================
  {
    id: 'auth-controller',
    name: 'AuthController',
    basePath: '/api/v1/auth',
    module: 'auth',
    description: 'Authentication and User Management',
    swaggerTag: 'Authentication',
    endpoints: [
      {
        id: 'auth-register',
        method: 'POST',
        path: '/register',
        summary: 'Register a new user',
        description: 'Creates a new user account with Firebase authentication',
        requestBody: 'RegisterRequest',
        responseBody: 'AuthResponse',
      },
      {
        id: 'auth-login',
        method: 'POST',
        path: '/login',
        summary: 'Login user',
        description: 'Authenticates user with Firebase token and returns user details',
        requestBody: 'LoginRequest',
        responseBody: 'AuthResponse',
      },
      {
        id: 'auth-get-user',
        method: 'GET',
        path: '/users/{id}',
        summary: 'Get user by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true, description: 'User ID' }
        ],
        responseBody: 'UserResponse',
      },
      {
        id: 'auth-get-users',
        method: 'GET',
        path: '/users',
        summary: 'Get all users',
        responseBody: 'List<UserResponse>',
      },
      {
        id: 'auth-update-user',
        method: 'PUT',
        path: '/users/{id}',
        summary: 'Update user',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'UserResponse',
        responseBody: 'UserResponse',
      },
      {
        id: 'auth-delete-user',
        method: 'DELETE',
        path: '/users/{id}',
        summary: 'Delete user (soft delete)',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
      {
        id: 'auth-get-users-company',
        method: 'GET',
        path: '/users/company/{companyId}',
        summary: 'Get users by company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<UserResponse>',
      },
      {
        id: 'auth-get-user-firebase',
        method: 'GET',
        path: '/users/firebase/{firebaseUid}',
        summary: 'Get user by Firebase UID',
        parameters: [
          { name: 'firebaseUid', type: 'String', location: 'path', required: true }
        ],
        responseBody: 'UserResponse',
      },
    ],
  },

  // ========================
  // FLEET MODULE - VEHICLES
  // ========================
  {
    id: 'vehicle-controller',
    name: 'VehicleController',
    basePath: '/api/v1/vehicles',
    module: 'fleet',
    description: 'Vehicle Management',
    swaggerTag: 'Fleet - Vehicles',
    endpoints: [
      {
        id: 'vehicle-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all vehicles',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<VehicleResponse>>',
      },
      {
        id: 'vehicle-create',
        method: 'POST',
        path: '/',
        summary: 'Register new vehicle',
        requestBody: 'VehicleRequest',
        responseBody: 'VehicleResponse',
      },
      {
        id: 'vehicle-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get vehicle by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'VehicleResponse',
      },
      {
        id: 'vehicle-get-by-company',
        method: 'GET',
        path: '/company/{companyId}',
        summary: 'Get vehicles by company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<VehicleResponse>',
      },
      {
        id: 'vehicle-update-location',
        method: 'PUT',
        path: '/{id}/location',
        summary: 'Update vehicle location',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true },
          { name: 'latitude', type: 'Double', location: 'query', required: true },
          { name: 'longitude', type: 'Double', location: 'query', required: true }
        ],
        responseBody: 'VehicleResponse',
      },
      {
        id: 'vehicle-update',
        method: 'PUT',
        path: '/{id}',
        summary: 'Update vehicle',
        description: 'Update an existing vehicle. Validates fuel-type specific fields',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'VehicleRequest',
        responseBody: 'VehicleResponse',
      },
      {
        id: 'vehicle-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete vehicle',
        description: 'Cannot delete vehicles in active trip, with assigned driver, or charging',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
      {
        id: 'vehicle-health',
        method: 'GET',
        path: '/health',
        summary: 'Health check',
        responseBody: 'String',
      },
    ],
  },

  // ========================
  // FLEET MODULE - TRIPS
  // ========================
  {
    id: 'trip-controller',
    name: 'TripController',
    basePath: '/api/v1/fleet/trips',
    module: 'fleet',
    description: 'Trip Management',
    swaggerTag: 'Fleet - Trips',
    endpoints: [
      {
        id: 'trip-start',
        method: 'POST',
        path: '/start',
        summary: 'Start a trip',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'query', required: true },
          { name: 'driverId', type: 'Long', location: 'query', required: false },
          { name: 'startLatitude', type: 'Double', location: 'query', required: true },
          { name: 'startLongitude', type: 'Double', location: 'query', required: true }
        ],
        responseBody: 'TripResponse',
      },
      {
        id: 'trip-complete',
        method: 'POST',
        path: '/{id}/complete',
        summary: 'Complete a trip',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true },
          { name: 'endLatitude', type: 'Double', location: 'query', required: true },
          { name: 'endLongitude', type: 'Double', location: 'query', required: true },
          { name: 'distance', type: 'Double', location: 'query', required: true },
          { name: 'energyConsumed', type: 'BigDecimal', location: 'query', required: false },
          { name: 'fuelConsumed', type: 'BigDecimal', location: 'query', required: false }
        ],
        responseBody: 'TripResponse',
      },
      {
        id: 'trip-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get trip by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'TripResponse',
      },
      {
        id: 'trip-get-by-vehicle',
        method: 'GET',
        path: '/vehicle/{vehicleId}',
        summary: 'Get trips by vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<TripResponse>',
      },
      {
        id: 'trip-get-by-company',
        method: 'GET',
        path: '/company/{companyId}',
        summary: 'Get trips by company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<TripResponse>',
      },
    ],
  },

  // ========================
  // FLEET MODULE - BATTERY HEALTH
  // ========================
  {
    id: 'battery-health-controller',
    name: 'BatteryHealthController',
    basePath: '/api/v1/battery-health',
    module: 'fleet',
    description: 'EV Battery Health Tracking',
    swaggerTag: 'Fleet - Battery',
    endpoints: [
      {
        id: 'battery-get-by-vehicle',
        method: 'GET',
        path: '/vehicle/{vehicleId}',
        summary: 'Get battery health for vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<BatteryHealthResponse>',
      },
      {
        id: 'battery-get-latest',
        method: 'GET',
        path: '/vehicle/{vehicleId}/latest',
        summary: 'Get latest battery health',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'BatteryHealthResponse',
      },
    ],
  },

  // ========================
  // DRIVER MODULE
  // ========================
  {
    id: 'driver-controller',
    name: 'DriverController',
    basePath: '/api/v1/drivers',
    module: 'driver',
    description: 'Driver Management',
    swaggerTag: 'Drivers',
    endpoints: [
      {
        id: 'driver-create',
        method: 'POST',
        path: '/',
        summary: 'Create driver',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        requestBody: 'DriverRequest',
        responseBody: 'ApiResponse<DriverResponse>',
      },
      {
        id: 'driver-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all drivers',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<DriverResponse>>',
      },
      {
        id: 'driver-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get driver by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<DriverResponse>',
      },
      {
        id: 'driver-get-active',
        method: 'GET',
        path: '/active',
        summary: 'Get active drivers',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<DriverResponse>>',
      },
      {
        id: 'driver-get-available',
        method: 'GET',
        path: '/available',
        summary: 'Get available drivers (not assigned)',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<DriverResponse>>',
      },
      {
        id: 'driver-expiring-licenses',
        method: 'GET',
        path: '/expiring-licenses',
        summary: 'Get drivers with expiring licenses',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'daysAhead', type: 'int', location: 'query', required: false, defaultValue: '30' }
        ],
        responseBody: 'ApiResponse<List<DriverResponse>>',
      },
      {
        id: 'driver-update',
        method: 'PUT',
        path: '/{id}',
        summary: 'Update driver',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'DriverRequest',
        responseBody: 'ApiResponse<DriverResponse>',
      },
      {
        id: 'driver-assign',
        method: 'POST',
        path: '/{id}/assign',
        summary: 'Assign vehicle to driver',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true },
          { name: 'vehicleId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<DriverResponse>',
      },
      {
        id: 'driver-unassign',
        method: 'POST',
        path: '/{id}/unassign',
        summary: 'Unassign vehicle from driver',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<DriverResponse>',
      },
      {
        id: 'driver-leaderboard',
        method: 'GET',
        path: '/leaderboard',
        summary: 'Get driver leaderboard',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<DriverResponse>>',
      },
      {
        id: 'driver-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete driver',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
    ],
  },

  // ========================
  // CHARGING MODULE - STATIONS
  // ========================
  {
    id: 'charging-station-controller',
    name: 'ChargingStationController',
    basePath: '/api/v1/charging/stations',
    module: 'charging',
    description: 'Charging Station Management',
    swaggerTag: 'Charging - Stations',
    endpoints: [
      {
        id: 'station-create',
        method: 'POST',
        path: '/',
        summary: 'Register new charging station',
        requestBody: 'ChargingStation',
        responseBody: 'ChargingStationResponse',
      },
      {
        id: 'station-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all charging stations',
        responseBody: 'List<ChargingStationResponse>',
      },
      {
        id: 'station-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get station by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ChargingStationResponse',
      },
      {
        id: 'station-get-available',
        method: 'GET',
        path: '/available',
        summary: 'Get available stations',
        responseBody: 'List<ChargingStationResponse>',
      },
      {
        id: 'station-get-nearby',
        method: 'GET',
        path: '/nearby',
        summary: 'Get nearby stations',
        parameters: [
          { name: 'latitude', type: 'Double', location: 'query', required: true },
          { name: 'longitude', type: 'Double', location: 'query', required: true },
          { name: 'limit', type: 'int', location: 'query', required: false, defaultValue: '10' }
        ],
        responseBody: 'List<ChargingStationResponse>',
      },
    ],
  },

  // ========================
  // CHARGING MODULE - SESSIONS
  // ========================
  {
    id: 'charging-session-controller',
    name: 'ChargingSessionController',
    basePath: '/api/v1/charging/sessions',
    module: 'charging',
    description: 'Charging Session Management',
    swaggerTag: 'Charging - Sessions',
    endpoints: [
      {
        id: 'session-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all charging sessions',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<ChargingSessionResponse>>',
      },
      {
        id: 'session-start',
        method: 'POST',
        path: '/start',
        summary: 'Start charging session',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'query', required: true },
          { name: 'stationId', type: 'Long', location: 'query', required: true },
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'initialSoc', type: 'Double', location: 'query', required: false }
        ],
        responseBody: 'ChargingSessionResponse',
      },
      {
        id: 'session-complete',
        method: 'POST',
        path: '/{id}/complete',
        summary: 'Complete charging session',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true },
          { name: 'energyConsumed', type: 'BigDecimal', location: 'query', required: true },
          { name: 'finalSoc', type: 'Double', location: 'query', required: false }
        ],
        responseBody: 'ChargingSessionResponse',
      },
      {
        id: 'session-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get session by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ChargingSessionResponse',
      },
      {
        id: 'session-get-by-vehicle',
        method: 'GET',
        path: '/vehicle/{vehicleId}',
        summary: 'Get sessions by vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<ChargingSessionResponse>',
      },
      {
        id: 'session-get-by-company',
        method: 'GET',
        path: '/company/{companyId}',
        summary: 'Get sessions by company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'List<ChargingSessionResponse>',
      },
    ],
  },

  // ========================
  // MAINTENANCE MODULE
  // ========================
  {
    id: 'maintenance-controller',
    name: 'MaintenanceController',
    basePath: '/api/v1/maintenance',
    module: 'maintenance',
    description: 'Vehicle Maintenance Management',
    swaggerTag: 'Maintenance',
    endpoints: [
      {
        id: 'maint-create',
        method: 'POST',
        path: '/records',
        summary: 'Create maintenance record',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        requestBody: 'MaintenanceRecordRequest',
        responseBody: 'ApiResponse<MaintenanceRecordResponse>',
      },
      {
        id: 'maint-get-all',
        method: 'GET',
        path: '/records',
        summary: 'Get all maintenance records',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<MaintenanceRecordResponse>>',
      },
      {
        id: 'maint-get-by-id',
        method: 'GET',
        path: '/records/{id}',
        summary: 'Get maintenance record by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<MaintenanceRecordResponse>',
      },
      {
        id: 'maint-get-by-vehicle',
        method: 'GET',
        path: '/records/vehicle/{vehicleId}',
        summary: 'Get maintenance records by vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<MaintenanceRecordResponse>>',
      },
      {
        id: 'maint-get-upcoming',
        method: 'GET',
        path: '/records/upcoming',
        summary: 'Get upcoming maintenance',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<MaintenanceRecordResponse>>',
      },
      {
        id: 'maint-update',
        method: 'PUT',
        path: '/records/{id}',
        summary: 'Update maintenance record',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'MaintenanceRecordRequest',
        responseBody: 'ApiResponse<MaintenanceRecordResponse>',
      },
      {
        id: 'maint-complete',
        method: 'POST',
        path: '/records/{id}/complete',
        summary: 'Complete maintenance',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<MaintenanceRecordResponse>',
      },
      {
        id: 'maint-delete',
        method: 'DELETE',
        path: '/records/{id}',
        summary: 'Delete maintenance record',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
      {
        id: 'maint-alerts',
        method: 'GET',
        path: '/alerts',
        summary: 'Get maintenance alerts',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'daysAhead', type: 'Integer', location: 'query', required: false, defaultValue: '30' }
        ],
        responseBody: 'ApiResponse<List<MaintenanceAlertResponse>>',
      },
      {
        id: 'maint-types',
        method: 'GET',
        path: '/types',
        summary: 'Get all maintenance types',
        responseBody: 'ApiResponse<List<String>>',
      },
      {
        id: 'maint-types-vehicle',
        method: 'GET',
        path: '/types/vehicle/{vehicleId}',
        summary: 'Get valid maintenance types for vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<String>>',
      },
    ],
  },

  // ========================
  // BILLING MODULE
  // ========================
  {
    id: 'billing-controller',
    name: 'BillingController',
    basePath: '/api/v1/billing',
    module: 'billing',
    description: 'Billing and Subscription Management',
    swaggerTag: 'Billing',
    endpoints: [
      {
        id: 'billing-get-subscription',
        method: 'GET',
        path: '/subscription',
        summary: 'Get current subscription',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<SubscriptionResponse>',
      },
      {
        id: 'billing-update-subscription',
        method: 'POST',
        path: '/subscription/update',
        summary: 'Update subscription tier and cycle',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        requestBody: 'SubscriptionRequest',
        responseBody: 'ApiResponse<SubscriptionResponse>',
      },
      {
        id: 'billing-cancel-subscription',
        method: 'POST',
        path: '/subscription/cancel',
        summary: 'Cancel subscription',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
      },
      {
        id: 'billing-get-invoices',
        method: 'GET',
        path: '/invoices',
        summary: 'Get all invoices',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<InvoiceResponse>>',
      },
      {
        id: 'billing-get-invoice',
        method: 'GET',
        path: '/invoices/{invoiceId}',
        summary: 'Get invoice by ID',
        parameters: [
          { name: 'invoiceId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<InvoiceResponse>',
      },
      {
        id: 'billing-pay-invoice',
        method: 'POST',
        path: '/invoices/{invoiceId}/pay',
        summary: 'Process payment for invoice',
        parameters: [
          { name: 'invoiceId', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'PaymentRequest',
        responseBody: 'ApiResponse<PaymentResponse>',
      },
      {
        id: 'billing-get-payments',
        method: 'GET',
        path: '/payments',
        summary: 'Get payment history',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<PaymentResponse>>',
      },
    ],
  },

  // ========================
  // CUSTOMER MODULE
  // ========================
  {
    id: 'customer-controller',
    name: 'CustomerController',
    basePath: '/api/customers',
    module: 'customer',
    description: 'Customer & Feedback Management',
    swaggerTag: 'Customer Management',
    endpoints: [
      {
        id: 'customer-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all customers',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<CustomerResponse>>',
      },
      {
        id: 'customer-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get customer by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<CustomerResponse>',
      },
      {
        id: 'customer-create',
        method: 'POST',
        path: '/',
        summary: 'Create a new customer',
        requestBody: 'CustomerRequest',
        responseBody: 'ApiResponse<CustomerResponse>',
      },
      {
        id: 'customer-update',
        method: 'PUT',
        path: '/{id}',
        summary: 'Update an existing customer',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'CustomerRequest',
        responseBody: 'ApiResponse<CustomerResponse>',
      },
      {
        id: 'customer-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete a customer',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
      {
        id: 'customer-feedback',
        method: 'POST',
        path: '/feedback',
        summary: 'Submit customer feedback',
        requestBody: 'FeedbackRequest',
        responseBody: 'ApiResponse<FeedbackResponse>',
      },
      {
        id: 'customer-get-feedback',
        method: 'GET',
        path: '/{id}/feedback',
        summary: 'Get feedback for a customer',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<FeedbackResponse>>',
      },
    ],
  },

  // ========================
  // ROUTING MODULE
  // ========================
  {
    id: 'route-controller',
    name: 'RouteController',
    basePath: '/api/routes',
    module: 'routing',
    description: 'Route Planning & Optimization',
    swaggerTag: 'Route Planning',
    endpoints: [
      {
        id: 'route-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all routes for a company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<RoutePlanResponse>>',
      },
      {
        id: 'route-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get route by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<RoutePlanResponse>',
      },
      {
        id: 'route-create',
        method: 'POST',
        path: '/',
        summary: 'Create a new route plan',
        requestBody: 'RoutePlanRequest',
        responseBody: 'ApiResponse<RoutePlanResponse>',
      },
      {
        id: 'route-update',
        method: 'PUT',
        path: '/{id}',
        summary: 'Update an existing route plan',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'RoutePlanRequest',
        responseBody: 'ApiResponse<RoutePlanResponse>',
      },
      {
        id: 'route-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete a route plan',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
      {
        id: 'route-add-waypoint',
        method: 'POST',
        path: '/{id}/waypoints',
        summary: 'Add a waypoint to a route',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'WaypointRequest',
        responseBody: 'ApiResponse<WaypointResponse>',
      },
      {
        id: 'route-start',
        method: 'POST',
        path: '/{id}/start',
        summary: 'Start route execution',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<RoutePlanResponse>',
      },
      {
        id: 'route-complete',
        method: 'POST',
        path: '/{id}/complete',
        summary: 'Complete route execution',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<RoutePlanResponse>',
      },
    ],
  },

  // ========================
  // GEOFENCING MODULE
  // ========================
  {
    id: 'geofence-controller',
    name: 'GeofenceController',
    basePath: '/api/geofences',
    module: 'geofencing',
    description: 'Geofencing & Spatial Alerts',
    swaggerTag: 'Geofencing',
    endpoints: [
      {
        id: 'geofence-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all geofences',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<GeofenceResponse>>',
      },
      {
        id: 'geofence-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get geofence by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<GeofenceResponse>',
      },
      {
        id: 'geofence-create',
        method: 'POST',
        path: '/',
        summary: 'Create a new geofence',
        requestBody: 'GeofenceRequest',
        responseBody: 'ApiResponse<GeofenceResponse>',
      },
      {
        id: 'geofence-update',
        method: 'PUT',
        path: '/{id}',
        summary: 'Update an existing geofence',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        requestBody: 'GeofenceRequest',
        responseBody: 'ApiResponse<GeofenceResponse>',
      },
      {
        id: 'geofence-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete a geofence',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
    ],
  },

  // ========================
  // NOTIFICATION MODULE
  // ========================
  {
    id: 'notification-controller',
    name: 'NotificationController',
    basePath: '/api/v1/notifications',
    module: 'notification',
    description: 'Notification Management',
    swaggerTag: 'Notifications',
    endpoints: [
      {
        id: 'notif-get-alerts',
        method: 'GET',
        path: '/alerts',
        summary: 'Get all alerts (high-priority)',
        responseBody: 'ApiResponse<List<NotificationResponse>>',
      },
      {
        id: 'notif-get-alerts-priority',
        method: 'GET',
        path: '/alerts/{priority}',
        summary: 'Get alerts by priority',
        parameters: [
          { name: 'priority', type: 'String', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<NotificationResponse>>',
      },
      {
        id: 'notif-get-all',
        method: 'GET',
        path: '/',
        summary: 'Get all notifications for user',
        parameters: [
          { name: 'userId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<NotificationResponse>>',
      },
      {
        id: 'notif-get-unread',
        method: 'GET',
        path: '/unread',
        summary: 'Get unread notifications',
        parameters: [
          { name: 'userId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<NotificationResponse>>',
      },
      {
        id: 'notif-get-unread-count',
        method: 'GET',
        path: '/unread/count',
        summary: 'Get unread notification count',
        parameters: [
          { name: 'userId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<Long>',
      },
      {
        id: 'notif-mark-read',
        method: 'PUT',
        path: '/{id}/read',
        summary: 'Mark notification as read',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<NotificationResponse>',
      },
      {
        id: 'notif-mark-all-read',
        method: 'PUT',
        path: '/read-all',
        summary: 'Mark all notifications as read',
        parameters: [
          { name: 'userId', type: 'Long', location: 'query', required: true }
        ],
      },
      {
        id: 'notif-delete',
        method: 'DELETE',
        path: '/{id}',
        summary: 'Delete notification',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
      },
    ],
  },

  // ========================
  // DOCUMENT MODULE
  // ========================
  {
    id: 'document-controller',
    name: 'DocumentController',
    basePath: '/api/documents',
    module: 'document',
    description: 'Document Management',
    swaggerTag: 'Documents',
    endpoints: [
      {
        id: 'doc-upload',
        method: 'POST',
        path: '/',
        summary: 'Upload a document',
        description: 'Multipart form upload',
        parameters: [
          { name: 'file', type: 'MultipartFile', location: 'body', required: true },
          { name: 'documentType', type: 'String', location: 'query', required: true },
          { name: 'entityType', type: 'String', location: 'query', required: true },
          { name: 'entityId', type: 'Long', location: 'query', required: true },
          { name: 'expiryDate', type: 'String', location: 'query', required: false }
        ],
        responseBody: 'DocumentResponse',
      },
      {
        id: 'doc-get-by-id',
        method: 'GET',
        path: '/{id}',
        summary: 'Get document by ID',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'DocumentResponse',
      },
      {
        id: 'doc-download',
        method: 'GET',
        path: '/{id}/download',
        summary: 'Download document file',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'Resource',
      },
      {
        id: 'doc-verify',
        method: 'PUT',
        path: '/{id}/verify',
        summary: 'Verify a document',
        parameters: [
          { name: 'id', type: 'Long', location: 'path', required: true },
          { name: 'verifiedBy', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'DocumentResponse',
      },
    ],
  },

  // ========================
  // TELEMATICS MODULE
  // ========================
  {
    id: 'telematics-controller',
    name: 'TelematicsController',
    basePath: '/api/v1/telematics',
    module: 'telematics',
    description: 'Telematics and Driver Behavior Monitoring',
    swaggerTag: 'Telematics',
    endpoints: [
      {
        id: 'telem-ingest-event',
        method: 'POST',
        path: '/events',
        summary: 'Ingest telematics event from vehicle sensors',
        requestBody: 'TelematicsEventRequest',
        responseBody: 'ApiResponse<DrivingEventResponse>',
      },
      {
        id: 'telem-events-trip',
        method: 'GET',
        path: '/events/trip/{tripId}',
        summary: 'Get all events for a trip',
        parameters: [
          { name: 'tripId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<DrivingEventResponse>>',
      },
      {
        id: 'telem-events-driver',
        method: 'GET',
        path: '/events/driver/{driverId}',
        summary: 'Get all events for a driver',
        parameters: [
          { name: 'driverId', type: 'Long', location: 'path', required: true },
          { name: 'start', type: 'LocalDateTime', location: 'query', required: false },
          { name: 'end', type: 'LocalDateTime', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<DrivingEventResponse>>',
      },
      {
        id: 'telem-events-vehicle',
        method: 'GET',
        path: '/events/vehicle/{vehicleId}',
        summary: 'Get all events for a vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<List<DrivingEventResponse>>',
      },
      {
        id: 'telem-driver-stats',
        method: 'GET',
        path: '/stats/driver/{driverId}',
        summary: 'Get event statistics for a driver',
        parameters: [
          { name: 'driverId', type: 'Long', location: 'path', required: true },
          { name: 'start', type: 'LocalDateTime', location: 'query', required: true },
          { name: 'end', type: 'LocalDateTime', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<DriverEventStats>',
      },
      {
        id: 'telem-health',
        method: 'GET',
        path: '/health',
        summary: 'Get telematics provider health status',
        responseBody: 'ApiResponse<Object>',
      },
      {
        id: 'telem-providers',
        method: 'GET',
        path: '/providers',
        summary: 'List all available telematics providers',
        responseBody: 'ApiResponse<List<String>>',
      },
    ],
  },

  // ========================
  // ANALYTICS MODULE
  // ========================
  {
    id: 'analytics-controller',
    name: 'AnalyticsController',
    basePath: '/api/v1/analytics',
    module: 'analytics',
    description: 'Fleet Analytics and Reporting',
    swaggerTag: 'Analytics',
    endpoints: [
      {
        id: 'analytics-fleet',
        method: 'GET',
        path: '/fleet',
        summary: 'Get fleet analytics summary (default: today)',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<FleetSummaryResponse>',
      },
      {
        id: 'analytics-fleet-summary',
        method: 'GET',
        path: '/fleet-summary',
        summary: 'Get fleet summary for a specific date',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'date', type: 'LocalDate', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<FleetSummaryResponse>',
      },
      {
        id: 'analytics-fleet-today',
        method: 'GET',
        path: '/fleet-summary/today',
        summary: "Get today's fleet summary",
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<FleetSummaryResponse>',
      },
      {
        id: 'analytics-fleet-range',
        method: 'GET',
        path: '/fleet-summary/range',
        summary: 'Get fleet summary for a date range',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: true },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<FleetSummaryResponse>>',
      },
      {
        id: 'analytics-monthly-report',
        method: 'GET',
        path: '/monthly-report',
        summary: 'Get monthly fleet report',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'year', type: 'int', location: 'query', required: true },
          { name: 'month', type: 'int', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<FleetSummaryResponse>>',
      },
      {
        id: 'analytics-fleet-analytics',
        method: 'GET',
        path: '/fleet-analytics',
        summary: 'Get comprehensive fleet analytics',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<FleetAnalyticsResponse>',
      },
      {
        id: 'analytics-utilization',
        method: 'GET',
        path: '/utilization-reports',
        summary: 'Get vehicle utilization reports',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: false },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<VehicleUtilizationResponse>>',
      },
      {
        id: 'analytics-cost',
        method: 'GET',
        path: '/cost-analytics',
        summary: 'Get cost analytics for company',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: false },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<CostAnalyticsResponse>>',
      },
      {
        id: 'analytics-tco',
        method: 'GET',
        path: '/tco-analysis/{vehicleId}',
        summary: 'Get TCO analysis for vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true }
        ],
        responseBody: 'ApiResponse<TCOAnalysisResponse>',
      },
      {
        id: 'analytics-tco-trend',
        method: 'GET',
        path: '/tco/{vehicleId}/trend',
        summary: 'Get TCO trend for a vehicle over time',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: true },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<List<TCOAnalysisResponse>>',
      },
      {
        id: 'analytics-energy',
        method: 'GET',
        path: '/energy-consumption/{vehicleId}',
        summary: 'Get energy consumption analytics for a vehicle',
        parameters: [
          { name: 'vehicleId', type: 'Long', location: 'path', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: false },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<List<EnergyConsumptionResponse>>',
      },
    ],
  },

  // ========================
  // DASHBOARD CONTROLLER
  // ========================
  {
    id: 'dashboard-controller',
    name: 'DashboardMetricsController',
    basePath: '/api/v1/dashboard',
    module: 'analytics',
    description: 'Dashboard Metrics',
    swaggerTag: 'Dashboard',
    endpoints: [
      {
        id: 'dashboard-metrics',
        method: 'GET',
        path: '/metrics',
        summary: 'Get dashboard metrics',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<DashboardMetricsResponse>',
      },
    ],
  },

  // ========================
  // ESG CONTROLLER
  // ========================
  {
    id: 'esg-controller',
    name: 'ESGController',
    basePath: '/api/v1/esg',
    module: 'analytics',
    description: 'Environmental, Social, Governance Reporting',
    swaggerTag: 'ESG',
    endpoints: [
      {
        id: 'esg-report',
        method: 'GET',
        path: '/report',
        summary: 'Get ESG report',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true },
          { name: 'startDate', type: 'LocalDate', location: 'query', required: false },
          { name: 'endDate', type: 'LocalDate', location: 'query', required: false }
        ],
        responseBody: 'ApiResponse<ESGReportResponse>',
      },
      {
        id: 'esg-carbon',
        method: 'GET',
        path: '/carbon-footprint',
        summary: 'Get carbon footprint data',
        parameters: [
          { name: 'companyId', type: 'Long', location: 'query', required: true }
        ],
        responseBody: 'ApiResponse<CarbonFootprintResponse>',
      },
    ],
  },
];

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

// Group controllers by module
export const controllersByModule = apiControllers.reduce((acc, controller) => {
  if (!acc[controller.module]) {
    acc[controller.module] = [];
  }
  acc[controller.module].push(controller);
  return acc;
}, {} as Record<string, ApiController[]>);

// Get all endpoints flat
export const allEndpoints: (ApiEndpoint & { controller: string; fullPath: string })[] = apiControllers.flatMap(
  controller => controller.endpoints.map(endpoint => ({
    ...endpoint,
    controller: controller.name,
    fullPath: controller.basePath + endpoint.path,
  }))
);

// API statistics
export const apiStats = {
  totalControllers: apiControllers.length,
  totalEndpoints: allEndpoints.length,
  endpointsByMethod: {
    GET: allEndpoints.filter(e => e.method === 'GET').length,
    POST: allEndpoints.filter(e => e.method === 'POST').length,
    PUT: allEndpoints.filter(e => e.method === 'PUT').length,
    DELETE: allEndpoints.filter(e => e.method === 'DELETE').length,
    PATCH: allEndpoints.filter(e => e.method === 'PATCH').length,
  },
  controllersByModule: Object.entries(controllersByModule).map(([module, controllers]) => ({
    module,
    controllerCount: controllers.length,
    endpointCount: controllers.reduce((sum, c) => sum + c.endpoints.length, 0),
  })),
};

// Module colors for visualization
export const moduleColors: Record<string, string> = {
  auth: '#E91E63',
  fleet: '#4CAF50',
  driver: '#2196F3',
  charging: '#FF9800',
  maintenance: '#9C27B0',
  billing: '#00BCD4',
  customer: '#FF5722',
  routing: '#3F51B5',
  geofencing: '#795548',
  notification: '#607D8B',
  document: '#FFC107',
  analytics: '#009688',
  telematics: '#673AB7',
};
