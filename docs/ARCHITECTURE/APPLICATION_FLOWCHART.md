# Commercial EV Fleet Management Platform - Complete Application Flow Chart

## Table of Contents
1. [High-Level Architecture Overview](#high-level-architecture-overview)
2. [System Architecture Diagram](#system-architecture-diagram)
3. [User Authentication Flow](#user-authentication-flow)
4. [Frontend Application Flow](#frontend-application-flow)
5. [Backend Microservices Architecture](#backend-microservices-architecture)
6. [Data Flow Diagrams](#data-flow-diagrams)
7. [Event-Driven Communication](#event-driven-communication)
8. [Database Architecture](#database-architecture)
9. [API Request Flow](#api-request-flow)
10. [Component Interaction Maps](#component-interaction-maps)

---

## High-Level Architecture Overview

```mermaid
flowchart TB
    subgraph "Client Layer"
        WEB[ðŸŒ Web Browser]
        MOBILE[ðŸ“± Mobile App]
    end

    subgraph "Frontend - React Application :3000"
        REACT[React 18+<br/>Material-UI]
        REDUX[Redux Toolkit<br/>State Management]
        FIREBASE_SDK[Firebase SDK<br/>Authentication]
    end

    subgraph "API Layer"
        GATEWAY[ðŸšª API Gateway<br/>:8080]
        EUREKA[ðŸ“¡ Eureka Server<br/>Service Discovery<br/>:8761]
    end

    subgraph "Business Services Layer"
        AUTH[ðŸ” Auth Service<br/>:8081]
        FLEET[ðŸš— Fleet Service<br/>:8082]
        CHARGING[âš¡ Charging Service<br/>:8083]
        MAINT[ðŸ”§ Maintenance Service<br/>:8084]
        DRIVER[ðŸ‘¤ Driver Service<br/>:8085]
        ANALYTICS[ðŸ“Š Analytics Service<br/>:8086]
        NOTIFY[ðŸ”” Notification Service<br/>:8087]
        BILLING[ðŸ’° Billing Service<br/>:8088]
    end

    subgraph "Data Layer"
        POSTGRES[(PostgreSQL<br/>:5432)]
        REDIS[(Redis Cache<br/>:6379)]
        
    end

    subgraph "External Services"
        FIREBASE_CLOUD[â˜ï¸ Firebase Auth]
        MAPS[ðŸ—ºï¸ Maps API]
    end

    WEB --> REACT
    MOBILE --> REACT
    REACT --> REDUX
    REACT --> FIREBASE_SDK
    FIREBASE_SDK --> FIREBASE_CLOUD
    
    REACT --> GATEWAY
    GATEWAY --> EUREKA
    
    GATEWAY --> AUTH
    GATEWAY --> FLEET
    GATEWAY --> CHARGING
    GATEWAY --> MAINT
    GATEWAY --> DRIVER
    GATEWAY --> ANALYTICS
    GATEWAY --> NOTIFY
    GATEWAY --> BILLING
    
    AUTH --> POSTGRES
    FLEET --> POSTGRES
    CHARGING --> POSTGRES
    MAINT --> POSTGRES
    DRIVER --> POSTGRES
    ANALYTICS --> POSTGRES
    NOTIFY --> POSTGRES
    BILLING --> POSTGRES
    
    AUTH --> REDIS
    FLEET --> REDIS
    GATEWAY --> REDIS
    






    
    REACT --> MAPS
```

---

## System Architecture Diagram

```mermaid
flowchart LR
    subgraph "Infrastructure Services"
        direction TB
        ES[Eureka Server<br/>Service Registry<br/>:8761]
        GW[API Gateway<br/>Load Balancing<br/>Rate Limiting<br/>:8080]
    end

    subgraph "Core Business Modules"
        direction TB
        AS[Auth Service<br/>â€¢ User Management<br/>â€¢ Firebase Integration<br/>â€¢ Role-Based Access<br/>:8081]
        FS[Fleet Service<br/>â€¢ Vehicle Management<br/>â€¢ Fleet Operations<br/>â€¢ Events & State<br/>:8082]
        CS[Charging Service<br/>â€¢ Station Management<br/>â€¢ Session Tracking<br/>â€¢ Network Integration<br/>:8083]
    end

    subgraph "Supporting Modules"
        direction TB
        MS[Maintenance Service<br/>â€¢ Scheduling<br/>â€¢ Predictive Analytics<br/>â€¢ Work Orders<br/>:8084]
        DS[Driver Service<br/>â€¢ Driver Management<br/>â€¢ Performance<br/>â€¢ Assignments<br/>:8085]
        ANS[Analytics Service<br/>â€¢ Reporting<br/>â€¢ Business Intelligence<br/>â€¢ TCO Analysis<br/>:8086]
    end

    subgraph "Communication Modules"
        direction TB
        NS[Notification Service<br/>â€¢ Alerts<br/>â€¢ Email/SMS/Push<br/>â€¢ Templates<br/>:8087]
        BS[Billing Service<br/>â€¢ Invoicing<br/>â€¢ Cost Tracking<br/>â€¢ Payments<br/>:8088]
    end

    ES <--> GW
    GW --> AS
    GW --> FS
    GW --> CS
    GW --> MS
    GW --> DS
    GW --> ANS
    GW --> NS
    GW --> BS
```

---

## User Authentication Flow

```mermaid
sequenceDiagram
    participant U as User Browser
    participant FE as React Frontend
    participant FB as Firebase Auth
    participant GW as API Gateway
    participant AS as Auth Service
    participant DB as PostgreSQL

    Note over U,DB: Registration Flow
    U->>FE: 1. Click Register
    FE->>FE: 2. Display Registration Form
    U->>FE: 3. Enter Email/Password
    FE->>FB: 4. createUserWithEmailAndPassword()
    FB-->>FE: 5. Return Firebase User + ID Token
    FE->>GW: 6. POST /api/auth/register (with token)
    GW->>AS: 7. Forward Request
    AS->>AS: 8. Verify Firebase Token
    AS->>DB: 9. Create User Record
    DB-->>AS: 10. User Created
    AS-->>GW: 11. Return User Profile
    GW-->>FE: 12. Registration Success
    FE->>FE: 13. Store in Redux & Redirect

    Note over U,DB: Login Flow
    U->>FE: 1. Click Login
    FE->>FE: 2. Display Login Form
    U->>FE: 3. Enter Credentials
    FE->>FB: 4. signInWithEmailAndPassword()
    FB-->>FE: 5. Return Firebase User + ID Token
    FE->>GW: 6. POST /api/auth/login (with token)
    GW->>AS: 7. Forward Request
    AS->>AS: 8. Verify Firebase Token
    AS->>DB: 9. Fetch User & Company
    DB-->>AS: 10. Return User Data
    AS-->>GW: 11. Return User + Roles
    GW-->>FE: 12. Login Success
    FE->>FE: 13. Store in Redux & Navigate to Dashboard

    Note over U,DB: Token Refresh Flow
    FE->>FB: Automatic Token Refresh
    FB-->>FE: New ID Token
    FE->>FE: Update Auth Headers
```

---

## Frontend Application Flow

```mermaid
flowchart TB
    subgraph "Entry Point"
        INDEX[index.tsx]
        APP[App.tsx]
    end

    subgraph "Authentication Layer"
        AUTH_HOOK[useAuth Hook]
        PROTECTED[ProtectedRoute]
        PUBLIC[PublicRoute]
    end

    subgraph "Navigation"
        HEADER[Header Component]
        SIDEBAR[Sidebar Component]
        ROUTES[AppRoutes]
    end

    subgraph "Public Pages"
        LOGIN[LoginPage]
        REGISTER[Register]
        FORGOT[ForgotPassword]
    end

    subgraph "Protected Pages"
        DASH[DashboardPage]
        FLEET_P[FleetManagementPage]
        CHARGING_P[ChargingPage]
        STATIONS[StationDiscoveryPage]
        DRIVERS_P[DriversPage]
        MAINT_P[MaintenancePage]
        ANALYTICS_P[AnalyticsPage]
        BILLING_P[BillingPage]
        PROFILE[ProfilePage]
        SETTINGS[SettingsPage]
        REPORTS[VehicleReportPage]
        DOCS[DocumentManagementPage]
        EXPENSES[ExpenseManagementPage]
        ROUTES_P[RouteOptimizationPage]
        CUSTOMERS[CustomerManagementPage]
        GEO[GeofenceManagementPage]
    end

    subgraph "State Management - Redux"
        STORE[Redux Store]
        AUTH_SLICE[authSlice]
        FLEET_SLICE[vehicleSlice]
        CHARGE_SLICE[chargingSlice]
        OTHER_SLICES[Other Slices...]
    end

    subgraph "API Services"
        API[api.ts - Axios Instance]
        AUTH_SVC[authService]
        VEHICLE_SVC[vehicleService]
        CHARGE_SVC[chargingService]
        DRIVER_SVC[driverService]
        MAINT_SVC[maintenanceService]
        ANALYTICS_SVC[analyticsService]
        BILLING_SVC[billingService]
        NOTIF_SVC[notificationService]
    end

    INDEX --> APP
    APP --> AUTH_HOOK
    AUTH_HOOK --> PROTECTED
    AUTH_HOOK --> PUBLIC
    
    APP --> HEADER
    APP --> SIDEBAR
    APP --> ROUTES
    
    PUBLIC --> LOGIN
    PUBLIC --> REGISTER
    PUBLIC --> FORGOT
    
    PROTECTED --> DASH
    PROTECTED --> FLEET_P
    PROTECTED --> CHARGING_P
    PROTECTED --> STATIONS
    PROTECTED --> DRIVERS_P
    PROTECTED --> MAINT_P
    PROTECTED --> ANALYTICS_P
    PROTECTED --> BILLING_P
    PROTECTED --> PROFILE
    PROTECTED --> SETTINGS
    PROTECTED --> REPORTS
    PROTECTED --> DOCS
    PROTECTED --> EXPENSES
    PROTECTED --> ROUTES_P
    PROTECTED --> CUSTOMERS
    PROTECTED --> GEO

    DASH --> STORE
    FLEET_P --> STORE
    CHARGING_P --> STORE
    
    STORE --> AUTH_SLICE
    STORE --> FLEET_SLICE
    STORE --> CHARGE_SLICE
    STORE --> OTHER_SLICES
    
    AUTH_SLICE --> API
    FLEET_SLICE --> API
    
    API --> AUTH_SVC
    API --> VEHICLE_SVC
    API --> CHARGE_SVC
    API --> DRIVER_SVC
    API --> MAINT_SVC
    API --> ANALYTICS_SVC
    API --> BILLING_SVC
    API --> NOTIF_SVC
```

---

## Backend Microservices Architecture

### Monolith Backend Structure

```mermaid
flowchart TB
    subgraph "evfleet-monolith"
        MAIN[EvFleetApplication.java<br/>Main Entry Point]
        
        subgraph "Common Module"
            CONFIG[config/]
            CONSTANTS[constants/]
            DTO_COMMON[dto/]
            ENTITY[entity/]
            EVENT_COMMON[event/]
            EXCEPTION[exception/]
            INTERCEPTOR[interceptor/]
            UTIL[util/]
        end
        
        subgraph "Auth Module"
            AUTH_CTRL[AuthController]
            AUTH_SVC[AuthService]
            AUTH_REPO[UserRepository<br/>RoleRepository<br/>CompanyRepository]
            AUTH_MODEL[User<br/>Role<br/>Company<br/>Permission]
        end
        
        subgraph "Fleet Module"
            FLEET_CTRL[FleetController<br/>VehicleController]
            FLEET_SVC[FleetService<br/>VehicleService<br/>VehicleEventService<br/>VehicleStateService]
            FLEET_REPO[FleetRepository<br/>VehicleRepository<br/>VehicleEventRepository]
            FLEET_MODEL[Fleet<br/>Vehicle<br/>VehicleEvent<br/>VehicleCurrentState]
        end
        
        subgraph "Charging Module"
            CHARGE_CTRL[ChargingController<br/>StationController]
            CHARGE_SVC[ChargingService<br/>ChargingSessionService]
            CHARGE_REPO[ChargingStationRepository<br/>ChargingSessionRepository]
            CHARGE_MODEL[ChargingStation<br/>ChargingSession<br/>Connector]
        end
        
        subgraph "Maintenance Module"
            MAINT_CTRL[MaintenanceController]
            MAINT_SVC[MaintenanceService<br/>WorkOrderService]
            MAINT_REPO[MaintenanceRepository<br/>WorkOrderRepository]
            MAINT_MODEL[MaintenanceSchedule<br/>WorkOrder<br/>ServiceRecord]
        end
        
        subgraph "Driver Module"
            DRIVER_CTRL[DriverController]
            DRIVER_SVC[DriverService<br/>DriverPerformanceService]
            DRIVER_REPO[DriverRepository<br/>DriverAssignmentRepository]
            DRIVER_MODEL[Driver<br/>DriverAssignment<br/>DriverPerformance]
        end
        
        subgraph "Analytics Module"
            ANALYTICS_CTRL[AnalyticsController]
            ANALYTICS_SVC[AnalyticsService<br/>ReportService]
            ANALYTICS_REPO[AnalyticsRepository]
            ANALYTICS_MODEL[FleetMetrics<br/>UtilizationReport]
        end
        
        subgraph "Notification Module"
            NOTIF_CTRL[NotificationController]
            NOTIF_SVC[NotificationService<br/>AlertService]
            NOTIF_REPO[NotificationRepository<br/>AlertRepository]
            NOTIF_MODEL[Notification<br/>Alert<br/>Template]
        end
        
        subgraph "Billing Module"
            BILL_CTRL[BillingController<br/>InvoiceController]
            BILL_SVC[BillingService<br/>InvoiceService]
            BILL_REPO[InvoiceRepository<br/>PaymentRepository]
            BILL_MODEL[Invoice<br/>Payment<br/>Subscription]
        end
        
        subgraph "Telematics Module"
            TELEM_CTRL[TelematicsController]
            TELEM_SVC[TelematicsService<br/>GPSTrackingService]
            TELEM_REPO[TelematicsRepository]
            TELEM_MODEL[TelematicsData<br/>GPSLocation]
        end
        
        subgraph "Routing Module"
            ROUTE_CTRL[RouteController]
            ROUTE_SVC[RouteService<br/>OptimizationService]
            ROUTE_REPO[RouteRepository]
            ROUTE_MODEL[Route<br/>Waypoint<br/>DeliveryStop]
        end
        
        subgraph "Customer Module"
            CUST_CTRL[CustomerController]
            CUST_SVC[CustomerService<br/>FeedbackService]
            CUST_REPO[CustomerRepository<br/>FeedbackRepository]
            CUST_MODEL[Customer<br/>Feedback]
        end
        
        subgraph "Document Module"
            DOC_CTRL[DocumentController]
            DOC_SVC[DocumentService]
            DOC_REPO[DocumentRepository]
            DOC_MODEL[Document<br/>DocumentType]
        end
        
        subgraph "Geofencing Module"
            GEO_CTRL[GeofenceController]
            GEO_SVC[GeofenceService]
            GEO_REPO[GeofenceRepository]
            GEO_MODEL[Geofence<br/>GeofenceAlert]
        end
    end

    MAIN --> CONFIG
    AUTH_CTRL --> AUTH_SVC --> AUTH_REPO --> AUTH_MODEL
    FLEET_CTRL --> FLEET_SVC --> FLEET_REPO --> FLEET_MODEL
    CHARGE_CTRL --> CHARGE_SVC --> CHARGE_REPO --> CHARGE_MODEL
    MAINT_CTRL --> MAINT_SVC --> MAINT_REPO --> MAINT_MODEL
    DRIVER_CTRL --> DRIVER_SVC --> DRIVER_REPO --> DRIVER_MODEL
    ANALYTICS_CTRL --> ANALYTICS_SVC --> ANALYTICS_REPO --> ANALYTICS_MODEL
    NOTIF_CTRL --> NOTIF_SVC --> NOTIF_REPO --> NOTIF_MODEL
    BILL_CTRL --> BILL_SVC --> BILL_REPO --> BILL_MODEL
    TELEM_CTRL --> TELEM_SVC --> TELEM_REPO --> TELEM_MODEL
    ROUTE_CTRL --> ROUTE_SVC --> ROUTE_REPO --> ROUTE_MODEL
    CUST_CTRL --> CUST_SVC --> CUST_REPO --> CUST_MODEL
    DOC_CTRL --> DOC_SVC --> DOC_REPO --> DOC_MODEL
    GEO_CTRL --> GEO_SVC --> GEO_REPO --> GEO_MODEL
```

---

## Data Flow Diagrams

### Vehicle Management Data Flow

```mermaid
flowchart LR
    subgraph "Frontend"
        UI[Fleet Management UI]
        FORM[Vehicle Form]
        LIST[Vehicle List]
    end
    
    subgraph "API Layer"
        GW[API Gateway :8080]
    end
    
    subgraph "Fleet Service"
        CTRL[VehicleController]
        SVC[VehicleService]
        EVENT_SVC[VehicleEventService]
        STATE_SVC[VehicleStateService]
    end
    
    subgraph "Data Stores"
        DB[(PostgreSQL<br/>evfleet_fleet)]
        CACHE[(Redis Cache)]
        
    end
    
    subgraph "Consumers"
        ANALYTICS[Analytics Service]
        NOTIFY[Notification Service]
        BILLING[Billing Service]
    end

    UI --> FORM
    FORM -->|POST /api/fleet/vehicles| GW
    GW --> CTRL
    CTRL --> SVC
    SVC --> DB
    SVC --> CACHE
    SVC --> EVENT_SVC
    EVENT_SVC -->|VehicleCreatedEvent| MQ
    
    MQ --> ANALYTICS
    MQ --> NOTIFY
    MQ --> BILLING
    
    LIST -->|GET /api/fleet/vehicles| GW
    CACHE -->|Cache Hit| GW
    DB -->|Cache Miss| SVC
```

### Charging Session Data Flow

```mermaid
flowchart TB
    subgraph "Vehicle/Driver"
        VEHICLE[ðŸš— Electric Vehicle]
        DRIVER[ðŸ‘¤ Driver App]
    end
    
    subgraph "Charging Infrastructure"
        STATION[âš¡ Charging Station]
        CONNECTOR[ðŸ”Œ Connector]
    end
    
    subgraph "Backend Processing"
        GW[API Gateway]
        CHARGE_SVC[Charging Service]
        SESSION[Session Manager]
        RATE[Rate Calculator]
    end
    
    subgraph "Events & Notifications"
        
        NOTIFY[Notification Service]
        ANALYTICS[Analytics Service]
    end
    
    subgraph "Data Storage"
        DB[(PostgreSQL)]
        CACHE[(Redis)]
    end

    DRIVER -->|1. Request Charging| GW
    GW -->|2. Find Station| CHARGE_SVC
    CHARGE_SVC -->|3. Check Availability| DB
    CHARGE_SVC -->|4. Reserve Connector| CACHE
    
    VEHICLE -->|5. Plug In| STATION
    STATION -->|6. Start Session| CHARGE_SVC
    SESSION -->|7. Create Session Record| DB
    SESSION -->|8. ChargingStartedEvent| MQ
    
    MQ --> NOTIFY
    NOTIFY -->|9. SMS/Push to Driver| DRIVER
    
    STATION -->|10. Energy Delivery| VEHICLE
    SESSION -->|11. Update Progress| CACHE
    
    VEHICLE -->|12. Full Charge| STATION
    STATION -->|13. End Session| CHARGE_SVC
    SESSION -->|14. Calculate Cost| RATE
    RATE --> DB
    SESSION -->|15. ChargingCompletedEvent| MQ
    
    MQ --> ANALYTICS
    MQ --> NOTIFY
```

---

## Event-Driven Communication

```mermaid
flowchart TB
    subgraph "Event Publishers"
        FLEET_PUB[Fleet Service]
        CHARGE_PUB[Charging Service]
        MAINT_PUB[Maintenance Service]
        TELEM_PUB[Telematics Service]
    end
    
    subgraph "Event Processing (removed)"
        direction TB
        
        subgraph "Exchanges"
            FLEET_EX[fleet.exchange]
            CHARGE_EX[charging.exchange]
            MAINT_EX[maintenance.exchange]
            TELEM_EX[telematics.exchange]
        end
        
        subgraph "Queues"
            ANALYTICS_Q[analytics.queue]
            NOTIFY_Q[notification.queue]
            BILLING_Q[billing.queue]
        end
    end
    
    subgraph "Event Consumers"
        ANALYTICS_CONS[Analytics Service<br/>Listener]
        NOTIFY_CONS[Notification Service<br/>Listener]
        BILLING_CONS[Billing Service<br/>Listener]
    end
    
    FLEET_PUB -->|VehicleCreatedEvent<br/>VehicleUpdatedEvent<br/>TripStartedEvent| FLEET_EX
    CHARGE_PUB -->|ChargingStartedEvent<br/>ChargingCompletedEvent| CHARGE_EX
    MAINT_PUB -->|MaintenanceScheduledEvent<br/>MaintenanceCompletedEvent| MAINT_EX
    TELEM_PUB -->|LocationUpdateEvent<br/>AlertTriggeredEvent| TELEM_EX
    
    FLEET_EX --> ANALYTICS_Q
    FLEET_EX --> NOTIFY_Q
    FLEET_EX --> BILLING_Q
    
    CHARGE_EX --> ANALYTICS_Q
    CHARGE_EX --> NOTIFY_Q
    CHARGE_EX --> BILLING_Q
    
    MAINT_EX --> ANALYTICS_Q
    MAINT_EX --> NOTIFY_Q
    MAINT_EX --> BILLING_Q
    
    TELEM_EX --> ANALYTICS_Q
    TELEM_EX --> NOTIFY_Q
    
    ANALYTICS_Q --> ANALYTICS_CONS
    NOTIFY_Q --> NOTIFY_CONS
    BILLING_Q --> BILLING_CONS
```

### Event Types

```mermaid
classDiagram
    class BaseEvent {
        +UUID eventId
        +String eventType
        +LocalDateTime timestamp
        +String companyId
        +String userId
    }
    
    class VehicleEvent {
        +UUID vehicleId
        +String vehicleNumber
        +VehicleEventType type
    }
    
    class TripEvent {
        +UUID tripId
        +UUID vehicleId
        +UUID driverId
        +TripEventType type
    }
    
    class ChargingEvent {
        +UUID sessionId
        +UUID vehicleId
        +UUID stationId
        +ChargingEventType type
    }
    
    class MaintenanceEvent {
        +UUID workOrderId
        +UUID vehicleId
        +MaintenanceEventType type
    }
    
    class AlertEvent {
        +UUID alertId
        +UUID vehicleId
        +AlertSeverity severity
        +String message
    }
    
    BaseEvent <|-- VehicleEvent
    BaseEvent <|-- TripEvent
    BaseEvent <|-- ChargingEvent
    BaseEvent <|-- MaintenanceEvent
    BaseEvent <|-- AlertEvent
```

---

## Database Architecture

```mermaid
erDiagram
    %% Auth Database
    COMPANY ||--o{ USER : has
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned
    ROLE ||--o{ ROLE_PERMISSION : has
    PERMISSION ||--o{ ROLE_PERMISSION : granted
    
    %% Fleet Database
    COMPANY ||--o{ FLEET : owns
    FLEET ||--o{ VEHICLE : contains
    VEHICLE ||--o{ VEHICLE_EVENT : generates
    VEHICLE ||--|| VEHICLE_CURRENT_STATE : has
    VEHICLE ||--o{ VEHICLE_DOCUMENT : has
    
    %% Driver Database
    COMPANY ||--o{ DRIVER : employs
    DRIVER ||--o{ DRIVER_ASSIGNMENT : has
    VEHICLE ||--o{ DRIVER_ASSIGNMENT : assigned
    DRIVER ||--o{ DRIVER_PERFORMANCE : tracked
    
    %% Charging Database
    COMPANY ||--o{ CHARGING_STATION : manages
    CHARGING_STATION ||--o{ CONNECTOR : has
    VEHICLE ||--o{ CHARGING_SESSION : uses
    CHARGING_STATION ||--o{ CHARGING_SESSION : hosts
    CONNECTOR ||--o{ CHARGING_SESSION : provides
    
    %% Maintenance Database
    VEHICLE ||--o{ MAINTENANCE_SCHEDULE : has
    VEHICLE ||--o{ WORK_ORDER : requires
    WORK_ORDER ||--o{ SERVICE_RECORD : contains
    
    %% Routing Database
    COMPANY ||--o{ ROUTE : plans
    ROUTE ||--o{ WAYPOINT : contains
    ROUTE ||--o{ DELIVERY_STOP : includes
    VEHICLE ||--o{ ROUTE : executes
    
    %% Customer Database
    COMPANY ||--o{ CUSTOMER : serves
    CUSTOMER ||--o{ FEEDBACK : provides
    CUSTOMER ||--o{ DELIVERY_STOP : receives
    
    %% Billing Database
    COMPANY ||--o{ INVOICE : receives
    INVOICE ||--o{ INVOICE_LINE : contains
    INVOICE ||--o{ PAYMENT : paid_by
    
    %% Analytics Database
    COMPANY ||--o{ FLEET_METRICS : tracked
    VEHICLE ||--o{ VEHICLE_METRICS : measured
```

### Database Separation by Service

```mermaid
flowchart TB
    subgraph "PostgreSQL Server :5432"
        AUTH_DB[(evfleet_auth)]
        FLEET_DB[(evfleet_fleet)]
        CHARGING_DB[(evfleet_charging)]
        MAINT_DB[(evfleet_maintenance)]
        DRIVER_DB[(evfleet_driver)]
        ANALYTICS_DB[(evfleet_analytics)]
        NOTIFY_DB[(evfleet_notification)]
        BILLING_DB[(evfleet_billing)]
    end
    
    AUTH[Auth Service] --> AUTH_DB
    FLEET[Fleet Service] --> FLEET_DB
    CHARGING[Charging Service] --> CHARGING_DB
    MAINT[Maintenance Service] --> MAINT_DB
    DRIVER[Driver Service] --> DRIVER_DB
    ANALYTICS[Analytics Service] --> ANALYTICS_DB
    NOTIFY[Notification Service] --> NOTIFY_DB
    BILLING[Billing Service] --> BILLING_DB
```

---

## API Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant EU as Eureka
    participant RE as Redis
    participant SVC as Microservice
    participant DB as PostgreSQL
    

    Note over C,MQ: Typical API Request Flow
    
    C->>GW: 1. HTTP Request with JWT Token
    GW->>GW: 2. Validate JWT Token
    GW->>RE: 3. Check Rate Limit
    RE-->>GW: 4. Rate Limit OK
    GW->>EU: 5. Discover Service Instance
    EU-->>GW: 6. Return Service URL
    GW->>RE: 7. Check Cache
    
    alt Cache Hit
        RE-->>GW: 8a. Return Cached Data
        GW-->>C: 9a. Return Response
    else Cache Miss
        GW->>SVC: 8b. Forward Request
        SVC->>DB: 9b. Query Database
        DB-->>SVC: 10b. Return Data
        SVC-->>GW: 11b. Return Response
        GW->>RE: 12b. Update Cache
        GW-->>C: 13b. Return Response
    end
    
    opt Event Publishing
        SVC->>MQ: Publish Domain Event
    end
```

### API Endpoint Structure

```mermaid
flowchart LR
    subgraph "API Gateway Routes"
        GW["/api/*"]
    end
    
    subgraph "Auth Endpoints"
        AUTH_API["/api/auth/**"]
        AUTH_LOGIN[POST /login]
        AUTH_REG[POST /register]
        AUTH_ME[GET /me]
        AUTH_COMPANY["/company/**"]
    end
    
    subgraph "Fleet Endpoints"
        FLEET_API["/api/fleet/**"]
        FLEET_VEHICLES["/vehicles/**"]
        FLEET_TRIPS["/trips/**"]
        FLEET_EVENTS["/events/**"]
        FLEET_STATE["/state/**"]
    end
    
    subgraph "Charging Endpoints"
        CHARGE_API["/api/charging/**"]
        CHARGE_STATIONS["/stations/**"]
        CHARGE_SESSIONS["/sessions/**"]
        CHARGE_NETWORKS["/networks/**"]
    end
    
    subgraph "Maintenance Endpoints"
        MAINT_API["/api/maintenance/**"]
        MAINT_SCHEDULES["/schedules/**"]
        MAINT_ORDERS["/work-orders/**"]
        MAINT_RECORDS["/records/**"]
    end
    
    subgraph "Driver Endpoints"
        DRIVER_API["/api/drivers/**"]
        DRIVER_ASSIGN["/assignments/**"]
        DRIVER_PERF["/performance/**"]
    end
    
    subgraph "Analytics Endpoints"
        ANALYTICS_API["/api/analytics/**"]
        ANALYTICS_FLEET["/fleet/**"]
        ANALYTICS_COSTS["/costs/**"]
        ANALYTICS_REPORTS["/reports/**"]
    end

    GW --> AUTH_API
    GW --> FLEET_API
    GW --> CHARGE_API
    GW --> MAINT_API
    GW --> DRIVER_API
    GW --> ANALYTICS_API
    
    AUTH_API --> AUTH_LOGIN
    AUTH_API --> AUTH_REG
    AUTH_API --> AUTH_ME
    AUTH_API --> AUTH_COMPANY
    
    FLEET_API --> FLEET_VEHICLES
    FLEET_API --> FLEET_TRIPS
    FLEET_API --> FLEET_EVENTS
    FLEET_API --> FLEET_STATE
    
    CHARGE_API --> CHARGE_STATIONS
    CHARGE_API --> CHARGE_SESSIONS
    CHARGE_API --> CHARGE_NETWORKS
    
    MAINT_API --> MAINT_SCHEDULES
    MAINT_API --> MAINT_ORDERS
    MAINT_API --> MAINT_RECORDS
    
    DRIVER_API --> DRIVER_ASSIGN
    DRIVER_API --> DRIVER_PERF
    
    ANALYTICS_API --> ANALYTICS_FLEET
    ANALYTICS_API --> ANALYTICS_COSTS
    ANALYTICS_API --> ANALYTICS_REPORTS
```

---

## Component Interaction Maps

### Dashboard Data Flow

```mermaid
flowchart TB
    subgraph "Dashboard Page"
        DASH[DashboardPage.tsx]
        CARDS[Summary Cards]
        CHARTS[Charts & Graphs]
        MAP[Fleet Map]
        ALERTS[Alert Panel]
    end
    
    subgraph "Redux State"
        DASH_STATE[Dashboard State]
        VEHICLE_STATE[Vehicle State]
        ALERT_STATE[Alert State]
    end
    
    subgraph "API Calls"
        FLEET_API[Fleet Service API]
        ANALYTICS_API[Analytics Service API]
        NOTIFY_API[Notification Service API]
    end
    
    subgraph "Backend Services"
        FLEET_SVC[Fleet Service]
        ANALYTICS_SVC[Analytics Service]
        NOTIFY_SVC[Notification Service]
    end
    
    DASH --> CARDS
    DASH --> CHARTS
    DASH --> MAP
    DASH --> ALERTS
    
    CARDS --> DASH_STATE
    CHARTS --> DASH_STATE
    MAP --> VEHICLE_STATE
    ALERTS --> ALERT_STATE
    
    DASH_STATE --> FLEET_API
    DASH_STATE --> ANALYTICS_API
    VEHICLE_STATE --> FLEET_API
    ALERT_STATE --> NOTIFY_API
    
    FLEET_API --> FLEET_SVC
    ANALYTICS_API --> ANALYTICS_SVC
    NOTIFY_API --> NOTIFY_SVC
```

### Vehicle Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Registered: Add Vehicle
    Registered --> Active: Activate
    Active --> OnTrip: Start Trip
    OnTrip --> Active: End Trip
    Active --> Charging: Start Charging
    Charging --> Active: End Charging
    Active --> InMaintenance: Schedule Service
    InMaintenance --> Active: Complete Service
    Active --> Inactive: Deactivate
    Inactive --> Active: Reactivate
    Inactive --> [*]: Remove Vehicle
    
    OnTrip --> Alert: Issue Detected
    Alert --> OnTrip: Issue Resolved
    
    Charging --> Alert: Charging Failure
    Alert --> Charging: Retry Charging
```

### Trip Management Flow

```mermaid
flowchart TB
    subgraph "Trip Planning"
        CREATE[Create Trip]
        ASSIGN[Assign Driver]
        ROUTE[Set Route]
        STOPS[Add Stops]
    end
    
    subgraph "Trip Execution"
        START[Start Trip]
        NAVIGATE[Navigation]
        DELIVERY[Deliveries]
        POD[Proof of Delivery]
    end
    
    subgraph "Trip Monitoring"
        TRACK[GPS Tracking]
        ETA[ETA Updates]
        ALERTS[Real-time Alerts]
        STATUS[Status Updates]
    end
    
    subgraph "Trip Completion"
        COMPLETE[Complete Trip]
        METRICS[Capture Metrics]
        INVOICE[Generate Invoice]
        FEEDBACK[Customer Feedback]
    end
    
    CREATE --> ASSIGN --> ROUTE --> STOPS
    STOPS --> START
    START --> NAVIGATE --> DELIVERY --> POD
    
    NAVIGATE --> TRACK
    NAVIGATE --> ETA
    NAVIGATE --> ALERTS
    NAVIGATE --> STATUS
    
    POD --> COMPLETE --> METRICS --> INVOICE --> FEEDBACK
```

---

## Infrastructure Deployment Flow

```mermaid
flowchart TB
    subgraph "Development"
        DEV[Developer Workstation]
        GIT[Git Repository]
    end
    
    subgraph "CI/CD Pipeline"
        CI[GitHub Actions]
        BUILD[Build & Test]
        DOCKER_BUILD[Docker Build]
        PUSH[Push to Registry]
    end
    
    subgraph "Docker Environment"
        subgraph "Infrastructure Containers"
            PG[PostgreSQL]
            REDIS[Redis]
            
        end
        
        subgraph "Service Containers"
            EUREKA[Eureka Server]
            GATEWAY[API Gateway]
            AUTH_C[Auth Service]
            FLEET_C[Fleet Service]
            OTHER_C[Other Services...]
        end
        
        subgraph "Frontend Container"
            REACT_C[React App + Nginx]
        end
    end
    
    subgraph "External Access"
        LB[Load Balancer]
        DNS[DNS]
        CLIENT[Client Browsers]
    end
    
    DEV -->|Push Code| GIT
    GIT -->|Trigger| CI
    CI --> BUILD
    BUILD --> DOCKER_BUILD
    DOCKER_BUILD --> PUSH
    PUSH --> EUREKA
    PUSH --> GATEWAY
    PUSH --> AUTH_C
    PUSH --> FLEET_C
    PUSH --> OTHER_C
    PUSH --> REACT_C
    
    PG --> AUTH_C
    PG --> FLEET_C
    REDIS --> GATEWAY
    
    
    EUREKA --> GATEWAY
    GATEWAY --> AUTH_C
    GATEWAY --> FLEET_C
    
    CLIENT --> DNS --> LB --> REACT_C
    REACT_C --> GATEWAY
```

---

## Security Architecture

```mermaid
flowchart TB
    subgraph "Authentication Layer"
        FIREBASE[Firebase Authentication]
        JWT[JWT Token Validation]
        SESSION[Session Management]
    end
    
    subgraph "Authorization Layer"
        RBAC[Role-Based Access Control]
        PERM[Permission Checking]
        TENANT[Multi-Tenant Isolation]
    end
    
    subgraph "Roles"
        SUPER[SUPER_ADMIN]
        ADMIN[ADMIN]
        MANAGER[FLEET_MANAGER]
        DRIVER_R[DRIVER]
        VIEWER[VIEWER]
    end
    
    subgraph "Protected Resources"
        VEHICLES[Vehicles]
        DRIVERS[Drivers]
        BILLING_R[Billing]
        ANALYTICS_R[Analytics]
        SETTINGS_R[Settings]
    end
    
    FIREBASE --> JWT
    JWT --> SESSION
    SESSION --> RBAC
    RBAC --> PERM
    PERM --> TENANT
    
    SUPER --> VEHICLES
    SUPER --> DRIVERS
    SUPER --> BILLING_R
    SUPER --> ANALYTICS_R
    SUPER --> SETTINGS_R
    
    ADMIN --> VEHICLES
    ADMIN --> DRIVERS
    ADMIN --> BILLING_R
    ADMIN --> ANALYTICS_R
    
    MANAGER --> VEHICLES
    MANAGER --> DRIVERS
    MANAGER --> ANALYTICS_R
    
    DRIVER_R --> VEHICLES
    
    VIEWER --> ANALYTICS_R
```

---

## Summary

This document provides a comprehensive flow chart of the Commercial EV Fleet Management Platform, covering:

1. **High-Level Architecture**: Overall system topology showing frontend, API layer, business services, and data stores
2. **Authentication Flow**: Firebase-based authentication with backend user synchronization
3. **Frontend Structure**: React application with Redux state management and protected routing
4. **Backend Architecture**: Monolith structure with modular domain-driven design
5. **Data Flow**: How data moves through the system for various operations
6. **Event-Driven Communication**: Spring Modulith ApplicationEvents for in-process communication
7. **Database Architecture**: Multi-database setup with service isolation
8. **API Structure**: RESTful API endpoint organization
9. **Component Interactions**: Detailed flows for dashboard, vehicle lifecycle, and trips
10. **Infrastructure**: Docker-based deployment with CI/CD pipeline
11. **Security**: RBAC-based authorization with Firebase authentication

---

## Port Reference

| Service | Port | Description |
|---------|------|-------------|
| React Frontend | 3000 | Web Application |
| API Gateway | 8080 | Single Entry Point |
| Auth Service | 8081 | Authentication & Authorization |
| Fleet Service | 8082 | Vehicle & Fleet Management |
| Charging Service | 8083 | Charging Infrastructure |
| Maintenance Service | 8084 | Predictive Maintenance |
| Driver Service | 8085 | Driver Management |
| Analytics Service | 8086 | Business Intelligence |
| Notification Service | 8087 | Alerts & Notifications |
| Billing Service | 8088 | Cost Tracking & Invoicing |
| Eureka Server | 8761 | Service Discovery |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Cache |



---

*Document Generated: December 2025*
*Platform Version: 2.1.0*
