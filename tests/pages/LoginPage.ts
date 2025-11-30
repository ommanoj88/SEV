import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class LoginPage extends BasePage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly rememberMeCheckbox: Locator;
  readonly forgotPasswordLink: Locator;
  readonly registerLink: Locator;
  readonly errorAlert: Locator;

  constructor(page: Page) {
    super(page);
    this.emailInput = page.locator('input[type="email"], input[name="email"]');
    this.passwordInput = page.locator('input[type="password"], input[name="password"]');
    this.loginButton = page.getByRole('button', { name: /login|sign in/i });
    this.rememberMeCheckbox = page.locator('input[type="checkbox"]');
    this.forgotPasswordLink = page.getByText(/forgot password/i);
    this.registerLink = page.getByText(/register|sign up/i);
    this.errorAlert = page.locator('.MuiAlert-root, .error-message');
  }

  async goto() {
    await this.page.goto('/login');
    await this.waitForPageLoad();
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
    await this.waitForSpinnerToDisappear();
  }

  async loginWithRememberMe(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.rememberMeCheckbox.check();
    await this.loginButton.click();
    await this.waitForSpinnerToDisappear();
  }

  async getErrorText(): Promise<string | null> {
    const isVisible = await this.errorAlert.isVisible();
    if (isVisible) {
      return await this.errorAlert.textContent();
    }
    return null;
  }

  async expectErrorMessage(message: string) {
    await expect(this.errorAlert).toContainText(message);
  }

  async expectLoginSuccess() {
    await expect(this.page).toHaveURL(/dashboard|home/);
  }
}
