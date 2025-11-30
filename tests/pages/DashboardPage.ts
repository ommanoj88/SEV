import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class DashboardPage extends BasePage {
  readonly welcomeMessage: Locator;
  readonly totalVehiclesCard: Locator;
  readonly activeTripsCard: Locator;
  readonly chargingStatusCard: Locator;
  readonly maintenanceAlertsCard: Locator;
  readonly fleetOverviewChart: Locator;
  readonly recentActivitiesTable: Locator;
  readonly refreshButton: Locator;
  readonly sideNav: Locator;

  constructor(page: Page) {
    super(page);
    this.welcomeMessage = page.locator('h1, h2').filter({ hasText: /welcome|dashboard/i });
    this.totalVehiclesCard = page.locator('[data-testid="total-vehicles"], .vehicle-count-card');
    this.activeTripsCard = page.locator('[data-testid="active-trips"], .active-trips-card');
    this.chargingStatusCard = page.locator('[data-testid="charging-status"], .charging-status-card');
    this.maintenanceAlertsCard = page.locator('[data-testid="maintenance-alerts"], .maintenance-alerts-card');
    this.fleetOverviewChart = page.locator('.fleet-chart, canvas, .recharts-wrapper');
    this.recentActivitiesTable = page.locator('table, .activities-table');
    this.refreshButton = page.getByRole('button', { name: /refresh/i });
    this.sideNav = page.locator('nav, .sidebar, .MuiDrawer-root');
  }

  async goto() {
    await this.page.goto('/dashboard');
    await this.waitForPageLoad();
  }

  async expectDashboardLoaded() {
    await expect(this.welcomeMessage).toBeVisible();
  }

  async getVehicleCount(): Promise<string> {
    return (await this.totalVehiclesCard.textContent()) || '0';
  }

  async getActiveTripsCount(): Promise<string> {
    return (await this.activeTripsCard.textContent()) || '0';
  }

  async refreshDashboard() {
    await this.refreshButton.click();
    await this.waitForSpinnerToDisappear();
  }

  async navigateToModule(moduleName: string) {
    await this.sideNav.getByText(moduleName, { exact: false }).click();
    await this.waitForPageLoad();
  }

  async expectWidgetsLoaded() {
    await expect(this.totalVehiclesCard).toBeVisible();
    await expect(this.activeTripsCard).toBeVisible();
  }
}
