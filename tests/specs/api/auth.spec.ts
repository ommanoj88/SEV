import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';

/**
 * Auth API Tests
 * 
 * Note: This backend uses Firebase Authentication. 
 * The login endpoint requires firebaseUid and firebaseToken.
 * These tests validate the API structure and error handling.
 */

test.describe('Login API Tests', () => {
  
  test('POST /api/v1/auth/login - requires firebaseUid and firebaseToken', async ({ request }) => {
    // Test that login requires Firebase credentials
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { email: TEST_USER.email, password: TEST_USER.password },
    });
    
    // Should return 400 because firebaseUid and firebaseToken are required
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.validationErrors).toBeDefined();
  });

  test('POST /api/v1/auth/login - empty request body', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: {},
    });
    
    expect(response.status()).toBe(400);
  });

  test('POST /api/v1/auth/login - missing firebaseToken', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { firebaseUid: 'test-uid-123' },
    });
    
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.validationErrors?.firebaseToken).toBeDefined();
  });

  test('POST /api/v1/auth/login - missing firebaseUid', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { firebaseToken: 'test-token-123' },
    });
    
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.validationErrors?.firebaseUid).toBeDefined();
  });

  test('POST /api/v1/auth/login - invalid Firebase token returns error', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { 
        firebaseUid: 'invalid-uid-123',
        firebaseToken: 'invalid-token-xyz'
      },
    });
    
    // Should return 401 (invalid token) or 400 (bad request) or 500 (Firebase error)
    expect([400, 401, 500]).toContain(response.status());
  });
});

test.describe('Registration API Tests', () => {

  test('POST /api/v1/auth/register - endpoint exists', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {},
    });
    
    // Should return 400 (validation error) not 404
    expect(response.status()).not.toBe(404);
  });

  test('POST /api/v1/auth/register - requires email', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {
        name: 'Test User',
        phone: '+91-9876543000',
      },
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  test('POST /api/v1/auth/register - invalid email format rejected', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {
        email: 'not-an-email',
        name: 'Test User',
        firebaseUid: 'test-uid',
        firebaseToken: 'test-token',
      },
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });
});

test.describe('Auth Users Endpoint Tests', () => {

  test('GET /api/v1/auth/users - list all users', async ({ request }) => {
    const response = await request.get('http://localhost:8080/api/v1/auth/users');
    
    // Should return 200 (permitted in dev) or 401/403 if secured
    expect([200, 401, 403]).toContain(response.status());
    
    if (response.status() === 200) {
      const users = await response.json();
      expect(Array.isArray(users)).toBe(true);
    }
  });

  test('GET /api/v1/auth/users/{id} - get user by ID', async ({ request }) => {
    const response = await request.get('http://localhost:8080/api/v1/auth/users/1');
    
    // Should return 200 (if user exists) or 404 (if not) or 401/403 (if secured)
    expect([200, 404, 401, 403]).toContain(response.status());
  });

  test('GET /api/v1/auth/users/{id} - non-existent user returns 404', async ({ request }) => {
    const response = await request.get('http://localhost:8080/api/v1/auth/users/999999');
    
    // Should return 404 for non-existent user
    expect([404, 401, 403]).toContain(response.status());
  });
});

test.describe('Health & Actuator Endpoints', () => {

  test('GET /actuator/health - health check', async ({ request }) => {
    const response = await request.get('http://localhost:8080/actuator/health');
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.status).toBe('UP');
  });

  test('GET /actuator/info - app info', async ({ request }) => {
    const response = await request.get('http://localhost:8080/actuator/info');
    
    // Info endpoint may or may not be enabled
    expect([200, 404]).toContain(response.status());
  });
});

test.describe('API Documentation', () => {

  test('GET /swagger-ui.html - Swagger UI accessible', async ({ request }) => {
    const response = await request.get('http://localhost:8080/swagger-ui.html');
    
    // Should redirect or return the Swagger UI
    expect([200, 302]).toContain(response.status());
  });

  test('GET /v3/api-docs - OpenAPI spec accessible', async ({ request }) => {
    const response = await request.get('http://localhost:8080/v3/api-docs');
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.openapi).toBeDefined();
    expect(body.info).toBeDefined();
  });
});
