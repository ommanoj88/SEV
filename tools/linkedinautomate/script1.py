import time
import json
from datetime import datetime, timedelta
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException
import os

# ============== CONFIGURATION ==============
LINKEDIN_EMAIL = 'ommanoj88@gmail.com'
LINKEDIN_PASSWORD = 'Shobharain11@'
WEEKLY_CONNECTION_LIMIT = 100  # LinkedIn's safe limit per week
REQUESTS_PER_SESSION = 100     # How many requests per run
DELAY_BETWEEN_REQUESTS = 15    # Seconds between each request (anti-spam)

# Target companies from your research - 800+ potential customers
TARGET_COMPANIES = [
    # Last-Mile Delivery & E-commerce
    'Zomato', 'Swiggy', 'Blinkit', 'Zepto', 'BigBasket', 'Dunzo',
    'Amazon India', 'Flipkart', 'Grofers', 'JioMart',

    # Logistics & Transportation
    'Delhivery', 'Ecom Express', 'BlueDart', 'DTDC', 'Gati',
    'XpressBees', 'Shadowfax', 'Ekart Logistics', 'Porter',

    # EV Fleet Operators & Leasing
    'Zypp Electric', 'Yelo EV', 'EMotorad', 'SmartE',
    'Battery Smart', 'SUN Mobility', 'Lithion Power',

    # Corporate Fleets (FMCG, Telecom, Pharma)
    'Unilever', 'ITC Limited', 'Nestle India', 'Britannia',
    'Airtel', 'Vodafone Idea', 'Cipla', 'Dr. Reddy\'s',

    # Add remaining 750+ companies here from your research
]

# Decision-maker job titles to target
TARGET_TITLES = [
    'Fleet Manager', 'Fleet Operations', 'Fleet Head',
    'COO', 'Chief Operating Officer', 'VP Operations',
    'Operations Manager', 'Operations Head', 'Director Operations',
    'Logistics Manager', 'Logistics Head', 'Supply Chain Manager',
    'Sustainability Officer', 'ESG Manager', 'Head Sustainability',
    'CTO', 'Chief Technology Officer', 'VP Technology',
    'Mobility Head', 'EV Fleet', 'Electric Vehicle',
]

# Personalized connection note templates
# These will be customized based on actual job title and company
CONNECTION_NOTES = [
    "Hi {name}, I noticed you're with {company}. We're building an EV fleet management platform to help logistics companies optimize charging, maintenance & vehicle utilization. Would love to understand your current challenges—could we connect?",
    "Hello {name}, I'm researching fleet operations and EV transition strategies at {company}. Your insights on managing large fleets would be invaluable. Open to a quick chat?",
    "Hi {name}, we're developing solutions for EV fleet optimization in India. I'd love to learn about {company}'s approach to charging infrastructure & fleet efficiency. Would be great to connect!",
    "Hi {name}, I see you're in a leadership role at {company}. We're helping logistics firms reduce costs through intelligent EV fleet management. Would you be open to a brief conversation?",
    "Hello {name}, we're working on fleet management solutions specifically for Indian logistics operators like {company}. Your expertise would help us build something truly useful—could we connect?",
]

# Tracking files
TRACKING_FILE = 'connection_requests_sent.txt'
WEEKLY_STATS_FILE = 'weekly_stats.json'

# ============== HELPER FUNCTIONS ==============

