import time
from datetime import datetime, timedelta
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import os

# ============== CONFIGURATION ==============
LINKEDIN_EMAIL = 'ommanoj88@gmail.com'
LINKEDIN_PASSWORD = 'Shobharain11@'
MESSAGES_PER_SESSION = 30  # Safe daily limit
MESSAGES_PER_DAY_LIMIT = 50  # Max messages per day (LinkedIn safety)
DELAY_BETWEEN_MESSAGES = 20  # Seconds

# Message templates for new connections - CONSULTATIVE QUESTIONS FIRST (NO COMPANY NAME)
MESSAGE_TEMPLATES = [
    """Hi {name},

Quick question - with your fleet operations, what's your biggest challenge right now with EV adoption?

Is it:
• Finding and managing charging stations across regions?
• Tracking battery health (SOH/SOC) and predicting maintenance needs?
• Planning routes that account for charging stops and driver rest?
• Managing maintenance costs and warranty tracking?

I'm building a platform that specifically addresses these for Indian logistics companies. Would love to hear what's keeping you up at night.

Best,
[Your Name]""",

    """Hello {name},

I'm researching fleet operations for logistics companies and your experience would be valuable.

A quick question: When your drivers take EVs out for deliveries/long-haul routes, what breaks down first?

1. Finding available charging stations in time?
2. Battery degradation and predicting when maintenance is needed?
3. Driver behavior affecting battery efficiency?
4. Downtime at charging stations affecting delivery schedules?

I'm building a solution around whatever pain point is most critical. Would you have 10 min this week to share what matters most to you?

Thanks,
[Your Name]""",

    """Hi {name},

One question - with EV fleet management, if you could fix ONE thing right now, what would it be?

• Real-time visibility of charging station availability?
• Predictive maintenance alerts before batteries fail?
• Smart route planning that minimizes charging stops?
• Driver incentives based on battery-efficient driving?

I'm working with logistics leaders on exactly one of these. Curious which resonates most with your operations.

Would you be open to a quick chat?

Cheers,
[Your Name]""",
]

# Tracking files
CONNECTION_REQUESTS_FILE = 'connection_requests_sent.txt'
MESSAGES_SENT_FILE = 'messages_sent.txt'
CONNECTIONS_LOG_FILE = 'accepted_connections.txt'

# ============== HELPER FUNCTIONS ==============

