import { test as base } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import { ApiClient } from '../utils/ApiClient';

// Test data
export const TEST_USER = {
  email: process.env.TEST_USER_EMAIL || 'testuser1@gmail.com',
  password: process.env.TEST_USER_PASSWORD || 'Password@123',
  companyId: 1,
};

export const TEST_ADMIN = {
  email: 'testadmin@gmail.com',
  password: 'Password@123',
  companyId: 1,
};

export const TEST_DRIVER = {
  email: 'testdriver@gmail.com',
  password: 'Password@123',
  companyId: 1,
};

// Extended test fixtures
type TestFixtures = {
  loginPage: LoginPage;
  dashboardPage: DashboardPage;
  apiClient: ApiClient;
  authenticatedApiClient: ApiClient;
};

export const test = base.extend<TestFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },

  dashboardPage: async ({ page }, use) => {
    await use(new DashboardPage(page));
  },

  apiClient: async ({ request }, use) => {
    const client = new ApiClient(request);
    await use(client);
  },

  // In dev mode, API endpoints are permitAll so no auth needed
  // For production, would need Firebase token
  authenticatedApiClient: async ({ request }, use) => {
    const client = new ApiClient(request);
    // Skip login - dev mode allows unauthenticated access
    // await client.login(TEST_USER.email, TEST_USER.password);
    await use(client);
  },
});

export { expect } from '@playwright/test';
