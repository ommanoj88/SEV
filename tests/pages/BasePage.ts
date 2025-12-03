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

  /**
   * Remove webpack dev server overlay that may intercept clicks
   */
  async removeDevOverlays() {
    await this.page.evaluate(() => {
      // Remove webpack dev server overlay
      const overlay = document.getElementById('webpack-dev-server-client-overlay');
      if (overlay) overlay.remove();
      
      // Remove any error overlays or iframes that might block interactions
      const iframes = document.querySelectorAll('iframe[id*="webpack"], iframe[src="about:blank"]');
      iframes.forEach(el => el.remove());
      
      // Remove any full-screen overlays
      const overlays = document.querySelectorAll('[id*="overlay"]:not([role])');
      overlays.forEach(el => {
        if ((el as HTMLElement).style.position === 'fixed' || 
            (el as HTMLElement).style.position === 'absolute') {
          el.remove();
        }
      });
    });
  }

  async waitForPageLoad() {
    await this.page.waitForLoadState('networkidle');
    await this.loadingSpinner.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
    // Remove any dev overlays after page load
    await this.removeDevOverlays();
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
