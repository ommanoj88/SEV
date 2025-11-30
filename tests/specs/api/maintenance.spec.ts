import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Maintenance API Tests', () => {

  test('GET /api/v1/maintenance/records - list all records', async ({ authenticatedApiClient }) => {
    const records = await authenticatedApiClient.getMaintenanceRecords();
    expect(Array.isArray(records)).toBe(true);
  });

  test('POST /api/v1/maintenance/records - create maintenance record', async ({ authenticatedApiClient }) => {
    const record = await authenticatedApiClient.post('/api/v1/maintenance/records', {
      vehicleId: 1,
      type: 'ROUTINE',
      description: 'Scheduled maintenance test',
      scheduledDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(), // 7 days from now
      cost: 5000.00,
      odometerAtService: 15500,
    });
    expect(record).toBeDefined();
  });

  test('GET /api/v1/maintenance/records/upcoming - get upcoming maintenance', async ({ authenticatedApiClient }) => {
    const records = await authenticatedApiClient.get('/api/v1/maintenance/records/upcoming');
    expect(Array.isArray(records)).toBe(true);
  });

  test('GET /api/v1/maintenance/records/overdue - get overdue maintenance', async ({ authenticatedApiClient }) => {
    const records = await authenticatedApiClient.get('/api/v1/maintenance/records/overdue');
    expect(Array.isArray(records)).toBe(true);
  });

  test('POST /api/v1/maintenance/records/{id}/complete - complete maintenance', async ({ authenticatedApiClient }) => {
    // First create a record to complete
    const record = await authenticatedApiClient.post('/api/v1/maintenance/records', {
      vehicleId: 5,
      type: 'OIL_CHANGE',
      description: 'Oil change for completion test',
      scheduledDate: new Date().toISOString(),
      cost: 3000.00,
      odometerAtService: 46000,
    });

    if (record && record.id) {
      const completed = await authenticatedApiClient.post(`/api/v1/maintenance/records/${record.id}/complete`, {
        completedDate: new Date().toISOString(),
        notes: 'Completed successfully',
      });
      expect(completed).toBeDefined();
    }
  });

  test('GET /api/v1/maintenance/records/vehicle/{vehicleId} - get vehicle maintenance history', async ({ authenticatedApiClient }) => {
    const records = await authenticatedApiClient.get('/api/v1/maintenance/records/vehicle/1');
    expect(Array.isArray(records)).toBe(true);
  });

  test('GET /api/v1/maintenance/types - get maintenance types', async ({ authenticatedApiClient }) => {
    const types = await authenticatedApiClient.get('/api/v1/maintenance/types');
    expect(Array.isArray(types) || typeof types === 'object').toBe(true);
  });
});

test.describe('Battery Health API Tests', () => {

  test('POST /api/v1/battery-health - create battery health record', async ({ authenticatedApiClient }) => {
    const record = await authenticatedApiClient.post('/api/v1/battery-health', {
      vehicleId: 1,
      sohPercentage: 97.8,
      cycleCount: 150,
      degradationRate: 0.014,
      notes: 'Regular battery health check',
    });
    expect(record).toBeDefined();
  });

  test('GET /api/v1/battery-health/vehicle/{id}/latest - get latest battery health', async ({ authenticatedApiClient }) => {
    const record = await authenticatedApiClient.get('/api/v1/battery-health/vehicle/1/latest');
    expect(record).toBeDefined();
  });

  test('GET /api/v1/battery-health/low-soh - get vehicles with low SOH', async ({ authenticatedApiClient }) => {
    const records = await authenticatedApiClient.get('/api/v1/battery-health/low-soh?threshold=80');
    expect(Array.isArray(records)).toBe(true);
  });

  test('GET /api/v1/battery-health/vehicle/{id}/trend - get SOH trend', async ({ authenticatedApiClient }) => {
    const trend = await authenticatedApiClient.get('/api/v1/battery-health/vehicle/1/trend');
    expect(trend).toBeDefined();
  });
});

test.describe('Maintenance Edge Cases', () => {

  test('Invalid date range should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/maintenance/records', {
        vehicleId: 1,
        type: 'ROUTINE',
        description: 'Invalid date test',
        scheduledDate: 'invalid-date',
        cost: 5000.00,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Negative cost should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/maintenance/records', {
        vehicleId: 1,
        type: 'ROUTINE',
        description: 'Negative cost test',
        scheduledDate: new Date().toISOString(),
        cost: -100.00,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Non-existent vehicle should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/maintenance/records', {
        vehicleId: 99999,
        type: 'ROUTINE',
        description: 'Non-existent vehicle test',
        scheduledDate: new Date().toISOString(),
        cost: 5000.00,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 404]).toContain(error.status);
    }
  });
});
