// ============================================================================
// COMPREHENSIVE DATABASE SCHEMA DATA
// Based on actual @Entity analysis from backend/evfleet-monolith
// ============================================================================

export interface ColumnDef {
  name: string;
  type: string;
  nullable?: boolean;
  unique?: boolean;
  primaryKey?: boolean;
  foreignKey?: string; // Reference to another table
  defaultValue?: string;
  description?: string;
}

export interface IndexDef {
  name: string;
  columns: string[];
  unique?: boolean;
}

export interface TableDef {
  id: string;
  name: string;
  displayName: string;
  module: string;
  description: string;
  columns: ColumnDef[];
  indexes?: IndexDef[];
  relationships?: {
    type: 'one-to-many' | 'many-to-one' | 'many-to-many' | 'one-to-one';
    table: string;
    description: string;
  }[];
}

export interface EnumDef {
  name: string;
  module: string;
  values: string[];
}

// ============================================================================
// ENUMS - All enums used in entity classes
// ============================================================================
export const databaseEnums: EnumDef[] = [
  // Auth Module
  { 
    name: 'RoleType', 
    module: 'auth',
    values: ['SUPER_ADMIN', 'ADMIN', 'FLEET_MANAGER', 'DRIVER', 'VIEWER']
  },
  
  // Fleet Module
  {
    name: 'VehicleType',
    module: 'fleet',
    values: ['SEDAN', 'SUV', 'HATCHBACK', 'VAN', 'TRUCK', 'BUS', 'MOTORCYCLE', 'THREE_WHEELER']
  },
  {
    name: 'FuelType',
    module: 'fleet',
    values: ['EV', 'ICE', 'HYBRID']
  },
  {
    name: 'VehicleStatus',
    module: 'fleet',
    values: ['AVAILABLE', 'IN_USE', 'MAINTENANCE', 'CHARGING', 'OFFLINE', 'RESERVED']
  },
  {
    name: 'TripStatus',
    module: 'fleet',
    values: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
  },

  // Driver Module
  {
    name: 'DriverStatus',
    module: 'driver',
    values: ['ACTIVE', 'INACTIVE', 'ON_TRIP', 'ON_LEAVE']
  },

  // Charging Module
  {
    name: 'StationStatus',
    module: 'charging',
    values: ['AVAILABLE', 'FULL', 'MAINTENANCE', 'OFFLINE']
  },
  {
    name: 'SessionStatus',
    module: 'charging',
    values: ['ACTIVE', 'COMPLETED', 'FAILED', 'CANCELLED']
  },

  // Maintenance Module
  {
    name: 'MaintenanceType',
    module: 'maintenance',
    values: [
      'ROUTINE_SERVICE', 'TIRE_REPLACEMENT', 'BRAKE_SERVICE', 'EMERGENCY_REPAIR',
      'OIL_CHANGE', 'FILTER_REPLACEMENT', 'EMISSION_TEST', 'COOLANT_FLUSH',
      'TRANSMISSION_SERVICE', 'ENGINE_DIAGNOSTICS',
      'BATTERY_CHECK', 'HV_SYSTEM_CHECK', 'FIRMWARE_UPDATE', 
      'CHARGING_PORT_INSPECTION', 'THERMAL_MANAGEMENT_CHECK',
      'HYBRID_SYSTEM_CHECK'
    ]
  },
  {
    name: 'MaintenanceStatus',
    module: 'maintenance',
    values: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'OVERDUE']
  },

  // Billing Module
  {
    name: 'InvoiceStatus',
    module: 'billing',
    values: ['DRAFT', 'PENDING', 'PAID', 'PARTIALLY_PAID', 'OVERDUE', 'CANCELLED']
  },
  {
    name: 'PaymentStatus',
    module: 'billing',
    values: ['PENDING', 'COMPLETED', 'FAILED', 'REFUNDED']
  },
  {
    name: 'SubscriptionStatus',
    module: 'billing',
    values: ['ACTIVE', 'CANCELLED', 'EXPIRED', 'TRIAL']
  },

  // Customer Module
  {
    name: 'CustomerType',
    module: 'customer',
    values: ['INDIVIDUAL', 'BUSINESS']
  },

  // Routing Module
  {
    name: 'RouteStatus',
    module: 'routing',
    values: ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
  },
  {
    name: 'OptimizationCriteria',
    module: 'routing',
    values: ['SHORTEST_DISTANCE', 'SHORTEST_TIME', 'LOWEST_COST', 'LEAST_ENERGY']
  },

  // Geofencing Module
  {
    name: 'GeofenceType',
    module: 'geofencing',
    values: ['CHARGING_ZONE', 'DEPOT', 'NO_GO_ZONE', 'SERVICE_AREA', 'DELIVERY_ZONE', 'MAINTENANCE_ZONE', 'CUSTOM']
  },

  // Notification Module
  {
    name: 'NotificationType',
    module: 'notification',
    values: ['INFO', 'WARNING', 'ALERT', 'CRITICAL']
  },
];

