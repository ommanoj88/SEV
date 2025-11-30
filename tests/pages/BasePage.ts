import { Page, Locator } from '@playwright/test';

export class BasePage {
  readonly page: Page;
  readonly loadingSpinner: Locator;
  readonly toastMessage: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.loadingSpinner = page.locator('.loading-spinner, .MuiCircularProgress-root');
    this.toastMessage = page.locator('.Toastify__toast, .MuiSnackbar-root');
    this.errorMessage = page.locator('.error-message, .MuiAlert-root');
  }

  async waitForPageLoad() {
    await this.page.waitForLoadState('networkidle');
    await this.loadingSpinner.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
  }

  async waitForSpinnerToDisappear() {
    await this.loadingSpinner.waitFor({ state: 'hidden', timeout: 30000 }).catch(() => {});
  }

  async getToastMessage(): Promise<string> {
    await this.toastMessage.waitFor({ state: 'visible', timeout: 5000 });
    return this.toastMessage.textContent() || '';
  }

  async getErrorMessage(): Promise<string | null> {
    const isVisible = await this.errorMessage.isVisible();
    if (isVisible) {
      return this.errorMessage.textContent();
    }
    return null;
  }

  async clickButton(text: string) {
    await this.page.getByRole('button', { name: text }).click();
  }

  async fillInput(label: string, value: string) {
    await this.page.getByLabel(label).fill(value);
  }

  async selectOption(label: string, value: string) {
    await this.page.getByLabel(label).selectOption(value);
  }

  async navigateTo(path: string) {
    await this.page.goto(path);
    await this.waitForPageLoad();
  }

  async screenshot(name: string) {
    await this.page.screenshot({ path: `reports/screenshots/${name}.png`, fullPage: true });
  }

  async logAction(action: string) {
    console.log(`[${new Date().toISOString()}] ${action}`);
  }
}
