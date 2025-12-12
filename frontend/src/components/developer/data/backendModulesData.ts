// ============================================================================
// COMPREHENSIVE BACKEND MODULES DATA
// Based on actual Spring Modulith structure from backend/evfleet-monolith
// ============================================================================

export interface ServiceDef {
  name: string;
  description: string;
  interface?: string; // For services with interfaces
}

export interface RepositoryDef {
  name: string;
  entity: string;
  extends: string;
}

export interface DTODef {
  name: string;
  type: 'request' | 'response' | 'model';
  description?: string;
}

export interface ModuleDef {
  id: string;
  name: string;
  displayName: string;
  packagePath: string;
  description: string;
  features: string[];
  entities: string[];
  services: ServiceDef[];
  repositories: RepositoryDef[];
  dtos: DTODef[];
  controllers: string[];
  dependencies: string[]; // Other modules this depends on
  eventPublisher?: boolean; // Publishes events to other modules
  eventListener?: boolean; // Listens to events from other modules
}

// ============================================================================
// TECH STACK
// ============================================================================
export const techStack = {
  framework: {
    name: 'Spring Boot',
    version: '3.2.0',
    description: 'Production-grade Spring-based Applications',
  },
  architecture: {
    name: 'Spring Modulith',
    version: '1.1.0',
    description: 'Modular monolith architecture pattern - modules communicate via events',
  },
  database: {
    name: 'PostgreSQL',
    version: '15+',
    description: 'Advanced open-source relational database',
  },
  orm: {
    name: 'Spring Data JPA / Hibernate',
    version: '6.x',
    description: 'ORM for database access',
  },
  auth: {
    name: 'Firebase Auth',
    description: 'Authentication via Firebase tokens',
  },
  api: {
    name: 'Spring Web MVC',
    description: 'RESTful API with OpenAPI/Swagger documentation',
  },
  validation: {
    name: 'Jakarta Validation (Bean Validation 3.0)',
    description: 'Request validation',
  },
  messaging: {
    status: 'NOT USED (monolith)',
    note: 'External message broker removed; no AMQP integration in codebase.',
  },
  testing: {
    unit: 'JUnit 5',
    integration: 'Spring Boot Test',
    documentation: 'SpringDoc OpenAPI 3',
  },
};

