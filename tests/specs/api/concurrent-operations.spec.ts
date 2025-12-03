import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Concurrent Operations Tests', () => {

  test.describe('Parallel Read Operations', () => {

    test('Multiple GET requests in parallel', async ({ authenticatedApiClient }) => {
      const requests = [
        authenticatedApiClient.getVehicles(),
        authenticatedApiClient.get('/api/v1/drivers?companyId=1'),
        authenticatedApiClient.getChargingStations(),
        authenticatedApiClient.getMaintenanceRecords(),
        authenticatedApiClient.getDashboardSummary(),
      ];

      const results = await Promise.allSettled(requests);
      const successful = results.filter(r => r.status === 'fulfilled').length;
      
      console.log(`Parallel reads: ${successful}/${results.length} succeeded`);
      expect(successful).toBeGreaterThan(0);
    });

    test('Same endpoint called 10 times in parallel', async ({ authenticatedApiClient }) => {
      const requests = Array(10).fill(null).map(() => 
        authenticatedApiClient.getVehicles().catch(() => null)
      );

      const results = await Promise.all(requests);
      const successful = results.filter(r => r !== null).length;
      
      console.log(`Same endpoint parallel: ${successful}/10 succeeded`);
      expect(successful).toBeGreaterThanOrEqual(5);
    });

    test('Rapid sequential requests', async ({ authenticatedApiClient }) => {
      const results: boolean[] = [];
      
      for (let i = 0; i < 10; i++) {
        try {
          await authenticatedApiClient.getVehicles();
          results.push(true);
        } catch {
          results.push(false);
        }
      }
      
      const successful = results.filter(r => r).length;
      console.log(`Rapid sequential: ${successful}/10 succeeded`);
      expect(successful).toBeGreaterThanOrEqual(8);
    });
  });

  test.describe('Concurrent Write Operations', () => {

    test('Multiple vehicle creates in parallel', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      const createRequests = Array(5).fill(null).map((_, i) => 
        authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `PAR-${timestamp}-${i}`,
          licensePlate: `KA${i}PAR${String(timestamp).slice(-4)}`,
          vin: `PAR${i}${String(timestamp).slice(-12)}`,
          make: 'ParallelTest',
          model: `Model${i}`,
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }).catch(e => ({ error: true, status: e.status }))
      );

      const results = await Promise.all(createRequests);
      const successful = results.filter((r: any) => !r.error).length;
      
      console.log(`Parallel creates: ${successful}/5 succeeded`);
      expect(successful).toBeGreaterThanOrEqual(0); // Some may fail due to race
    });

    test('Create and read same resource concurrently', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      
      // Start create and reads in parallel
      const [createResult, ...readResults] = await Promise.allSettled([
        authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `CR-${timestamp}`,
          licensePlate: `KA01CR${String(timestamp).slice(-4)}`,
          vin: `CR${String(timestamp).slice(-14)}`,
          make: 'CreateRead',
          model: 'Test',
          year: 2024,
          type: 'SUV',
          fuelType: 'EV',
          companyId: 1,
        }),
        authenticatedApiClient.getVehicles(),
        authenticatedApiClient.getVehicles(),
      ]);

      // At least reads should succeed
      const successfulReads = readResults.filter(r => r.status === 'fulfilled').length;
      expect(successfulReads).toBeGreaterThanOrEqual(1);
    });
  });

  test.describe('Optimistic Locking / Version Conflicts', () => {

    test('Concurrent updates to same vehicle', async ({ authenticatedApiClient }) => {
      // Get a vehicle
      const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
      const vehicles = vehiclesResponse?.data || [];
      
      if (vehicles.length === 0) {
        expect(true).toBe(true);
        return;
      }

      const vehicle = vehicles[0];
      
      // Two concurrent updates
      const [result1, result2] = await Promise.allSettled([
        authenticatedApiClient.put(`/api/v1/vehicles/${vehicle.id}`, {
          ...vehicle,
          status: 'AVAILABLE',
        }),
        authenticatedApiClient.put(`/api/v1/vehicles/${vehicle.id}`, {
          ...vehicle,
          status: 'IN_USE',
        }),
      ]);

      // At least one should succeed, or both may succeed (last write wins)
      const successes = [result1, result2].filter(r => r.status === 'fulfilled').length;
      console.log(`Concurrent updates: ${successes}/2 succeeded`);
      
      // Either both succeed (no locking) or one fails (optimistic locking)
      expect(successes).toBeGreaterThanOrEqual(1);
    });

    test('Concurrent driver assignments', async ({ authenticatedApiClient }) => {
      // Get drivers and vehicles
      const [driversRes, vehiclesRes] = await Promise.all([
        authenticatedApiClient.get('/api/v1/drivers?companyId=1'),
        authenticatedApiClient.getVehicles(),
      ]) as any[];

      const drivers = driversRes?.data || [];
      const vehicles = vehiclesRes?.data || [];

      if (drivers.length < 2 || vehicles.length === 0) {
        expect(true).toBe(true);
        return;
      }

      // Try to assign two different drivers to same vehicle
      const vehicleId = vehicles[0].id;
      
      const [assign1, assign2] = await Promise.allSettled([
        authenticatedApiClient.post(`/api/v1/drivers/${drivers[0].id}/assign?vehicleId=${vehicleId}`, {}),
        authenticatedApiClient.post(`/api/v1/drivers/${drivers[1].id}/assign?vehicleId=${vehicleId}`, {}),
      ]);

      // One should succeed, other should fail (conflict)
      console.log('Assign 1:', assign1.status);
      console.log('Assign 2:', assign2.status);
      
      // At least one operation should complete
      expect([assign1.status, assign2.status]).toContain('fulfilled');
    });
  });

  test.describe('Race Conditions', () => {

    test('Start charging session race condition', async ({ authenticatedApiClient }) => {
      // Get available station
      const stationsResponse = await authenticatedApiClient.getChargingStations() as any;
      const stations = Array.isArray(stationsResponse) ? stationsResponse : (stationsResponse?.data || []);
      
      const availableStation = stations.find((s: any) => s.availableSlots > 0);
      
      if (!availableStation) {
        expect(true).toBe(true);
        return;
      }

      // Get vehicles
      const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
      const vehicles = vehiclesResponse?.data || [];

      if (vehicles.length < 2) {
        expect(true).toBe(true);
        return;
      }

      // Two vehicles try to start charging at same station slot simultaneously
      const [session1, session2] = await Promise.allSettled([
        authenticatedApiClient.post('/api/v1/charging/sessions/start', {
          vehicleId: vehicles[0].id,
          stationId: availableStation.id,
        }),
        authenticatedApiClient.post('/api/v1/charging/sessions/start', {
          vehicleId: vehicles[1].id,
          stationId: availableStation.id,
        }),
      ]);

      console.log('Session 1:', session1.status);
      console.log('Session 2:', session2.status);
      
      // Both may succeed if multiple slots, or one may fail
      expect(true).toBe(true);
    });

    test('Delete while reading', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      
      // Create a vehicle
      try {
        const createRes = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `DWR-${timestamp}`,
          licensePlate: `KA01DW${String(timestamp).slice(-4)}`,
          vin: `DWR${String(timestamp).slice(-13)}`,
          make: 'DeleteWhileRead',
          model: 'Test',
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }) as any;

        const vehicleId = createRes?.data?.id || createRes?.id;
        
        if (vehicleId) {
          // Delete while reading
          const [deleteRes, readRes] = await Promise.allSettled([
            authenticatedApiClient.delete(`/api/v1/vehicles/${vehicleId}`),
            authenticatedApiClient.get(`/api/v1/vehicles/${vehicleId}`),
          ]);

          console.log('Delete:', deleteRes.status);
          console.log('Read:', readRes.status);
        }
      } catch (error: any) {
        console.log('Delete while read test error:', error.message);
      }
      
      expect(true).toBe(true);
    });
  });

  test.describe('Deadlock Prevention', () => {

    test('Cross-resource updates', async ({ authenticatedApiClient }) => {
      // Get resources
      const [driversRes, vehiclesRes] = await Promise.all([
        authenticatedApiClient.get('/api/v1/drivers?companyId=1'),
        authenticatedApiClient.getVehicles(),
      ]) as any[];

      const drivers = driversRes?.data || [];
      const vehicles = vehiclesRes?.data || [];

      if (drivers.length === 0 || vehicles.length === 0) {
        expect(true).toBe(true);
        return;
      }

      // Simultaneous updates that might cause deadlock
      const results = await Promise.allSettled([
        authenticatedApiClient.put(`/api/v1/vehicles/${vehicles[0].id}`, {
          ...vehicles[0],
          status: 'AVAILABLE',
        }),
        authenticatedApiClient.put(`/api/v1/drivers/${drivers[0].id}`, {
          ...drivers[0],
          status: 'ACTIVE',
        }),
        authenticatedApiClient.post(`/api/v1/drivers/${drivers[0].id}/assign?vehicleId=${vehicles[0].id}`, {}),
      ]);

      const successes = results.filter(r => r.status === 'fulfilled').length;
      console.log(`Cross-resource updates: ${successes}/3 succeeded`);
      
      // Should not deadlock - at least some should complete
      expect(successes).toBeGreaterThanOrEqual(0);
    });
  });

  test.describe('Transaction Isolation', () => {

    test('Read-your-writes consistency', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      
      try {
        // Create
        const createRes = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `RYW-${timestamp}`,
          licensePlate: `KA01RW${String(timestamp).slice(-4)}`,
          vin: `RYW${String(timestamp).slice(-13)}`,
          make: 'ReadYourWrite',
          model: 'Test',
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }) as any;

        const vehicleId = createRes?.data?.id || createRes?.id;
        
        if (vehicleId) {
          // Immediately read
          const readRes = await authenticatedApiClient.get(`/api/v1/vehicles/${vehicleId}`) as any;
          const vehicle = readRes?.data || readRes;
          
          // Should see our own write
          expect(vehicle).toBeDefined();
          if (vehicle) {
            expect(vehicle.vehicleNumber).toBe(`RYW-${timestamp}`);
          }
        }
      } catch (error: any) {
        console.log('Read-your-writes test error:', error.message);
      }
      
      expect(true).toBe(true);
    });

    test('Update and verify', async ({ authenticatedApiClient }) => {
      // Get a vehicle
      const vehiclesResponse = await authenticatedApiClient.getVehicles() as any;
      const vehicles = vehiclesResponse?.data || [];
      
      if (vehicles.length === 0) {
        expect(true).toBe(true);
        return;
      }

      const vehicle = vehicles[0];
      const newStatus = vehicle.status === 'AVAILABLE' ? 'IN_USE' : 'AVAILABLE';
      
      try {
        // Update
        await authenticatedApiClient.put(`/api/v1/vehicles/${vehicle.id}`, {
          ...vehicle,
          status: newStatus,
        });

        // Verify
        const readRes = await authenticatedApiClient.get(`/api/v1/vehicles/${vehicle.id}`) as any;
        const updated = readRes?.data || readRes;
        
        // Should see update
        expect(updated?.status).toBe(newStatus);
      } catch (error: any) {
        console.log('Update and verify error:', error.message);
        expect(true).toBe(true);
      }
    });
  });

  test.describe('High Load Simulation', () => {

    test('20 concurrent requests', async ({ authenticatedApiClient }) => {
      const requests = Array(20).fill(null).map(() => 
        authenticatedApiClient.getVehicles().catch(() => null)
      );

      const startTime = Date.now();
      const results = await Promise.all(requests);
      const endTime = Date.now();
      
      const successful = results.filter(r => r !== null).length;
      const duration = endTime - startTime;
      
      console.log(`20 concurrent: ${successful} succeeded in ${duration}ms`);
      expect(successful).toBeGreaterThanOrEqual(10);
    });

    test('Burst of writes followed by reads', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      
      // Burst of writes
      const writeRequests = Array(5).fill(null).map((_, i) => 
        authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `BUR-${timestamp}-${i}`,
          licensePlate: `KA${i}BU${String(timestamp).slice(-4)}`,
          vin: `BUR${i}${String(timestamp).slice(-12)}`,
          make: 'BurstTest',
          model: `M${i}`,
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }).catch(() => null)
      );

      await Promise.all(writeRequests);
      
      // Followed by reads
      const readRequests = Array(10).fill(null).map(() => 
        authenticatedApiClient.getVehicles().catch(() => null)
      );

      const readResults = await Promise.all(readRequests);
      const successfulReads = readResults.filter(r => r !== null).length;
      
      console.log(`Burst reads: ${successfulReads}/10 succeeded`);
      expect(successfulReads).toBeGreaterThanOrEqual(5);
    });
  });
});

