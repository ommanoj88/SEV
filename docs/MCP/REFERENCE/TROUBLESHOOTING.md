# Troubleshooting Guide

## 🔧 Common Issues & Solutions

### 1. MCP Not Showing Up After Installation

**Problem:** Run `claude mcp list` but your MCP isn't showing

**Solutions:**

```bash
# 1. Restart Claude
# Close and reopen Claude Code

# 2. Check if actually installed
claude mcp list | grep "your-mcp-name"

# 3. Manually reload MCPs
claude mcp reload

# 4. Check .mcp.json config
cat .mcp.json

# 5. Verify config is valid JSON
# Use a JSON validator or: Python -m json.tool .mcp.json
```

**Windows PowerShell Specific:**
```powershell
# Check MCPs
claude mcp list

# Reload
claude mcp reload

# Validate JSON
Get-Content .mcp.json | ConvertFrom-Json
```

---

### 2. "Authentication Failed" or "Invalid Token"

**Problem:** MCP commands fail with auth errors

**Solutions:**

#### Step 1: Verify Token Setup
```bash
# Check if environment variables are set (without displaying values)
test -n "$MYGITHUB_TOKEN" && echo "MYGITHUB_TOKEN: Set" || echo "MYGITHUB_TOKEN: Not set"
test -n "$SENTRY_AUTH_TOKEN" && echo "SENTRY_AUTH_TOKEN: Set" || echo "SENTRY_AUTH_TOKEN: Not set"
test -n "$LINEAR_API_KEY" && echo "LINEAR_API_KEY: Set" || echo "LINEAR_API_KEY: Not set"
test -n "$STRIPE_API_KEY" && echo "STRIPE_API_KEY: Set" || echo "STRIPE_API_KEY: Not set"
```

**Windows PowerShell:**
```powershell
# Check if environment variables are set (without displaying values)
if ($env:MYGITHUB_TOKEN) { "MYGITHUB_TOKEN: Set" } else { "MYGITHUB_TOKEN: Not set" }
if ($env:SENTRY_AUTH_TOKEN) { "SENTRY_AUTH_TOKEN: Set" } else { "SENTRY_AUTH_TOKEN: Not set" }
if ($env:LINEAR_API_KEY) { "LINEAR_API_KEY: Set" } else { "LINEAR_API_KEY: Not set" }
if ($env:STRIPE_API_KEY) { "STRIPE_API_KEY: Set" } else { "STRIPE_API_KEY: Not set" }
```

#### Step 2: Re-authenticate
```bash
# For each MCP, re-authenticate
claude mcp auth github          # Login to GitHub
claude mcp auth sentry          # Login to Sentry
claude mcp auth linear          # Login to Linear
claude mcp auth stripe          # Login to Stripe
```

#### Step 3: Check Token Expiry
- Some tokens expire after 30-90 days
- Regenerate new token from service dashboard
- Update environment variable
- Restart Claude

#### Step 4: Check MCP URL
```bash
# Verify URL is correct in .mcp.json
grep "url" .mcp.json
# Should show live URLs like https://mcp.github.com/mcp
```

---

### 3. Command Not Found or Returns Empty

**Problem:** Run command but get "command not found" or no results

**Solutions:**

#### Check Available Commands
```bash
# List all commands for MCP
claude mcp commands github
claude mcp commands linear
claude mcp commands stripe

# Or check docs
# See COMMANDS.md for full reference
```

#### Verify Syntax
```bash
# Wrong: claude github get-issues
# Right: claude mcp github list-issues

# See COMMANDS.md for exact syntax
```

#### Check Resource Names
```bash
# Wrong: get-issue project:MY_PROJECT issue:123
# Right: get-issue project:prod-123 issue:abc-456

# Use correct IDs from your service
claude mcp linear list-teams     # See available teams
claude mcp github list-repos     # See available repos
```