// ============================================================================
// ALL BACKEND MODULES
// ============================================================================
export const backendModules: ModuleDef[] = [
  // ========================
  // AUTH MODULE
  // ========================
  {
    id: 'auth',
    name: 'auth',
    displayName: 'Authentication & Authorization',
    packagePath: 'com.evfleet.auth',
    description: 'User management, Firebase authentication, and RBAC authorization',
    features: [
      'Firebase Auth integration',
      'JWT token validation',
      'Role-based access control (RBAC)',
      'User CRUD operations',
      'Multi-tenant company support',
      'Soft delete for users',
    ],
    entities: ['User', 'Role'],
    services: [
      { name: 'UserService', description: 'User registration, login, CRUD operations' },
      { name: 'FirebaseService', description: 'Firebase token verification' },
    ],
    repositories: [
      { name: 'UserRepository', entity: 'User', extends: 'JpaRepository' },
      { name: 'RoleRepository', entity: 'Role', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'RegisterRequest', type: 'request' },
      { name: 'LoginRequest', type: 'request' },
      { name: 'AuthResponse', type: 'response' },
      { name: 'UserResponse', type: 'response' },
    ],
    controllers: ['AuthController'],
    dependencies: [],
    eventPublisher: true,
  },

  // ========================
  // FLEET MODULE
  // ========================
  {
    id: 'fleet',
    name: 'fleet',
    displayName: 'Fleet Management',
    packagePath: 'com.evfleet.fleet',
    description: 'Multi-fuel vehicle management (EV, ICE, Hybrid), trips, and GPS tracking',
    features: [
      'Multi-fuel vehicle support (EV/ICE/Hybrid)',
      'Vehicle registration and management',
      'Real-time GPS location tracking',
      'Trip management (start, complete, track)',
      'Battery health monitoring (EV)',
      'Fuel level tracking (ICE)',
      'Vehicle status management',
      'Company-level vehicle isolation',
    ],
    entities: ['Vehicle', 'Trip', 'TripLocationHistory', 'BatteryHealth'],
    services: [
      { name: 'VehicleService', description: 'Vehicle CRUD, location updates' },
      { name: 'TripService', description: 'Trip lifecycle management' },
      { name: 'BatteryHealthService', description: 'EV battery health tracking' },
    ],
    repositories: [
      { name: 'VehicleRepository', entity: 'Vehicle', extends: 'JpaRepository' },
      { name: 'TripRepository', entity: 'Trip', extends: 'JpaRepository' },
      { name: 'TripLocationHistoryRepository', entity: 'TripLocationHistory', extends: 'JpaRepository' },
      { name: 'BatteryHealthRepository', entity: 'BatteryHealth', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'VehicleRequest', type: 'request' },
      { name: 'VehicleResponse', type: 'response' },
      { name: 'TripResponse', type: 'response' },
      { name: 'BatteryHealthResponse', type: 'response' },
    ],
    controllers: ['VehicleController', 'TripController', 'BatteryHealthController'],
    dependencies: ['auth', 'driver'],
    eventPublisher: true,
    eventListener: true,
  },

  // ========================
  // DRIVER MODULE
  // ========================
  {
    id: 'driver',
    name: 'driver',
    displayName: 'Driver Management',
    packagePath: 'com.evfleet.driver',
    description: 'Driver profiles, vehicle assignments, and performance tracking',
    features: [
      'Driver profile management',
      'License expiry tracking',
      'Vehicle assignment/unassignment',
      'Driver availability status',
      'Safety score tracking',
      'Performance metrics (trips, distance)',
      'Driver leaderboard',
      'Fuel efficiency tracking',
    ],
    entities: ['Driver'],
    services: [
      { name: 'DriverService', description: 'Driver CRUD, assignments, performance' },
    ],
    repositories: [
      { name: 'DriverRepository', entity: 'Driver', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'DriverRequest', type: 'request' },
      { name: 'DriverResponse', type: 'response' },
    ],
    controllers: ['DriverController'],
    dependencies: ['auth', 'fleet'],
    eventListener: true,
  },

  // ========================
  // CHARGING MODULE
  // ========================
  {
    id: 'charging',
    name: 'charging',
    displayName: 'Charging Management',
    packagePath: 'com.evfleet.charging',
    description: 'EV charging infrastructure, stations, and session management',
    features: [
      'Charging station registry',
      'Station availability tracking',
      'Nearby station search (geo-query)',
      'Charging session lifecycle',
      'Energy consumption tracking',
      'Cost calculation',
      'SOC (State of Charge) tracking',
      'Charger type support (CCS, CHAdeMO, Type2)',
    ],
    entities: ['ChargingStation', 'ChargingSession'],
    services: [
      { name: 'ChargingStationService', description: 'Station management, geo-queries' },
      { name: 'ChargingSessionService', description: 'Session lifecycle, cost calculation' },
    ],
    repositories: [
      { name: 'ChargingStationRepository', entity: 'ChargingStation', extends: 'JpaRepository' },
      { name: 'ChargingSessionRepository', entity: 'ChargingSession', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'ChargingStationResponse', type: 'response' },
      { name: 'ChargingSessionResponse', type: 'response' },
    ],
    controllers: ['ChargingStationController', 'ChargingSessionController'],
    dependencies: ['fleet'],
    eventPublisher: true,
  },

  // ========================
  // MAINTENANCE MODULE
  // ========================
  {
    id: 'maintenance',
    name: 'maintenance',
    displayName: 'Maintenance Management',
    packagePath: 'com.evfleet.maintenance',
    description: 'Vehicle maintenance scheduling, tracking, and policy management',
    features: [
      'Maintenance record CRUD',
      'Fuel-type specific maintenance types',
      'Scheduled maintenance alerts',
      'Maintenance policy engine',
      'Odometer-based triggers',
      'Time-based triggers',
      'Cost tracking with line items',
      'Service provider tracking',
    ],
    entities: ['MaintenanceRecord', 'MaintenancePolicy', 'MaintenanceLineItem'],
    services: [
      { name: 'MaintenanceService', description: 'Maintenance records, alerts' },
      { name: 'MaintenancePolicyService', description: 'Policy management, scheduling' },
    ],
    repositories: [
      { name: 'MaintenanceRecordRepository', entity: 'MaintenanceRecord', extends: 'JpaRepository' },
      { name: 'MaintenancePolicyRepository', entity: 'MaintenancePolicy', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'MaintenanceRecordRequest', type: 'request' },
      { name: 'MaintenanceRecordResponse', type: 'response' },
      { name: 'MaintenanceAlertResponse', type: 'response' },
    ],
    controllers: ['MaintenanceController'],
    dependencies: ['fleet'],
    eventListener: true,
  },

  // ========================
  // BILLING MODULE
  // ========================
  {
    id: 'billing',
    name: 'billing',
    displayName: 'Billing & Subscriptions',
    packagePath: 'com.evfleet.billing',
    description: 'Subscription management, invoicing, and payment processing',
    features: [
      'Subscription tiers (Basic, Pro, Enterprise)',
      'Monthly/Annual billing cycles',
      'Invoice generation',
      'Payment processing',
      'Payment history',
      'Auto-renewal',
      'Webhook support (Razorpay/Stripe)',
      'GST compliance (India)',
    ],
    entities: ['Invoice', 'Payment', 'Subscription', 'PricingPlan', 'BillingAddress', 'PaymentOrder', 'WebhookEvent'],
    services: [
      { name: 'BillingService', description: 'Subscription, invoicing, payments' },
      { name: 'PaymentWebhookService', description: 'Payment gateway webhooks' },
    ],
    repositories: [
      { name: 'InvoiceRepository', entity: 'Invoice', extends: 'JpaRepository' },
      { name: 'PaymentRepository', entity: 'Payment', extends: 'JpaRepository' },
      { name: 'SubscriptionRepository', entity: 'Subscription', extends: 'JpaRepository' },
      { name: 'PricingPlanRepository', entity: 'PricingPlan', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'SubscriptionRequest', type: 'request' },
      { name: 'SubscriptionResponse', type: 'response' },
      { name: 'InvoiceResponse', type: 'response' },
      { name: 'PaymentRequest', type: 'request' },
      { name: 'PaymentResponse', type: 'response' },
    ],
    controllers: ['BillingController', 'PaymentWebhookController'],
    dependencies: ['auth'],
    eventPublisher: true,
  },

  // ========================
  // CUSTOMER MODULE
  // ========================
  {
    id: 'customer',
    name: 'customer',
    displayName: 'Customer Management',
    packagePath: 'com.evfleet.customer',
    description: 'Customer profiles, feedback, and business client management',
    features: [
      'Customer CRUD (Individual/Business)',
      'GST/PAN number support (India)',
      'Customer feedback collection',
      'Rating system (1-5 stars)',
      'Feedback management',
    ],
    entities: ['Customer', 'CustomerFeedback'],
    services: [
      { name: 'CustomerService', description: 'Customer CRUD, feedback' },
    ],
    repositories: [
      { name: 'CustomerRepository', entity: 'Customer', extends: 'JpaRepository' },
      { name: 'CustomerFeedbackRepository', entity: 'CustomerFeedback', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'CustomerRequest', type: 'request' },
      { name: 'CustomerResponse', type: 'response' },
      { name: 'FeedbackRequest', type: 'request' },
      { name: 'FeedbackResponse', type: 'response' },
    ],
    controllers: ['CustomerController'],
    dependencies: ['auth'],
  },

  // ========================
  // ROUTING MODULE
  // ========================
  {
    id: 'routing',
    name: 'routing',
    displayName: 'Route Planning',
    packagePath: 'com.evfleet.routing',
    description: 'Route optimization, waypoint management, and execution tracking',
    features: [
      'Route plan creation',
      'Multi-waypoint support',
      'Optimization criteria (distance, time, cost, energy)',
      'Route execution tracking',
      'ETA calculation',
      'Distance/duration estimation',
    ],
    entities: ['RoutePlan', 'RouteWaypoint'],
    services: [
      { name: 'RouteService', description: 'Route CRUD, optimization, waypoints' },
    ],
    repositories: [
      { name: 'RoutePlanRepository', entity: 'RoutePlan', extends: 'JpaRepository' },
      { name: 'RouteWaypointRepository', entity: 'RouteWaypoint', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'RoutePlanRequest', type: 'request' },
      { name: 'RoutePlanResponse', type: 'response' },
      { name: 'WaypointRequest', type: 'request' },
      { name: 'WaypointResponse', type: 'response' },
    ],
    controllers: ['RouteController'],
    dependencies: ['fleet', 'driver'],
  },

  // ========================
  // GEOFENCING MODULE
  // ========================
  {
    id: 'geofencing',
    name: 'geofencing',
    displayName: 'Geofencing',
    packagePath: 'com.evfleet.geofencing',
    description: 'Geographic boundaries, zone management, and spatial alerts',
    features: [
      'Circular geofence creation',
      'Multiple zone types (depot, no-go, charging, service)',
      'Entry/exit alerts',
      'Speed limit enforcement',
      'Active/inactive zones',
    ],
    entities: ['Geofence'],
    services: [
      { name: 'GeofenceService', description: 'Geofence CRUD, zone checks' },
    ],
    repositories: [
      { name: 'GeofenceRepository', entity: 'Geofence', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'GeofenceRequest', type: 'request' },
      { name: 'GeofenceResponse', type: 'response' },
    ],
    controllers: ['GeofenceController'],
    dependencies: ['fleet'],
    eventPublisher: true,
  },

  // ========================
  // TELEMATICS MODULE
  // ========================
  {
    id: 'telematics',
    name: 'telematics',
    displayName: 'Telematics & Behavior',
    packagePath: 'com.evfleet.telematics',
    description: 'Vehicle telemetry, driver behavior monitoring, and real-time alerts',
    features: [
      'Telemetry data ingestion',
      'Driver behavior events (harsh braking, speeding, idling)',
      'Real-time alerts',
      'Event severity levels',
      'Driver statistics',
      'Multi-provider support',
      'Provider health monitoring',
    ],
    entities: ['TelemetrySnapshot', 'TelemetryAlert', 'DrivingEvent'],
    services: [
      { name: 'TelematicsService', description: 'Event ingestion, statistics' },
      { name: 'TelemetrySyncScheduler', description: 'Scheduled sync with providers' },
    ],
    repositories: [
      { name: 'TelemetrySnapshotRepository', entity: 'TelemetrySnapshot', extends: 'JpaRepository' },
      { name: 'DrivingEventRepository', entity: 'DrivingEvent', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'TelematicsEventRequest', type: 'request' },
      { name: 'DrivingEventResponse', type: 'response' },
    ],
    controllers: ['TelematicsController', 'TelemetryAlertController', 'DrivingEventController'],
    dependencies: ['fleet', 'driver'],
    eventPublisher: true,
  },

  // ========================
  // ANALYTICS MODULE
  // ========================
  {
    id: 'analytics',
    name: 'analytics',
    displayName: 'Analytics & Reporting',
    packagePath: 'com.evfleet.analytics',
    description: 'Fleet analytics, TCO analysis, energy consumption, and ESG reporting',
    features: [
      'Fleet summary dashboard',
      'Vehicle utilization reports',
      'Cost analytics',
      'Total Cost of Ownership (TCO) analysis',
      'Energy consumption tracking',
      'ESG (Environmental, Social, Governance) reporting',
      'Carbon footprint calculation',
      'Monthly/daily reports',
      'Report generation (PDF, Excel)',
    ],
    entities: ['FleetSummary', 'TCOAnalysis', 'ESGReport', 'EnergyConsumptionAnalytics', 'HistoricalMetric'],
    services: [
      { name: 'AnalyticsService', description: 'Fleet analytics, summaries' },
      { name: 'TCOAnalysisService', description: 'Total Cost of Ownership calculations' },
      { name: 'EnergyAnalyticsService', description: 'Energy consumption analysis' },
      { name: 'ReportGenerationService', description: 'PDF/Excel report generation' },
      { name: 'ESGReportService', description: 'Environmental reporting' },
    ],
    repositories: [
      { name: 'FleetSummaryRepository', entity: 'FleetSummary', extends: 'JpaRepository' },
      { name: 'TCOAnalysisRepository', entity: 'TCOAnalysis', extends: 'JpaRepository' },
      { name: 'EnergyConsumptionRepository', entity: 'EnergyConsumptionAnalytics', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'FleetSummaryResponse', type: 'response' },
      { name: 'FleetAnalyticsResponse', type: 'response' },
      { name: 'VehicleUtilizationResponse', type: 'response' },
      { name: 'CostAnalyticsResponse', type: 'response' },
      { name: 'TCOAnalysisResponse', type: 'response' },
      { name: 'EnergyConsumptionResponse', type: 'response' },
      { name: 'ESGReportResponse', type: 'response' },
    ],
    controllers: ['AnalyticsController', 'DashboardMetricsController', 'ESGController'],
    dependencies: ['fleet', 'charging', 'maintenance'],
    eventListener: true,
  },

  // ========================
  // NOTIFICATION MODULE
  // ========================
  {
    id: 'notification',
    name: 'notification',
    displayName: 'Notifications',
    packagePath: 'com.evfleet.notification',
    description: 'In-app notifications, alerts, and user notification management',
    features: [
      'In-app notifications',
      'Priority levels (info, warning, alert, critical)',
      'Read/unread tracking',
      'Mark all as read',
      'Entity-linked notifications',
      'Bulk delete',
    ],
    entities: ['Notification'],
    services: [
      { name: 'NotificationService', description: 'Notification CRUD, read status' },
    ],
    repositories: [
      { name: 'NotificationRepository', entity: 'Notification', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'NotificationResponse', type: 'response' },
    ],
    controllers: ['NotificationController'],
    dependencies: ['auth'],
    eventListener: true,
  },

  // ========================
  // DOCUMENT MODULE
  // ========================
  {
    id: 'document',
    name: 'document',
    displayName: 'Document Management',
    packagePath: 'com.evfleet.document',
    description: 'Vehicle and driver document storage, verification, and expiry tracking',
    features: [
      'Document upload (multipart)',
      'Document types (RC, Insurance, Permit, License)',
      'Entity linking (Vehicle/Driver)',
      'Expiry tracking',
      'Verification workflow',
      'Document download',
    ],
    entities: ['Document'],
    services: [
      { name: 'DocumentService', description: 'Document upload, download, verification' },
    ],
    repositories: [
      { name: 'DocumentRepository', entity: 'Document', extends: 'JpaRepository' },
    ],
    dtos: [
      { name: 'DocumentRequest', type: 'request' },
      { name: 'DocumentResponse', type: 'response' },
    ],
    controllers: ['DocumentController'],
    dependencies: ['fleet', 'driver'],
  },

  // ========================
  // COMMON MODULE
  // ========================
  {
    id: 'common',
    name: 'common',
    displayName: 'Common/Shared',
    packagePath: 'com.evfleet.common',
    description: 'Shared utilities, DTOs, exceptions, and cross-cutting concerns',
    features: [
      'ApiResponse wrapper',
      'Global exception handling',
      'Base entity (auditing)',
      'Common DTOs',
      'Validation utilities',
    ],
    entities: ['BaseEntity'],
    services: [],
    repositories: [],
    dtos: [
      { name: 'ApiResponse', type: 'response', description: 'Standard API response wrapper' },
      { name: 'PageResponse', type: 'response', description: 'Paginated response' },
    ],
    controllers: ['GlobalExceptionHandler'],
    dependencies: [],
  },
];

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

// Module statistics
export const moduleStats = {
  totalModules: backendModules.length,
  totalEntities: backendModules.reduce((sum, m) => sum + m.entities.length, 0),
  totalServices: backendModules.reduce((sum, m) => sum + m.services.length, 0),
  totalControllers: backendModules.reduce((sum, m) => sum + m.controllers.length, 0),
  totalDTOs: backendModules.reduce((sum, m) => sum + m.dtos.length, 0),
  eventPublishers: backendModules.filter(m => m.eventPublisher).length,
  eventListeners: backendModules.filter(m => m.eventListener).length,
};

// Module dependencies graph
export const moduleDependencies = backendModules.map(module => ({
  id: module.id,
  name: module.displayName,
  dependencies: module.dependencies,
  dependents: backendModules
    .filter(m => m.dependencies.includes(module.id))
    .map(m => m.id),
}));

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
  common: '#9E9E9E',
};
