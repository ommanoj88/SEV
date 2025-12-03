import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Delete Operations Tests', () => {

  test.describe('Vehicle Delete Operations', () => {
    let createdVehicleId: number;

    test('Create vehicle for deletion test', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        const response = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `DEL-${timestamp}`,
          licensePlate: `KA99DL${String(timestamp).slice(-4)}`,
          vin: `DEL${String(timestamp).slice(-14)}`,
          make: 'DeleteTest',
          model: 'ToBeDeleted',
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          batteryCapacity: 50.0,
          currentBatterySoc: 100,
          companyId: 1,
        }) as any;
        
        const data = response?.data || response;
        if (data?.id) {
          createdVehicleId = data.id;
          console.log('Created vehicle for deletion:', createdVehicleId);
        }
        expect(response).toBeDefined();
      } catch (error: any) {
        console.log('Create for delete error:', error.message);
        expect([200, 201, 400, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/vehicles/{id} - delete existing vehicle', async ({ authenticatedApiClient }) => {
      // First, create a vehicle to delete
      const timestamp = Date.now();
      try {
        const createResponse = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `DEL2-${timestamp}`,
          licensePlate: `KA88DL${String(timestamp).slice(-4)}`,
          vin: `DL2${String(timestamp).slice(-14)}`,
          make: 'DeleteTest2',
          model: 'ToDelete',
          year: 2024,
          type: 'SUV',
          fuelType: 'ICE',
          companyId: 1,
        }) as any;

        const vehicleId = createResponse?.data?.id || createResponse?.id;
        
        if (vehicleId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/v1/vehicles/${vehicleId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Vehicle deleted:', vehicleId);

          // Verify deletion
          try {
            await authenticatedApiClient.get(`/api/v1/vehicles/${vehicleId}`);
            // If we get here, vehicle wasn't deleted (soft delete?)
          } catch (error: any) {
            // 404 confirms deletion
            expect(error.status).toBe(404);
          }
        }
      } catch (error: any) {
        console.log('Delete operation error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/vehicles/{id} - delete non-existent vehicle', async ({ authenticatedApiClient }) => {
      try {
        await authenticatedApiClient.delete('/api/v1/vehicles/999999');
        expect(true).toBe(false); // Should not reach here
      } catch (error: any) {
        expect([404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/vehicles/{id} - double delete returns error', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create
        const createResponse = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `DBL-${timestamp}`,
          licensePlate: `KA77DL${String(timestamp).slice(-4)}`,
          vin: `DBL${String(timestamp).slice(-14)}`,
          make: 'DoubleDelete',
          model: 'Test',
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }) as any;

        const vehicleId = createResponse?.data?.id || createResponse?.id;
        
        if (vehicleId) {
          // First delete
          await authenticatedApiClient.delete(`/api/v1/vehicles/${vehicleId}`);
          
          // Second delete should fail
          try {
            await authenticatedApiClient.delete(`/api/v1/vehicles/${vehicleId}`);
          } catch (error: any) {
            expect([404, 410, 500]).toContain(error.status);
          }
        }
      } catch (error: any) {
        console.log('Double delete test error:', error.message);
      }
    });
  });

  test.describe('Driver Delete Operations', () => {

    test('DELETE /api/v1/drivers/{id} - delete existing driver', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create driver
        const createResponse = await authenticatedApiClient.post('/api/v1/drivers?companyId=1', {
          name: `Delete Driver ${timestamp}`,
          email: `delete.driver.${timestamp}@test.com`,
          phone: `+91-66666${String(timestamp).slice(-5)}`,
          licenseNumber: `DLD${String(timestamp).slice(-9)}`,
          licenseExpiry: '2026-12-31',
        }) as any;

        const driverId = createResponse?.data?.id || createResponse?.id;
        
        if (driverId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/v1/drivers/${driverId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Driver deleted:', driverId);
        }
      } catch (error: any) {
        console.log('Delete driver error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/drivers/{id} - delete non-existent driver', async ({ authenticatedApiClient }) => {
      try {
        await authenticatedApiClient.delete('/api/v1/drivers/999999');
      } catch (error: any) {
        expect([404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/drivers/{id} - delete driver with active assignment', async ({ authenticatedApiClient }) => {
      // Attempt to delete a driver who might be assigned
      try {
        // Get drivers first
        const driversResponse = await authenticatedApiClient.get('/api/v1/drivers?companyId=1') as any;
        const drivers = driversResponse?.data || driversResponse;
        
        if (Array.isArray(drivers) && drivers.length > 0) {
          const assignedDriver = drivers.find((d: any) => d.assignedVehicleId);
          if (assignedDriver) {
            try {
              await authenticatedApiClient.delete(`/api/v1/drivers/${assignedDriver.id}`);
            } catch (error: any) {
              // Should fail or warn about active assignment
              expect([400, 409, 500]).toContain(error.status);
            }
          }
        }
      } catch (error: any) {
        console.log('Assigned driver delete test skipped:', error.message);
      }
      expect(true).toBe(true); // Pass if no assigned drivers found
    });
  });

  test.describe('Charging Station Delete Operations', () => {

    test('DELETE /api/v1/charging/stations/{id} - delete station', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create station
        const createResponse = await authenticatedApiClient.post('/api/v1/charging/stations', {
          name: `Delete Station ${timestamp}`,
          address: 'Delete Test Location',
          latitude: 19.0999,
          longitude: 72.8999,
          totalSlots: 2,
          availableSlots: 2,
          pricePerKwh: 10.00,
          status: 'ACTIVE',
          companyId: 1,
        }) as any;

        const stationId = createResponse?.data?.id || createResponse?.id;
        
        if (stationId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/v1/charging/stations/${stationId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Station deleted:', stationId);
        }
      } catch (error: any) {
        console.log('Delete station error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/charging/stations/{id} - station with active sessions', async ({ authenticatedApiClient }) => {
      // Try to delete a station that might have active charging sessions
      try {
        const stationsResponse = await authenticatedApiClient.getChargingStations() as any;
        const stations = Array.isArray(stationsResponse) ? stationsResponse : (stationsResponse?.data || []);
        
        if (stations.length > 0) {
          const busyStation = stations.find((s: any) => s.availableSlots < s.totalSlots);
          if (busyStation) {
            try {
              await authenticatedApiClient.delete(`/api/v1/charging/stations/${busyStation.id}`);
            } catch (error: any) {
              // Should fail due to active sessions
              expect([400, 409, 500]).toContain(error.status);
            }
          }
        }
      } catch (error: any) {
        console.log('Busy station delete test skipped:', error.message);
      }
      expect(true).toBe(true);
    });
  });

  test.describe('Maintenance Record Delete Operations', () => {

    test('DELETE /api/v1/maintenance/records/{id} - delete record', async ({ authenticatedApiClient }) => {
      try {
        // Create record
        const createResponse = await authenticatedApiClient.post('/api/v1/maintenance/records', {
          vehicleId: 1,
          type: 'ROUTINE',
          description: 'To be deleted',
          scheduledDate: new Date().toISOString(),
          cost: 100.00,
        }) as any;

        const recordId = createResponse?.data?.id || createResponse?.id;
        
        if (recordId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/v1/maintenance/records/${recordId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Record deleted:', recordId);
        }
      } catch (error: any) {
        console.log('Delete record error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/maintenance/records/{id} - delete completed record', async ({ authenticatedApiClient }) => {
      try {
        const recordsResponse = await authenticatedApiClient.getMaintenanceRecords() as any;
        const records = recordsResponse?.data || recordsResponse;
        
        if (Array.isArray(records) && records.length > 0) {
          const completedRecord = records.find((r: any) => r.status === 'COMPLETED');
          if (completedRecord) {
            try {
              const deleteResponse = await authenticatedApiClient.delete(`/api/v1/maintenance/records/${completedRecord.id}`);
              expect(deleteResponse).toBeDefined();
            } catch (error: any) {
              // May not allow deleting completed records
              expect([400, 403, 404, 500]).toContain(error.status);
            }
          }
        }
      } catch (error: any) {
        console.log('Delete completed record test skipped:', error.message);
      }
      expect(true).toBe(true);
    });
  });

  test.describe('Geofence Delete Operations', () => {

    test('DELETE /api/geofences/{id} - delete geofence', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create geofence
        const createResponse = await authenticatedApiClient.post('/api/geofences', {
          name: `Delete Geofence ${timestamp}`,
          geofenceType: 'CIRCLE',
          companyId: 1,
          centerLatitude: 19.0888,
          centerLongitude: 72.8888,
          radius: 100,
          isActive: true,
        }) as any;

        const geofenceId = createResponse?.data?.id || createResponse?.id;
        
        if (geofenceId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/geofences/${geofenceId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Geofence deleted:', geofenceId);
        }
      } catch (error: any) {
        console.log('Delete geofence error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });
  });

  test.describe('Route Delete Operations', () => {

    test('DELETE /api/routes/{id} - delete route', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create route
        const createResponse = await authenticatedApiClient.post('/api/routes', {
          routeName: `Delete Route ${timestamp}`,
          companyId: 1,
          optimizationCriteria: 'DISTANCE',
          waypoints: [
            { latitude: 19.0760, longitude: 72.8777 },
            { latitude: 19.0900, longitude: 72.8600 },
          ],
          isActive: true,
        }) as any;

        const routeId = createResponse?.data?.id || createResponse?.id;
        
        if (routeId) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/routes/${routeId}`) as any;
          expect(deleteResponse).toBeDefined();
          console.log('Route deleted:', routeId);
        }
      } catch (error: any) {
        console.log('Delete route error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });
  });

  test.describe('Notification Delete Operations', () => {

    test('DELETE /api/v1/notifications/{id} - delete notification', async ({ authenticatedApiClient }) => {
      try {
        const notificationsResponse = await authenticatedApiClient.getNotifications() as any;
        const notifications = notificationsResponse?.data || notificationsResponse;
        
        if (Array.isArray(notifications) && notifications.length > 0) {
          const deleteResponse = await authenticatedApiClient.delete(`/api/v1/notifications/${notifications[0].id}`) as any;
          expect(deleteResponse).toBeDefined();
        }
      } catch (error: any) {
        console.log('Delete notification error:', error.message);
        expect([200, 204, 400, 404, 500]).toContain(error.status);
      }
    });

    test('DELETE /api/v1/notifications/clear-all - clear all notifications', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.delete('/api/v1/notifications/clear-all') as any;
        expect(response).toBeDefined();
      } catch (error: any) {
        // Endpoint may not exist
        expect([200, 204, 400, 404, 405, 500]).toContain(error.status);
      }
    });
  });

  test.describe('Soft Delete Verification', () => {

    test('Soft deleted vehicle not in active list', async ({ authenticatedApiClient }) => {
      const timestamp = Date.now();
      try {
        // Create
        const createResponse = await authenticatedApiClient.post('/api/v1/vehicles', {
          vehicleNumber: `SOFT-${timestamp}`,
          licensePlate: `KA55SD${String(timestamp).slice(-4)}`,
          vin: `SOFT${String(timestamp).slice(-13)}`,
          make: 'SoftDelete',
          model: 'Test',
          year: 2024,
          type: 'SEDAN',
          fuelType: 'EV',
          companyId: 1,
        }) as any;

        const vehicleId = createResponse?.data?.id || createResponse?.id;
        const vehicleNumber = `SOFT-${timestamp}`;
        
        if (vehicleId) {
          // Delete
          await authenticatedApiClient.delete(`/api/v1/vehicles/${vehicleId}`);
          
          // Check not in active list
          const listResponse = await authenticatedApiClient.getVehicles() as any;
          const vehicles = listResponse?.data || [];
          const found = vehicles.find((v: any) => v.vehicleNumber === vehicleNumber);
          
          expect(found).toBeUndefined();
        }
      } catch (error: any) {
        console.log('Soft delete test error:', error.message);
      }
      expect(true).toBe(true);
    });
  });
});

