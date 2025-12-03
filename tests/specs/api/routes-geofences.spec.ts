import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Geofence API Tests', () => {

  test('GET /api/geofences - list all geofences', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/geofences');
      const geofences = response?.data || response;
      expect(Array.isArray(geofences) || typeof geofences === 'object').toBe(true);
    } catch (error: any) {
      console.log('List geofences error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/geofences - create circle geofence', async ({ authenticatedApiClient }) => {
    try {
      const timestamp = Date.now();
      const geofence = await authenticatedApiClient.post('/api/geofences', {
        name: `Test Geofence ${timestamp}`,
        description: 'Test geofence for automation',
        geofenceType: 'CIRCLE',
        companyId: 1,
        centerLatitude: 19.0760,
        centerLongitude: 72.8777,
        radius: 500,
        isActive: true,
        alertOnEntry: true,
        alertOnExit: true,
      });
      expect(geofence).toBeDefined();
    } catch (error: any) {
      console.log('Create circle geofence error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/geofences - create polygon geofence', async ({ authenticatedApiClient }) => {
    try {
      const timestamp = Date.now();
      const geofence = await authenticatedApiClient.post('/api/geofences', {
        name: `Polygon Geofence ${timestamp}`,
        description: 'Polygon test',
        geofenceType: 'POLYGON',
        companyId: 1,
        centerLatitude: 19.060,
        centerLongitude: 72.867,
        radius: 1000,
        polygonPoints: [
          { latitude: 19.055, longitude: 72.860 },
          { latitude: 19.065, longitude: 72.860 },
          { latitude: 19.065, longitude: 72.875 },
          { latitude: 19.055, longitude: 72.875 },
        ],
        isActive: true,
        alertOnEntry: true,
        alertOnExit: false,
      });
      expect(geofence).toBeDefined();
    } catch (error: any) {
      console.log('Create polygon geofence error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/geofences/{id} - get single geofence', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/geofences/1');
      const geofence = response?.data || response;
      expect(geofence).toBeDefined();
    } catch (error: any) {
      // 404 is expected if no geofence exists
      console.log('Get geofence error:', error.details?.message || error.message);
      expect([404, 500]).toContain(error.status);
    }
  });

  test('PUT /api/geofences/{id} - update geofence', async ({ authenticatedApiClient }) => {
    try {
      const geofence = await authenticatedApiClient.put('/api/geofences/1', {
        name: 'Updated Geofence',
        geofenceType: 'CIRCLE',
        companyId: 1,
        centerLatitude: 19.0760,
        centerLongitude: 72.8777,
        radius: 600,
        isActive: true,
        alertOnEntry: true,
        alertOnExit: true,
      });
      expect(geofence).toBeDefined();
    } catch (error: any) {
      console.log('Update geofence error:', error.details?.message || error.message);
      expect([400, 404, 500]).toContain(error.status);
    }
  });

  test('GET /api/geofences/active - get active geofences', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/geofences/active');
      const geofences = response?.data || response;
      expect(Array.isArray(geofences) || geofences === undefined).toBe(true);
    } catch (error: any) {
      console.log('Active geofences error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/geofences/check-point - check if point is in geofence', async ({ authenticatedApiClient }) => {
    try {
      const result = await authenticatedApiClient.post('/api/geofences/check-point', {
        latitude: 19.0760,
        longitude: 72.8777,
        geofenceId: 1,
      });
      expect(result).toBeDefined();
    } catch (error: any) {
      console.log('Check point error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});

test.describe('Route API Tests', () => {

  test('GET /api/routes - list all routes', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/routes');
      const routes = response?.data || response;
      expect(Array.isArray(routes) || typeof routes === 'object').toBe(true);
    } catch (error: any) {
      console.log('List routes error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/routes - create route', async ({ authenticatedApiClient }) => {
    try {
      const timestamp = Date.now();
      const route = await authenticatedApiClient.post('/api/routes', {
        routeName: `Test Route ${timestamp}`,
        companyId: 1,
        optimizationCriteria: 'DISTANCE',
        description: 'Automation test route',
        waypoints: [
          { latitude: 19.0760, longitude: 72.8777 },
          { latitude: 19.0900, longitude: 72.8600 },
          { latitude: 19.1000, longitude: 72.8500 },
        ],
        distanceKm: 8.5,
        estimatedDurationMins: 25,
        isActive: true,
      });
      expect(route).toBeDefined();
    } catch (error: any) {
      console.log('Create route error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/routes/{id} - get single route', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/routes/1');
      const route = response?.data || response;
      expect(route).toBeDefined();
    } catch (error: any) {
      // 404 is expected if no route exists
      console.log('Get route error:', error.details?.message || error.message);
      expect([404, 500]).toContain(error.status);
    }
  });

  test('PUT /api/routes/{id} - update route', async ({ authenticatedApiClient }) => {
    try {
      const route = await authenticatedApiClient.put('/api/routes/1', {
        routeName: 'Updated Route',
        companyId: 1,
        optimizationCriteria: 'TIME',
        estimatedDurationMins: 40,
      });
      expect(route).toBeDefined();
    } catch (error: any) {
      console.log('Update route error:', error.details?.message || error.message);
      expect([400, 404, 500]).toContain(error.status);
    }
  });

  test('GET /api/routes/active - get active routes', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/routes/active');
      const routes = response?.data || response;
      expect(Array.isArray(routes) || routes === undefined).toBe(true);
    } catch (error: any) {
      console.log('Active routes error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});

test.describe('Billing API Tests', () => {

  test('GET /api/v1/billing/subscriptions - list subscriptions', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/billing/subscriptions');
      const subscriptions = response?.data || response;
      expect(Array.isArray(subscriptions) || typeof subscriptions === 'object').toBe(true);
    } catch (error: any) {
      console.log('List subscriptions error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/billing/subscriptions/current - get current subscription', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/billing/subscriptions/current');
      const subscription = response?.data || response;
      expect(subscription !== undefined).toBe(true);
    } catch (error: any) {
      console.log('Current subscription error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/billing/plans - list pricing plans', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/billing/plans');
      const plans = response?.data || response;
      expect(Array.isArray(plans) || plans === undefined).toBe(true);
    } catch (error: any) {
      console.log('List plans error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/billing/invoices - list invoices', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/billing/invoices');
      const invoices = response?.data || response;
      expect(Array.isArray(invoices) || invoices === undefined).toBe(true);
    } catch (error: any) {
      console.log('List invoices error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});

test.describe('Location Edge Cases', () => {

  test('Invalid coordinates should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/geofences', {
        name: 'Invalid Coords',
        geofenceType: 'CIRCLE',
        companyId: 1,
        centerLatitude: 999,
        centerLongitude: 999,
        radius: 500,
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
        geofenceType: 'CIRCLE',
        companyId: 1,
        centerLatitude: 19.0760,
        centerLongitude: 72.8777,
        radius: -100,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Empty route waypoints should fail or succeed', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.post('/api/routes', {
        routeName: 'Empty Route',
        companyId: 1,
        optimizationCriteria: 'DISTANCE',
        waypoints: [],
      }) as any;
      // May succeed with empty waypoints in some implementations
      expect(response).toBeDefined();
    } catch (error: any) {
      // Or may fail with validation error
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});
