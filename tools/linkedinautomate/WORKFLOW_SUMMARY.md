# LinkedIn Automation - Complete Workflow

## Overview

This automation system consists of **2 coordinated scripts** that work together to conduct B2B outreach on LinkedIn:

- **Script 1 (run.bat)**: Sends connection requests to prospects
- **Script 2 (run_script2.bat)**: Sends follow-up messages to accepted connections

---

## Script 1: Connection Request Automation

### What It Does
Searches for fleet managers, operations heads, and logistics decision-makers at 800+ target companies (Zomato, Swiggy, Blinkit, Flipkart, Amazon, Zypp Electric, etc.) and sends them personalized connection requests.

### How to Run
```
Double-click: run.bat
```

### What Happens
1. Opens Chrome browser
2. Logs in to LinkedIn with credentials
3. Searches for prospects: `[Company] + [Job Title Keyword]`
4. For each prospect found:
   - Clicks Connect button
   - Adds personalized note about EV fleet management
   - Submits connection request
5. Tracks all sent requests to avoid duplicates
6. Saves progress to `connection_requests_sent.txt`

### Key Features
- **Target Companies**: 800+ logistics/e-commerce companies
- **Target Titles**: Fleet Manager, COO, Operations Head, Sustainability Officer, CTO, etc.
- **Personalized Notes**: 5 templates mentioning prospect name and company
- **Weekly Limit**: 100 connection requests per week (LinkedIn safety measure)
- **Session Limit**: Up to 100 per session
- **Tracking**: Pipe-delimited format (URL|Name|Company|Title|Timestamp)
- **Duplicate Prevention**: Checks if request already sent before attempting

### Output Files
```
connection_requests_sent.txt
├─ URL|Name|Company|Title|Timestamp
├─ https://linkedin.com/in/john-doe/|John Doe|Zomato|Fleet Manager|2025-11-08 14:30:45
└─ ...
```

### Quotas
```
WEEKLY LIMIT = 100 requests
  ├─ Resets every 7 days (rolling window)
  ├─ Each request takes ~15-20 seconds
  └─ Typical session: 1-2 hours for 100 requests

REQUESTS_PER_SESSION = 100 (can send all weekly limit in one session)
DELAY_BETWEEN_REQUESTS = 15 seconds (prevents rate limiting)
```

### Troubleshooting

| Problem | Solution |
|---------|----------|
| 0 results found | Check if LinkedIn changed HTML structure, review search filters |
| Request fails for some profiles | Normal - some users have messaging disabled |
| Daily quota exceeded | Wait 24 hours or check `connection_requests_sent.txt` |
| Chrome crashes | Update ChromeDriver to match Chrome version |

---

## Script 2: Follow-up Message Automation

### What It Does
After 3-7 days when people accept your connection requests, automatically sends them personalized follow-up messages about the EV fleet management platform.

### How to Run
```
Double-click: run_script2.bat
```

### What Happens
1. Opens Chrome browser
2. Logs in to LinkedIn
3. Gets all accepted connections
4. For each connection that:
   - You sent a request to (in `connection_requests_sent.txt`)
   - Accepted the request
   - Hasn't received a message yet
5. Sends personalized follow-up message
6. Records message in `messages_sent.txt`
7. Logs interaction to `accepted_connections.txt`

### Key Features
- **Checks Acceptance**: Only messages people who accepted your connection request
- **Smart Deduplication**: Skips people you've already messaged
- **Personalized Messages**: 2 templates with name and company customization
- **Daily Limits**: 50 messages per day max (LinkedIn safety)
- **Session Limits**: 30 messages per session
- **Message Delay**: 20 seconds between messages (prevents rate limiting)
- **Tracking**: Pipe-delimited format matching connection_requests_sent.txt

### Output Files
```
messages_sent.txt
├─ URL|Name|Company|Timestamp
├─ https://linkedin.com/in/john-doe/|John Doe|Zomato|2025-11-09 15:45:30
└─ ...

accepted_connections.txt
├─ URL|Name|Company|Timestamp
├─ https://linkedin.com/in/john-doe/|John Doe|Zomato|2025-11-09 14:50:15
└─ ...
```

### Message Templates
```
Template 1:
"Hi {name},

Thanks for connecting! I'm working on an EV fleet management platform
specifically for Indian logistics companies.

I'd love to learn about the operational challenges you face with EV adoption—
particularly around charging infrastructure, battery health monitoring, and
route optimization.

Would you be open to a 10-minute call this week?"

Template 2:
"Hello {name},

Great to connect! I noticed you're involved in {company}'s fleet operations.

I'm building a solution to help logistics companies manage EV fleets more
efficiently—addressing pain points like charging downtime, maintenance costs,
and utilization tracking.

Would you be interested in a brief conversation about the challenges you're
experiencing?"
```