#### For Payment MCPs (Stripe/PayPal)
```bash
# Wrong: get-charge id:123
# Right: get-charge charge_id:ch_1234567890

# Include full IDs from dashboard
```

---

### 4. MCP Connection Timeout or Server Error

**Problem:** Getting timeout or 500 errors

**Solutions:**

```bash
# 1. Check internet connection
ping google.com

# 2. Try again (might be temporary)
# Wait 30 seconds, retry

# 3. Check MCP status
# Go to official docs URL to verify service is up

# 4. Check your network
# If behind VPN/proxy, might be blocked
# Try different network

# 5. Check rate limits
# If running many commands quickly, wait between them
```

---

### 5. Windows PowerShell Specific Issues

**Problem:** Commands work on Mac/Linux but fail on Windows

**Solutions:**

#### Issue: Special Characters in Paths
```powershell
# Wrong: claude mcp add --config C:\Users\Name\.claude\config
# Right: claude mcp add --config "C:\Users\Name\.claude\config"

# Always quote paths with spaces or special chars
```

#### Issue: Environment Variables Not Set
```powershell
# PowerShell syntax (NOT $GITHUB_TOKEN on Windows)
$env:MYGITHUB_TOKEN = "ghp_your_token_here"

# Verify it's set
Write-Host $env:MYGITHUB_TOKEN
```

#### Issue: JSON Validation
```powershell
# Validate .mcp.json
$json = Get-Content .mcp.json | ConvertFrom-Json
$json | ConvertTo-Json   # Should print without errors
```

#### Issue: File Encoding
```powershell
# If creating config files on Windows, ensure UTF-8
Set-Content -Path .mcp.json -Value $content -Encoding UTF8
```

---

### 6. "Permission Denied" or "Access Blocked"

**Problem:** MCP can't access resource or service

**Solutions:**

#### Check Permissions
```bash
# 1. Verify token has right scopes
# Go to GitHub/Linear/etc settings → Personal Access Tokens
# Check: repo, read:org, workflow, etc.

# 2. Regenerate token with all needed scopes
# Settings → Developer settings → Personal access tokens
# Click "Regenerate" and check ALL scopes

# 3. Add token to environment
export MYGITHUB_TOKEN="new_token_here"  # Linux/Mac
$env:MYGITHUB_TOKEN = "new_token_here"  # PowerShell
```

#### Check Resource Access
```bash
# 1. Verify you have access to resource
claude mcp github list-repos        # Should show your repos
claude mcp linear list-issues       # Should show your issues

# 2. If empty, check:
# - Are you using right account?
# - Do you have access to org/team?
# - Are resources shared with you?
```

---

### 7. "Unknown Transport" or MCP Not Working

**Problem:** MCP defined but not loading

**Solutions:**

```bash
# 1. Check transport type in .mcp.json
grep "transport" .mcp.json
# Should show: "http", "sse", or "stdio"

# 2. Verify URL format
# HTTP should be: https://mcp.service.com/mcp
# SSE should be: https://service.com/sse
# Should NOT end with query params or extra /

# 3. Test MCP URL directly
curl https://mcp.github.com/mcp    # Should return info, not 404
```

---

### 8. Multiple MCPs Conflicting or Interfering

**Problem:** Two MCPs have same command names

**Solutions:**

```bash
# 1. Use full MCP name
claude mcp github:list-issues        # Specify which MCP
claude mcp gitlab:list-issues        # Different MCP

# 2. Check command precedence in .mcp.json
# First matching MCP wins

# 3. Rename conflicting commands
# Edit .mcp.json to add prefixes or rename
```

---

### 9. Performance Issues - Commands Slow

**Problem:** MCP commands taking too long

**Solutions:**

```bash
# 1. Check network speed
ping mcp.service.com

# 2. Reduce result size
# Instead of: list-issues
# Use: list-issues --limit 10

# 3. Filter early
# Instead of: list-all | grep "open"
# Use: list-issues --filter "state:open"

# 4. Close unused MCPs
# Disable MCPs you don't use
# Edit .mcp.json, set "enabled": false

# 5. Check Claude memory
# Restart Claude if running many commands
```

