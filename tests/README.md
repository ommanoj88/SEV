# Playwright Test Suite - EV Fleet Management

Comprehensive automation testing for the EV Fleet Management application.

## Quick Start

```bash
cd tests

# Install dependencies
npm install

# Install Playwright browsers
npx playwright install

# Run all tests
npm test

# Run specific test suites
npm run test:api      # API tests only
npm run test:e2e      # E2E tests only
npm run test:ui       # UI/Chromium tests

# Run in headed mode (visible browser)
npm run test:headed

# Debug mode
npm run test:debug

# View report
npm run report
```

## Test Credentials

```
Email: testuser1@gmail.com
Password: Password@123
Company ID: 1
Role: FLEET_MANAGER
```

## Test Structure

```
tests/
├── fixtures/           # Test fixtures and shared setup
│   └── test-fixtures.ts
├── pages/              # Page Object Models
│   ├── BasePage.ts
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── specs/              # Test specifications
│   ├── api/            # API tests
│   │   ├── auth.spec.ts
│   │   ├── vehicles.spec.ts
│   │   ├── drivers.spec.ts
│   │   ├── charging.spec.ts
│   │   ├── maintenance.spec.ts
│   │   ├── trips-analytics.spec.ts
│   │   ├── routes-geofences.spec.ts
│   │   └── error-handling.spec.ts
│   ├── ui/             # UI tests
│   │   ├── login.spec.ts
│   │   └── dashboard.spec.ts
│   └── e2e/            # End-to-end tests
│       └── user-journeys.spec.ts
├── seed/               # Test data
│   └── seed-test-data.sql
├── utils/              # Utilities
│   └── ApiClient.ts
└── reports/            # Test reports (generated)
```

## Test Coverage

| Module | API Tests | UI Tests | E2E Tests |
|--------|-----------|----------|-----------|
| Authentication | ✅ 8 | ✅ 12 | ✅ |
| Vehicles | ✅ 12 | ✅ | ✅ |
| Drivers | ✅ 10 | ✅ | ✅ |
| Charging | ✅ 12 | ✅ | ✅ |
| Maintenance | ✅ 10 | ✅ | ✅ |
| Trips | ✅ 8 | ✅ | ✅ |
| Analytics | ✅ 6 | ✅ | ✅ |
| Routes/Geofences | ✅ 12 | - | ✅ |
| Billing | ✅ 4 | ✅ | ✅ |
| Notifications | ✅ 5 | ✅ | ✅ |
| Error Handling | ✅ 15 | ✅ | ✅ |

**Total: 200+ test cases**

## Seed Data

The seed data (`seed/seed-test-data.sql`) includes:

- 1 Company (Test Fleet Company)
- 4 Users (Admin, Fleet Manager, Driver, Viewer)
- 8 Vehicles (EV, ICE, HYBRID with various statuses)
- 6 Drivers (active, on_trip, expired license scenarios)
- 5 Charging Stations (with different availability)
- 4 Charging Sessions
- 4 Trips
- 6 Maintenance Records (scheduled, completed, overdue)
- 6 Notifications
- 4 Geofences
- 3 Routes
- 3 Pricing Plans
- 1 Active Subscription
- 6 Battery Health Records

## Running Tests

### Prerequisites

1. Backend running on `http://localhost:8080`
2. Frontend running on `http://localhost:3000`
3. Database seeded with test data

### Seed the Database

```bash
# From tests folder
psql -U postgres -d evfleet_auth -f seed/seed-test-data.sql
```

### Run Tests

```bash
# All tests
npm test

# With UI (visible browser)
npm run test:headed

# Specific file
npx playwright test specs/api/auth.spec.ts

# Specific test
npx playwright test -g "valid credentials"
```

## CI/CD

Tests run automatically on:
- Push to `main` or `develop`
- Pull requests to `main`

See `.github/workflows/playwright.yml`

## Reporting

After tests run, view the HTML report:

```bash
npm run report
```

Reports include:
- Test results with screenshots on failure
- Video recordings on retry
- Trace files for debugging

## Writing New Tests

### API Test Example

```typescript
import { test, expect } from '../../fixtures/test-fixtures';

test('GET /api/v1/vehicles - list vehicles', async ({ authenticatedApiClient }) => {
  const vehicles = await authenticatedApiClient.getVehicles();
  expect(Array.isArray(vehicles)).toBe(true);
});
```

### UI Test Example

```typescript
import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';

test('Login flow', async ({ loginPage }) => {
  await loginPage.goto();
  await loginPage.login(TEST_USER.email, TEST_USER.password);
  await loginPage.expectLoginSuccess();
});
```

## Troubleshooting

### Tests failing with 401?
- Ensure backend is running
- Check test credentials match seed data
- Verify JWT token generation

### Timeouts?
- Increase timeout in `playwright.config.ts`
- Check if backend is responding

### Flaky tests?
- Add proper waits
- Use `waitForLoadState('networkidle')`
- Check for race conditions
