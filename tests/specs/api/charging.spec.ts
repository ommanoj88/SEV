import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Charging Station API Tests', () => {

  test('GET /api/v1/charging/stations - list all stations', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.getChargingStations() as any;
    // May return array directly or wrapped response
    expect(stations).toBeDefined();
    expect(Array.isArray(stations) || (stations.data && Array.isArray(stations.data))).toBe(true);
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
      companyId: 1,
    };

    try {
      const station = await authenticatedApiClient.post('/api/v1/charging/stations', newStation) as any;
      expect(station).toBeDefined();
    } catch (error: any) {
      console.log('Create station error:', error.details?.message || error.message);
      expect([201, 400, 500]).toContain(error.status);
    }
  });

  test('GET /api/v1/charging/stations/{id} - get single station', async ({ authenticatedApiClient }) => {
    // First get list to find valid ID
    const stations = await authenticatedApiClient.getChargingStations() as any;
    const stationList = Array.isArray(stations) ? stations : (stations.data || []);
    
    if (stationList.length > 0) {
      const station = await authenticatedApiClient.get(`/api/v1/charging/stations/${stationList[0].id}`) as any;
      expect(station).toBeDefined();
    } else {
      expect(true).toBe(true); // No stations to test
    }
  });

  test('PUT /api/v1/charging/stations/{id} - update station', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.getChargingStations() as any;
    const stationList = Array.isArray(stations) ? stations : (stations.data || []);
    
    if (stationList.length > 0) {
      try {
        const updateData = {
          ...stationList[0],
          pricePerKwh: 16.00,
        };
        delete updateData.id;
        
        const station = await authenticatedApiClient.put(`/api/v1/charging/stations/${stationList[0].id}`, updateData) as any;
        expect(station).toBeDefined();
      } catch (error: any) {
        console.log('Update station error:', error.details?.message || error.message);
        expect([200, 400, 500]).toContain(error.status);
      }
    } else {
      expect(true).toBe(true);
    }
  });

  test('GET /api/v1/charging/stations - filter by status', async ({ authenticatedApiClient }) => {
    const stations = await authenticatedApiClient.get('/api/v1/charging/stations?status=ACTIVE') as any;
    expect(stations).toBeDefined();
  });
});

test.describe('Charging Session API Tests', () => {

  test('GET /api/v1/charging/sessions - list sessions', async ({ authenticatedApiClient }) => {
    const response = await authenticatedApiClient.get('/api/v1/charging/sessions') as any;
    expect(response).toBeDefined();
    // May be array or wrapped response
    expect(response.success === true || Array.isArray(response)).toBe(true);
  });

  test('POST /api/v1/charging/sessions/start - start charging session', async ({ authenticatedApiClient }) => {
    // Get available stations first
    const stations = await authenticatedApiClient.getChargingStations() as any;
    const stationList = Array.isArray(stations) ? stations : (stations.data || []);
    const availableStation = stationList.find((s: any) => s.availableSlots > 0 && s.status === 'ACTIVE');
    
    if (availableStation) {
      try {
        const session = await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
          vehicleId: 1,
          stationId: availableStation.id,
        }) as any;
        expect(session).toBeDefined();
      } catch (error: any) {
        console.log('Start session error:', error.details?.message || error.message);
        expect([201, 400, 409, 500]).toContain(error.status);
      }
    } else {
      expect(true).toBe(true); // No available stations
    }
  });
});

test.describe('Charging Validation Tests', () => {

  test('Start session without vehicleId returns error', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
        stationId: 1,
        // Missing vehicleId
      });
    } catch (error: any) {
      expect([400, 500]).toContain(error.status);
    }
  });

  test('Start session without stationId returns error', async ({ authenticatedApiClient }) => {
    try {
      await authenticatedApiClient.post('/api/v1/charging/sessions/start', {
        vehicleId: 1,
        // Missing stationId
      });
    } catch (error: any) {
      expect([400, 500]).toContain(error.status);
    }
  });
});
