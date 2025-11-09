# LinkedIn B2B Prospecting Automation - EV Fleet Management

Two-script system for systematically finding and messaging potential customers in the EV fleet management space across 800+ target companies.

## What These Scripts Do

### Script 1: `script1.py` - Connection Request Sender
**Purpose**: Find decision-makers at target companies and send them personalized connection requests.

**Workflow**:
1. Searches LinkedIn for people at your 800+ target companies
2. Filters by decision-maker titles (Fleet Manager, COO, Operations Head, etc.)
3. Sends personalized connection requests ONLY to people you haven't requested before
4. Tracks all requests in `connection_requests_sent.txt`
5. Respects LinkedIn's weekly limit (100 requests/week)

**Key Features**:
- ✓ Avoids duplicate requests (checks tracking file)
- ✓ Weekly quota management (100 requests/week max)
- ✓ Personalized notes mentioning EV fleet challenges
- ✓ Timestamp tracking of when requests were sent
- ✓ Automatic detection and skipping of duplicates

### Script 2: `script2.py` - Message Sender
**Purpose**: Message new connections (people who accepted your requests) with follow-up questions about their pain points.

**Workflow**:
1. Fetches all your LinkedIn connections
2. Cross-references with `connection_requests_sent.txt` to find who accepted
3. Checks `messages_sent.txt` to avoid re-messaging
4. Sends personalized messages ONLY to new connections not yet messaged
5. Tracks all messages in `messages_sent.txt`

**Key Features**:
- ✓ Only messages new connections (people who accepted requests)
- ✓ Avoids duplicate messages
- ✓ Daily message quota (50 messages/day max)
- ✓ Detailed analysis of your connection pipeline
- ✓ Full timestamp and audit trail

## Setup & Configuration

### Prerequisites
```bash
pip install selenium
```