---

### 10. MCP Accidentally Deleted or Won't Load

**Problem:** Accidentally removed MCP or config corrupted

**Solutions:**

```bash
# 1. Restore from backup
# If you have backup of .mcp.json, restore it

# 2. Recreate MCP entry
# Copy entry from ./mcp-docs/REFERENCE/MCP_LIST.md
# Add back to .mcp.json

# 3. Validate JSON before saving
# Use JSON validator
# python -m json.tool .mcp.json

# 4. Restart Claude
# Close and reopen
```

---

## 🔍 Debug Mode

### Enable Verbose Logging
```bash
# Run with debug flag (if supported)
claude mcp --debug list

# Or set debug environment variable
export DEBUG=*                       # All
export DEBUG=claude:mcp:*            # MCPs only
```

**Windows PowerShell:**
```powershell
$env:DEBUG = "*"                     # All
$env:DEBUG = "claude:mcp:*"          # MCPs only
```

---

## 📋 Diagnostic Checklist

Before asking for help, verify:

- [ ] Internet connection working
- [ ] Claude restarted recently
- [ ] `claude mcp list` shows your MCPs
- [ ] Authentication tokens set correctly
- [ ] Tokens have required scopes
- [ ] `.mcp.json` is valid (use JSON validator)
- [ ] MCP URLs respond (curl/Postman test)
- [ ] No typos in command names
- [ ] Using correct resource IDs (not names)
- [ ] Under rate limits for service
- [ ] Windows users using PowerShell syntax
- [ ] Checked official MCP documentation

---

## 🆘 Still Stuck?

### Collect Debug Info
```bash
# Save diagnostic info
echo "=== Claude MCP Debug ===" > debug.txt
claude mcp list >> debug.txt 2>&1
echo "=== Environment ===" >> debug.txt
env | grep -E "GITHUB|SENTRY|LINEAR|STRIPE|MYGITHUB" >> debug.txt
echo "=== .mcp.json ===" >> debug.txt
cat .mcp.json >> debug.txt
```

**Windows PowerShell:**
```powershell
# Save diagnostic info
"=== Claude MCP Debug ===" | Out-File debug.txt
claude mcp list | Out-File debug.txt -Append
"=== Environment ===" | Out-File debug.txt -Append
Get-ChildItem Env: | Where-Object {$_.Name -match "GITHUB|SENTRY|LINEAR|STRIPE|MYGITHUB"} | Out-File debug.txt -Append
"=== .mcp.json ===" | Out-File debug.txt -Append
Get-Content .mcp.json | Out-File debug.txt -Append
```

### Share Diagnostic File
- Include `debug.txt` (remove sensitive tokens!)
- Describe what you're trying to do
- Share error message exactly as shown
- Include MCP name and command you're running

---

## 📚 Related Documentation

- **Setup:** See `SETUP_GUIDE.md`
- **Commands:** See `COMMANDS.md`
- **MCP List:** See `MCP_LIST.md`
- **Configuration:** See `.mcp.json`

---

## ✅ Common Quick Fixes

| Issue | Quick Fix |
|-------|-----------|
| MCP not showing | Restart Claude, run `claude mcp reload` |
| Auth failing | Regenerate token, update env var, restart |
| Command not found | Check exact command name in COMMANDS.md |
| Timeout/slow | Check internet, reduce result size |
| Windows issue | Use PowerShell syntax, quote paths |
| JSON error | Validate with JSON tool, check formatting |
| Empty results | Verify filters, check access permissions |
| Multiple MCPs conflict | Use full MCP name: `mcp:command` |
| Rate limited | Wait 60 seconds, try again |
| Token expired | Regenerate from service, update .env |
