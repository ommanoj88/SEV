# EV Fleet Management Platform - Frontend

A comprehensive React-based frontend application for managing electric vehicle fleets with real-time tracking, analytics, and optimization features.

## Features

### Core Functionality
- **Dashboard**: Real-time fleet overview with key metrics and charts
- **Fleet Management**: Vehicle tracking, management, and real-time location monitoring
- **Charging Management**: Station locator, session management, and route optimization
- **Driver Management**: Driver profiles, performance tracking, and leaderboards
- **Maintenance**: Service scheduling, history tracking, and battery health monitoring
- **Analytics**: TCO analysis, carbon footprint, utilization reports, and trends
- **Billing**: Subscription management, invoices, and payment history

### Technical Features
- **Authentication**: Firebase Authentication with email/password and Google Sign-In
- **Real-time Updates**: WebSocket integration for live vehicle tracking
- **Maps Integration**: Mapbox for vehicle and charging station visualization
- **State Management**: Redux Toolkit with async thunks
- **Form Validation**: React Hook Form with Yup schemas
- **Charts & Visualization**: Recharts for analytics dashboards
- **Responsive Design**: Material-UI with mobile, tablet, and desktop support
- **Type Safety**: Full TypeScript implementation
- **Error Handling**: Error boundaries and comprehensive error management

## Tech Stack

- **React** 18.2.0 - UI framework
- **TypeScript** 4.9.5 - Type safety
- **Material-UI** 5.14.0 - Component library
- **Redux Toolkit** 1.9.5 - State management
- **React Router** 6.14.0 - Routing
- **Firebase** 10.1.0 - Authentication
- **Mapbox GL** 2.15.0 - Maps
- **Recharts** 2.7.0 - Charts
- **Axios** 1.4.0 - HTTP client
- **React Hook Form** 7.45.0 - Form management
- **Yup** 1.2.0 - Validation
- **date-fns** 2.30.0 - Date utilities

## Getting Started

### Prerequisites
- Node.js 18+ and npm
- Firebase project credentials
- Mapbox access token
- Backend microservices running

### Installation

1. **Clone the repository**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Configure environment variables**
```bash
cp .env.example .env
```

Edit `.env` with your credentials:
```env
# API Configuration
REACT_APP_API_URL=http://localhost:8080/api/v1
REACT_APP_WEBSOCKET_URL=ws://localhost:8080/ws

# Firebase Configuration
REACT_APP_FIREBASE_API_KEY=your_api_key
REACT_APP_FIREBASE_AUTH_DOMAIN=your_domain
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
REACT_APP_FIREBASE_STORAGE_BUCKET=your_bucket
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
REACT_APP_FIREBASE_APP_ID=your_app_id

# Mapbox Configuration
REACT_APP_MAPBOX_TOKEN=your_mapbox_token
```

4. **Start development server**
```bash
npm start
```

The app will open at http://localhost:3000

### Build for Production

```bash
npm run build
```

## Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── manifest.json
├── src/
│   ├── components/
│   │   ├── analytics/      # Analytics components
│   │   ├── auth/           # Authentication components
│   │   ├── billing/        # Billing components
│   │   ├── charging/       # Charging management
│   │   ├── common/         # Shared components
│   │   ├── dashboard/      # Dashboard components
│   │   ├── drivers/        # Driver management
│   │   ├── fleet/          # Fleet management
│   │   └── maintenance/    # Maintenance components
│   ├── hooks/              # Custom React hooks
│   ├── pages/              # Page components
│   ├── redux/
│   │   ├── slices/         # Redux slices
│   │   ├── hooks.ts        # Typed hooks
│   │   └── store.ts        # Store configuration
│   ├── services/           # API services
│   ├── styles/             # Global styles and theme
│   ├── types/              # TypeScript interfaces
│   ├── utils/              # Utility functions
│   ├── App.tsx             # Main app component
│   ├── routes.tsx          # Route definitions
│   └── index.tsx           # Entry point
├── .env.example
├── Dockerfile
├── nginx.conf
├── package.json
├── tsconfig.json
└── README.md
```

## Key Components

### Dashboard
- **Dashboard.tsx**: Main dashboard with fleet overview
- **FleetSummaryCard.tsx**: Summary metrics cards
- **BatterySummaryCard.tsx**: Battery status overview
- **AlertsCard.tsx**: Recent alerts and notifications
- **UtilizationChart.tsx**: Fleet utilization trends

### Fleet Management
- **VehicleList.tsx**: Searchable, filterable vehicle list
- **VehicleDetails.tsx**: Detailed vehicle information
- **VehicleMap.tsx**: Real-time vehicle location map
- **AddVehicle.tsx**: Add new vehicle form
- **TripHistory.tsx**: Vehicle trip history

### Charging
- **ChargingStations.tsx**: Interactive charging station map
- **ChargingSessionList.tsx**: Charging session history
- **StartChargingSession.tsx**: Initiate charging session
- **RouteOptimization.tsx**: Optimize routes with charging stops

### Drivers
- **DriverList.tsx**: Driver management table
- **DriverDetails.tsx**: Individual driver profile
- **DriverLeaderboard.tsx**: Performance rankings
- **AssignDriver.tsx**: Assign drivers to vehicles

### Maintenance
- **MaintenanceSchedule.tsx**: Upcoming service schedule
- **ServiceHistory.tsx**: Past maintenance records
- **BatteryHealth.tsx**: Battery health trends
- **ScheduleMaintenance.tsx**: Schedule new service

### Analytics
- **FleetAnalytics.tsx**: Overall fleet metrics
- **TCOAnalysis.tsx**: Total cost of ownership comparison
- **CarbonFootprint.tsx**: Environmental impact metrics
- **UtilizationReport.tsx**: Vehicle utilization analysis

### Billing
- **Subscriptions.tsx**: Subscription management
- **Invoices.tsx**: Invoice history
- **Payments.tsx**: Payment transactions
- **PricingPlans.tsx**: Available pricing tiers

## API Integration

The frontend integrates with multiple microservices:

- **Vehicle Service**: Vehicle CRUD, location tracking, trips
- **Charging Service**: Stations, sessions, route optimization
- **Driver Service**: Driver management, performance, behavior
- **Maintenance Service**: Schedules, records, battery health
- **Analytics Service**: Metrics, trends, reports
- **Billing Service**: Subscriptions, invoices, payments

All API calls are centralized in the `services/` directory.

## State Management

Redux Toolkit manages application state with slices for:
- **auth**: Authentication state
- **vehicle**: Vehicle data and operations
- **charging**: Charging stations and sessions
- **driver**: Driver information and performance
- **maintenance**: Maintenance records and schedules
- **analytics**: Analytics data and metrics
- **notification**: Alerts and notifications

## Authentication

Firebase Authentication provides:
- Email/password authentication
- Google Sign-In
- Session management
- Protected routes
- Auto-login with stored tokens

## Deployment

### Docker

Build and run with Docker:

```bash
docker build -t ev-fleet-frontend .
docker run -p 80:80 ev-fleet-frontend
```

### Nginx

The production build uses Nginx for serving:
- Static file caching
- Gzip compression
- Security headers
- Client-side routing support
- API proxy (optional)

## Development

### Code Style
- TypeScript strict mode enabled
- ESLint for code quality
- Prettier for formatting (recommended)

### Testing
```bash
npm test
```

### Type Checking
```bash
npm run type-check
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| REACT_APP_API_URL | Backend API URL | Yes |
| REACT_APP_WEBSOCKET_URL | WebSocket URL | Yes |
| REACT_APP_FIREBASE_API_KEY | Firebase API key | Yes |
| REACT_APP_FIREBASE_AUTH_DOMAIN | Firebase auth domain | Yes |
| REACT_APP_FIREBASE_PROJECT_ID | Firebase project ID | Yes |
| REACT_APP_MAPBOX_TOKEN | Mapbox access token | Yes |

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## License

Proprietary - All rights reserved

## Support

For support, email support@evfleet.com or open an issue in the repository.

## Version

Current version: 1.0.0

---

Built with ❤️ using React and TypeScript
