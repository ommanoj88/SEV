# MCP Commands Reference

## 🎯 Quick Commands

### Check Status
```bash
claude mcp list              # List all MCPs
/mcp                         # Check in Claude Code
```

### List MCPs by Category

| Category | Command | Count |
|----------|---------|-------|
| Development | GitHub, Sentry, Socket, Jam, Hugging Face | 5 |
| Deployment | Vercel, Netlify, Cloudflare | 3 |
| Payments | Stripe, PayPal, Square, Plaid | 4 |
| Projects | Linear, Asana, Notion, Monday, Atlassian, Intercom | 6 |
| CRM | HubSpot, Box | 2 |
| Design | Figma, Canva, Cloudinary | 3 |
| Security | Stytch | 1 |
| Automation | Workato | 1 |

---

## ⚙️ MCP Operations

### Add MCP
```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

### Get Details
```bash
claude mcp get github
```

### Remove MCP
```bash
claude mcp remove github
```

### List All
```bash
claude mcp list
```

---

## 🚀 Common Commands

### GitHub
```
/mcp__github__list_repos
/mcp__github__list_prs
/mcp__github__create_issue "Title"
/mcp__github__create_branch "feature/name"
```

### Sentry
```
/mcp__sentry__errors_last_24_hours
/mcp__sentry__errors_by_service "service-name"
/mcp__sentry__get_error "error-id"
```

### Linear
```
/mcp__linear__list_issues
/mcp__linear__create_issue "Title"
/mcp__linear__update_issue "id"
```

### Stripe
```
/mcp__stripe__recent_transactions
/mcp__stripe__create_payment
/mcp__stripe__get_transaction "txn_id"
```

### Vercel
```
/mcp__vercel__deployments
/mcp__vercel__deploy
/mcp__vercel__deployment_status
```

### HubSpot
```
/mcp__hubspot__get_contacts
/mcp__hubspot__create_contact
/mcp__hubspot__get_companies
```

---

## 📌 Resource References

### @ Mention Resources
```
@github:issue://123
@github:pr://456
@sentry:issue://789
@stripe:payment://txn_id
@notion:doc://page-id
@figma:file://design-id
@linear:issue://id
```

### Syntax
```
@service:type://resource-id
```

---

## 🎯 By Role

### Frontend Developer Commands
```bash
# Design
/mcp__figma__list_files
/mcp__canva__browse_designs

# Deploy
/mcp__vercel__deployments
/mcp__netlify__status

# Code
/mcp__github__list_prs
/mcp__github__create_branch
```

### Backend Developer Commands
```bash
# Error tracking
/mcp__sentry__errors_last_hour
/mcp__sentry__errors_by_service "api-gateway"

# Code management
/mcp__github__create_issue
/mcp__github__list_commits

# Issue tracking
/mcp__linear__create_issue
/mcp__linear__list_issues
```

### DevOps Engineer Commands
```bash
# Deployment
/mcp__vercel__deploy
/mcp__netlify__deploy
/mcp__cloudflare__analytics

# Monitoring
/mcp__sentry__dashboard
/mcp__sentry__performance_metrics
```

### Project Manager Commands
```bash
# Issues
/mcp__linear__create_issue
/mcp__linear__list_issues

# Tasks
/mcp__asana__create_task
/mcp__asana__list_tasks

# Boards
/mcp__monday__add_item
/mcp__monday__update_board
```

---

## 💡 Quick Workflows

### Debug Production Bug
```bash
# Step 1: Check errors
/mcp__sentry__errors_last_24_hours

# Step 2: Create issue
/mcp__github__create_issue "Fix production bug"

# Step 3: Review PR
@github:pr://123

# Step 4: Deploy fix
/mcp__vercel__deploy
```

### Process Payment
```bash
# Step 1: Create payment
/mcp__stripe__create_payment

# Step 2: Verify
/mcp__paypal__verify_transaction

# Step 3: Log
/mcp__notion__create_page
```

### Team Workflow
```bash
# Create issue
/mcp__linear__create_issue "Feature request"

# Assign task
/mcp__asana__create_task

# Track in board
/mcp__monday__add_item
```

---

## 🔐 Authentication

### Authenticate Service
```
/mcp in Claude Code
→ Select service
→ Follow OAuth flow
```

### Verify Authentication
```
/mcp  # Check status
```

### Clear Authentication
```
/mcp
→ Select "Clear authentication"
```

---

## 🪟 Windows PowerShell Tips

### Set Environment Variables
```powershell
$env:STRIPE_API_KEY = "sk_..."
$env:GITHUB_TOKEN = "ghp_..."

# Verify
echo $env:STRIPE_API_KEY
```

### Run MCP List
```powershell
cd "c:\Users\omman\Desktop\SEV"
claude mcp list
```

---

## 📞 Command Help

### Get Service Info
```bash
claude mcp get github
```

### Check All Services
```bash
claude mcp list
```

### Get Status in Claude Code
```
/mcp
```

---

## 🎯 25 MCPs Available

**GitHub**, Sentry, Socket, Jam, Hugging Face, Vercel, Netlify, Cloudflare, Stripe, PayPal, Square, Plaid, Linear, Asana, Notion, Monday, Atlassian, Intercom, HubSpot, Box, Figma, Canva, Cloudinary, Stytch, Workato

---

**Need more help?** See `MCP_DOCS/REFERENCE/TROUBLESHOOTING.md`
