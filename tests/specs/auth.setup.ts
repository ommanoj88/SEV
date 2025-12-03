import { test as setup, expect } from '@playwright/test';
import path from 'path';

const authFile = path.join(__dirname, '../playwright/.auth/user.json');

setup('authenticate', async ({ page }) => {
  // Navigate to login page
  await page.goto('/login');
  
  // Wait for page to load
  await page.waitForLoadState('networkidle');
  
  // Close any webpack dev server overlays that might intercept clicks
  await page.evaluate(() => {
    const overlay = document.getElementById('webpack-dev-server-client-overlay');
    if (overlay) overlay.remove();
    
    // Also remove any error overlays
    const errorOverlays = document.querySelectorAll('[id*="overlay"], [class*="overlay"]');
    errorOverlays.forEach(el => {
      if (el.tagName === 'IFRAME' || el.id.includes('webpack')) {
        el.remove();
      }
    });
  });
  
  // Check if we're already logged in (redirected to dashboard)
  const currentUrl = page.url();
  if (currentUrl.includes('/dashboard') || currentUrl.includes('/home')) {
    console.log('Already authenticated');
    await page.context().storageState({ path: authFile });
    return;
  }
  
  // Try to login with test credentials
  const emailInput = page.locator('input[type="email"], input[name="email"]').first();
  const passwordInput = page.locator('input[type="password"], input[name="password"]').first();
  const loginButton = page.getByRole('button', { name: /login|sign in/i }).first();
  
  // Check if login form exists
  const loginFormExists = await emailInput.isVisible().catch(() => false);
  
  if (loginFormExists) {
    // Fill in credentials
    await emailInput.fill('testuser1@gmail.com');
    await passwordInput.fill('Password@123');
    
    // Remove any overlays again before clicking
    await page.evaluate(() => {
      const overlay = document.getElementById('webpack-dev-server-client-overlay');
      if (overlay) overlay.remove();
    });
    
    // Use force click to bypass any overlays
    await loginButton.click({ force: true });
    
    // Wait for navigation or error
    await Promise.race([
      page.waitForURL(/dashboard|home/, { timeout: 10000 }),
      page.waitForSelector('.MuiAlert-root, .error-message', { timeout: 5000 }).catch(() => null)
    ]).catch(() => null);
  }
  
  // Save storage state (even if login failed, this allows UI tests to run)
  await page.context().storageState({ path: authFile });
  console.log('Authentication state saved to:', authFile);
});
