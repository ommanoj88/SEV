"""
Simple LinkedIn Login Test
Checks if login works before running full automation
"""

import time
from selenium import webdriver
from selenium.webdriver.common.by import By

# Configuration
LINKEDIN_EMAIL = 'ommanoj88@gmail.com'
LINKEDIN_PASSWORD = 'Shobharain11@'

print("=" * 70)
print("LinkedIn Login Test")
print("=" * 70)

try:
    print("\n[1/4] Starting Chrome browser...")
    options = webdriver.ChromeOptions()
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    driver = webdriver.Chrome(options=options)
    print("✓ Chrome started successfully")

    print("\n[2/4] Opening LinkedIn login page...")
    driver.get('https://www.linkedin.com/login')
    time.sleep(3)
    print("✓ LinkedIn login page loaded")

    print("\n[3/4] Attempting to log in...")
    print(f"  Email: {LINKEDIN_EMAIL}")

    try:
        driver.find_element(By.ID, 'username').send_keys(LINKEDIN_EMAIL)
        driver.find_element(By.ID, 'password').send_keys(LINKEDIN_PASSWORD)
        driver.find_element(By.XPATH, '//button[@type="submit"]').click()
        time.sleep(5)
        print("✓ Login button clicked")
    except Exception as e:
        print(f"✗ Login failed: {e}")
        driver.quit()
        exit(1)

    print("\n[4/4] Checking if login was successful...")

    # Check if we're logged in by looking for main feed or 2FA
    try:
        driver.find_element(By.XPATH, '//a[contains(@href, "/feed/")]')
        print("✓ Login successful! Home feed found.")
        print("\n" + "=" * 70)
        print("SUCCESS - LinkedIn login is working!")
        print("You can now run: python script1.py")
        print("=" * 70)
    except:
        # Check if 2FA prompt is showing
        try:
            driver.find_element(By.XPATH, '//input[@inputmode="numeric"]')
            print("⚠ 2FA (Two-Factor Authentication) prompt detected")
            print("\nLinkedIn is asking for verification code")
            print("Please enter the code sent to your phone")
            print("(Keep Chrome window open)")
            input("\nPress Enter when you've completed 2FA...")
            time.sleep(5)
            print("✓ Continuing...")
        except:
            print("? Cannot determine login status")
            print("  Check Chrome window for any prompts")

    driver.quit()

except Exception as e:
    print(f"✗ Error: {e}")
    try:
        driver.quit()
    except:
        pass
    exit(1)