def load_sent_requests():
    """Load previously sent connection requests - returns dict by URL for fast lookup"""
    if not os.path.exists(TRACKING_FILE):
        return {}

    requests_dict = {}
    try:
        with open(TRACKING_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().split('|')
                    if len(parts) >= 5:
                        profile_url = parts[0]
                        requests_dict[profile_url] = {
                            'name': parts[1],
                            'company': parts[2],
                            'title': parts[3],
                            'timestamp': parts[4]
                        }
    except Exception as e:
        print(f"⚠ Warning: Could not load tracking file: {e}")

    return requests_dict

def get_weekly_requests_count():
    """Get count of requests sent THIS WEEK"""
    if not os.path.exists(TRACKING_FILE):
        return 0

    one_week_ago = datetime.now() - timedelta(days=7)
    count = 0

    try:
        with open(TRACKING_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().split('|')
                    if len(parts) >= 5:
                        try:
                            request_time = datetime.strptime(parts[4], '%Y-%m-%d %H:%M:%S')
                            if request_time > one_week_ago:
                                count += 1
                        except:
                            pass
    except Exception as e:
        print(f"⚠ Warning: Could not read weekly stats: {e}")

    return count

def can_send_more_requests():
    """Check if we can send more requests this week"""
    weekly_count = get_weekly_requests_count()
    remaining = WEEKLY_CONNECTION_LIMIT - weekly_count
    print(f"📊 Weekly quota: {weekly_count}/{WEEKLY_CONNECTION_LIMIT} (Remaining: {remaining})")
    return remaining > 0

def save_sent_request(profile_url, name, company, title):
    """Save sent request to tracking file"""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    with open(TRACKING_FILE, 'a', encoding='utf-8') as f:
        f.write(f"{profile_url}|{name}|{company}|{title}|{timestamp}\n")

def get_connection_note(name, company):
    """Get personalized connection note"""
    import random
    template = random.choice(CONNECTION_NOTES)
    return template.format(name=name.split()[0], company=company)

def login_to_linkedin(driver):
    """Login to LinkedIn"""
    driver.get('https://www.linkedin.com/login')
    time.sleep(3)

    driver.find_element(By.ID, 'username').send_keys(LINKEDIN_EMAIL)
    driver.find_element(By.ID, 'password').send_keys(LINKEDIN_PASSWORD)
    driver.find_element(By.XPATH, '//button[@type="submit"]').click()
    time.sleep(5)
    print("✓ Logged in to LinkedIn")

def search_prospects(driver, company, title_keyword):
    """Search for prospects using LinkedIn search"""
    search_query = f"{company} {title_keyword}"
    search_url = f'https://www.linkedin.com/search/results/people/?keywords={search_query.replace(" ", "%20")}'
    print(f"    [SEARCH] Loading URL: {search_url}")
    driver.get(search_url)
    print(f"    [SEARCH] Page loaded, waiting 5s...")
    time.sleep(5)  # Increased wait time for page load

    try:
        # SEARCH STRATEGY: Handle LinkedIn's lazy loading and dynamic content
        print(f"    [SEARCH] Starting search with scrolling for lazy-loaded content...")

        # Scroll down a few times to load more results
        for scroll_attempt in range(3):
            print(f"    [SEARCH] Scroll attempt {scroll_attempt + 1}/3...")
            driver.execute_script("window.scrollBy(0, 500)")
            time.sleep(2)

        # Find all profile links - try multiple patterns
        print(f"    [SEARCH] Searching for profile links...")
        all_profile_links = []

        # Pattern 1: Standard /in/ URLs
        try:
            pattern1 = driver.find_elements(By.XPATH, '//a[contains(@href, "/in/")]')
            print(f"    [SEARCH] Pattern 1 (/in/ URLs): Found {len(pattern1)} links")
            all_profile_links.extend(pattern1)
        except:
            pass

        # Pattern 2: Data attributes with profile info
        try:
            pattern2 = driver.find_elements(By.XPATH, '//a[@data-test-id and contains(@href, "/in/")]')
            print(f"    [SEARCH] Pattern 2 (data attributes): Found {len(pattern2)} links")
            for p in pattern2:
                if p not in all_profile_links:
                    all_profile_links.append(p)
        except:
            pass

        # Pattern 3: LinkedIn profile cards with specific classes
        try:
            pattern3 = driver.find_elements(By.XPATH, '//a[contains(@class, "app-aware-link") and contains(@href, "/in/")]')
            print(f"    [SEARCH] Pattern 3 (app-aware links): Found {len(pattern3)} links")
            for p in pattern3:
                if p not in all_profile_links:
                    all_profile_links.append(p)
        except:
            pass

        print(f"    [SEARCH] Total unique profile links found: {len(all_profile_links)}")

        if not all_profile_links:
            print(f"    [SEARCH] ⚠ NO PROFILE LINKS FOUND!")
            print(f"    [SEARCH] Checking page content for debug...")
            # Debug: show all links on page
            all_links = driver.find_elements(By.XPATH, '//a')
            print(f"    [SEARCH] Total links on page: {len(all_links)}")
            if all_links:
                print(f"    [SEARCH] First few links:")
                for i, link in enumerate(all_links[:5]):
                    try:
                        href = link.get_attribute('href')
                        text = link.text[:50] if link.text else "(no text)"
                        print(f"    [SEARCH]   Link {i}: {href} - {text}")
                    except:
                        pass
            return []

        # Filter out navigation/header links - keep only profile cards
        valid_results = []
        for link in all_profile_links:
            try:
                # Get the closest parent container
                parent = link.find_element(By.XPATH, './ancestor::div[2]')
                parent_text = parent.text.strip()

                # Profile cards have meaningful text (name, title, company)
                if len(parent_text) > 10:  # More content = likely a real profile
                    if parent not in valid_results:
                        valid_results.append(parent)
            except:
                pass

        print(f"    [SEARCH] Extracted {len(valid_results)} valid result cards")

        if valid_results:
            return valid_results[:20]

        # If we can't extract parents, return the links directly
        print(f"    [SEARCH] Returning {len(all_profile_links[:20])} profile links directly")
        return all_profile_links[:20]

    except Exception as e:
        print(f"    [SEARCH] Error searching: {e}")
        import traceback
        traceback.print_exc()
        return []

def extract_profile_data(result_element):
    """Extract profile information from search result"""
    try:
        # Get profile link first (most reliable)
        profile_link = None
        try:
            # If result_element is a link itself
            if result_element.tag_name == 'a' and '/in/' in result_element.get_attribute('href'):
                profile_link = result_element.get_attribute('href')
        except:
            pass

        if not profile_link:
            # Search for profile link within the element
            try:
                profile_link = result_element.find_element(By.XPATH, './/a[contains(@href, "/in/")]').get_attribute('href')
            except:
                return None

        if not profile_link:
            return None

        # Clean up the URL
        clean_url = profile_link.split('?')[0]

        # Get name - extract from text or title attribute
        name = None
        try:
            # Try direct text content
            text_content = result_element.text.split('\n')[0]
            if text_content and len(text_content) > 1:
                name = text_content
        except:
            pass

        if not name:
            try:
                # Try finding any text-like element
                elem = result_element.find_element(By.XPATH, './/span | .//h3 | .//div[contains(@class, "title")]')
                name = elem.text
            except:
                pass

        if not name:
            # Use profile URL username as fallback
            try:
                username = clean_url.split('/in/')[1].split('?')[0].split('/')[0]
                name = username.replace('-', ' ').title()
            except:
                name = 'Unknown'

        # Get title and company from subtitle/description
        title = 'Unknown'
        company = 'Unknown'

        try:
            # Get all text from the element and parse it
            all_text = result_element.text
            lines = all_text.split('\n')

            if len(lines) > 1:
                # Usually second line has title and company info
                subtitle = lines[1]
                if ' at ' in subtitle:
                    parts = subtitle.split(' at ')
                    title = parts[0].strip()
                    company = parts[1].strip()
                else:
                    title = subtitle.strip()
        except:
            pass

        profile_data = {
            'name': name.strip() if name else 'Unknown',
            'profile_url': clean_url,
            'title': title,
            'company': company
        }

        # Filter out invalid profiles (mutual connections cards, group cards, etc.)
        # Check if the extracted name looks like a real single person
        name_lower = profile_data['name'].lower()
        title_lower = profile_data['title'].lower()

        # Reject if name contains "and" followed by numbers or "mutual connection"
        if (' and ' in name_lower and any(char.isdigit() for char in name_lower)) or \
           'mutual' in name_lower or \
           'mutual' in title_lower or \
           'connection' in name_lower or \
           profile_data['name'] == 'Unknown':
            print(f"      Skipping invalid profile: {profile_data['name']} - {profile_data['title']}")
            return None

        # Debug output
        print(f"      Extracted: {profile_data['name']} - {profile_data['title']} @ {profile_data['company']}")

        return profile_data
    except Exception as e:
        print(f"      Error extracting profile data: {e}")
        return None

def send_connection_request(driver, profile_data, sent_requests):
    """Send connection request with personalized note"""
    profile_url = profile_data['profile_url']

    # Check if already sent - FIXED: now properly checks the dict keys
    if profile_url in sent_requests:
        print(f"  ⊘ Already sent to {profile_data['name']} on {sent_requests[profile_url]['timestamp']} - skipping")
        return False

    try:
        # Navigate to profile first
        print(f"    [STEP 1] Navigating to profile: {profile_url}")
        driver.get(profile_url)
        print(f"    [STEP 1] ✓ Navigated to profile, waiting 4s for load...")
        time.sleep(4)  # Wait longer for profile to load

        # Wait for page to be ready - look for profile info to load
        print(f"    [STEP 2] Waiting for profile headline to load...")
        try:
            WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.XPATH, '//h1 | //div[@data-test-id="profile-headline"]'))
            )
            print(f"    [STEP 2] ✓ Profile headline found")
        except:
            print(f"    [STEP 2] ⚠ Profile headline not found, continuing anyway...")
            pass  # Continue anyway

        time.sleep(2)  # Extra wait for all elements to render
        print(f"    [STEP 3] Looking for Connect button...")

        # Find Connect button - try multiple selectors
        connect_button = None
        connect_selectors = [
            '//button[contains(@aria-label, "Invite")]',
            '//button[contains(text(), "Connect")]',
            '//button[@aria-label="Connect with"]',
            '//button[contains(@class, "mt4") and contains(text(), "Connect")]',
            '//button[@type="button" and contains(., "Connect")]',
        ]

        for selector in connect_selectors:
            try:
                elements = driver.find_elements(By.XPATH, selector)
                if elements:
                    connect_button = elements[0]
                    print(f"    [STEP 3] ✓ Found Connect button with selector: {selector[:50]}...")
                    break
            except:
                continue

        if not connect_button:
            # They might already be connected (no Connect button = already connected)
            print(f"    [STEP 3] ✗ Could not find Connect button")
            print(f"  ⊘ Already connected or messaging disabled for {profile_data['name']}")
            return False

        # Scroll button into view
        print(f"    [STEP 4] Scrolling Connect button into view...")
        try:
            driver.execute_script("arguments[0].scrollIntoView(true);", connect_button)
            time.sleep(1)
            print(f"    [STEP 4] ✓ Scrolled into view")
        except Exception as e:
            print(f"    [STEP 4] ⚠ Scroll failed: {str(e)}")
            pass

        # Try to click using JavaScript first (more reliable)
        print(f"    [STEP 5] Clicking Connect button...")
        try:
            driver.execute_script("arguments[0].click();", connect_button)
            print(f"    [STEP 5] ✓ Clicked via JavaScript")
            time.sleep(2)
        except Exception as e:
            print(f"    [STEP 5] JavaScript click failed: {str(e)}, trying regular click...")
            # Fallback to regular click
            try:
                connect_button.click()
                print(f"    [STEP 5] ✓ Clicked via Selenium")
                time.sleep(2)
            except Exception as e:
                print(f"    [STEP 5] ✗ Regular click also failed: {str(e)}")
                print(f"  ✗ Failed to click Connect button for {profile_data['name']}")
                return False

        # Send request - add note and click the "Send" button
        try:
            print(f"    [STEP 6] Waiting for modal to appear...")
            # Wait longer for modal to fully appear
            time.sleep(3)

            # Try to wait for modal to appear using WebDriverWait
            try:
                WebDriverWait(driver, 5).until(
                    EC.presence_of_element_located((By.XPATH, '//div[contains(@class, "modal") or contains(@class, "dialog")]'))
                )
                print(f"    [STEP 6] ✓ Modal found")
            except:
                print(f"    [STEP 6] ⚠ Modal not found with standard selectors, continuing...")
                pass  # Modal might not have those classes, continue anyway

            time.sleep(2)  # Extra wait for modal elements to render

            # STEP 6.5: Try to add a note
            print(f"    [STEP 6.5] Looking for note input field...")
            note_added = False
            note_selectors = [
                '//textarea[contains(@placeholder, "Add a note")]',
                '//textarea[@data-testid="connection-message"]',
                '//textarea[contains(@placeholder, "message")]',
                '//input[@placeholder="Add a note"]',
                '//div[contains(@class, "message")]//textarea',
                '//textarea',  # Last resort - any textarea
            ]

            for note_selector in note_selectors:
                try:
                    note_field = driver.find_element(By.XPATH, note_selector)
                    if note_field.is_displayed():
                        print(f"    [STEP 6.5] ✓ Found note field with selector: {note_selector[:50]}...")

                        # Generate personalized note
                        note_message = get_connection_note(profile_data['name'], profile_data['company'])
                        print(f"    [STEP 6.5] Adding note: {note_message[:50]}...")

                        # Clear field first and add note
                        note_field.clear()
                        note_field.send_keys(note_message)
                        print(f"    [STEP 6.5] ✓ Note added successfully")
                        note_added = True
                        break
                except:
                    pass

            if not note_added:
                print(f"    [STEP 6.5] ⚠ Could not find note field, will send without note")

            time.sleep(1)  # Wait for note to be processed

            # Find and click Send button - be more flexible with button text
            print(f"    [STEP 7] Looking for Send button...")
            send_button = None

            # Try different button selectors and text patterns
            button_patterns = [
                ('//button[contains(text(), "Send") and not(contains(text(), "Cancel"))]', "Send button (contains Send)"),
                ('//button[@aria-label="Send without adding a note"]', "Send without note button"),
                ('//button[contains(@aria-label, "Send")]', "Send button (aria-label)"),
                ('//button[contains(translate(., "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz"), "send")]', "Send button (any case)"),
                ('//div[contains(@class, "modal") or contains(@class, "dialog")]//button[not(contains(text(), "Cancel")) and not(contains(text(), "Back"))]', "Non-cancel button in modal"),
            ]

            for xpath_pattern, description in button_patterns:
                try:
                    buttons = driver.find_elements(By.XPATH, xpath_pattern)
                    if buttons:
                        for btn in buttons:
                            if btn.is_displayed():
                                send_button = btn
                                print(f"    [STEP 7] ✓ Found button: {description}")
                                break
                    if send_button:
                        break
                except:
                    pass

            # Last resort: find any visible button that's not "Cancel" or "Back"
            if not send_button:
                print(f"    [STEP 7] No button patterns matched, trying last resort...")
                try:
                    all_buttons = driver.find_elements(By.XPATH, '//button')
                    print(f"    [STEP 7] Found {len(all_buttons)} total buttons on page")
                    for idx, btn in enumerate(all_buttons):
                        if btn.is_displayed():
                            btn_text = btn.text.strip().lower()
                            print(f"    [STEP 7] Button {idx}: '{btn.text.strip()}' (visible, {len(btn_text)} chars)")
                            # Skip cancel, back, and empty buttons
                            if btn_text and 'cancel' not in btn_text and 'back' not in btn_text and len(btn_text) < 30:
                                send_button = btn
                                print(f"    [STEP 7] ✓ Found button (last resort): '{btn.text.strip()}'")
                                break
                except Exception as e:
                    print(f"    [STEP 7] ⚠ Error in last resort search: {str(e)}")
                    pass

            if not send_button:
                print(f"    [STEP 7] ✗ Could not find send button")
                print(f"  ✗ Could not find send button for {profile_data['name']}")
                return False

            # Click the send button
            print(f"    [STEP 8] Clicking Send button...")
            try:
                driver.execute_script("arguments[0].scrollIntoView(true);", send_button)
                time.sleep(1)
                driver.execute_script("arguments[0].click();", send_button)
                print(f"    [STEP 8] ✓ Clicked via JavaScript")
            except Exception as e:
                print(f"    [STEP 8] JavaScript click failed: {str(e)}, trying Selenium click...")
                try:
                    send_button.click()
                    print(f"    [STEP 8] ✓ Clicked via Selenium")
                except Exception as e:
                    print(f"    [STEP 8] ✗ All click methods failed: {str(e)}")
                    print(f"  ✗ Failed to click send button for {profile_data['name']}")
                    return False

            time.sleep(2)
            print(f"    [STEP 9] Saving to tracking file...")

            # Save to tracking file
            save_sent_request(
                profile_data['profile_url'],
                profile_data['name'],
                profile_data['company'],
                profile_data['title']
            )

            print(f"  ✓ Sent request to {profile_data['name']} ({profile_data['title']} at {profile_data['company']})")
            return True

        except Exception as e:
            print(f"  ✗ Failed to send request to {profile_data['name']}: {str(e)}")
            import traceback
            traceback.print_exc()
            return False

    except Exception as e:
        print(f"  ✗ Failed to send request to {profile_data['name']}: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

# ============== MAIN EXECUTION ==============

def main():
    print("=" * 70)
    print("LinkedIn Connection Request Automation - EV Fleet Platform")
    print("=" * 70)

    # Check weekly limit FIRST
    if not can_send_more_requests():
        print("\n⛔ WEEKLY LIMIT REACHED - Please wait until next week to send more requests")
        return

    # Load tracking data
    sent_requests = load_sent_requests()
    print(f"\n✓ Loaded {len(sent_requests)} previously sent requests (all time)\n")

    # Initialize browser
    options = webdriver.ChromeOptions()
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    driver = webdriver.Chrome(options=options)

    try:
        # Login
        login_to_linkedin(driver)

        requests_sent_this_session = 0
        requests_skipped_this_session = 0

        # Iterate through companies and titles
        for company in TARGET_COMPANIES:
            if requests_sent_this_session >= REQUESTS_PER_SESSION:
                print(f"\n✓ Reached session limit ({REQUESTS_PER_SESSION} requests)")
                break

            if not can_send_more_requests():
                print(f"\n⛔ Weekly limit reached during session - stopping")
                break

            for title_keyword in TARGET_TITLES[:3]:  # Limit title variations per company
                if requests_sent_this_session >= REQUESTS_PER_SESSION:
                    break

                if not can_send_more_requests():
                    break

                print(f"\n🔍 Searching: {company} + {title_keyword}")

                try:
                    # Search for prospects
                    results = search_prospects(driver, company, title_keyword)
                    print(f"  [DEBUG] search_prospects returned {len(results)} results")

                    if not results:
                        print(f"  (No results found)")
                        continue

                    print(f"  [DEBUG] Processing top 5 of {len(results)} results")
                    for idx, result in enumerate(results[:5]):  # Top 5 results per search
                        print(f"  [DEBUG] Processing result {idx + 1}/5")

                        if requests_sent_this_session >= REQUESTS_PER_SESSION:
                            print(f"  [DEBUG] Session limit reached ({requests_sent_this_session}/{REQUESTS_PER_SESSION})")
                            break

                        if not can_send_more_requests():
                            print(f"  [DEBUG] Weekly limit reached")
                            break

                        print(f"  [DEBUG] Calling extract_profile_data()...")
                        profile_data = extract_profile_data(result)

                        if profile_data:
                            print(f"  [DEBUG] Got profile_data: {profile_data['name']} ({profile_data['profile_url']})")
                            print(f"  [DEBUG] Calling send_connection_request()...")

                            send_result = send_connection_request(driver, profile_data, sent_requests)
                            print(f"  [DEBUG] send_connection_request returned: {send_result}")

                            if send_result:
                                requests_sent_this_session += 1
                                sent_requests[profile_data['profile_url']] = {
                                    'name': profile_data['name'],
                                    'company': profile_data['company'],
                                    'title': profile_data['title'],
                                    'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                                }
                                print(f"  [DEBUG] Waiting {DELAY_BETWEEN_REQUESTS}s before next request...")
                                time.sleep(DELAY_BETWEEN_REQUESTS)
                            else:
                                requests_skipped_this_session += 1
                                print(f"  [DEBUG] Request was skipped/failed")
                        else:
                            print(f"  [DEBUG] extract_profile_data returned None (profile was filtered out)")

                except Exception as e:
                    print(f"  ✗ Error searching {company}: {str(e)}")
                    import traceback
                    traceback.print_exc()
                    continue

        print(f"\n{'='*70}")
        print(f"✓ Session complete:")
        print(f"  - Sent: {requests_sent_this_session} new connection requests")
        print(f"  - Skipped: {requests_skipped_this_session} (duplicates/errors)")
        print(f"  - Total all time: {len(sent_requests)}")
        print(f"  - Weekly quota: {get_weekly_requests_count()}/{WEEKLY_CONNECTION_LIMIT}")
        print(f"{'='*70}\n")

    finally:
        driver.quit()

if __name__ == '__main__':
    main()