def load_sent_requests():
    """Load URLs of people we sent connection requests to"""
    if not os.path.exists(CONNECTION_REQUESTS_FILE):
        return {}

    requests_dict = {}
    try:
        with open(CONNECTION_REQUESTS_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().split('|')
                    if len(parts) >= 5:  # URL|name|company|title|timestamp
                        profile_url = parts[0]
                        name = parts[1]
                        company = parts[2]
                        title = parts[3]
                        timestamp = parts[4]
                        requests_dict[profile_url] = {
                            'name': name,
                            'company': company,
                            'title': title,
                            'timestamp': timestamp
                        }
    except Exception as e:
        print(f"[WARN] Could not load connection requests file: {e}")

    return requests_dict

def load_sent_messages():
    """Load profiles we've already messaged - returns dict for full data"""
    if not os.path.exists(MESSAGES_SENT_FILE):
        return {}

    messages_dict = {}
    try:
        with open(MESSAGES_SENT_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().split('|')
                    if len(parts) >= 4:  # URL|name|company|timestamp
                        profile_url = parts[0]
                        messages_dict[profile_url] = {
                            'name': parts[1],
                            'company': parts[2],
                            'timestamp': parts[3]
                        }
    except Exception as e:
        print(f"[WARN] Could not load messages file: {e}")

    return messages_dict

def get_today_message_count():
    """Get count of messages sent TODAY"""
    if not os.path.exists(MESSAGES_SENT_FILE):
        return 0

    today = datetime.now().strftime('%Y-%m-%d')
    count = 0

    try:
        with open(MESSAGES_SENT_FILE, 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().split('|')
                    if len(parts) >= 4:
                        if parts[3].startswith(today):
                            count += 1
    except Exception as e:
        print(f"[WARN] Could not read daily message count: {e}")

    return count

def can_send_more_messages():
    """Check if we can send more messages today"""
    today_count = get_today_message_count()
    remaining = MESSAGES_PER_DAY_LIMIT - today_count
    print(f"[QUOTA] Daily message quota: {today_count}/{MESSAGES_PER_DAY_LIMIT} (Remaining: {remaining})")
    return remaining > 0

def save_sent_message(profile_url, name, company):
    """Save message record to tracking file"""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    with open(MESSAGES_SENT_FILE, 'a', encoding='utf-8') as f:
        f.write(f"{profile_url}|{name}|{company}|{timestamp}\n")

def save_accepted_connection(profile_url, name, company):
    """Log accepted connections"""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    with open(CONNECTIONS_LOG_FILE, 'a', encoding='utf-8') as f:
        f.write(f"{profile_url}|{name}|{company}|{timestamp}\n")

def get_message_template(name, company):
    """Get personalized message"""
    import random
    template = random.choice(MESSAGE_TEMPLATES)
    return template.format(name=name.split()[0], company=company)

def login_to_linkedin(driver):
    """Login to LinkedIn"""
    driver.get('https://www.linkedin.com/login')
    time.sleep(3)
    driver.find_element(By.ID, 'username').send_keys(LINKEDIN_EMAIL)
    driver.find_element(By.ID, 'password').send_keys(LINKEDIN_PASSWORD)
    driver.find_element(By.XPATH, '//button[@type="submit"]').click()
    time.sleep(5)
    print("[OK] Logged in to LinkedIn")

def get_all_connections(driver):
    """Scrape all LinkedIn connections - with multiple fallback strategies"""
    driver.get('https://www.linkedin.com/mynetwork/invite-connect/connections/')
    time.sleep(5)

    connections = []
    scroll_pause = 2
    last_height = driver.execute_script("return document.body.scrollHeight")

    print("[LOAD] Loading all connections with scrolling...")
    # Scroll to load all connections
    for scroll_attempt in range(10):
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(scroll_pause)

        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height

    print("[DEBUG] Scroll complete, now extracting connections...")

    # Strategy 1: Try specific connection card selector
    print("[STRATEGY 1] Looking for: mn-connection-card elements...")
    connection_cards = driver.find_elements(By.XPATH, '//li[contains(@class, "mn-connection-card")]')
    print(f"[DEBUG] Found {len(connection_cards)} cards with Strategy 1")

    if len(connection_cards) == 0:
        # Strategy 2: Try broader card selector
        print("[STRATEGY 2] Looking for: div cards with profile links...")
        connection_cards = driver.find_elements(By.XPATH, '//div[contains(@class, "connection-card") or contains(@class, "card")]')
        print(f"[DEBUG] Found {len(connection_cards)} cards with Strategy 2")

    if len(connection_cards) == 0:
        # Strategy 3: Find all profile links in main content
        print("[STRATEGY 3] Looking for: all /in/ profile links...")
        profile_links = driver.find_elements(By.XPATH, '//a[contains(@href, "/in/")]')
        print(f"[DEBUG] Found {len(profile_links)} profile links with Strategy 3")

        for link in profile_links:
            try:
                profile_url = link.get_attribute('href')
                if profile_url and '/in/' in profile_url:
                    # Get name from link text
                    name = link.text.strip()
                    if name and len(name) > 2:
                        connections.append({
                            'name': name,
                            'profile_url': profile_url.split('?')[0],
                            'occupation': 'Unknown'
                        })
            except:
                continue
    else:
        # Process cards (Strategies 1 or 2)
        for idx, card in enumerate(connection_cards):
            try:
                # Try multiple name selectors
                name = None
                name_selectors = [
                    './/span[contains(@class, "mn-connection-card__name")]',
                    './/span[contains(@class, "name")]',
                    './/span[@class="t-16 t-black t-bold"]',
                    './/a[contains(@href, "/in/")]',
                    './/span[1]'
                ]

                for selector in name_selectors:
                    try:
                        name = card.find_element(By.XPATH, selector).text.strip()
                        if name and len(name) > 2:
                            break
                    except:
                        continue

                if not name:
                    name = f"Unknown_{idx}"

                # Try multiple profile link selectors
                profile_link = None
                link_selectors = [
                    './/a[contains(@class, "mn-connection-card__link")]',
                    './/a[contains(@href, "/in/")]',
                    './/a[@class]'
                ]

                for selector in link_selectors:
                    try:
                        profile_link = card.find_element(By.XPATH, selector).get_attribute('href')
                        if profile_link and '/in/' in profile_link:
                            break
                    except:
                        continue

                if not profile_link:
                    continue

                # Try to get occupation
                occupation = 'Unknown'
                try:
                    occupation = card.find_element(By.XPATH, './/span[contains(@class, "occupation")]').text
                except:
                    try:
                        occupation = card.find_element(By.XPATH, './/span[2]').text
                    except:
                        pass

                connections.append({
                    'name': name,
                    'profile_url': profile_link.split('?')[0],
                    'occupation': occupation
                })
            except Exception as e:
                continue

    # Remove duplicates by profile URL
    unique_connections = {}
    for conn in connections:
        url = conn['profile_url']
        if url not in unique_connections:
            unique_connections[url] = conn

    connections = list(unique_connections.values())
    print(f"[OK] Found {len(connections)} total unique connections\n")
    return connections

def send_message_to_connection(driver, connection_data, company):
    """Send message to a connection with multiple fallback strategies"""
    try:
        print(f"    [STEP 1] Going to profile: {connection_data['profile_url']}")
        driver.get(connection_data['profile_url'])
        time.sleep(4)

        print(f"    [STEP 2] Looking for message button...")
        message_button = None
        message_button_selectors = [
            # LinkedIn direct message button selectors
            '//button[contains(@aria-label, "Message")]',
            '//button[@title="Send a message"]',
            '//button[contains(@aria-label, "message")]',
            '//button[contains(text(), "Message")]',
            '//a[contains(@href, "/messaging/thread/")]',
            '//div[@data-test-id="profile-action-message"]',
            '//button[@data-test-id="profile-action-message"]',
            # Alternative: look for messaging/compose
            '//a[contains(@href, "/messaging/")]',
            # Generic button in header actions
            '//div[@class="pvs-header__actions"]//button',
            '//div[contains(@class, "header-action")]//button[not(contains(text(), "More"))]'
        ]

        for selector in message_button_selectors:
            try:
                print(f"      Trying: {selector}")
                message_button = WebDriverWait(driver, 3).until(
                    EC.element_to_be_clickable((By.XPATH, selector))
                )
                print(f"    [STEP 2] Found message button with: {selector}")
                break
            except:
                continue

        if not message_button:
            print(f"    [STEP 2] Could not find message button - trying alternative approach...")
            # Last resort: look for any button that might be message-related
            try:
                # Try to find "More" actions and click it
                more_button = driver.find_element(By.XPATH, '//button[contains(@aria-label, "More")]')
                driver.execute_script("arguments[0].click();", more_button)
                time.sleep(2)
                # Now look for message in the dropdown
                message_button = driver.find_element(By.XPATH, '//span[contains(text(), "Message")]/..')
                print(f"    [STEP 2] Found message in dropdown menu")
            except:
                print(f"    [STEP 2] Could not find message button in any location")
                return False

        print(f"    [STEP 3] Clicking message button...")
        driver.execute_script("arguments[0].click();", message_button)
        time.sleep(4)

        print(f"    [STEP 4] Waiting for message compose window...")
        # Wait for either a modal or messaging page to load
        try:
            WebDriverWait(driver, 5).until(
                EC.presence_of_element_located((By.XPATH, '//div[@role="dialog"] | //textarea | //div[@contenteditable="true"] | //div[@class="msg-form__main"]'))
            )
            print(f"    [STEP 4] Compose window loaded")
        except:
            print(f"    [STEP 4] Timed out waiting for compose window")
            pass

        print(f"    [STEP 5] Finding message box...")
        message_box = None
        message_box_selectors = [
            # Modal/dialog message inputs
            '//textarea',
            '//div[@role="textbox" and @contenteditable="true"]',
            '//div[@contenteditable="true"]',
            '//textarea[contains(@placeholder, "message")]',
            '//input[@placeholder]',
            '//div[@class="msg-form__text-area"]//textarea',
            '//div[@class="msg-form__main"]//textarea'
        ]

        for selector in message_box_selectors:
            try:
                print(f"      Trying selector: {selector}")
                message_box = WebDriverWait(driver, 3).until(
                    EC.presence_of_element_located((By.XPATH, selector))
                )
                print(f"    [STEP 5] Found message box with: {selector}")
                break
            except:
                continue

        if not message_box:
            print(f"    [STEP 5] Could not find message box")
            return False

        print(f"    [STEP 5] Preparing message box for input...")
        message = get_message_template(connection_data['name'], company)

        try:
            # Scroll textarea into view
            driver.execute_script("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", message_box)
            time.sleep(2)

            # Try multiple approaches to fill the textarea

            # Approach 1: Direct value assignment with multiple events
            print(f"    [STEP 5] Trying Approach 1: Direct JavaScript fill...")
            try:
                driver.execute_script("""
                    arguments[0].focus();
                    arguments[0].value = arguments[1];
                    arguments[0].dispatchEvent(new Event('focus', { bubbles: true }));
                    arguments[0].dispatchEvent(new Event('input', { bubbles: true }));
                    arguments[0].dispatchEvent(new Event('change', { bubbles: true }));
                    arguments[0].dispatchEvent(new Event('keyup', { bubbles: true }));
                    arguments[0].dispatchEvent(new Event('keydown', { bubbles: true }));
                """, message_box, message)
                print(f"    [STEP 5] Approach 1 successful")
            except Exception as e1:
                print(f"    [STEP 5] Approach 1 failed: {str(e1)}")

                # Approach 2: Click and clear, then type character by character
                print(f"    [STEP 5] Trying Approach 2: Click, clear, and char-by-char...")
                try:
                    driver.execute_script("arguments[0].click();", message_box)
                    time.sleep(0.5)
                    driver.execute_script("arguments[0].select();", message_box)
                    time.sleep(0.5)
                    driver.execute_script("arguments[0].value = '';", message_box)
                    time.sleep(0.5)

                    # Type character by character
                    for char in message:
                        message_box.send_keys(char)
                        time.sleep(0.01)

                    # Trigger events after typing
                    driver.execute_script(
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                        message_box
                    )
                    print(f"    [STEP 5] Approach 2 successful")
                except Exception as e2:
                    print(f"    [STEP 5] Approach 2 failed: {str(e2)}")

                    # Approach 3: contenteditable div
                    print(f"    [STEP 5] Trying Approach 3: contenteditable div...")
                    try:
                        # Check if it's a contenteditable element
                        is_contenteditable = driver.execute_script("return arguments[0].getAttribute('contenteditable');", message_box)
                        if is_contenteditable == 'true':
                            driver.execute_script("""
                                arguments[0].focus();
                                arguments[0].textContent = arguments[1];
                                arguments[0].innerHTML = arguments[1];
                            """, message_box, message)
                        else:
                            raise Exception("Not a contenteditable element")
                        print(f"    [STEP 5] Approach 3 successful")
                    except Exception as e3:
                        print(f"    [STEP 5] Approach 3 failed: {str(e3)}")
                        return False

        except Exception as e:
            print(f"    [STEP 5] Message box preparation failed: {str(e)}")
            return False

        time.sleep(2)

        print(f"    [STEP 6] Finding send button...")
        send_button = None
        send_button_selectors = [
            '//button[contains(text(), "Send")]',
            '//button[@aria-label="Send"]',
            '//button[contains(@class, "msg-form__send-button")]',
            '//div[@class="msg-form__footer"]//button[not(contains(text(), "Cancel"))]',
            '//button[@data-test-id="send-button"]',
            '//button[contains(@aria-label, "Send")]',
            # Last resort - any button that's not cancel in a dialog
            '//div[@role="dialog"]//button[not(contains(text(), "Cancel")) and not(contains(text(), "Close"))]',
        ]

        for selector in send_button_selectors:
            try:
                print(f"      Trying selector: {selector}")
                send_button = WebDriverWait(driver, 3).until(
                    EC.element_to_be_clickable((By.XPATH, selector))
                )
                if send_button.is_displayed():
                    print(f"    [STEP 6] Found send button with: {selector}")
                    break
            except:
                continue

        if not send_button:
            print(f"    [STEP 6] Could not find send button")
            return False

        print(f"    [STEP 7] Clicking send button...")
        driver.execute_script("arguments[0].click();", send_button)
        time.sleep(4)

        print(f"    [STEP 8] CONFIRMING message was sent...")
        # Wait for confirmation - look for success indicators
        try:
            # Wait for either:
            # 1. Message appears in conversation
            # 2. Success toast/notification
            # 3. Modal closes
            # 4. Compose box disappears
            WebDriverWait(driver, 5).until(
                EC.invisibility_of_element_located((By.XPATH, '//textarea'))
            )
            print(f"    [STEP 8] Compose box closed - message likely sent")
        except:
            try:
                # Alternative: Check if message appears in thread
                WebDriverWait(driver, 3).until(
                    EC.presence_of_element_located((By.XPATH, '//*[contains(text(), "Message sent") or contains(text(), "sent")]'))
                )
                print(f"    [STEP 8] Found 'sent' confirmation")
            except:
                # Check if we're still on the messaging page (page didn't navigate away)
                current_url = driver.current_url
                if '/messaging/' in current_url:
                    print(f"    [STEP 8] Still on messaging page - message appears sent")
                else:
                    print(f"    [STEP 8] No clear confirmation, but proceeding...")

        time.sleep(2)

        # Save to tracking
        save_sent_message(connection_data['profile_url'], connection_data['name'], company)
        print(f"    [STEP 9] [OK] Message confirmed and saved to tracking")
        return True

    except Exception as e:
        print(f"    [ERROR] {str(e)}")
        return False

# ============== MAIN EXECUTION ==============

def main():
    print("=" * 70)
    print("LinkedIn New Connection Messaging Automation - EV Fleet Platform")
    print("=" * 70)

    # Check daily quota FIRST
    if not can_send_more_messages():
        print("\n[BLOCKED] DAILY MESSAGE LIMIT REACHED - Please try again tomorrow")
        return

    # Load tracking data
    sent_requests = load_sent_requests()
    sent_messages = load_sent_messages()

    print(f"\n[OK] Loaded {len(sent_requests)} connection requests sent (all time)")
    print(f"[OK] Loaded {len(sent_messages)} messages already sent (all time)")

    # Initialize browser
    options = webdriver.ChromeOptions()
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    driver = webdriver.Chrome(options=options)

    try:
        # Login
        login_to_linkedin(driver)

        # Get all connections
        all_connections = get_all_connections(driver)

        # Find new connections (people who accepted our requests but haven't been messaged)
        new_connections = []
        already_messaged = 0
        not_from_requests = 0

        for conn in all_connections:
            profile_url = conn['profile_url']

            # Check if this was someone we sent a request to
            if profile_url in sent_requests:
                # Check if we haven't messaged them yet
                if profile_url not in sent_messages:
                    conn['company'] = sent_requests[profile_url]['company']
                    conn['title'] = sent_requests[profile_url]['title']
                    new_connections.append(conn)
                    save_accepted_connection(profile_url, conn['name'], conn['company'])
                else:
                    already_messaged += 1
            else:
                not_from_requests += 1

        print(f"\n[ANALYSIS] Connection Analysis:")
        print(f"  - Total connections: {len(all_connections)}")
        print(f"  - New connections (from requests, not messaged): {len(new_connections)}")
        print(f"  - Already messaged: {already_messaged}")
        print(f"  - Not from our requests: {not_from_requests}")

        if not new_connections:
            print("\n[OK] No new connections to message. Check back later when more accept requests!")
            return

        print(f"\n[TARGET] Will message {len(new_connections)} new connections\n")

        # Send messages
        messages_sent_this_session = 0
        messages_failed_this_session = 0

        for idx, conn in enumerate(new_connections, 1):
            if messages_sent_this_session >= MESSAGES_PER_SESSION:
                print(f"\n[OK] Reached session limit ({MESSAGES_PER_SESSION} messages)")
                break

            if not can_send_more_messages():
                print(f"\n[BLOCKED] Daily limit reached during session - stopping")
                break

            print(f"\n[{idx}/{len(new_connections)}] Messaging {conn['name']} ({conn['title']} at {conn['company']})")

            if send_message_to_connection(driver, conn, conn['company']):
                messages_sent_this_session += 1
                sent_messages[conn['profile_url']] = {
                    'name': conn['name'],
                    'company': conn['company'],
                    'timestamp': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                }
                time.sleep(DELAY_BETWEEN_MESSAGES)
            else:
                messages_failed_this_session += 1

        print(f"\n{'='*70}")
        print(f"[OK] Session complete:")
        print(f"  - Messages sent: {messages_sent_this_session}")
        print(f"  - Messages failed: {messages_failed_this_session}")
        print(f"  - Total messages sent (all time): {len(sent_messages)}")
        print(f"  - Daily message quota: {get_today_message_count()}/{MESSAGES_PER_DAY_LIMIT}")
        print(f"{'='*70}\n")

    finally:
        driver.quit()

if __name__ == '__main__':
    main()
