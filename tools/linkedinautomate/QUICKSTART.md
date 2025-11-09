# Quick Start Guide - 5 Minutes Setup

## Step 1: Install Dependencies (1 minute)
```bash
pip install selenium
```

Download ChromeDriver: https://chromedriver.chromium.org/

## Step 2: Configure (2 minutes)

Open `script1.py` and `script2.py`, find this section:
```python
LINKEDIN_EMAIL = 'your_email@example.com'
LINKEDIN_PASSWORD = 'your_password'
```

Replace with YOUR LinkedIn credentials.

## Step 3: Add Companies (2 minutes)

In `script1.py`, find:
```python
TARGET_COMPANIES = [
    'Zomato', 'Swiggy', 'Delhivery',  # ... etc
]
```

Add all your 800+ target companies here.

---

## Now Run It

### Day 1: Send Connection Requests
```bash
python script1.py
```

**What you'll see**:
- Logs into LinkedIn
- Searches for fleet managers at your companies
- Sends ~20 personalized connection requests
- Shows: "✓ Sent 18 new connection requests"

**Output Files Created**:
- `connection_requests_sent.txt` - Tracks who you sent requests to

### Daily (After ~3+ days): Message Acceptances
```bash
python script2.py
```

**What you'll see**:
- Loads all your LinkedIn connections
- Finds who accepted YOUR requests
- Sends them personalized follow-up messages
- Shows: "✓ Sent 5 messages"

**Output Files Updated**:
- `messages_sent.txt` - Tracks who you've messaged

---

## Weekly Schedule

```
Monday:    python script1.py  (send 20 requests)
Tuesday:   python script1.py  (send 20 requests)
Wednesday: python script1.py  (send 20 requests)
Thursday:  python script1.py  (send 20 requests)
Friday:    python script1.py  (send 20 requests)
           python script2.py  (message new connections)

Saturday & Sunday: Rest (or run script2.py again)
```

**Total**: 100 connection requests/week + unlimited messaging

---

## Track Your Progress

### See All Requests Sent
```bash
wc -l connection_requests_sent.txt
```

### See All Messages Sent
```bash
wc -l messages_sent.txt
```

### Check Quota
Run the scripts - they show quota at top:
```
📊 Weekly quota: 45/100 (Remaining: 55)
```

---

## Troubleshooting

**"Login failed"**
- Check credentials in script
- Make sure LinkedIn isn't blocking automation (use 2FA-free account)

**"No results found"**
- Company name might be different on LinkedIn
- Try exact spelling from LinkedIn

**"Already sent to X - skipping"**
- Good! Duplicate detection is working

**"No new connections to message"**
- Wait 1-7 days for requests to be accepted
- Check back tomorrow

---

## That's It!

Two scripts. Two commands. Done.

1. `python script1.py` - Find & request
2. `python script2.py` - Follow up & message

Everything else is tracked automatically.

---

For more details, see:
- `README.md` - Full documentation
- `IMPROVEMENTS_SUMMARY.md` - What we fixed
