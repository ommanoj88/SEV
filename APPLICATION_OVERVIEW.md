# EV Fleet Management Platform - Application Overview

**Last Updated:** November 16, 2025
**Architecture:** Modular Monolith
**Version:** 1.0.0

---

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Functional Modules](#functional-modules)
3. [External Integrations](#external-integrations)
4. [Standalone vs Integration-Dependent Features](#standalone-vs-integration-dependent-features)
5. [Current Implementation Status](#current-implementation-status)
6. [Known Limitations & Missing Features](#known-limitations--missing-features)
7. [Database Schema](#database-schema)
8. [API Endpoints Summary](#api-endpoints-summary)
9. [Frontend Pages](#frontend-pages)
10. [Configuration Requirements](#configuration-requirements)

---

## System Architecture

### Overview
The application uses a **Modular Monolith** architecture - a single deployable Spring Boot application organized into distinct domain modules with separate databases for each module.

### Technology Stack

**Backend:**
- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17+
- **Build Tool:** Maven
- **Database:** PostgreSQL 17
- **Authentication:** Firebase Authentication + JWT
- **API Documentation:** Swagger/OpenAPI 3.0
- **Caching:** Simple In-Memory (Redis disabled)
- **Messaging:** Direct method calls (RabbitMQ removed)

**Frontend:**
- **Framework:** React 18 + TypeScript
- **State Management:** Redux Toolkit
- **UI Library:** Material-UI (MUI) v5
- **Routing:** React Router v6
- **HTTP Client:** Axios
- **Build Tool:** Create React App

**Database:**
- **8 Separate PostgreSQL Databases** (module isolation):
  - `evfleet_auth` - User authentication and authorization
  - `evfleet_fleet` - Vehicle and fleet management
  - `evfleet_charging` - Charging infrastructure
  - `evfleet_maintenance` - Maintenance scheduling
  - `evfleet_driver` - Driver management
  - `evfleet_analytics` - Analytics and reporting
  - `evfleet_notification` - Notifications and alerts
  - `evfleet_billing` - Billing and invoicing

---

## Functional Modules

### 1. Authentication & Authorization (`auth`)
**Status:** ✅ Fully Functional

**Features:**
- Firebase Authentication integration
- Email/Password registration and login
- Google OAuth Sign-In
- JWT token management
- Role-based access control (RBAC)
- User profile management
- firstName/lastName support (added Nov 2025)

**Database:** `evfleet_auth`
**Tables:** users, roles, user_roles
**Controller:** AuthController
**Endpoints:** 5

**Roles:**
1. SUPER_ADMIN - System-level access
2. ADMIN - Full administrative access
3. FLEET_MANAGER - Vehicle and fleet management (default)
4. DRIVER - Limited access to assigned vehicles
5. VIEWER - Read-only access

**External Dependencies:**
- ✅ **Firebase Authentication** (REQUIRED)
  - Used for: User authentication, OAuth, token verification
  - Configuration: `firebase-credentials.json`

---

### 2. Fleet Management (`fleet`)
**Status:** ✅ Core Features Functional

**Features:**
- Multi-fuel vehicle support (EV, ICE, Hybrid)
- Vehicle registration and management
- Real-time vehicle tracking
- Trip management
- Fuel consumption tracking
- Document management
- Route planning
- Geofencing
- Customer management

**Database:** `evfleet_fleet`
**Tables:** vehicles, trips, documents, geofences, route_plans, customers, customer_feedback, fuel_consumption
**Controllers:** VehicleController, TripController
**Endpoints:** 15+

**Supported Fuel Types:**
- `ELECTRIC` - Pure electric vehicles
- `PETROL` - Petrol/gasoline vehicles
- `DIESEL` - Diesel vehicles
- `CNG` - Compressed Natural Gas
- `HYBRID` - Hybrid vehicles

**External Dependencies:**
- ⚠️ **Google Maps API** (Optional)
  - Used for: Route optimization, geocoding, distance calculation
  - Impact: Route planning and geofencing work with limited functionality

---

### 3. Charging Management (`charging`)
**Status:** ⚠️ Partially Functional

**Features:**
- Charging station management
- Charging session tracking
- Route optimization for charging
- Station discovery
- Real-time availability
- Cost calculation

**Database:** `evfleet_charging`
**Tables:** charging_stations, charging_sessions, charging_networks, route_optimizations
**Controllers:** ChargingSessionController, ChargingStationController
**Endpoints:** 12+

**Current Implementation:**
- ✅ Basic station and session CRUD
- ✅ Cost calculation
- ⚠️ Route optimization (algorithm implemented, needs external mapping data)
- ❌ Real-time station availability (no external integration)
- ❌ Payment processing integration

**External Dependencies:**
- ❌ **Charging Network APIs** (NOT INTEGRATED)
  - Examples: ChargePoint, EVgo, Electrify America
  - Used for: Real-time availability, pricing, reservation
  - Impact: Works with mock/manual data only

- ⚠️ **Payment Gateway** (Razorpay configured but not active)
  - Configuration present but not connected
  - Impact: Payment tracking is manual

---

### 4. Maintenance Management (`maintenance`)
**Status:** ✅ Core Features Functional

**Features:**
- Maintenance scheduling
- Service history tracking
- Battery health monitoring (EV-specific)
- Preventive maintenance alerts
- Cost analytics
- Multi-fuel maintenance support

**Database:** `evfleet_maintenance`
**Tables:** maintenance_schedules, service_history, battery_health
**Controller:** MaintenanceController
**Endpoints:** 8+

**Maintenance Types:**
- ROUTINE - Regular scheduled maintenance
- EMERGENCY - Unplanned urgent repairs
- INSPECTION - Periodic inspections
- BATTERY_CHECK - EV-specific battery diagnostics
- TIRE_ROTATION - Tire maintenance
- OIL_CHANGE - ICE vehicle oil changes
- BRAKE_SERVICE - Brake system maintenance

**External Dependencies:**
- None (fully standalone)

---

### 5. Driver Management (`driver`)
**Status:** ✅ Functional

**Features:**
- Driver registration and profiles
- Driver-vehicle assignment
- Performance tracking
- Behavior monitoring
- License management

**Database:** `evfleet_driver`
**Tables:** drivers, driver_assignments, driver_behavior
**Controller:** DriverController
**Endpoints:** 6+

**External Dependencies:**
- None (fully standalone)

---

### 6. Analytics & Reporting (`analytics`)
**Status:** ✅ Core Analytics Functional

**Features:**
- Fleet summary dashboards
- Utilization reports
- Cost analytics
- TCO (Total Cost of Ownership) analysis
- Energy consumption tracking
- Vehicle reports (PDF generation)
- Historical data analysis

**Database:** `evfleet_analytics`
**Tables:** fleet_summaries, cost_analytics, utilization_reports, tco_analyses
**Controller:** AnalyticsController
**Endpoints:** 8+

**Report Types:**
- Fleet summary (daily/monthly)
- Vehicle genealogy reports
- Cost breakdowns
- Utilization metrics

**External Dependencies:**
- None (fully standalone)

---

### 7. Notifications & Alerts (`notification`)
**Status:** ⚠️ Basic Functionality Only

**Features:**
- System notifications
- Alert management
- User preferences
- Email notifications (configured but not sending)
- SMS notifications (adapter present but not active)

**Database:** `evfleet_notification`
**Tables:** notifications, alert_rules
**Controller:** NotificationController
**Endpoints:** 10+

**Current Implementation:**
- ✅ In-app notifications
- ✅ Alert creation and storage
- ⚠️ Email sending (configuration present but not tested)
- ❌ SMS sending (adapter not connected)
- ❌ Push notifications

**External Dependencies:**
- ⚠️ **Email Service** (SMTP configured but untested)
  - Configuration: application.yml (mail settings)
  - Impact: Email notifications don't send

- ❌ **SMS Service** (NOT INTEGRATED)
  - Adapter: SMSAdapter.java exists
  - Impact: SMS notifications unavailable

---

### 8. Billing & Invoicing (`billing`)
**Status:** ⚠️ Core Features Present, Payment Integration Missing

**Features:**
- Subscription management
- Invoice generation
- Payment tracking
- Expense management
- Pricing tiers
- Cost calculation

**Database:** `evfleet_billing`
**Tables:** subscriptions, invoices, payments, payment_methods, pricing_plans, expenses
**Controller:** BillingController
**Endpoints:** 12+

**Current Implementation:**
- ✅ Subscription management
- ✅ Invoice generation
- ✅ Expense tracking
- ⚠️ Payment processing (Razorpay adapter present but not connected)
- ❌ Automated billing cycles
- ❌ Payment gateway integration

**External Dependencies:**
- ❌ **Razorpay Payment Gateway** (NOT INTEGRATED)
  - Adapter: RazorpayAdapter.java exists
  - Configuration: API keys not configured
  - Impact: Payments must be tracked manually

---

## External Integrations

### Required (Application Won't Start Without)

#### 1. PostgreSQL Database
**Status:** ✅ REQUIRED
**Configuration:** application.yml (datasource)
**Impact:** Application fails to start without database

#### 2. Firebase Authentication
**Status:** ✅ REQUIRED
**Configuration:** `firebase-credentials.json`
**Setup Required:**
1. Create Firebase project
2. Enable Authentication
3. Enable Email/Password provider
4. Enable Google Sign-In provider
5. Download service account credentials
6. Place in `backend/evfleet-monolith/src/main/resources/firebase-credentials.json`

**Impact:** Authentication module fails without Firebase

---

### Optional (Application Starts But Features Limited)

#### 3. Google Maps API
**Status:** ⚠️ OPTIONAL
**Used By:** Fleet module (route optimization, geofencing)
**Impact:** Route optimization uses basic algorithms without real road data

#### 4. Charging Network APIs
**Status:** ❌ NOT INTEGRATED
**Examples:** ChargePoint, EVgo, Electrify America APIs
**Used By:** Charging module
**Impact:** Station data is mock/manual entry only

#### 5. Razorpay Payment Gateway
**Status:** ❌ NOT CONFIGURED
**Used By:** Billing module
**Impact:** Payment processing is manual

#### 6. Email SMTP Service
**Status:** ⚠️ CONFIGURED BUT UNTESTED
**Used By:** Notification module
**Configuration:** application.yml (spring.mail)
**Impact:** Email notifications may not send

#### 7. SMS Service
**Status:** ❌ NOT INTEGRATED
**Used By:** Notification module
**Impact:** SMS notifications unavailable

---

### Removed Integrations

#### Redis
**Status:** ❌ REMOVED (Nov 2025)
**Reason:** Not needed for monolith - using simple in-memory caching
**Configuration:** Disabled in application.yml

#### RabbitMQ
**Status:** ❌ REMOVED (Nov 2025)
**Reason:** Monolith uses direct method calls instead of message brokers
**Configuration:** Disabled in application.yml

---

## Standalone vs Integration-Dependent Features

### ✅ Fully Functional Standalone Features

These work perfectly without any external integrations (except database and Firebase):

1. **User Authentication & Authorization**
   - Email/password login
   - Google OAuth login
   - User profile management
   - Role-based access control

2. **Vehicle Management**
   - Vehicle registration (EV, ICE, Hybrid)
   - Fleet overview
   - Vehicle status tracking
   - Basic trip logging

3. **Driver Management**
   - Driver registration
   - Driver-vehicle assignments
   - Performance tracking

4. **Maintenance Management**
   - Maintenance scheduling
   - Service history
   - Battery health tracking
   - Cost analytics

5. **Analytics & Reporting**
   - Fleet summaries
   - Cost analysis
   - Utilization reports
   - PDF report generation

6. **Basic Billing**
   - Subscription creation
   - Invoice generation
   - Expense tracking

7. **In-App Notifications**
   - Alert creation
   - Notification display
   - User preferences

---

### ⚠️ Partially Functional (Limited Without Integrations)

These features work but with reduced functionality:

1. **Route Optimization**
   - Basic algorithms work
   - Missing real-time traffic data
   - Missing accurate distance calculation
   - **Needs:** Google Maps API

2. **Charging Station Management**
   - Manual station entry works
   - Session tracking works
   - Missing real-time availability
   - Missing dynamic pricing
   - **Needs:** Charging network APIs

3. **Email Notifications**
   - SMTP configured
   - Not tested/verified
   - **Needs:** Valid SMTP credentials

4. **Geofencing**
   - Basic radius-based geofencing works
   - Missing polygon geofences
   - Missing real-time alerts
   - **Needs:** Google Maps API (for polygon drawing)

---

### ❌ Non-Functional (Requires External Integration)

These features are implemented but won't work without integration:

1. **Payment Processing**
   - Razorpay adapter exists
   - No API keys configured
   - **Needs:** Razorpay account + API keys

2. **SMS Notifications**
   - SMS adapter exists
   - No service connected
   - **Needs:** Twilio/AWS SNS integration

3. **Real-Time Charging Station Availability**
   - No external API integration
   - **Needs:** ChargePoint/EVgo API access

4. **Automated Payment Collection**
   - No payment gateway active
   - **Needs:** Razorpay configuration

---

## Current Implementation Status

### Backend (Spring Boot Monolith)

**Modules:** 8
**Controllers:** 10
**Services:** 25+
**Repositories:** 30+
**Total Endpoints:** ~80

**Code Quality:**
- ✅ Consistent package structure
- ✅ Service layer abstraction
- ✅ DTO pattern for API responses
- ✅ ApiResponse wrapper for all endpoints
- ✅ Exception handling
- ✅ Swagger documentation
- ✅ Lombok for boilerplate reduction
- ⚠️ Limited unit tests
- ❌ No integration tests

---

### Frontend (React + TypeScript)

**Pages:** 24
**Services:** 12
**Redux Slices:** 11
**Components:** 50+

**Implementation Status:**
- ✅ All main pages created
- ✅ Redux state management
- ✅ API integration via services
- ✅ ApiResponse unwrapping (fixed Nov 2025)
- ✅ Defensive null checks (fixed Nov 2025)
- ✅ Material-UI components
- ✅ Responsive design
- ⚠️ Some pages use mock data fallback
- ⚠️ Limited error handling on some pages
- ❌ No comprehensive testing

**Pages:**
1. Dashboard - ✅ Functional
2. Login/Register - ✅ Functional
3. Fleet Management - ✅ Functional
4. Vehicle Details - ✅ Functional
5. Trip Management - ⚠️ Basic functionality
6. Charging Management - ✅ Functional (with mock data)
7. Charging Stations - ✅ Functional (with mock data)
8. Maintenance - ✅ Functional
9. Drivers - ✅ Functional
10. Analytics - ✅ Functional
11. Billing - ⚠️ Basic functionality
12. Notifications - ✅ Functional
13. Profile - ✅ Functional
14. Settings - ⚠️ Basic functionality
15. Reports - ⚠️ Partial implementation

---

## Known Limitations & Missing Features

### Critical Issues ✅ FIXED (Nov 2025)

1. ~~ApiResponse unwrapping issue~~ ✅ FIXED
   - Frontend was receiving `{success, data, message}` wrapper
   - Now properly extracts `data` field

2. ~~Backend endpoint path mismatches~~ ✅ FIXED
   - Changed `/api/v1/fleet/vehicles` → `/api/v1/vehicles`
   - Added missing GET endpoints for collections

3. ~~Frontend undefined errors~~ ✅ FIXED
   - Added defensive `|| []` checks in DashboardPage
   - Added defensive checks in ChargingPage

4. ~~Default roles not seeding~~ ✅ FIXED
   - Role names now match Java constants
   - Seed script integrated into reset_database.py

---

### Current Limitations

#### 1. Authentication & Security
- ⚠️ Firebase credentials in repository (should use secrets manager)
- ⚠️ No password complexity requirements
- ⚠️ No session timeout configuration
- ⚠️ No rate limiting on API endpoints
- ❌ No OAuth providers beyond Google

#### 2. Data & Performance
- ❌ No database indexing strategy documented
- ❌ No query optimization
- ❌ No pagination on list endpoints (returns empty lists)
- ❌ No caching strategy (simple cache only)
- ❌ No connection pooling configuration

#### 3. Monitoring & Observability
- ⚠️ Basic health endpoints only
- ❌ No application metrics (Prometheus/Grafana)
- ❌ No distributed tracing
- ❌ No centralized logging
- ❌ No error tracking (Sentry/Rollbar)

#### 4. Testing
- ❌ No unit tests for controllers
- ❌ No integration tests
- ❌ No end-to-end tests
- ❌ No load testing
- ❌ No security testing

#### 5. Deployment
- ❌ No Docker containerization
- ❌ No CI/CD pipeline
- ❌ No environment-specific configurations
- ❌ No deployment documentation
- ❌ No rollback strategy

#### 6. Documentation
- ⚠️ API documentation via Swagger (generated)
- ❌ No API usage examples
- ❌ No architecture decision records (ADRs)
- ❌ No runbook for operations
- ❌ No disaster recovery plan

---

### Missing Features

#### High Priority

1. **Pagination**
   - All list endpoints return empty lists without companyId
   - Need: Page number, page size, sorting parameters
   - Affects: All collection endpoints

2. **Real-Time Updates**
   - WebSocket configuration exists but not implemented
   - Need: Socket.IO backend implementation
   - Affects: Live vehicle tracking, charging status

3. **File Upload**
   - Document management exists but no file storage
   - Need: S3/local file storage implementation
   - Affects: Vehicle documents, driver licenses

4. **Search & Filtering**
   - No global search
   - Limited filtering on lists
   - Need: Full-text search implementation

5. **Audit Logging**
   - BaseEntity has audit fields
   - No audit trail viewing
   - Need: Audit log table and UI

#### Medium Priority

6. **Batch Operations**
   - No bulk vehicle import
   - No bulk invoice generation
   - Need: CSV import/export

7. **Email Templates**
   - Email infrastructure exists
   - No HTML email templates
   - Need: Template engine integration

8. **Multi-Tenancy**
   - CompanyId exists in entities
   - No company isolation enforcement
   - Need: Row-level security

9. **API Versioning**
   - Currently /v1 only
   - No deprecation strategy
   - Need: Version management plan

10. **Backup & Restore**
    - No automated backups
    - No data export/import tools
    - Need: Backup scripts

#### Low Priority

11. **Mobile App Support**
    - No mobile-specific APIs
    - No push notification support
    - Need: Mobile backend adjustments

12. **Advanced Analytics**
    - Basic reports only
    - No ML/AI predictions
    - Need: Predictive maintenance models

13. **Third-Party Integrations**
    - No fleet telematics integration
    - No ERP integration
    - Need: Integration framework

---

## Database Schema

### Database Count: 8

Each module has its own PostgreSQL database for isolation:

1. **evfleet_auth** - Authentication (users, roles)
2. **evfleet_fleet** - Vehicles, trips, customers
3. **evfleet_charging** - Stations, sessions
4. **evfleet_maintenance** - Schedules, service history
5. **evfleet_driver** - Driver profiles, assignments
6. **evfleet_analytics** - Aggregated analytics
7. **evfleet_notification** - Alerts, notifications
8. **evfleet_billing** - Invoices, payments

### Total Tables: ~30+

See individual module sections above for table details.

---

## API Endpoints Summary

### Authentication (5 endpoints)
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- GET /api/v1/auth/me
- POST /api/v1/auth/sync-firebase-user
- POST /api/v1/auth/logout

### Fleet/Vehicles (8+ endpoints)
- GET /api/v1/vehicles
- POST /api/v1/vehicles
- GET /api/v1/vehicles/{id}
- GET /api/v1/vehicles/company/{companyId}
- PUT /api/v1/vehicles/{id}/location
- ...

### Charging (12+ endpoints)
- GET /api/v1/charging/sessions
- POST /api/v1/charging/sessions/start
- GET /api/v1/charging/sessions/{id}
- GET /api/v1/charging/stations
- POST /api/v1/charging/stations
- ...

### Maintenance (8+ endpoints)
- GET /api/v1/maintenance
- POST /api/v1/maintenance
- GET /api/v1/maintenance/{id}
- ...

### Drivers (6+ endpoints)
- GET /api/v1/drivers
- POST /api/v1/drivers
- GET /api/v1/drivers/{id}
- ...

### Analytics (8+ endpoints)
- GET /api/v1/analytics/fleet
- GET /api/v1/analytics/fleet-summary
- GET /api/v1/analytics/fleet-summary/today
- GET /api/v1/analytics/monthly-report
- ...

### Notifications (10+ endpoints)
- GET /api/v1/notifications
- GET /api/v1/notifications/alerts
- GET /api/v1/notifications/unread
- PUT /api/v1/notifications/{id}/read
- ...

### Billing (12+ endpoints)
- GET /api/v1/billing/subscriptions
- POST /api/v1/billing/invoices
- GET /api/v1/billing/expenses
- ...

**Total: ~80 endpoints**

---

## Frontend Pages

### Public Pages
1. Login Page
2. Register Page

### Protected Pages
3. Dashboard (Main overview)
4. Fleet Management (Vehicle list)
5. Vehicle Details (Individual vehicle view)
6. Trip Management
7. Charging Management (Sessions)
8. Charging Stations (Station discovery)
9. Station Discovery (Map view)
10. Route Optimization
11. Maintenance Scheduling
12. Maintenance Page (Service history)
13. Driver Management
14. Driver Management Page (Assignments)
15. Analytics Page (Reports)
16. Detailed Analytics Dashboard
17. Billing Page (Subscriptions)
18. Invoicing Page
19. Expense Management
20. Notification Center
21. Profile Page
22. Settings Page
23. Document Management
24. Vehicle Report Generator

**Total: 24 pages**

---

## Configuration Requirements

### Minimum Required

1. **PostgreSQL 17**
   - 8 databases
   - Connection: localhost:5432
   - User: postgres
   - Password: (configured)

2. **Firebase Project**
   - Authentication enabled
   - Email/Password provider enabled
   - Google OAuth enabled
   - Service account credentials JSON file

3. **Java 17+**
   - JDK 17 or higher

4. **Node.js 18+**
   - For frontend development

5. **Maven 3.8+**
   - For backend builds

### Optional (For Full Functionality)

6. **Google Maps API Key**
   - For route optimization
   - For geofencing

7. **Razorpay Account**
   - For payment processing
   - API Key + Secret

8. **SMTP Server**
   - For email notifications
   - Host, port, credentials

9. **SMS Service**
   - Twilio/AWS SNS account
   - API credentials

---

## Next Steps & Recommendations

### Immediate (Critical)

1. ✅ Fix API response unwrapping - DONE
2. ✅ Fix backend endpoint paths - DONE
3. ✅ Add defensive null checks - DONE
4. ✅ Fix role seeding - DONE
5. ⚠️ Add pagination to all list endpoints
6. ⚠️ Implement proper error handling frontend-wide
7. ⚠️ Add loading states to all async operations

### Short Term (High Priority)

8. Add unit tests (controllers, services)
9. Add integration tests (API endpoints)
10. Implement file upload for documents
11. Add search and filtering
12. Configure email sending (test SMTP)
13. Add audit logging viewer
14. Implement real-time WebSocket updates

### Medium Term

15. Add Docker containerization
16. Set up CI/CD pipeline
17. Implement payment gateway
18. Add monitoring (Prometheus/Grafana)
19. Implement caching strategy
20. Add API rate limiting

### Long Term

21. Mobile app support
22. Advanced analytics (ML/AI)
23. Third-party integrations (telematics, ERP)
24. Multi-region deployment
25. Disaster recovery plan

---

## Conclusion

The EV Fleet Management Platform is a **functional modular monolith** with solid core features for managing electric and multi-fuel vehicle fleets. The application successfully demonstrates:

✅ **Working Core Features:**
- User authentication with Firebase
- Vehicle management (EV, ICE, Hybrid)
- Fleet tracking and analytics
- Maintenance scheduling
- Driver management
- Basic billing and invoicing
- In-app notifications

⚠️ **Features with Limitations:**
- Charging station management (mock data)
- Route optimization (basic algorithms)
- Email notifications (configured but untested)
- Payment processing (infrastructure present but not connected)

❌ **Missing Critical Features:**
- Pagination on all endpoints
- Comprehensive testing suite
- Real-time updates (WebSocket)
- File upload functionality
- SMS notifications
- Payment gateway integration

**Overall Assessment:** The application is production-ready for **basic fleet management operations** but requires additional work for **advanced features and enterprise-grade deployment**.

---

**For questions or issues, refer to:**
- [README.md](README.md) - General setup
- architectural-archives/ - Architecture documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
