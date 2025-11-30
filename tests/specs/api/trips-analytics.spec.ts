import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Trip API Tests', () => {

  test('GET /api/v1/fleet/trips - list all trips', async ({ authenticatedApiClient }) => {
    const trips = await authenticatedApiClient.getTrips();
    expect(Array.isArray(trips)).toBe(true);
  });

  test('POST /api/v1/fleet/trips/start - start a trip', async ({ authenticatedApiClient }) => {
    const trip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
      vehicleId: 1,
      driverId: 1,
      startLocation: 'Mumbai Central',
      startLatitude: 18.9690,
      startLongitude: 72.8193,
    });
    expect(trip).toBeDefined();
  });

  test('GET /api/v1/fleet/trips/{id} - get single trip', async ({ authenticatedApiClient }) => {
    const trip = await authenticatedApiClient.get('/api/v1/fleet/trips/1');
    expect(trip).toBeDefined();
  });

  test('POST /api/v1/fleet/trips/{id}/complete - complete a trip', async ({ authenticatedApiClient }) => {
    // First start a trip
    const startTrip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
      vehicleId: 7, // Hybrid vehicle
      driverId: 5, // Vikram Singh
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
  });

  test('GET /api/v1/fleet/trips/active - get active trips', async ({ authenticatedApiClient }) => {
    const trips = await authenticatedApiClient.get('/api/v1/fleet/trips/active');
    expect(Array.isArray(trips)).toBe(true);
  });

  test('GET /api/v1/fleet/trips/vehicle/{vehicleId} - get trips by vehicle', async ({ authenticatedApiClient }) => {
    const trips = await authenticatedApiClient.get('/api/v1/fleet/trips/vehicle/1');
    expect(Array.isArray(trips)).toBe(true);
  });

  test('GET /api/v1/fleet/trips/driver/{driverId} - get trips by driver', async ({ authenticatedApiClient }) => {
    const trips = await authenticatedApiClient.get('/api/v1/fleet/trips/driver/1');
    expect(Array.isArray(trips)).toBe(true);
  });
});

test.describe('Analytics API Tests', () => {

  test('GET /api/v1/dashboard/summary - get dashboard summary', async ({ authenticatedApiClient }) => {
    const summary = await authenticatedApiClient.getDashboardSummary();
    expect(summary).toBeDefined();
  });

  test('GET /api/v1/dashboard/fleet-overview - get fleet overview', async ({ authenticatedApiClient }) => {
    const overview = await authenticatedApiClient.get('/api/v1/dashboard/fleet-overview');
    expect(overview).toBeDefined();
  });

  test('GET /api/v1/dashboard/battery-status - get battery status', async ({ authenticatedApiClient }) => {
    const status = await authenticatedApiClient.get('/api/v1/dashboard/battery-status');
    expect(status).toBeDefined();
  });

  test('GET /api/v1/analytics/fleet-summary - get fleet summary', async ({ authenticatedApiClient }) => {
    const summary = await authenticatedApiClient.get('/api/v1/analytics/fleet-summary');
    expect(summary).toBeDefined();
  });

  test('GET /api/v1/esg/quick - get ESG quick summary', async ({ authenticatedApiClient }) => {
    const esg = await authenticatedApiClient.get('/api/v1/esg/quick');
    expect(esg).toBeDefined();
  });

  test('GET /api/v1/analytics/date-range - get data for date range', async ({ authenticatedApiClient }) => {
    const today = new Date();
    const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
    const summary = await authenticatedApiClient.get(
      `/api/v1/analytics/fleet-summary?startDate=${weekAgo.toISOString()}&endDate=${today.toISOString()}`
    );
    expect(summary).toBeDefined();
  });
});

test.describe('Notification API Tests', () => {

  test('GET /api/v1/notifications - list notifications', async ({ authenticatedApiClient }) => {
    const notifications = await authenticatedApiClient.getNotifications();
    expect(Array.isArray(notifications)).toBe(true);
  });

  test('GET /api/v1/notifications/unread - get unread count', async ({ authenticatedApiClient }) => {
    const result = await authenticatedApiClient.get('/api/v1/notifications/unread');
    expect(result).toBeDefined();
  });

  test('POST /api/v1/notifications/{id}/read - mark as read', async ({ authenticatedApiClient }) => {
    const result = await authenticatedApiClient.post('/api/v1/notifications/1/read', {});
    expect(result).toBeDefined();
  });

  test('POST /api/v1/notifications/read-all - mark all as read', async ({ authenticatedApiClient }) => {
    const result = await authenticatedApiClient.post('/api/v1/notifications/read-all', {});
    expect(result).toBeDefined();
  });

  test('DELETE /api/v1/notifications/{id} - delete notification', async ({ authenticatedApiClient }) => {
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
  });
});
