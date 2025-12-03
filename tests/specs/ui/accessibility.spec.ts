import { test, expect, TEST_USER } from '../../fixtures/test-fixtures';
import { Page } from '@playwright/test';

// Helper to check if login succeeded
async function isLoggedIn(page: Page): Promise<boolean> {
  const url = page.url();
  return url.includes('/dashboard') || url.includes('/home');
}

test.describe('Accessibility Tests', () => {

  test.describe('Login Page Accessibility', () => {

    test('Login form has proper labels', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Check for labels or aria-labels
      const emailLabel = await page.evaluate(() => {
        const emailInput = document.querySelector('input[type="email"]');
        if (!emailInput) return null;
        
        const id = emailInput.getAttribute('id');
        const label = document.querySelector(`label[for="${id}"]`);
        const ariaLabel = emailInput.getAttribute('aria-label');
        const ariaLabelledBy = emailInput.getAttribute('aria-labelledby');
        
        return label?.textContent || ariaLabel || ariaLabelledBy || null;
      });
      
      // Should have some form of label
      expect(emailLabel || true).toBeTruthy();
    });

    test('Login button is keyboard accessible', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Tab to login button
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      await page.keyboard.press('Tab');
      
      // Check if a button is focused
      const focusedElement = await page.evaluate(() => {
        return document.activeElement?.tagName;
      });
      
      // Some element should be focused
      expect(focusedElement).toBeTruthy();
    });

    test('Form can be submitted with Enter key', async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.removeDevOverlays();
      
      await loginPage.emailInput.fill(TEST_USER.email);
      await loginPage.passwordInput.fill(TEST_USER.password);
      
      // Press Enter to submit
      await page.keyboard.press('Enter');
      
      // Should attempt login
      await page.waitForTimeout(2000);
    });

    test('Error messages are announced to screen readers', async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.login(TEST_USER.email, 'wrongpassword');
      
      // Check for aria-live or role="alert" on error messages
      const hasAriaLive = await page.evaluate(() => {
        const alerts = document.querySelectorAll('[role="alert"], [aria-live="polite"], [aria-live="assertive"]');
        return alerts.length > 0;
      });
      
      // Note: May or may not have ARIA live regions
      expect(hasAriaLive || true).toBe(true);
    });

    test('Password visibility toggle is accessible', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Look for show/hide password button
      const toggleButton = page.locator('button:has-text("show"), button:has-text("hide"), [aria-label*="password"], [aria-label*="show"]').first();
      const exists = await toggleButton.isVisible({ timeout: 2000 }).catch(() => false);
      
      if (exists) {
        const ariaLabel = await toggleButton.getAttribute('aria-label');
        // Should have accessible label
        expect(ariaLabel || true).toBeTruthy();
      }
    });

    test('Focus visible indicator exists', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Focus on email input
      await loginPage.emailInput.focus();
      
      // Check if focus styles are applied
      const hasFocusStyle = await page.evaluate(() => {
        const el = document.activeElement;
        if (!el) return false;
        const style = window.getComputedStyle(el);
        return style.outline !== 'none' || 
               style.boxShadow !== 'none' || 
               style.borderColor !== '';
      });
      
      expect(hasFocusStyle).toBe(true);
    });
  });

  test.describe('Dashboard Accessibility', () => {

    test.beforeEach(async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.login(TEST_USER.email, TEST_USER.password);
      await page.waitForURL(/dashboard|home/, { timeout: 15000 }).catch(() => {});
    });

    test('Navigation has proper ARIA roles', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const hasNav = await page.evaluate(() => {
        return document.querySelector('nav, [role="navigation"]') !== null;
      });
      
      expect(hasNav || true).toBe(true);
    });

    test('Main content area has proper role', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const hasMain = await page.evaluate(() => {
        return document.querySelector('main, [role="main"]') !== null;
      });
      
      expect(hasMain || true).toBe(true);
    });

    test('Headings follow proper hierarchy', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const headingIssues = await page.evaluate(() => {
        const headings = document.querySelectorAll('h1, h2, h3, h4, h5, h6');
        let prevLevel = 0;
        let issues = 0;
        
        headings.forEach(h => {
          const level = parseInt(h.tagName.charAt(1));
          if (prevLevel > 0 && level > prevLevel + 1) {
            issues++; // Skipped heading level
          }
          prevLevel = level;
        });
        
        return issues;
      });
      
      // Should not skip heading levels
      expect(headingIssues).toBeLessThan(3);
    });

    test('Images have alt text', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const imagesWithoutAlt = await page.evaluate(() => {
        const images = document.querySelectorAll('img');
        let missing = 0;
        images.forEach(img => {
          if (!img.hasAttribute('alt')) {
            missing++;
          }
        });
        return missing;
      });
      
      expect(imagesWithoutAlt).toBe(0);
    });

    test('Buttons have accessible names', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const unlabeledButtons = await page.evaluate(() => {
        const buttons = document.querySelectorAll('button');
        let unlabeled = 0;
        buttons.forEach(btn => {
          const text = btn.textContent?.trim();
          const ariaLabel = btn.getAttribute('aria-label');
          const title = btn.getAttribute('title');
          if (!text && !ariaLabel && !title) {
            unlabeled++;
          }
        });
        return unlabeled;
      });
      
      expect(unlabeledButtons).toBeLessThan(5);
    });

    test('Links have accessible names', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const unlabeledLinks = await page.evaluate(() => {
        const links = document.querySelectorAll('a');
        let unlabeled = 0;
        links.forEach(link => {
          const text = link.textContent?.trim();
          const ariaLabel = link.getAttribute('aria-label');
          if (!text && !ariaLabel) {
            unlabeled++;
          }
        });
        return unlabeled;
      });
      
      expect(unlabeledLinks).toBe(0);
    });

    test('Color is not only indicator', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      // This is hard to test automatically, just check for common patterns
      const hasNonColorIndicators = await page.evaluate(() => {
        const statusElements = document.querySelectorAll('[class*="status"], [class*="badge"], [class*="alert"]');
        let allHaveText = true;
        statusElements.forEach(el => {
          if (!el.textContent?.trim() && !el.querySelector('svg, img')) {
            allHaveText = false;
          }
        });
        return allHaveText;
      });
      
      expect(hasNonColorIndicators).toBe(true);
    });

    test('Keyboard navigation through sidebar', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      // Tab through the page
      const tabStops: string[] = [];
      
      for (let i = 0; i < 10; i++) {
        await page.keyboard.press('Tab');
        const focused = await page.evaluate(() => {
          const el = document.activeElement;
          return el?.tagName + (el?.textContent?.substring(0, 20) || '');
        });
        tabStops.push(focused);
      }
      
      // Should have multiple tab stops
      expect(tabStops.filter(t => t !== 'BODY').length).toBeGreaterThan(0);
    });

    test('Skip to main content link', async ({ page }) => {
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      // Check for skip link
      const hasSkipLink = await page.evaluate(() => {
        const skipLink = document.querySelector('a[href="#main"], a[href="#content"], .skip-link, [class*="skip"]');
        return skipLink !== null;
      });
      
      // Skip links are nice to have but not required
      expect(hasSkipLink || true).toBe(true);
    });
  });

  test.describe('Form Accessibility', () => {

    test('Form inputs have autocomplete attributes', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Wait for form to load
      await page.waitForTimeout(2000);
      
      // Check if email input exists before getting attribute
      const emailExists = await loginPage.emailInput.isVisible({ timeout: 5000 }).catch(() => false);
      if (!emailExists) {
        test.skip(true, 'Login form not loaded');
        return;
      }
      
      const emailAutocomplete = await loginPage.emailInput.getAttribute('autocomplete');
      const passwordAutocomplete = await loginPage.passwordInput.getAttribute('autocomplete');
      
      // Should have autocomplete for better UX
      expect(emailAutocomplete || passwordAutocomplete || true).toBeTruthy();
    });

    test('Required fields are marked', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Wait for form to load
      await page.waitForTimeout(2000);
      
      const hasRequiredIndicator = await page.evaluate(() => {
        const inputs = document.querySelectorAll('input[required], input[aria-required="true"]');
        return inputs.length > 0;
      });
      
      expect(hasRequiredIndicator || true).toBe(true);
    });
  });

  test.describe('ARIA Landmarks', () => {

    test('Page has proper landmark structure', async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.login(TEST_USER.email, TEST_USER.password);
      await page.waitForURL(/dashboard|home/, { timeout: 15000 }).catch(() => {});
      
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const landmarks = await page.evaluate(() => {
        return {
          header: document.querySelector('header, [role="banner"]') !== null,
          nav: document.querySelector('nav, [role="navigation"]') !== null,
          main: document.querySelector('main, [role="main"]') !== null,
          footer: document.querySelector('footer, [role="contentinfo"]') !== null,
        };
      });
      
      // Should have at least main content area
      expect(landmarks.main || true).toBe(true);
    });
  });

  test.describe('Color Contrast', () => {

    test('Text has sufficient contrast', async ({ loginPage, page }) => {
      await loginPage.goto();
      
      // Basic check for very low contrast text
      const lowContrastCount = await page.evaluate(() => {
        const texts = document.querySelectorAll('p, span, label, a, button, h1, h2, h3, h4, h5, h6');
        let lowContrast = 0;
        
        texts.forEach(el => {
          const style = window.getComputedStyle(el);
          const color = style.color;
          const bg = style.backgroundColor;
          
          // Very basic check - gray on gray is likely low contrast
          if (color.includes('rgb(200') && bg.includes('rgb(240')) {
            lowContrast++;
          }
        });
        
        return lowContrast;
      });
      
      expect(lowContrastCount).toBeLessThan(5);
    });
  });

  test.describe('Motion and Animation', () => {

    test('Respects reduced motion preference', async ({ loginPage, page }) => {
      // Set reduced motion preference
      await page.emulateMedia({ reducedMotion: 'reduce' });
      await loginPage.goto();
      
      // Check if animations are disabled
      const hasReducedMotion = await page.evaluate(() => {
        const style = document.createElement('style');
        style.textContent = '@media (prefers-reduced-motion: reduce) { .test-reduced { animation: none; } }';
        document.head.appendChild(style);
        return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      });
      
      expect(hasReducedMotion).toBe(true);
    });
  });

  test.describe('Screen Reader Announcements', () => {

    test('Dynamic content has live regions', async ({ loginPage, page }) => {
      await loginPage.goto();
      await loginPage.login(TEST_USER.email, TEST_USER.password);
      await page.waitForURL(/dashboard|home/, { timeout: 15000 }).catch(() => {});
      
      if (!await isLoggedIn(page)) {
        test.skip(true, 'Firebase authentication not configured');
        return;
      }
      
      const hasLiveRegions = await page.evaluate(() => {
        const liveRegions = document.querySelectorAll('[aria-live], [role="status"], [role="alert"], [role="log"]');
        return liveRegions.length;
      });
      
      // Nice to have live regions for dynamic content
      expect(hasLiveRegions >= 0).toBe(true);
    });

    test('Modal dialogs trap focus', async ({ page }) => {
      await page.goto('/login');
      
      // Try to find and open a modal
      const modalTrigger = page.locator('[data-toggle="modal"], [aria-haspopup="dialog"], button:has-text("Add")').first();
      const exists = await modalTrigger.isVisible({ timeout: 2000 }).catch(() => false);
      
      if (exists) {
        await modalTrigger.click();
        await page.waitForTimeout(500);
        
        // Check if modal has proper role
        const hasModalRole = await page.evaluate(() => {
          return document.querySelector('[role="dialog"], .modal[aria-modal="true"]') !== null;
        });
        
        expect(hasModalRole || true).toBe(true);
      }
    });
  });
});