### Quotas
```
MESSAGES_PER_DAILY_LIMIT = 50 (LinkedIn safety)
MESSAGES_PER_SESSION = 30 (safe per-session limit)
DELAY_BETWEEN_MESSAGES = 20 seconds

When to Run Script 2:
├─ Start after 3-7 days (when people accept requests)
├─ Run every 2-3 days as more people accept
└─ Keep running until no new connections to message
```

### Troubleshooting

| Problem | Solution |
|---------|----------|
| No new connections to message | Wait 3-7 days for people to accept requests |
| Message not sent (fails on some profiles) | LinkedIn may have closed DM, try again later |
| Daily limit exceeded | Wait 24 hours or reduce session size |
| Wrong person messaged | Check `messages_sent.txt` - likely wrong profile match |

---

## Complete Weekly Workflow

### Week 1: Send Requests

**Day 1-2**: Monday/Tuesday
```
1. Double-click: run.bat
2. Send ~50-100 connection requests
3. Watch for any errors or blocks
```

**Day 3-4**: Wednesday/Thursday
```
1. Check if LinkedIn lifted any temporary blocks
2. Double-click: run.bat again
3. Send next batch (if weekly limit allows)
```

**Day 5**: Friday
```
1. Final run of run.bat for the week
2. Aim for ~100 total requests by week's end
```

### Week 2+: Send Messages

**Day 3-5** (of original request):
```
1. People start accepting requests
2. Double-click: run_script2.bat
3. Send messages to new connections
4. Repeat every 2-3 days
```

**Ongoing**:
```
├─ Keep track of Weekly quota in connection_requests_sent.txt
├─ Send messages to new connections as they accept
└─ When weekly reset happens, start new run.bat cycle
```

---

## File Structure

```
linkedinautomate/
├── run.bat                          ← Run Script 1 (send requests)
├── run_script2.bat                  ← Run Script 2 (send messages)
├── script1.py                       ← Connection request automation
├── script2.py                       ← Follow-up message automation
├── chromedriver.exe                 ← Chrome automation driver
├── connection_requests_sent.txt     ← Tracking: sent requests
├── messages_sent.txt                ← Tracking: sent messages
├── accepted_connections.txt         ← Log: accepted connections
│
├── START_HERE.txt                   ← Quick start guide
├── README.md                        ← Full documentation
├── QUICKSTART.md                    ← 5-minute setup
├── IMPROVEMENTS_SUMMARY.md          ← What was fixed
└── test_login.py                    ← Test login functionality
```

---

## Key Configuration (in script files)

### Script 1 (script1.py)
```python
LINKEDIN_EMAIL = 'ommanoj88@gmail.com'
LINKEDIN_PASSWORD = 'Shobharain11@'
WEEKLY_CONNECTION_LIMIT = 100
REQUESTS_PER_SESSION = 100
DELAY_BETWEEN_REQUESTS = 15 seconds
```

### Script 2 (script2.py)
```python
LINKEDIN_EMAIL = 'ommanoj88@gmail.com'
LINKEDIN_PASSWORD = 'Shobharain11@'
MESSAGES_PER_SESSION = 30
MESSAGES_PER_DAY_LIMIT = 50
DELAY_BETWEEN_MESSAGES = 20 seconds
```

---

## Safety Notes

1. **LinkedIn Rate Limits**: Don't exceed 100 requests/week or 50 messages/day
2. **2FA**: Have your phone ready in case LinkedIn asks for verification
3. **Human-Like Behavior**: Scripts include delays to appear natural
4. **Temporary Blocks**: If blocked, wait 24 hours before trying again
5. **Monitor Progress**: Check output files regularly to verify things are working

---

## What to Do If Something Goes Wrong

1. **Check START_HERE.txt** for quick troubleshooting
2. **Review output files** to see what was attempted
3. **Check Chrome window** for any prompts or errors
4. **Verify credentials** match your actual LinkedIn account
5. **Update ChromeDriver** to match your Chrome version
6. **Wait before retrying** (LinkedIn has temporary blocks for heavy activity)

---

## Next Steps

1. ✓ Get ChromeDriver (https://chromedriver.chromium.org/)
2. ✓ Double-click run.bat to send first batch of requests
3. ✓ Wait 3-7 days for acceptances
4. ✓ Double-click run_script2.bat to send follow-up messages
5. ✓ Repeat weekly to maintain pipeline of new connections and messages

---

**Created**: Nov 8, 2025
**Scripts Version**: 1.0 - Stable
**Last Updated**: Verified and documented complete 2-script workflow