// ============================================================================
// TABLES - All database tables from @Entity classes
// ============================================================================
export const databaseTables: TableDef[] = [
  // ========================
  // AUTH MODULE
  // ========================
  {
    id: 'users',
    name: 'users',
    displayName: 'Users',
    module: 'auth',
    description: 'User accounts with Firebase Auth integration',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true, description: 'Auto-generated ID' },
      { name: 'firebase_uid', type: 'VARCHAR(128)', unique: true, description: 'Firebase Auth UID' },
      { name: 'email', type: 'VARCHAR(255)', unique: true, description: 'User email' },
      { name: 'name', type: 'VARCHAR(100)', description: 'Full name' },
      { name: 'first_name', type: 'VARCHAR(50)', nullable: true },
      { name: 'last_name', type: 'VARCHAR(50)', nullable: true },
      { name: 'phone', type: 'VARCHAR(20)', nullable: true },
      { name: 'company_id', type: 'BIGINT', nullable: true, foreignKey: 'companies.id' },
      { name: 'company_name', type: 'VARCHAR(255)', nullable: true },
      { name: 'active', type: 'BOOLEAN', defaultValue: 'true' },
      { name: 'email_verified', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'profile_image_url', type: 'VARCHAR(500)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP', description: 'Audit field' },
      { name: 'updated_at', type: 'TIMESTAMP', description: 'Audit field' },
    ],
    indexes: [
      { name: 'idx_firebase_uid', columns: ['firebase_uid'] },
      { name: 'idx_email', columns: ['email'] },
      { name: 'idx_company_id', columns: ['company_id'] },
    ],
    relationships: [
      { type: 'many-to-many', table: 'roles', description: 'User roles via user_roles join table' },
    ],
  },
  {
    id: 'roles',
    name: 'roles',
    displayName: 'Roles',
    module: 'auth',
    description: 'RBAC roles for authorization',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'name', type: 'VARCHAR(50)', unique: true, description: 'Role name (e.g., ADMIN, DRIVER)' },
      { name: 'description', type: 'VARCHAR(255)', nullable: true },
      { name: 'permissions', type: 'TEXT', nullable: true, description: 'JSON array of permissions' },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    relationships: [
      { type: 'many-to-many', table: 'users', description: 'Users with this role' },
    ],
  },
  {
    id: 'user_roles',
    name: 'user_roles',
    displayName: 'User Roles (Join)',
    module: 'auth',
    description: 'Many-to-many join table for users and roles',
    columns: [
      { name: 'user_id', type: 'BIGINT', foreignKey: 'users.id' },
      { name: 'role_id', type: 'BIGINT', foreignKey: 'roles.id' },
    ],
  },

  // ========================
  // FLEET MODULE
  // ========================
  {
    id: 'vehicles',
    name: 'vehicles',
    displayName: 'Vehicles',
    module: 'fleet',
    description: 'Multi-fuel vehicles (EV, ICE, Hybrid)',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'version', type: 'BIGINT', description: 'Optimistic locking' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'vehicle_number', type: 'VARCHAR(50)', unique: true, description: 'License plate' },
      { name: 'type', type: 'VARCHAR(20)', description: 'VehicleType enum' },
      { name: 'fuel_type', type: 'VARCHAR(20)', description: 'EV/ICE/HYBRID' },
      { name: 'make', type: 'VARCHAR(100)', description: 'Manufacturer' },
      { name: 'model', type: 'VARCHAR(100)', description: 'Model name' },
      { name: 'year', type: 'INTEGER' },
      { name: 'battery_capacity', type: 'DOUBLE', nullable: true, description: 'EV: kWh' },
      { name: 'current_battery_soc', type: 'DOUBLE', nullable: true, description: 'EV: 0-100%' },
      { name: 'default_charger_type', type: 'VARCHAR(50)', nullable: true, description: 'EV: CCS/CHAdeMO/Type2' },
      { name: 'fuel_tank_capacity', type: 'DOUBLE', nullable: true, description: 'ICE: liters' },
      { name: 'fuel_level', type: 'DOUBLE', nullable: true, description: 'ICE: liters' },
      { name: 'engine_type', type: 'VARCHAR(50)', nullable: true, description: 'ICE: engine specs' },
      { name: 'status', type: 'VARCHAR(20)', description: 'VehicleStatus enum' },
      { name: 'latitude', type: 'DOUBLE', nullable: true },
      { name: 'longitude', type: 'DOUBLE', nullable: true },
      { name: 'last_updated', type: 'TIMESTAMP', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_company_id', columns: ['company_id'] },
      { name: 'idx_vehicle_number', columns: ['vehicle_number'] },
      { name: 'idx_status', columns: ['status'] },
      { name: 'idx_fuel_type', columns: ['fuel_type'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'trips', description: 'Vehicle trips' },
      { type: 'one-to-many', table: 'charging_sessions', description: 'Charging sessions' },
      { type: 'one-to-many', table: 'maintenance_records', description: 'Maintenance history' },
    ],
  },
  {
    id: 'trips',
    name: 'trips',
    displayName: 'Trips',
    module: 'fleet',
    description: 'Vehicle trips with tracking data',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'version', type: 'BIGINT' },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'driver_id', type: 'BIGINT', nullable: true, foreignKey: 'drivers.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'start_time', type: 'TIMESTAMP' },
      { name: 'end_time', type: 'TIMESTAMP', nullable: true },
      { name: 'start_latitude', type: 'DOUBLE', nullable: true },
      { name: 'start_longitude', type: 'DOUBLE', nullable: true },
      { name: 'end_latitude', type: 'DOUBLE', nullable: true },
      { name: 'end_longitude', type: 'DOUBLE', nullable: true },
      { name: 'distance', type: 'DOUBLE', nullable: true, description: 'km' },
      { name: 'duration', type: 'BIGINT', nullable: true, description: 'seconds' },
      { name: 'energy_consumed', type: 'DECIMAL(10,3)', nullable: true, description: 'kWh for EV' },
      { name: 'fuel_consumed', type: 'DECIMAL(10,3)', nullable: true, description: 'liters for ICE' },
      { name: 'status', type: 'VARCHAR(20)', description: 'TripStatus enum' },
      { name: 'notes', type: 'VARCHAR(500)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_trip_vehicle', columns: ['vehicle_id'] },
      { name: 'idx_trip_driver', columns: ['driver_id'] },
      { name: 'idx_trip_status', columns: ['status'] },
      { name: 'idx_trip_start_time', columns: ['start_time'] },
    ],
    relationships: [
      { type: 'many-to-one', table: 'vehicles', description: 'Vehicle used' },
      { type: 'many-to-one', table: 'drivers', description: 'Driver assigned' },
      { type: 'one-to-many', table: 'trip_location_history', description: 'GPS track points' },
    ],
  },
  {
    id: 'trip_location_history',
    name: 'trip_location_history',
    displayName: 'Trip Location History',
    module: 'fleet',
    description: 'GPS tracking points for trips',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'trip_id', type: 'BIGINT', foreignKey: 'trips.id' },
      { name: 'latitude', type: 'DOUBLE' },
      { name: 'longitude', type: 'DOUBLE' },
      { name: 'speed', type: 'DOUBLE', nullable: true, description: 'km/h' },
      { name: 'heading', type: 'DOUBLE', nullable: true, description: 'degrees' },
      { name: 'recorded_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'battery_health',
    name: 'battery_health',
    displayName: 'Battery Health',
    module: 'fleet',
    description: 'EV battery health tracking',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'state_of_health', type: 'DOUBLE', description: 'SOH percentage' },
      { name: 'cycle_count', type: 'INTEGER' },
      { name: 'degradation_rate', type: 'DOUBLE', nullable: true },
      { name: 'estimated_range', type: 'DOUBLE', nullable: true, description: 'km' },
      { name: 'recorded_at', type: 'TIMESTAMP' },
    ],
  },

  // ========================
  // DRIVER MODULE
  // ========================
  {
    id: 'drivers',
    name: 'drivers',
    displayName: 'Drivers',
    module: 'driver',
    description: 'Driver profiles with performance metrics',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'name', type: 'VARCHAR(100)' },
      { name: 'phone', type: 'VARCHAR(20)', unique: true },
      { name: 'email', type: 'VARCHAR(255)', nullable: true, unique: true },
      { name: 'license_number', type: 'VARCHAR(50)', unique: true },
      { name: 'license_expiry', type: 'DATE' },
      { name: 'status', type: 'VARCHAR(20)', description: 'DriverStatus enum' },
      { name: 'current_vehicle_id', type: 'BIGINT', nullable: true, foreignKey: 'vehicles.id' },
      { name: 'total_trips', type: 'INTEGER', nullable: true },
      { name: 'total_distance', type: 'DOUBLE', nullable: true, description: 'km' },
      { name: 'safety_score', type: 'DOUBLE', nullable: true, description: '0-100' },
      { name: 'fuel_efficiency', type: 'DOUBLE', nullable: true },
      { name: 'harsh_braking_events', type: 'INTEGER', nullable: true },
      { name: 'speeding_events', type: 'INTEGER', nullable: true },
      { name: 'idling_time_minutes', type: 'INTEGER', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_driver_company', columns: ['company_id'] },
      { name: 'idx_driver_status', columns: ['status'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'trips', description: 'Driver trips' },
      { type: 'one-to-one', table: 'vehicles', description: 'Currently assigned vehicle' },
    ],
  },

  // ========================
  // CHARGING MODULE
  // ========================
  {
    id: 'charging_stations',
    name: 'charging_stations',
    displayName: 'Charging Stations',
    module: 'charging',
    description: 'EV charging infrastructure',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'version', type: 'BIGINT' },
      { name: 'name', type: 'VARCHAR(200)' },
      { name: 'address', type: 'VARCHAR(500)' },
      { name: 'latitude', type: 'DOUBLE' },
      { name: 'longitude', type: 'DOUBLE' },
      { name: 'total_slots', type: 'INTEGER' },
      { name: 'available_slots', type: 'INTEGER' },
      { name: 'status', type: 'VARCHAR(20)', description: 'StationStatus enum' },
      { name: 'charger_type', type: 'VARCHAR(50)', nullable: true, description: 'CCS/CHAdeMO/Type2' },
      { name: 'power_output', type: 'DOUBLE', nullable: true, description: 'kW' },
      { name: 'price_per_kwh', type: 'DECIMAL(10,2)', nullable: true },
      { name: 'operator_name', type: 'VARCHAR(100)', nullable: true },
      { name: 'phone', type: 'VARCHAR(20)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_station_status', columns: ['status'] },
      { name: 'idx_station_location', columns: ['latitude', 'longitude'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'charging_sessions', description: 'Sessions at this station' },
    ],
  },
  {
    id: 'charging_sessions',
    name: 'charging_sessions',
    displayName: 'Charging Sessions',
    module: 'charging',
    description: 'Individual charging session records',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'station_id', type: 'BIGINT', foreignKey: 'charging_stations.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'start_time', type: 'TIMESTAMP' },
      { name: 'end_time', type: 'TIMESTAMP', nullable: true },
      { name: 'energy_consumed', type: 'DECIMAL(10,3)', nullable: true, description: 'kWh' },
      { name: 'cost', type: 'DECIMAL(10,2)', nullable: true, description: 'INR' },
      { name: 'status', type: 'VARCHAR(20)', description: 'SessionStatus enum' },
      { name: 'initial_soc', type: 'DOUBLE', nullable: true, description: '0-100%' },
      { name: 'final_soc', type: 'DOUBLE', nullable: true, description: '0-100%' },
      { name: 'notes', type: 'VARCHAR(500)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_session_vehicle', columns: ['vehicle_id'] },
      { name: 'idx_session_station', columns: ['station_id'] },
      { name: 'idx_session_status', columns: ['status'] },
      { name: 'idx_session_start_time', columns: ['start_time'] },
    ],
    relationships: [
      { type: 'many-to-one', table: 'vehicles', description: 'Vehicle being charged' },
      { type: 'many-to-one', table: 'charging_stations', description: 'Station used' },
    ],
  },

  // ========================
  // MAINTENANCE MODULE
  // ========================
  {
    id: 'maintenance_records',
    name: 'maintenance_records',
    displayName: 'Maintenance Records',
    module: 'maintenance',
    description: 'Vehicle maintenance history',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'type', type: 'VARCHAR(50)', description: 'MaintenanceType enum' },
      { name: 'scheduled_date', type: 'DATE' },
      { name: 'completed_date', type: 'DATE', nullable: true },
      { name: 'status', type: 'VARCHAR(20)', description: 'MaintenanceStatus enum' },
      { name: 'cost', type: 'DECIMAL(12,2)', nullable: true },
      { name: 'description', type: 'VARCHAR(500)', nullable: true },
      { name: 'service_provider', type: 'VARCHAR(100)', nullable: true },
      { name: 'vehicle_distance_km', type: 'DOUBLE', nullable: true, description: 'Odometer reading' },
      { name: 'policy_id', type: 'BIGINT', nullable: true, foreignKey: 'maintenance_policies.id' },
      { name: 'attachment_urls', type: 'VARCHAR(2000)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    relationships: [
      { type: 'many-to-one', table: 'vehicles', description: 'Vehicle maintained' },
      { type: 'one-to-many', table: 'maintenance_line_items', description: 'Parts/labor items' },
    ],
  },
  {
    id: 'maintenance_policies',
    name: 'maintenance_policies',
    displayName: 'Maintenance Policies',
    module: 'maintenance',
    description: 'Automated maintenance scheduling rules',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'name', type: 'VARCHAR(200)' },
      { name: 'maintenance_type', type: 'VARCHAR(50)' },
      { name: 'interval_km', type: 'DOUBLE', nullable: true, description: 'Distance trigger' },
      { name: 'interval_days', type: 'INTEGER', nullable: true, description: 'Time trigger' },
      { name: 'applicable_fuel_types', type: 'VARCHAR(100)', nullable: true, description: 'EV/ICE/HYBRID' },
      { name: 'is_active', type: 'BOOLEAN', defaultValue: 'true' },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'maintenance_line_items',
    name: 'maintenance_line_items',
    displayName: 'Maintenance Line Items',
    module: 'maintenance',
    description: 'Parts and labor for maintenance',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'maintenance_record_id', type: 'BIGINT', foreignKey: 'maintenance_records.id' },
      { name: 'item_type', type: 'VARCHAR(20)', description: 'PART/LABOR' },
      { name: 'description', type: 'VARCHAR(500)' },
      { name: 'quantity', type: 'INTEGER' },
      { name: 'unit_price', type: 'DECIMAL(10,2)' },
      { name: 'total_price', type: 'DECIMAL(10,2)' },
    ],
  },

  // ========================
  // BILLING MODULE
  // ========================
  {
    id: 'invoices',
    name: 'invoices',
    displayName: 'Invoices',
    module: 'billing',
    description: 'Customer invoices',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'subscription_id', type: 'BIGINT', nullable: true, foreignKey: 'subscriptions.id' },
      { name: 'invoice_number', type: 'VARCHAR(50)', unique: true },
      { name: 'invoice_date', type: 'DATE' },
      { name: 'due_date', type: 'DATE' },
      { name: 'subtotal', type: 'DECIMAL(12,2)' },
      { name: 'tax_amount', type: 'DECIMAL(12,2)', defaultValue: '0' },
      { name: 'discount_amount', type: 'DECIMAL(12,2)', defaultValue: '0' },
      { name: 'total_amount', type: 'DECIMAL(12,2)' },
      { name: 'status', type: 'VARCHAR(20)', description: 'InvoiceStatus enum' },
      { name: 'paid_date', type: 'DATE', nullable: true },
      { name: 'paid_amount', type: 'DECIMAL(12,2)', defaultValue: '0' },
      { name: 'remarks', type: 'VARCHAR(1000)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_company_id', columns: ['company_id'] },
      { name: 'idx_invoice_number', columns: ['invoice_number'] },
      { name: 'idx_status', columns: ['status'] },
      { name: 'idx_due_date', columns: ['due_date'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'payments', description: 'Payments received' },
    ],
  },
  {
    id: 'payments',
    name: 'payments',
    displayName: 'Payments',
    module: 'billing',
    description: 'Payment transactions',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'invoice_id', type: 'BIGINT', foreignKey: 'invoices.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'amount', type: 'DECIMAL(12,2)' },
      { name: 'payment_method', type: 'VARCHAR(50)' },
      { name: 'payment_date', type: 'DATE' },
      { name: 'transaction_id', type: 'VARCHAR(100)', nullable: true },
      { name: 'status', type: 'VARCHAR(20)', description: 'PaymentStatus enum' },
      { name: 'notes', type: 'VARCHAR(500)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'subscriptions',
    name: 'subscriptions',
    displayName: 'Subscriptions',
    module: 'billing',
    description: 'Customer subscription plans',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'pricing_plan_id', type: 'BIGINT', foreignKey: 'pricing_plans.id' },
      { name: 'status', type: 'VARCHAR(20)', description: 'SubscriptionStatus enum' },
      { name: 'start_date', type: 'DATE' },
      { name: 'end_date', type: 'DATE', nullable: true },
      { name: 'next_billing_date', type: 'DATE', nullable: true },
      { name: 'auto_renew', type: 'BOOLEAN', defaultValue: 'true' },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'pricing_plans',
    name: 'pricing_plans',
    displayName: 'Pricing Plans',
    module: 'billing',
    description: 'Available subscription plans',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'name', type: 'VARCHAR(100)' },
      { name: 'description', type: 'VARCHAR(500)', nullable: true },
      { name: 'monthly_price', type: 'DECIMAL(10,2)' },
      { name: 'annual_price', type: 'DECIMAL(10,2)', nullable: true },
      { name: 'max_vehicles', type: 'INTEGER' },
      { name: 'max_drivers', type: 'INTEGER' },
      { name: 'features', type: 'TEXT', nullable: true, description: 'JSON array' },
      { name: 'is_active', type: 'BOOLEAN', defaultValue: 'true' },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'billing_addresses',
    name: 'billing_addresses',
    displayName: 'Billing Addresses',
    module: 'billing',
    description: 'Company billing addresses',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'address_line1', type: 'VARCHAR(255)' },
      { name: 'address_line2', type: 'VARCHAR(255)', nullable: true },
      { name: 'city', type: 'VARCHAR(100)' },
      { name: 'state', type: 'VARCHAR(100)' },
      { name: 'postal_code', type: 'VARCHAR(20)' },
      { name: 'country', type: 'VARCHAR(100)' },
      { name: 'is_default', type: 'BOOLEAN', defaultValue: 'false' },
    ],
  },

  // ========================
  // CUSTOMER MODULE
  // ========================
  {
    id: 'customers',
    name: 'customers',
    displayName: 'Customers',
    module: 'customer',
    description: 'Business/individual customers',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'customer_type', type: 'VARCHAR(20)', description: 'CustomerType enum' },
      { name: 'name', type: 'VARCHAR(200)' },
      { name: 'email', type: 'VARCHAR(100)', nullable: true },
      { name: 'phone', type: 'VARCHAR(20)' },
      { name: 'address', type: 'VARCHAR(500)', nullable: true },
      { name: 'city', type: 'VARCHAR(100)', nullable: true },
      { name: 'state', type: 'VARCHAR(100)', nullable: true },
      { name: 'postal_code', type: 'VARCHAR(20)', nullable: true },
      { name: 'country', type: 'VARCHAR(100)', nullable: true },
      { name: 'gstin', type: 'VARCHAR(15)', nullable: true, description: 'GST number (India)' },
      { name: 'pan', type: 'VARCHAR(10)', nullable: true, description: 'PAN (India)' },
      { name: 'business_name', type: 'VARCHAR(300)', nullable: true },
      { name: 'date_of_birth', type: 'VARCHAR(20)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_customer_company', columns: ['company_id'] },
      { name: 'idx_customer_type', columns: ['customer_type'] },
      { name: 'idx_customer_email', columns: ['email'] },
      { name: 'idx_customer_phone', columns: ['phone'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'customer_feedback', description: 'Customer feedback' },
    ],
  },
  {
    id: 'customer_feedback',
    name: 'customer_feedback',
    displayName: 'Customer Feedback',
    module: 'customer',
    description: 'Customer feedback and ratings',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'customer_id', type: 'BIGINT', foreignKey: 'customers.id' },
      { name: 'trip_id', type: 'BIGINT', nullable: true, foreignKey: 'trips.id' },
      { name: 'rating', type: 'INTEGER', description: '1-5 stars' },
      { name: 'comment', type: 'VARCHAR(1000)', nullable: true },
      { name: 'is_addressed', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },

  // ========================
  // ROUTING MODULE
  // ========================
  {
    id: 'route_plans',
    name: 'route_plans',
    displayName: 'Route Plans',
    module: 'routing',
    description: 'Optimized route planning',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'vehicle_id', type: 'BIGINT', nullable: true, foreignKey: 'vehicles.id' },
      { name: 'driver_id', type: 'BIGINT', nullable: true, foreignKey: 'drivers.id' },
      { name: 'route_name', type: 'VARCHAR(200)' },
      { name: 'optimization_criteria', type: 'VARCHAR(20)', description: 'OptimizationCriteria enum' },
      { name: 'status', type: 'VARCHAR(20)', description: 'RouteStatus enum' },
      { name: 'total_distance', type: 'DOUBLE', nullable: true, description: 'km' },
      { name: 'estimated_duration', type: 'BIGINT', nullable: true, description: 'minutes' },
      { name: 'estimated_cost', type: 'DOUBLE', nullable: true },
      { name: 'started_at', type: 'TIMESTAMP', nullable: true },
      { name: 'completed_at', type: 'TIMESTAMP', nullable: true },
      { name: 'notes', type: 'VARCHAR(1000)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_route_vehicle', columns: ['vehicle_id'] },
      { name: 'idx_route_company', columns: ['company_id'] },
      { name: 'idx_route_status', columns: ['status'] },
    ],
    relationships: [
      { type: 'one-to-many', table: 'route_waypoints', description: 'Waypoints in route' },
    ],
  },
  {
    id: 'route_waypoints',
    name: 'route_waypoints',
    displayName: 'Route Waypoints',
    module: 'routing',
    description: 'Points along a route',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'route_plan_id', type: 'BIGINT', foreignKey: 'route_plans.id' },
      { name: 'sequence', type: 'INTEGER', description: 'Order in route' },
      { name: 'name', type: 'VARCHAR(200)' },
      { name: 'latitude', type: 'DOUBLE' },
      { name: 'longitude', type: 'DOUBLE' },
      { name: 'address', type: 'VARCHAR(500)', nullable: true },
      { name: 'estimated_arrival', type: 'TIMESTAMP', nullable: true },
      { name: 'actual_arrival', type: 'TIMESTAMP', nullable: true },
      { name: 'stop_duration_minutes', type: 'INTEGER', nullable: true },
    ],
  },

  // ========================
  // GEOFENCING MODULE
  // ========================
  {
    id: 'geofences',
    name: 'geofences',
    displayName: 'Geofences',
    module: 'geofencing',
    description: 'Geographic boundaries/zones',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'name', type: 'VARCHAR(200)' },
      { name: 'geofence_type', type: 'VARCHAR(30)', description: 'GeofenceType enum' },
      { name: 'description', type: 'VARCHAR(500)', nullable: true },
      { name: 'center_latitude', type: 'DOUBLE' },
      { name: 'center_longitude', type: 'DOUBLE' },
      { name: 'radius', type: 'DOUBLE', description: 'meters' },
      { name: 'speed_limit', type: 'DOUBLE', nullable: true, description: 'km/h' },
      { name: 'alert_on_entry', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'alert_on_exit', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'is_active', type: 'BOOLEAN', defaultValue: 'true' },
      { name: 'color', type: 'VARCHAR(20)', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
    indexes: [
      { name: 'idx_geofence_company', columns: ['company_id'] },
      { name: 'idx_geofence_type', columns: ['geofence_type'] },
      { name: 'idx_geofence_active', columns: ['is_active'] },
    ],
  },

  // ========================
  // NOTIFICATION MODULE
  // ========================
  {
    id: 'notifications',
    name: 'notifications',
    displayName: 'Notifications',
    module: 'notification',
    description: 'System notifications',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'user_id', type: 'BIGINT', foreignKey: 'users.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'type', type: 'VARCHAR(20)', description: 'NotificationType enum' },
      { name: 'title', type: 'VARCHAR(200)' },
      { name: 'message', type: 'VARCHAR(1000)' },
      { name: 'is_read', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'entity_type', type: 'VARCHAR(50)', nullable: true, description: 'Related entity' },
      { name: 'entity_id', type: 'BIGINT', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },

  // ========================
  // DOCUMENT MODULE
  // ========================
  {
    id: 'documents',
    name: 'documents',
    displayName: 'Documents',
    module: 'document',
    description: 'Vehicle and driver documents',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'entity_type', type: 'VARCHAR(20)', description: 'VEHICLE/DRIVER' },
      { name: 'entity_id', type: 'BIGINT' },
      { name: 'document_type', type: 'VARCHAR(50)', description: 'RC/INSURANCE/PERMIT/LICENSE' },
      { name: 'document_number', type: 'VARCHAR(100)', nullable: true },
      { name: 'file_url', type: 'VARCHAR(500)' },
      { name: 'file_name', type: 'VARCHAR(255)' },
      { name: 'expiry_date', type: 'DATE', nullable: true },
      { name: 'is_verified', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'verified_by', type: 'BIGINT', nullable: true, foreignKey: 'users.id' },
      { name: 'verified_at', type: 'TIMESTAMP', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
      { name: 'updated_at', type: 'TIMESTAMP' },
    ],
  },

  // ========================
  // ANALYTICS MODULE
  // ========================
  {
    id: 'fleet_summary',
    name: 'fleet_summary',
    displayName: 'Fleet Summary',
    module: 'analytics',
    description: 'Aggregated fleet statistics',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'total_vehicles', type: 'INTEGER' },
      { name: 'active_vehicles', type: 'INTEGER' },
      { name: 'ev_count', type: 'INTEGER' },
      { name: 'ice_count', type: 'INTEGER' },
      { name: 'hybrid_count', type: 'INTEGER' },
      { name: 'average_battery_soc', type: 'DOUBLE', nullable: true },
      { name: 'total_distance_km', type: 'DOUBLE' },
      { name: 'total_trips', type: 'INTEGER' },
      { name: 'summary_date', type: 'DATE' },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'tco_analysis',
    name: 'tco_analysis',
    displayName: 'TCO Analysis',
    module: 'analytics',
    description: 'Total Cost of Ownership analysis',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'analysis_period_start', type: 'DATE' },
      { name: 'analysis_period_end', type: 'DATE' },
      { name: 'purchase_cost', type: 'DECIMAL(12,2)' },
      { name: 'fuel_energy_cost', type: 'DECIMAL(12,2)' },
      { name: 'maintenance_cost', type: 'DECIMAL(12,2)' },
      { name: 'insurance_cost', type: 'DECIMAL(12,2)' },
      { name: 'depreciation', type: 'DECIMAL(12,2)' },
      { name: 'total_tco', type: 'DECIMAL(12,2)' },
      { name: 'cost_per_km', type: 'DECIMAL(10,4)' },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'esg_reports',
    name: 'esg_reports',
    displayName: 'ESG Reports',
    module: 'analytics',
    description: 'Environmental, Social, Governance reports',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'report_period_start', type: 'DATE' },
      { name: 'report_period_end', type: 'DATE' },
      { name: 'total_carbon_kg', type: 'DOUBLE' },
      { name: 'carbon_saved_kg', type: 'DOUBLE' },
      { name: 'ev_percentage', type: 'DOUBLE' },
      { name: 'renewable_energy_percentage', type: 'DOUBLE', nullable: true },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'energy_consumption_analytics',
    name: 'energy_consumption_analytics',
    displayName: 'Energy Consumption',
    module: 'analytics',
    description: 'Energy usage tracking',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'company_id', type: 'BIGINT', foreignKey: 'companies.id' },
      { name: 'period_date', type: 'DATE' },
      { name: 'energy_consumed_kwh', type: 'DOUBLE' },
      { name: 'distance_km', type: 'DOUBLE' },
      { name: 'efficiency_kwh_per_100km', type: 'DOUBLE' },
      { name: 'cost', type: 'DECIMAL(10,2)' },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },

  // ========================
  // TELEMATICS MODULE
  // ========================
  {
    id: 'telemetry_snapshots',
    name: 'telemetry_snapshots',
    displayName: 'Telemetry Snapshots',
    module: 'telematics',
    description: 'Vehicle telemetry data points',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'latitude', type: 'DOUBLE' },
      { name: 'longitude', type: 'DOUBLE' },
      { name: 'speed', type: 'DOUBLE', nullable: true },
      { name: 'heading', type: 'DOUBLE', nullable: true },
      { name: 'battery_soc', type: 'DOUBLE', nullable: true },
      { name: 'fuel_level', type: 'DOUBLE', nullable: true },
      { name: 'odometer', type: 'DOUBLE', nullable: true },
      { name: 'engine_status', type: 'VARCHAR(20)', nullable: true },
      { name: 'recorded_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'telemetry_alerts',
    name: 'telemetry_alerts',
    displayName: 'Telemetry Alerts',
    module: 'telematics',
    description: 'Real-time vehicle alerts',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'alert_type', type: 'VARCHAR(50)' },
      { name: 'severity', type: 'VARCHAR(20)' },
      { name: 'message', type: 'VARCHAR(500)' },
      { name: 'latitude', type: 'DOUBLE', nullable: true },
      { name: 'longitude', type: 'DOUBLE', nullable: true },
      { name: 'is_acknowledged', type: 'BOOLEAN', defaultValue: 'false' },
      { name: 'created_at', type: 'TIMESTAMP' },
    ],
  },
  {
    id: 'driving_events',
    name: 'driving_events',
    displayName: 'Driving Events',
    module: 'telematics',
    description: 'Driver behavior events',
    columns: [
      { name: 'id', type: 'BIGINT', primaryKey: true },
      { name: 'vehicle_id', type: 'BIGINT', foreignKey: 'vehicles.id' },
      { name: 'driver_id', type: 'BIGINT', nullable: true, foreignKey: 'drivers.id' },
      { name: 'trip_id', type: 'BIGINT', nullable: true, foreignKey: 'trips.id' },
      { name: 'event_type', type: 'VARCHAR(50)', description: 'HARSH_BRAKING/SPEEDING/IDLING' },
      { name: 'severity', type: 'VARCHAR(20)' },
      { name: 'latitude', type: 'DOUBLE' },
      { name: 'longitude', type: 'DOUBLE' },
      { name: 'speed', type: 'DOUBLE', nullable: true },
      { name: 'occurred_at', type: 'TIMESTAMP' },
    ],
  },
];

// Group tables by module
export const tablesByModule = databaseTables.reduce((acc, table) => {
  if (!acc[table.module]) {
    acc[table.module] = [];
  }
  acc[table.module].push(table);
  return acc;
}, {} as Record<string, TableDef[]>);

// Module colors for visualization
export const moduleColors: Record<string, string> = {
  auth: '#E91E63',        // Pink
  fleet: '#4CAF50',       // Green
  driver: '#2196F3',      // Blue
  charging: '#FF9800',    // Orange
  maintenance: '#9C27B0', // Purple
  billing: '#00BCD4',     // Cyan
  customer: '#FF5722',    // Deep Orange
  routing: '#3F51B5',     // Indigo
  geofencing: '#795548',  // Brown
  notification: '#607D8B',// Blue Grey
  document: '#FFC107',    // Amber
  analytics: '#009688',   // Teal
  telematics: '#673AB7',  // Deep Purple
};

// Database statistics
export const dbStats = {
  totalTables: databaseTables.length,
  totalEnums: databaseEnums.length,
  modules: Object.keys(tablesByModule).length,
  tablesByModule: Object.entries(tablesByModule).map(([module, tables]) => ({
    module,
    count: tables.length,
  })),
};
