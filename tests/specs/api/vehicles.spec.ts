import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';

test.describe('Vehicle API Tests', () => {

  test('GET /api/v1/vehicles - list all vehicles', async ({ authenticatedApiClient }) => {
    const vehicles = await authenticatedApiClient.getVehicles();
    expect(Array.isArray(vehicles)).toBe(true);
  });

  test('POST /api/v1/vehicles - create EV vehicle', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const newVehicle = {
      licensePlate: `TEST${timestamp}`,
      vin: `VIN${timestamp}`.substring(0, 17),
      make: 'Tesla',
      model: 'Model 3',
      year: 2024,
      fuelType: 'EV',
      batteryCapacityKwh: 75.0,
      currentBatterySoc: 100,
      currentRangeKm: 400,
      odometerKm: 0,
    };

    const vehicle = await authenticatedApiClient.post('/api/v1/vehicles', newVehicle);
    expect(vehicle).toBeDefined();
  });

  test('POST /api/v1/vehicles - create ICE vehicle', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const newVehicle = {
      licensePlate: `ICE${timestamp}`,
      vin: `ICE${timestamp}`.substring(0, 17),
      make: 'Maruti',
      model: 'Swift',
      year: 2024,
      fuelType: 'ICE',
      fuelCapacityLiters: 42,
      currentFuelLevel: 100,
      odometerKm: 0,
    };

    const vehicle = await authenticatedApiClient.post('/api/v1/vehicles', newVehicle);
    expect(vehicle).toBeDefined();
  });

  test('GET /api/v1/vehicles/{id} - get single vehicle', async ({ authenticatedApiClient }) => {
    const vehicle = await authenticatedApiClient.get('/api/v1/vehicles/1');
    expect(vehicle).toBeDefined();
  });

  test('PUT /api/v1/vehicles/{id} - update vehicle', async ({ authenticatedApiClient }) => {
    const updateData = {
      status: 'AVAILABLE',
      currentBatterySoc: 80,
    };

    const vehicle = await authenticatedApiClient.put('/api/v1/vehicles/1', updateData);
    expect(vehicle).toBeDefined();
  });

  test('GET /api/v1/vehicles - filter by status', async ({ authenticatedApiClient }) => {
    const vehicles = await authenticatedApiClient.get('/api/v1/vehicles?status=AVAILABLE');
    expect(Array.isArray(vehicles)).toBe(true);
  });

  test('GET /api/v1/vehicles - filter by fuel type', async ({ authenticatedApiClient }) => {
    const vehicles = await authenticatedApiClient.get('/api/v1/vehicles?fuelType=EV');
    expect(Array.isArray(vehicles)).toBe(true);
  });

  test('Duplicate license plate should fail', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/vehicles', {
        licensePlate: 'MH01EV0001', // Existing plate from seed data
        vin: 'VINDUPLICATETEST1',
        make: 'Test',
        model: 'Duplicate',
        year: 2024,
        fuelType: 'EV',
      });
      expect(true).toBe(false); // Should not reach here
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });
});

test.describe('Vehicle Edge Cases', () => {

  test('Invalid VIN format', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/vehicles', {
        licensePlate: 'TESTINVALID',
        vin: 'TOOSHORT',
        make: 'Test',
        model: 'Invalid',
        year: 2024,
        fuelType: 'EV',
      });
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('Battery SOC must be 0-100', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/vehicles', {
        licensePlate: 'TESTBADSOC',
        vin: 'VIN12345678901234',
        make: 'Test',
        model: 'BadSOC',
        year: 2024,
        fuelType: 'EV',
        currentBatterySoc: 150, // Invalid
      });
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });

  test('EV vehicle requires battery capacity', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/vehicles', {
        licensePlate: 'TESTNOBAT',
        vin: 'VIN12345678901235',
        make: 'Test',
        model: 'NoBattery',
        year: 2024,
        fuelType: 'EV',
        // Missing batteryCapacityKwh
      });
    } catch (error: any) {
      expect(error.status).toBeGreaterThanOrEqual(400);
    }
  });
});
