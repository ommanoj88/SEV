import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';

// Load test environment variables
dotenv.config({ path: path.resolve(__dirname, '.env.test') });

export default defineConfig({
  testDir: './specs',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'reports/html' }],
    ['json', { outputFile: 'reports/results.json' }],
    ['list'],
  ],
  
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure', // Keep videos for failed tests
    actionTimeout: 10000,
    navigationTimeout: 30000,
  },

  projects: [
    // Setup project for authentication
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
    // UI tests - uses browser with authentication - RECORDS VIDEO
    {
      name: 'ui',
      testMatch: /ui\/.*\.spec\.ts/,
      use: { 
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
        video: 'on', // Always record video for UI tests
        viewport: { width: 1280, height: 720 },
      },
      dependencies: ['setup'],
    },
    // E2E tests - full user journeys - RECORDS VIDEO
    {
      name: 'e2e',
      testMatch: /e2e\/.*\.spec\.ts/,
      use: { 
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
        video: 'on', // Always record video for E2E tests
        viewport: { width: 1280, height: 720 },
      },
      dependencies: ['setup'],
    },
    {
      name: 'api',
      testMatch: /api\/.*\.spec\.ts/,
      use: { baseURL: process.env.API_URL || 'http://localhost:8080' },
    },
  ],

  // WebServer is optional - only used for UI tests, not API tests
  // Backend should be started separately before running tests
  // webServer: {
  //   command: 'cd ../frontend && npm start',
  //   url: 'http://localhost:3000',
  //   reuseExistingServer: !process.env.CI,
  //   timeout: 120000,
  // },
});
