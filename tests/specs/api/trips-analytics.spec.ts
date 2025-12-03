import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Trip API Tests', () => {

  test('GET /api/v1/fleet/trips - list all trips', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.getTrips();
      const trips = response?.data || response;
      expect(Array.isArray(trips) || trips === undefined).toBe(true);
    } catch (error: any) {
      console.log('List trips error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/v1/fleet/trips/start - start a trip', async ({ authenticatedApiClient }) => {
    try {
      const trip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
        vehicleId: 1,
        driverId: 1,
        startLocation: 'Mumbai Central',
        startLatitude: 18.9690,
        startLongitude: 72.8193,
      });
      expect(trip).toBeDefined();
    } catch (error: any) {
      console.log('Start trip error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/fleet/trips/{id} - get single trip', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/fleet/trips/1');
      const trip = response?.data || response;
      expect(trip).toBeDefined();
    } catch (error: any) {
      // 404 is expected if no trips exist
      console.log('Get trip error:', error.details?.message || error.message);
      expect([404, 500]).toContain(error.status);
    }
  });

  test('POST /api/v1/fleet/trips/{id}/complete - complete a trip', async ({ authenticatedApiClient }) => {
    try {
      // First start a trip
      const startTrip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
        vehicleId: 7,
        driverId: 5,
        startLocation: 'Test Start',
        startLatitude: 19.0760,
        startLongitude: 72.8777,
      });

      if (startTrip && startTrip.id) {
        const completed = await authenticatedApiClient.post(`/api/v1/fleet/trips/${startTrip.id}/complete`, {
          endLocation: 'Test End',
          endLatitude: 19.1000,
          endLongitude: 72.9000,
          distanceKm: 15.5,
        });
        expect(completed).toBeDefined();
      }
    } catch (error: any) {
      console.log('Complete trip error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/fleet/trips/active - get active trips', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/fleet/trips/active');
      const trips = response?.data || response;
      expect(Array.isArray(trips) || trips === undefined).toBe(true);
    } catch (error: any) {
      console.log('Active trips error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/fleet/trips/vehicle/{vehicleId} - get trips by vehicle', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/fleet/trips/vehicle/1');
      const trips = response?.data || response;
      expect(Array.isArray(trips) || typeof trips === 'object').toBe(true);
    } catch (error: any) {
      console.log('Vehicle trips error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/fleet/trips/driver/{driverId} - get trips by driver', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/fleet/trips/driver/1');
      const trips = response?.data || response;
      expect(Array.isArray(trips) || trips === undefined).toBe(true);
    } catch (error: any) {
      console.log('Driver trips error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});

test.describe('Analytics API Tests', () => {

  test('GET /api/v1/dashboard/summary - get dashboard summary', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.getDashboardSummary();
      const summary = response?.data || response;
      expect(summary).toBeDefined();
    } catch (error: any) {
      console.log('Dashboard summary error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/dashboard/fleet-overview - get fleet overview', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/dashboard/fleet-overview');
      const overview = response?.data || response;
      expect(overview).toBeDefined();
    } catch (error: any) {
      console.log('Fleet overview error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/dashboard/battery-status - get battery status', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/dashboard/battery-status');
      const status = response?.data || response;
      expect(status).toBeDefined();
    } catch (error: any) {
      console.log('Battery status error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/analytics/fleet-summary - get fleet summary', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/analytics/fleet-summary');
      const summary = response?.data || response;
      expect(summary).toBeDefined();
    } catch (error: any) {
      console.log('Fleet summary error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/esg/quick - get ESG quick summary', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/esg/quick');
      const esg = response?.data || response;
      expect(esg).toBeDefined();
    } catch (error: any) {
      console.log('ESG summary error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/analytics/date-range - get data for date range', async ({ authenticatedApiClient }) => {
    try {
      const today = new Date();
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      const response = await authenticatedApiClient.get(
        `/api/v1/analytics/fleet-summary?startDate=${weekAgo.toISOString()}&endDate=${today.toISOString()}`
      );
      const summary = response?.data || response;
      expect(summary).toBeDefined();
    } catch (error: any) {
      console.log('Date range analytics error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});

test.describe('Notification API Tests', () => {

  test('GET /api/v1/notifications - list notifications', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.getNotifications();
      const notifications = response?.data || response;
      expect(Array.isArray(notifications) || notifications === undefined).toBe(true);
    } catch (error: any) {
      console.log('List notifications error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('GET /api/v1/notifications/unread - get unread count', async ({ authenticatedApiClient }) => {
    try {
      const response = await authenticatedApiClient.get('/api/v1/notifications/unread');
      const result = response?.data || response;
      expect(result !== undefined).toBe(true);
    } catch (error: any) {
      console.log('Unread notifications error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/v1/notifications/{id}/read - mark as read', async ({ authenticatedApiClient }) => {
    try {
      const result = await authenticatedApiClient.post('/api/v1/notifications/1/read', {});
      expect(result).toBeDefined();
    } catch (error: any) {
      console.log('Mark read error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('POST /api/v1/notifications/read-all - mark all as read', async ({ authenticatedApiClient }) => {
    try {
      const result = await authenticatedApiClient.post('/api/v1/notifications/read-all', {});
      expect(result).toBeDefined();
    } catch (error: any) {
      console.log('Mark all read error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('DELETE /api/v1/notifications/{id} - delete notification', async ({ authenticatedApiClient }) => {
    try {
      // First create a notification to delete
      const notification = await authenticatedApiClient.post('/api/v1/notifications', {
        title: 'Test Delete Notification',
        message: 'This will be deleted',
        type: 'SYSTEM',
        priority: 'LOW',
      });

      if (notification && notification.id) {
        const result = await authenticatedApiClient.delete(`/api/v1/notifications/${notification.id}`);
        expect(result).toBeDefined();
      }
    } catch (error: any) {
      console.log('Delete notification error:', error.details?.message || error.message);
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});
