# MCP Setup Guide

## ⚡ Quick Start (5 minutes)

### Step 1: Verify MCPs
```bash
cd c:\Users\omman\Desktop\SEV
claude mcp list
```
You should see 25 MCPs listed.

### Step 2: Authenticate
In Claude Code, type:
```
/mcp
```
Then authenticate with services you need.

### Step 3: Try It
```
/mcp__github__list_repos
/mcp__sentry__errors_last_24_hours
```

✅ Done! You're ready to use MCPs.

---

## 🔧 Complete Setup Instructions

### Prerequisites
- ✅ Claude Code installed
- ✅ `.mcp.json` in your project
- ✅ Node.js (optional, for some MCPs)

### Installation Options

#### Option 1: HTTP Servers (Recommended)
```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
claude mcp add --transport http sentry https://mcp.sentry.dev/mcp
```

#### Option 2: SSE Servers
```bash
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

#### Option 3: Already Pre-configured!
Your `.mcp.json` already has 25 MCPs configured. Just authenticate!

### List Configured MCPs
```bash
claude mcp list
```

### Get Server Details
```bash
claude mcp get github
```

### Remove Server (if needed)
```bash
claude mcp remove github
```

---

## 🔐 Authentication

### For Cloud Services
1. In Claude Code: `/mcp`
2. Select service to authenticate
3. Follow OAuth 2.0 flow in browser
4. OAuth tokens stored securely

### Environment Variables
Some MCPs need API keys:

```bash
# PowerShell
$env:STRIPE_API_KEY = "sk_..."
$env:GITHUB_TOKEN = "ghp_..."
```

### Verify Authentication
```bash
/mcp  # Check status in Claude Code
```

---

## 🎯 MCP Scopes

### Local Scope (Default)
- Project-specific
- Only you, current project
```bash
claude mcp add --scope local --transport http github ...
```

### Project Scope (Team)
- Shared via `.mcp.json`
- All team members access
```bash
claude mcp add --scope project --transport http github ...
```

### User Scope (Personal)
- All projects on your machine
- Personal utilities
```bash
claude mcp add --scope user --transport http github ...
```

---

## 📚 Your 25 Pre-Configured MCPs

Already in `.mcp.json`:

**Development (5):**
- GitHub, Sentry, Socket, Jam, Hugging Face

**Deployment (3):**
- Vercel, Netlify, Cloudflare

**Payments (4):**
- Stripe, PayPal, Square, Plaid

**Projects (6):**
- Linear, Asana, Notion, Monday, Atlassian, Intercom

**CRM (2):**
- HubSpot, Box

**Design (3):**
- Figma, Canva, Cloudinary

**Security (1):**
- Stytch

**Automation (1):**
- Workato

---

## ✅ Verification Checklist

- [ ] Run: `claude mcp list`
- [ ] Check: 25 MCPs shown
- [ ] Run: `/mcp` in Claude Code
- [ ] Authenticate with 3-5 services
- [ ] Try: `/mcp__github__list_repos`
- [ ] Try: `/mcp__sentry__errors_last_24_hours`
- [ ] Success: Both commands work

---

## 🚨 Troubleshooting

### MCP Server Not Found
```bash
claude mcp list
# If empty, MCPs not configured
# Copy .mcp.json to project root
```

### Authentication Failed
```
/mcp
# Select "Clear authentication"
# Re-authenticate
```

### Command Not Working
```bash
# Check server status
/mcp

# Verify configuration
claude mcp get github

# Check logs in Claude Code
```

### Windows Issues
For stdio servers, use `cmd /c` wrapper:
```bash
claude mcp add --transport stdio myserver -- cmd /c npx -y package
```

---

## 📖 Next Steps

1. **Setup:** Run `claude mcp list` ✓
2. **Authenticate:** Run `/mcp` ✓
3. **Reference:** Check `MCP_DOCS/REFERENCE/COMMANDS.md`
4. **Services:** Read `MCP_DOCS/SERVICES/MICROSERVICES_WORKFLOWS.md`

---

## 📞 Need Help?

- **Official Docs:** https://code.claude.com/docs/en/mcp
- **GitHub:** https://github.com/modelcontextprotocol/servers
- **Troubleshooting:** See `MCP_DOCS/REFERENCE/TROUBLESHOOTING.md`
