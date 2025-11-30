import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Charging Station API Tests', () => {

  test('GET /api/v1/charging/stations - list all stations', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.getChargingStations();
    expect(Array.isArray(stations)).toBe(true);
  });

  test('POST /api/v1/charging/stations - create station', async ({ authenticatedApiClient }) => {
    const timestamp = Date.now();
    const newStation = {
      name: `Test Station ${timestamp}`,
      address: 'Test Location, Mumbai',
      latitude: 19.0760,
      longitude: 72.8777,
      totalSlots: 5,
      availableSlots: 5,
      pricePerKwh: 15.00,
      status: 'ACTIVE',
      connectorTypes: ['CCS2', 'Type2'],
    };

    const station = await authenticatedApiClient.post('/api/v1/charging/stations', newStation);
    expect(station).toBeDefined();
  });

  test('GET /api/v1/charging/stations/{id} - get single station', async ({ authenticatedApiClient }) => {
    const station = await authenticatedApiClient.get('/api/v1/charging/stations/1');
    expect(station).toBeDefined();
  });

  test('PUT /api/v1/charging/stations/{id} - update station', async ({ authenticatedApiClient }) => {
    const updateData = {
      pricePerKwh: 16.00,
      availableSlots: 3,
    };

    const station = await authenticatedApiClient.put('/api/v1/charging/stations/1', updateData);
    expect(station).toBeDefined();
  });

  test('GET /api/v1/charging/stations - filter by status', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.get('/api/v1/charging/stations?status=ACTIVE');
    expect(Array.isArray(stations)).toBe(true);
  });

  test('GET /api/v1/charging/stations/nearby - get nearby stations', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.get('/api/v1/charging/stations/nearby?lat=19.0760&lng=72.8777&radius=10');
    expect(Array.isArray(stations) || typeof stations === 'object').toBe(true);
  });
});

test.describe('Charging Session API Tests', () => {

  test('POST /api/v1/charging/sessions/start - start charging session', async ({ authenticatedApiClient }) => {
    const session = await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
      vehicleId: 1,
      stationId: 2, // Andheri East Hub has 6 available slots
    });
    expect(session).toBeDefined();
  });

  test('GET /api/v1/charging/sessions - list sessions', async ({ authenticatedApiClient }) => {
    const sessions = await authenticatedApiClient.get('/api/v1/charging/sessions');
    expect(Array.isArray(sessions)).toBe(true);
  });

  test('GET /api/v1/charging/sessions/active - get active sessions', async ({ authenticatedApiClient }) => {
    const sessions = await authenticatedApiClient.get('/api/v1/charging/sessions/active');
    expect(Array.isArray(sessions)).toBe(true);
  });

  test('POST /api/v1/charging/sessions/{id}/end - end charging session', async ({ authenticatedApiClient }) => {
    // First start a session
    const startSession = await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
      vehicleId: 7, // Hybrid vehicle
      stationId: 2,
    });

    if (startSession && startSession.id) {
      const endSession = await authenticatedApiClient.post(`/api/v1/charging/sessions/${startSession.id}/end`, {
        energyKwh: 25.5,
      });
      expect(endSession).toBeDefined();
    }
  });
});

test.describe('Charging Edge Cases', () => {

  test('Cannot start session at station with no available slots', async ({ authenticatedApiClient }) => {
    try {
      // Station 4 (Powai Tech Park) has 0 available slots
      await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
        vehicleId: 1,
        stationId: 4,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });

  test('Cannot start session at inactive station', async ({ authenticatedApiClient }) => {
    try {
      // Station 5 is INACTIVE
      await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
        vehicleId: 1,
        stationId: 5,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });

  test('Cannot start session for vehicle already charging', async ({ authenticatedApiClient }) => {
    try {
      // Vehicle 3 is already CHARGING in seed data
      await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
        vehicleId: 3,
        stationId: 1,
      });
      expect(true).toBe(false);
    } catch (error: any) {
      expect([400, 409]).toContain(error.status);
    }
  });

  test('Cost calculation accuracy', async ({ authenticatedApiClient }) => {
    // Complete a session and verify cost
    const session = await authenticatedApiClient.get('/api/v1/charging/sessions/1');
    if (session && session.energyKwh && session.cost) {
      const expectedCost = session.energyKwh * 12.50; // Mumbai Central price
      expect(Math.abs(session.cost - expectedCost)).toBeLessThan(1);
    }
  });
});
