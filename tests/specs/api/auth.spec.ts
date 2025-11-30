import { test, expect, TEST_USER } from '../fixtures/test-fixtures';

test.describe('Login API Tests', () => {
  
  test('POST /api/v1/auth/login - valid credentials', async ({ apiClient }) => {
    const response = await apiClient.login(TEST_USER.email, TEST_USER.password);
    
    expect(response.token).toBeDefined();
    expect(response.user.email).toBe(TEST_USER.email);
    expect(response.user.companyId).toBe(TEST_USER.companyId);
  });

  test('POST /api/v1/auth/login - invalid password', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { email: TEST_USER.email, password: 'WrongPassword123' },
    });
    
    expect(response.status()).toBe(401);
  });

  test('POST /api/v1/auth/login - non-existent user', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { email: 'nonexistent@test.com', password: 'Password@123' },
    });
    
    expect(response.status()).toBe(401);
  });

  test('POST /api/v1/auth/login - empty credentials', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { email: '', password: '' },
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  test('POST /api/v1/auth/login - invalid email format', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/login', {
      data: { email: 'not-an-email', password: 'Password@123' },
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  test('Token validation - protected endpoint without token', async ({ request }) => {
    const response = await request.get('http://localhost:8080/api/v1/vehicles', {
      headers: { 'Content-Type': 'application/json' },
    });
    
    expect(response.status()).toBe(401);
  });

  test('Token validation - protected endpoint with valid token', async ({ authenticatedApiClient }) => {
    const vehicles = await authenticatedApiClient.getVehicles();
    expect(Array.isArray(vehicles) || typeof vehicles === 'object').toBe(true);
  });
});

test.describe('Registration API Tests', () => {

  test('POST /api/v1/auth/register - new user', async ({ request }) => {
    const timestamp = Date.now();
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {
        email: `test.user.${timestamp}@example.com`,
        password: 'Password@123',
        name: 'Test User',
        phone: '+91-9876543000',
        companyId: 1,
      },
    });
    
    // Could be 200 or 201 depending on implementation
    expect([200, 201]).toContain(response.status());
  });

  test('POST /api/v1/auth/register - duplicate email', async ({ request }) => {
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {
        email: TEST_USER.email,
        password: 'Password@123',
        name: 'Duplicate User',
        phone: '+91-9876543999',
        companyId: 1,
      },
    });
    
    expect([400, 409]).toContain(response.status());
  });

  test('POST /api/v1/auth/register - weak password', async ({ request }) => {
    const timestamp = Date.now();
    const response = await request.post('http://localhost:8080/api/v1/auth/register', {
      data: {
        email: `weak.pass.${timestamp}@example.com`,
        password: '123',
        name: 'Weak Password User',
        phone: '+91-9876543001',
        companyId: 1,
      },
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });
});

test.describe('Auth Session Tests', () => {

  test('GET /api/v1/auth/me - get current user', async ({ authenticatedApiClient }) => {
    const user = await authenticatedApiClient.get('/api/v1/auth/me');
    expect(user).toBeDefined();
  });

  test('Token should be JWT format', async ({ apiClient }) => {
    const response = await apiClient.login(TEST_USER.email, TEST_USER.password);
    const token = response.token;
    
    // JWT has 3 parts separated by dots
    const parts = token.split('.');
    expect(parts.length).toBe(3);
  });
});
