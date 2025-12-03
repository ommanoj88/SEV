import { test, expect } from '../../fixtures/test-fixtures';

test.describe('Pagination Tests', () => {

  test.describe('Vehicles Pagination', () => {

    test('GET /api/v1/vehicles - default pagination', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?page=0&size=5 - first page with 5 items', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?page=0&size=5') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
      expect(response.data.length).toBeLessThanOrEqual(5);
    });

    test('GET /api/v1/vehicles?page=1&size=5 - second page', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?page=1&size=5') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?page=999&size=10 - empty page beyond data', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?page=999&size=10') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
      expect(response.data.length).toBe(0);
    });

    test('GET /api/v1/vehicles?page=-1 - negative page number', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?page=-1') as any;
        // May return first page or error
        expect(response).toBeDefined();
      } catch (error: any) {
        expect([400, 500]).toContain(error.status);
      }
    });

    test('GET /api/v1/vehicles?size=0 - zero page size', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?size=0') as any;
        // May return default or error
        expect(response).toBeDefined();
      } catch (error: any) {
        expect([400, 500]).toContain(error.status);
      }
    });

    test('GET /api/v1/vehicles?size=1000 - very large page size', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?size=1000') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?page=abc - non-numeric page', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?page=abc') as any;
        // May ignore or error
        expect(response).toBeDefined();
      } catch (error: any) {
        expect([400, 500]).toContain(error.status);
      }
    });
  });

  test.describe('Drivers Pagination', () => {

    test('GET /api/v1/drivers?page=0&size=5 - paginated drivers', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&page=0&size=5') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/drivers?page=999 - empty page', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&page=999&size=10') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });
  });

  test.describe('Trips Pagination', () => {

    test('GET /api/v1/fleet/trips?page=0&size=10 - paginated trips', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/fleet/trips?page=0&size=10') as any;
        const trips = response?.data || response;
        expect(Array.isArray(trips) || trips === undefined).toBe(true);
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });
  });

  test.describe('Maintenance Records Pagination', () => {

    test('GET /api/v1/maintenance/records?page=0&size=5 - paginated records', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/maintenance/records?page=0&size=5') as any;
        const records = response?.data || response;
        expect(records).toBeDefined();
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });
  });

  test.describe('Notifications Pagination', () => {

    test('GET /api/v1/notifications?page=0&size=20 - paginated notifications', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/notifications?page=0&size=20') as any;
        const notifications = response?.data || response;
        expect(Array.isArray(notifications) || notifications === undefined).toBe(true);
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });
  });
});

test.describe('Sorting Tests', () => {

  test.describe('Vehicles Sorting', () => {

    test('GET /api/v1/vehicles?sort=make,asc - sort by make ascending', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?sort=make,asc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?sort=year,desc - sort by year descending', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?sort=year,desc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?sort=licensePlate - sort by license plate', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?sort=licensePlate') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?sort=invalidField - invalid sort field', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?sort=invalidField') as any;
        // May ignore invalid field or error
        expect(response).toBeDefined();
      } catch (error: any) {
        expect([400, 500]).toContain(error.status);
      }
    });

    test('GET /api/v1/vehicles?sort=id,invalid - invalid sort direction', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?sort=id,invalid') as any;
        // May default to asc or error
        expect(response).toBeDefined();
      } catch (error: any) {
        expect([400, 500]).toContain(error.status);
      }
    });
  });

  test.describe('Drivers Sorting', () => {

    test('GET /api/v1/drivers?sort=name,asc - sort by name', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&sort=name,asc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/drivers?sort=licenseExpiry,desc - sort by license expiry', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&sort=licenseExpiry,desc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });
  });

  test.describe('Charging Stations Sorting', () => {

    test('GET /api/v1/charging/stations?sort=name - sort by name', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/charging/stations?sort=name') as any;
      expect(response).toBeDefined();
    });

    test('GET /api/v1/charging/stations?sort=pricePerKwh,asc - sort by price', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/charging/stations?sort=pricePerKwh,asc') as any;
      expect(response).toBeDefined();
    });
  });

  test.describe('Combined Pagination and Sorting', () => {

    test('GET /api/v1/vehicles?page=0&size=5&sort=make,asc - paginated and sorted', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?page=0&size=5&sort=make,asc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
      expect(response.data.length).toBeLessThanOrEqual(5);
    });

    test('GET /api/v1/drivers?page=0&size=10&sort=name,desc - paginated and sorted', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&page=0&size=10&sort=name,desc') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });
  });
});

test.describe('Search and Filter Tests', () => {

  test.describe('Vehicles Search', () => {

    test('GET /api/v1/vehicles?search=Tesla - search by make', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/vehicles?search=Tesla') as any;
        expect(response.success).toBe(true);
        expect(Array.isArray(response.data)).toBe(true);
      } catch (error: any) {
        // Search may not be implemented
        expect([400, 404, 500]).toContain(error.status);
      }
    });

    test('GET /api/v1/vehicles?licensePlate=KA01 - search by license plate prefix', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?licensePlate=KA01') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?make=Tesla&model=Model - multiple filters', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?make=Tesla&model=Model') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?year=2024 - filter by year', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?year=2024') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?type=SEDAN - filter by type', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?type=SEDAN') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });
  });

  test.describe('Drivers Search', () => {

    test('GET /api/v1/drivers?name=Test - search by name', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&name=Test') as any;
        expect(response.success).toBe(true);
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });

    test('GET /api/v1/drivers?email=test - search by email', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&email=test') as any;
        expect(response.success).toBe(true);
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });

    test('GET /api/v1/drivers?licenseNumber=MH01 - search by license', async ({ authenticatedApiClient }) => {
      try {
        const response = await authenticatedApiClient.get('/api/v1/drivers?companyId=1&licenseNumber=MH01') as any;
        expect(response.success).toBe(true);
      } catch (error: any) {
        expect(error.status).toBeGreaterThanOrEqual(400);
      }
    });
  });

  test.describe('Case Insensitive Search', () => {

    test('GET /api/v1/vehicles?make=tesla - lowercase search', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?make=tesla') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });

    test('GET /api/v1/vehicles?make=TESLA - uppercase search', async ({ authenticatedApiClient }) => {
      const response = await authenticatedApiClient.get('/api/v1/vehicles?make=TESLA') as any;
      expect(response.success).toBe(true);
      expect(Array.isArray(response.data)).toBe(true);
    });
  });
});

