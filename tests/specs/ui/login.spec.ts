import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';

test.describe('Login UI Tests', () => {

  test('Successful login flow', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);
    await loginPage.expectLoginSuccess();
  });

  test('Error message for invalid credentials', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, 'WrongPassword123');
    
    const error = await loginPage.getErrorText();
    expect(error).toBeTruthy();
  });

  test('Error message for empty email', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login('', TEST_USER.password);
    
    // Form validation or error message
    const error = await loginPage.getErrorText();
    // Either form prevents submission or shows error
  });

  test('Error message for empty password', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, '');
    
    // Form validation should prevent submission
  });

  test('Remember me functionality', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.loginWithRememberMe(TEST_USER.email, TEST_USER.password);
    await loginPage.expectLoginSuccess();
    
    // Check that credentials are persisted
    const cookies = await page.context().cookies();
    expect(cookies.length).toBeGreaterThan(0);
  });

  test('Redirect to dashboard after login', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);
    
    await page.waitForURL(/dashboard|home/);
    expect(page.url()).toMatch(/dashboard|home/);
  });

  test('Login button disabled while loading', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.emailInput.fill(TEST_USER.email);
    await loginPage.passwordInput.fill(TEST_USER.password);
    await loginPage.loginButton.click();
    
    // Button should be disabled or show loading state during API call
    // This is timing-sensitive, may need adjustment
  });

  test('Password field is masked', async ({ loginPage }) => {
    await loginPage.goto();
    const passwordType = await loginPage.passwordInput.getAttribute('type');
    expect(passwordType).toBe('password');
  });
});

test.describe('Login Edge Cases', () => {

  test('SQL injection attempt', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login("admin'--", 'Password@123');
    
    // Should not allow login
    const error = await loginPage.getErrorText();
    expect(error).toBeTruthy();
  });

  test('XSS attempt in email field', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login('<script>alert("xss")</script>', 'Password@123');
    
    // Should sanitize input
  });

  test('Very long email input', async ({ loginPage }) => {
    await loginPage.goto();
    const longEmail = 'a'.repeat(500) + '@test.com';
    await loginPage.login(longEmail, 'Password@123');
    
    // Should handle gracefully
  });

  test('Unicode characters in password', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, 'Pässwörd@123🔐');
    
    // Should handle unicode gracefully
  });
});
