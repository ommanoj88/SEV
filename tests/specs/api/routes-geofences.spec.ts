import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Geofence API Tests', () => {

  test('GET /api/geofences - list all geofences', async ({ authenticatedApiClient }) => {
    const geofences = await authenticatedApiClient.get('/api/geofences');
    expect(Array.isArray(geofences)).toBe(true);
  });

  test('POST /api/geofences - create circle geofence', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const geofence = await authenticatedApiClient.post('/api/geofences', {
      name: `Test Geofence ${timestamp}`,
      description: 'Test geofence for automation',
      type: 'CIRCLE',
      coordinates: { lat: 19.0760, lng: 72.8777 },
      radiusMeters: 500,
      isActive: true,
      alertOnEntry: true,
      alertOnExit: true,
    });
    expect(geofence).toBeDefined();
  });

  test('POST /api/geofences - create polygon geofence', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const geofence = await authenticatedApiClient.post('/api/geofences', {
      name: `Polygon Geofence ${timestamp}`,
      description: 'Polygon test',
      type: 'POLYGON',
      coordinates: [
        { lat: 19.055, lng: 72.860 },
        { lat: 19.065, lng: 72.860 },
        { lat: 19.065, lng: 72.875 },
        { lat: 19.055, lng: 72.875 },
      ],
      isActive: true,
      alertOnEntry: true,
      alertOnExit: false,
    });
    expect(geofence).toBeDefined();
  });

  test('GET /api/geofences/{id} - get single geofence', async ({ authenticatedApiClient }) => {
    const geofence = await authenticatedApiClient.get('/api/geofences/1');
    expect(geofence).toBeDefined();
  });

  test('PUT /api/geofences/{id} - update geofence', async ({ authenticatedApiClient }) => {
    const geofence = await authenticatedApiClient.put('/api/geofences/1', {
      isActive: true,
      alertOnEntry: true,
      alertOnExit: true,
    });
    expect(geofence).toBeDefined();
  });

  test('GET /api/geofences/active - get active geofences', async ({ authenticatedApiClient }) => {
    const geofences = await authenticatedApiClient.get('/api/geofences/active');
    expect(Array.isArray(geofences)).toBe(true);
  });

  test('POST /api/geofences/check-point - check if point is in geofence', async ({ authenticatedApiClient }) => {
    const result = await authenticatedApiClient.post('/api/geofences/check-point', {
      latitude: 19.0760,
      longitude: 72.8777,
      geofenceId: 1,
    });
    expect(result).toBeDefined();
  });
});

test.describe('Route API Tests', () => {

  test('GET /api/routes - list all routes', async ({ authenticatedApiClient }) => {
    const routes = await authenticatedApiClient.get('/api/routes');
    expect(Array.isArray(routes)).toBe(true);
  });

  test('POST /api/routes - create route', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const route = await authenticatedApiClient.post('/api/routes', {
      name: `Test Route ${timestamp}`,
      description: 'Automation test route',
      waypoints: [
        { lat: 19.0760, lng: 72.8777 },
        { lat: 19.0900, lng: 72.8600 },
        { lat: 19.1000, lng: 72.8500 },
      ],
      distanceKm: 8.5,
      estimatedDurationMins: 25,
      isActive: true,
    });
    expect(route).toBeDefined();
  });

  test('GET /api/routes/{id} - get single route', async ({ authenticatedApiClient }) => {
    const route = await authenticatedApiClient.get('/api/routes/1');
    expect(route).toBeDefined();
  });

  test('PUT /api/routes/{id} - update route', async ({ authenticatedApiClient }) => {
    const route = await authenticatedApiClient.put('/api/routes/1', {
      estimatedDurationMins: 40,
    });
    expect(route).toBeDefined();
  });

  test('GET /api/routes/active - get active routes', async ({ authenticatedApiClient }) => {
    const routes = await authenticatedApiClient.get('/api/routes/active');
    expect(Array.isArray(routes)).toBe(true);
  });
});

test.describe('Billing API Tests', () => {

  test('GET /api/v1/billing/subscriptions - list subscriptions', async ({ authenticatedApiClient }) => {
    const subscriptions = await authenticatedApiClient.get('/api/v1/billing/subscriptions');
    expect(Array.isArray(subscriptions) || typeof subscriptions === 'object').toBe(true);
  });

  test('GET /api/v1/billing/subscriptions/current - get current subscription', async ({ authenticatedApiClient }) => {
    const subscription = await authenticatedApiClient.get('/api/v1/billing/subscriptions/current');
    expect(subscription).toBeDefined();
  });

  test('GET /api/v1/billing/plans - list pricing plans', async ({ authenticatedApiClient }) => {
    const plans = await authenticatedApiClient.get('/api/v1/billing/plans');
    expect(Array.isArray(plans)).toBe(true);
  });

  test('GET /api/v1/billing/invoices - list invoices', async ({ authenticatedApiClient }) => {
    const invoices = await authenticatedApiClient.get('/api/v1/billing/invoices');
    expect(Array.isArray(invoices)).toBe(true);
  });
});

test.describe('Location Edge Cases', () => {

  test('Invalid coordinates should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/geofences', {
        name: 'Invalid Coords',
        type: 'CIRCLE',
        coordinates: { lat: 999, lng: 999 }, // Invalid
        radiusMeters: 500,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Negative radius should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/geofences', {
        name: 'Negative Radius',
        type: 'CIRCLE',
        coordinates: { lat: 19.0760, lng: 72.8777 },
        radiusMeters: -100, // Invalid
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Empty route waypoints should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/routes', {
        name: 'Empty Route',
        waypoints: [], // Empty
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});
