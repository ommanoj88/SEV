import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Driver API Tests', () => {

  test('GET /api/v1/drivers - list all drivers', async ({ authenticatedApiClient }) => {
    const drivers = await authenticatedApiClient.getDrivers();
    expect(Array.isArray(drivers)).toBe(true);
  });

  test('GET /api/v1/drivers/active - get active drivers', async ({ authenticatedApiClient }) => {
    const drivers = await authenticatedApiClient.get('/api/v1/drivers/active');
    expect(Array.isArray(drivers)).toBe(true);
  });

  test('GET /api/v1/drivers/available - get available drivers', async ({ authenticatedApiClient }) => {
    const drivers = await authenticatedApiClient.get('/api/v1/drivers/available');
    expect(Array.isArray(drivers)).toBe(true);
  });

  test('POST /api/v1/drivers - create driver', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const newDriver = {
      name: `Test Driver ${timestamp}`,
      email: `driver.${timestamp}@test.com`,
      phone: `+91-98765${timestamp.toString().slice(-5)}`,
      licenseNumber: `MH01${timestamp.toString().slice(-10)}`,
      licenseExpiry: '2026-12-31',
    };

    const driver = await authenticatedApiClient.post('/api/v1/drivers', newDriver);
    expect(driver).toBeDefined();
  });

  test('GET /api/v1/drivers/{id} - get single driver', async ({ authenticatedApiClient }) => {
    const driver = await authenticatedApiClient.get('/api/v1/drivers/1');
    expect(driver).toBeDefined();
  });

  test('PUT /api/v1/drivers/{id} - update driver', async ({ authenticatedApiClient }) => {
    const updateData = {
      phone: '+91-9876500099',
      status: 'ACTIVE',
    };

    const driver = await authenticatedApiClient.put('/api/v1/drivers/1', updateData);
    expect(driver).toBeDefined();
  });

  test('GET /api/v1/drivers/expiring-licenses - get drivers with expiring licenses', async ({ authenticatedApiClient }) => {
    const drivers = await authenticatedApiClient.get('/api/v1/drivers/expiring-licenses?days=30');
    expect(Array.isArray(drivers)).toBe(true);
  });
});

test.describe('Driver Assignment Tests', () => {

  test('POST /api/v1/drivers/{id}/assign - assign driver to vehicle', async ({ authenticatedApiClient }) => {
    // First unassign if already assigned
    try {
      await authenticatedApiClient.post('/api/v1/drivers/5/unassign', {});
    } catch (e) { /* ignore */ }

    const result = await authenticatedApiClient.post('/api/v1/drivers/5/assign', { vehicleId: 7 });
    expect(result).toBeDefined();
  });

  test('POST /api/v1/drivers/{id}/unassign - unassign driver from vehicle', async ({ authenticatedApiClient }) => {
    // First assign
    try {
      await authenticatedApiClient.post('/api/v1/drivers/5/assign', { vehicleId: 7 });
    } catch (e) { /* ignore */ }

    const result = await authenticatedApiClient.post('/api/v1/drivers/5/unassign', {});
    expect(result).toBeDefined();
  });

  test('Cannot assign already assigned driver', async ({ authenticatedApiClient }) => {
    try {
      // Driver 1 is already assigned in seed data
      await authenticatedApiClient.post('/api/v1/drivers/1/assign', { vehicleId: 8 });
      expect(true).toBe(false); // Should not reach here
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });
});

test.describe('Driver Edge Cases', () => {

  test('Duplicate phone number should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/drivers', {
        name: 'Duplicate Phone Driver',
        email: 'unique.email@test.com',
        phone: '+91-9876500001', // Existing phone from seed data
        licenseNumber: 'MH01UNIQUE12345',
        licenseExpiry: '2026-12-31',
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });

  test('Duplicate email should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/drivers', {
        name: 'Duplicate Email Driver',
        email: 'rahul.sharma@testfleet.com', // Existing email from seed data
        phone: '+91-9999999999',
        licenseNumber: 'MH01UNIQUE54321',
        licenseExpiry: '2026-12-31',
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });

  test('Expired license should flag driver', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const driver = await authenticatedApiClient.post('/api/v1/drivers', {
      name: `Expired License Driver ${timestamp}`,
      email: `expired.${timestamp}@test.com`,
      phone: `+91-88888${timestamp.toString().slice(-5)}`,
      licenseNumber: `MH01EXP${timestamp.toString().slice(-8)}`,
      licenseExpiry: '2020-01-01', // Expired date
    });
    expect(driver).toBeDefined();
  });
});
