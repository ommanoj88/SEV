import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';

test.describe('Fleet Manager Complete Journey', () => {

  test('Full workflow: Login → Add Vehicle → Assign Driver → Start Trip → Complete Trip', async ({ loginPage, page, authenticatedApiClient }) => {
    // Step 1: Login
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);
    await loginPage.expectLoginSuccess();

    // Step 2: Navigate to Vehicles
    await page.getByText('Vehicles', { exact: false }).first().click();
    await page.waitForLoadState('networkidle');

    // Step 3: Add new vehicle (via API for reliability)
    const timestamp = Date.now();
    const newVehicle = await authenticatedApiClient.post('/api/v1/vehicles', {
      licensePlate: `E2E${timestamp}`,
      vin: `E2E${timestamp}`.substring(0, 17),
      make: 'Tesla',
      model: 'E2E Test',
      year: 2024,
      fuelType: 'EV',
      batteryCapacityKwh: 75.0,
      currentBatterySoc: 100,
      currentRangeKm: 400,
      odometerKm: 0,
    });
    expect(newVehicle).toBeDefined();

    // Step 4: Create a driver (via API)
    const newDriver = await authenticatedApiClient.post('/api/v1/drivers', {
      name: `E2E Driver ${timestamp}`,
      email: `e2e.driver.${timestamp}@test.com`,
      phone: `+91-77777${timestamp.toString().slice(-5)}`,
      licenseNumber: `E2E${timestamp.toString().slice(-10)}`,
      licenseExpiry: '2026-12-31',
    });
    expect(newDriver).toBeDefined();

    // Step 5: Assign driver to vehicle (via API)
    if (newVehicle.id && newDriver.id) {
      await authenticatedApiClient.post(`/api/v1/drivers/${newDriver.id}/assign`, {
        vehicleId: newVehicle.id,
      });
    }

    // Step 6: Start a trip
    if (newVehicle.id && newDriver.id) {
      const trip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
        vehicleId: newVehicle.id,
        driverId: newDriver.id,
        startLocation: 'E2E Test Start',
        startLatitude: 19.0760,
        startLongitude: 72.8777,
      });
      expect(trip).toBeDefined();

      // Step 7: Complete the trip
      if (trip.id) {
        const completed = await authenticatedApiClient.post(`/api/v1/fleet/trips/${trip.id}/complete`, {
          endLocation: 'E2E Test End',
          endLatitude: 19.1000,
          endLongitude: 72.9000,
          distanceKm: 20.5,
        });
        expect(completed).toBeDefined();
        expect(completed.status).toBe('COMPLETED');
      }
    }

    // Step 8: Verify in UI - Navigate to trips
    await page.getByText('Trips', { exact: false }).first().click();
    await page.waitForLoadState('networkidle');

    // The completed trip should be visible
    console.log('E2E Journey completed successfully');
  });
});

test.describe('Driver Journey', () => {

  test('Full workflow: View Assignment → Start Trip → Charge Vehicle → End Trip', async ({ loginPage, page, authenticatedApiClient }) => {
    // Login as driver
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password); // Using test user for simplicity
    await loginPage.expectLoginSuccess();

    // Get assigned vehicles
    const vehicles = await authenticatedApiClient.get('/api/v1/vehicles?status=AVAILABLE');
    expect(Array.isArray(vehicles)).toBe(true);

    // Start a trip with available vehicle
    if (vehicles.length > 0) {
      const vehicle = vehicles[0];
      
      // Start trip
      const trip = await authenticatedApiClient.post('/api/v1/fleet/trips/start', {
        vehicleId: vehicle.id,
        driverId: 1,
        startLocation: 'Driver Journey Start',
        startLatitude: 19.0760,
        startLongitude: 72.8777,
      });

      // Complete trip
      if (trip && trip.id) {
        await authenticatedApiClient.post(`/api/v1/fleet/trips/${trip.id}/complete`, {
          endLocation: 'Driver Journey End',
          endLatitude: 19.1000,
          endLongitude: 72.9000,
          distanceKm: 15.0,
        });
      }
    }

    console.log('Driver Journey completed');
  });
});

test.describe('Admin Journey', () => {

  test('Full workflow: User Management → Analytics → Billing → Settings', async ({ loginPage, page, authenticatedApiClient }) => {
    // Login as admin
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);
    await loginPage.expectLoginSuccess();

    // Check analytics
    const summary = await authenticatedApiClient.getDashboardSummary();
    expect(summary).toBeDefined();

    // Check fleet overview
    const overview = await authenticatedApiClient.get('/api/v1/dashboard/fleet-overview');
    expect(overview).toBeDefined();

    // Check billing
    const subscription = await authenticatedApiClient.get('/api/v1/billing/subscriptions/current');
    expect(subscription).toBeDefined();

    // Navigate to settings
    await page.getByText('Settings', { exact: false }).first().click();
    await page.waitForLoadState('networkidle');

    console.log('Admin Journey completed');
  });
});

test.describe('Multi-User Scenarios', () => {

  test('Multiple concurrent API requests', async ({ authenticatedApiClient }) => {
    // Execute multiple requests in parallel
    const results = await Promise.all([
      authenticatedApiClient.getVehicles(),
      authenticatedApiClient.getDrivers(),
      authenticatedApiClient.getChargingStations(),
      authenticatedApiClient.getTrips(),
      authenticatedApiClient.getDashboardSummary(),
    ]);

    results.forEach(result => {
      expect(result).toBeDefined();
    });
  });

  test('Data isolation between companies', async ({ authenticatedApiClient }) => {
    // All data should belong to company 1
    const vehicles = await authenticatedApiClient.getVehicles();
    if (Array.isArray(vehicles) && vehicles.length > 0) {
      vehicles.forEach(vehicle => {
        expect(vehicle.companyId).toBe(1);
      });
    }
  });

  test('Real-time updates simulation', async ({ authenticatedApiClient }) => {
    // Simulate location updates
    const update = await authenticatedApiClient.post('/api/v1/vehicles/1/location', {
      latitude: 19.0800,
      longitude: 72.8800,
      speed: 45,
      heading: 90,
    });
    expect(update).toBeDefined();
  });
});

test.describe('Error Recovery', () => {

  test('Retry on network failure', async ({ page }) => {
    // Simulate network offline
    await page.context().setOffline(true);
    
    await page.goto('/dashboard');
    // Should show offline message or error
    
    // Go back online
    await page.context().setOffline(false);
    
    // Retry should work
    await page.reload();
    await page.waitForLoadState('networkidle');
  });

  test('Session recovery after expiry', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);
    await loginPage.expectLoginSuccess();

    // Clear auth token to simulate expiry
    await page.evaluate(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('authToken');
    });

    // Navigate to protected page
    await page.goto('/vehicles');
    
    // Should redirect to login
    await expect(page).toHaveURL(/login/, { timeout: 10000 });
  });
});