You'll also need:
- ChromeDriver (from https://chromedriver.chromium.org/)
- LinkedIn account with valid credentials

### Step 1: Configure Credentials

Edit both `script1.py` and `script2.py` and replace:

```python
LINKEDIN_EMAIL = 'your_email@example.com'
LINKEDIN_PASSWORD = 'your_password'
```

With your actual LinkedIn credentials.

### Step 2: Customize Target Companies

In `script1.py`, update `TARGET_COMPANIES` list with your 800+ target companies:

```python
TARGET_COMPANIES = [
    'Zomato', 'Swiggy', 'Delhivery',  # etc - add all 800+ companies
]
```

Companies are organized by category:
- Last-Mile Delivery & E-commerce
- Logistics & Transportation
- EV Fleet Operators & Leasing
- Corporate Fleets (FMCG, Telecom, Pharma)

### Step 3: Review Target Job Titles

In `script1.py`, `TARGET_TITLES` is pre-configured with decision-maker roles:
- Fleet Manager, COO, Operations Head
- Logistics Manager, VP Operations
- Sustainability Officer, CTO
- EV Fleet, Mobility Head

Customize as needed for your specific outreach.

## Tracking Files (Auto-Generated)

### `connection_requests_sent.txt`
Records every connection request sent.

Format: `URL|Name|Company|Title|Timestamp`

Example:
```
https://linkedin.com/in/john-doe|John Doe|Zomato|Fleet Manager|2024-11-08 10:15:30
https://linkedin.com/in/jane-smith|Jane Smith|Delhivery|COO|2024-11-08 10:20:45
```

**Purpose**: Script 1 checks this file to avoid sending duplicate requests.

### `messages_sent.txt`
Records every message sent to connections.

Format: `URL|Name|Company|Timestamp`

Example:
```
https://linkedin.com/in/john-doe|John Doe|Zomato|2024-11-08 11:30:20
https://linkedin.com/in/jane-smith|Jane Smith|Delhivery|2024-11-08 11:45:10
```

**Purpose**: Script 2 checks this file to avoid messaging the same person twice.

### `accepted_connections.txt`
Log of people who accepted your requests AND were messaged.

Format: `URL|Name|Company|Timestamp`

**Purpose**: Analytics - track your conversion from request → acceptance → message.

### `weekly_stats.json` (Future)
Will track weekly quotas and resets.

## How to Use

### Running Script 1: Send Connection Requests

```bash
python script1.py
```

**What happens**:
1. Shows current weekly quota status (e.g., "3/100 requests this week")
2. Logs in to LinkedIn
3. Searches for prospects at your companies
4. Sends personalized requests (up to 20 per run)
5. Shows summary with total sent and quota remaining

**Output**:
```
============================================================
LinkedIn Connection Request Automation - EV Fleet Platform
============================================================

📊 Weekly quota: 3/100 (Remaining: 97)
✓ Loaded 3 previously sent requests

✓ Logged in to LinkedIn

🔍 Searching: Zomato + Fleet Manager
  ✓ Sent request to John Doe (Fleet Manager at Zomato)
  ✓ Sent request to Jane Smith (Operations Head at Zomato)
  ⊘ Already sent to Mike Johnson - skipping

...

============================================================
✓ Session complete:
  - Sent: 18 new connection requests
  - Skipped: 2 (duplicates/errors)
  - Total all time: 21
  - Weekly quota: 21/100
============================================================
```

**Tips**:
- Run this script 5 times per week (20 × 5 = 100 requests/week)
- Best practice: Run Monday, Tuesday, Wednesday, Thursday, Friday
- Respect delays between runs (wait several hours between sessions)

### Running Script 2: Message New Connections

```bash
python script2.py
```

**What happens**:
1. Shows current daily message quota
2. Logs in to LinkedIn
3. Loads all your connections
4. Identifies which are from YOUR requests
5. Checks which haven't been messaged yet
6. Sends personalized follow-up messages

**Output**:
```
============================================================
LinkedIn New Connection Messaging Automation - EV Fleet Platform
============================================================

📊 Daily message quota: 12/50 (Remaining: 38)
✓ Loaded 21 connection requests sent (all time)
✓ Loaded 8 messages already sent (all time)

✓ Logged in to LinkedIn

📥 Loading all connections...
✓ Found 15 total connections

📊 Connection Analysis:
  - Total connections: 15
  - New connections (from requests, not messaged): 5
  - Already messaged: 3
  - Not from our requests: 7

🎯 Will message 5 new connections

[1/5] Messaging John Doe (Fleet Manager at Zomato)
  ✓ Sent message to John Doe (Zomato)

[2/5] Messaging Jane Smith (Operations Head at Zomato)
  ✓ Sent message to Jane Smith (Zomato)

...

============================================================
✓ Session complete:
  - Messages sent: 5
  - Messages failed: 0
  - Total messages sent (all time): 13
  - Daily message quota: 17/50
============================================================
```

**Tips**:
- Run once per day (preferably in morning)
- Messages have delays between them (20 seconds default)
- Safe daily limit: 50 messages/day

## LinkedIn Safety & Quotas

**Weekly Connection Request Limits**:
- Safe limit: 100 requests/week
- Script enforces: 5 sessions × 20 requests = 100/week
- Recommended: Spread across 5 weekdays

**Daily Message Limits**:
- Safe limit: 50 messages/day
- Script enforces: Max 30 per session, 50 total/day
- Recommended: Run once per day in morning

**LinkedIn's Anti-Spam Rules**:
- Scripts use delays between requests (15-20 seconds)
- Personalized messages avoid spam detection
- Chrome options disable automation detection
- Staggered timing makes it look human

## Workflow: Complete Pipeline

### Day 1-5 (Week 1): Connection Requests
- Run `script1.py` once per day
- Sends: 20 requests × 5 days = 100 requests
- Tracks: All in `connection_requests_sent.txt`

### Day 1-7 (Week 1): Messaging
- Run `script2.py` once daily
- Messages new acceptances
- Tracks: All in `messages_sent.txt`

### Week 2+: Continue Cycle
- More people accept → more to message
- Send new batches of connection requests
- Message all new acceptances

## Understanding the Data

### Conversion Tracking

Check which requests are converting:

```bash
# Shows requests that resulted in connections and messages
grep "<URL>" connection_requests_sent.txt  # Find request
grep "<URL>" messages_sent.txt             # Check if messaged
grep "<URL>" accepted_connections.txt      # Confirmed acceptance + message
```

### Pipeline Metrics

- **Total Requests**: Lines in `connection_requests_sent.txt`
- **Acceptance Rate**: (Lines in `accepted_connections.txt` / Total Requests) × 100
- **Message Rate**: (Lines in `messages_sent.txt` / Lines in `accepted_connections.txt`) × 100

Example:
- Sent 100 requests → 25 accepted → 22 messaged = 22% success rate

## Troubleshooting

### "Already sent to X - skipping"
✓ This is normal. Duplicate detection is working correctly.

### Script times out or fails
- LinkedIn may need a re-login
- Try running again after 10 minutes
- Check internet connection
- Verify Chrome/ChromeDriver version match

### No new connections to message
- Requests take 1-7 days for people to accept
- Check back daily
- `accepted_connections.txt` should grow over time

### Messages not being sent
- Connection may have messaging disabled
- You may have hit daily quota (check output)
- LinkedIn may be rate-limiting you (wait 1 hour, try again)

## Security Notes

⚠️ **Important**:
- Never commit credentials to version control
- Use environment variables in production:
  ```python
  import os
  LINKEDIN_EMAIL = os.getenv('LINKEDIN_EMAIL')
  LINKEDIN_PASSWORD = os.getenv('LINKEDIN_PASSWORD')
  ```
- Tracking files contain real LinkedIn URLs - keep private
- Follow LinkedIn's Terms of Service

## Next Steps

1. **Week 1**: Configure scripts with your 800+ target companies
2. **Week 2**: Start Script 1 (send connection requests)
3. **Week 3+**: Start Script 2 (message acceptances) once you have connections

## Support

If scripts fail:
1. Check error messages in console
2. Verify LinkedIn credentials are correct
3. Ensure ChromeDriver is in your PATH
4. Try running individual scripts separately
5. Check tracking files aren't corrupted (they're plain text)

---

**Last Updated**: November 8, 2024
**Status**: Production Ready - Both scripts tested and working
