# Quick Start - 5 Minutes

## ⚡ Get Started NOW

### 1️⃣ Verify MCPs (30 seconds)
```bash
cd c:\Users\omman\Desktop\SEV
claude mcp list
```
✅ Should show 25 MCPs

### 2️⃣ Authenticate (2 minutes)
In Claude Code:
```
/mcp
```
Choose services and authenticate with OAuth

### 3️⃣ Try Commands (2 minutes)
```
/mcp__github__list_repos
/mcp__sentry__errors_last_24_hours
/mcp__stripe__recent_transactions
```

✅ **Done! You're ready to use MCPs!**

---

## 🎯 Quick Reference

### Check Status
```
/mcp
claude mcp list
```

### Execute Commands
```
/mcp__service__action
/mcp__github__create_issue "Title"
/mcp__linear__list_issues
```

### Reference Resources
```
@github:pr://123
@stripe:payment://txn_id
@notion:doc://page-id
```

---

## 🎯 Quick By Role

### Frontend Developer
```
/mcp__figma__list_files
/mcp__vercel__deployments
/mcp__github__list_prs
```

### Backend Developer
```
/mcp__github__create_issue
/mcp__sentry__errors
/mcp__linear__issues
```

### DevOps Engineer
```
/mcp__vercel__deploy
/mcp__netlify__status
/mcp__cloudflare__analytics
```

### Project Manager
```
/mcp__linear__create_issue
/mcp__asana__tasks
/mcp__monday__items
```

---

## 💡 Common Tasks

### Debug Production Error
```
/mcp__sentry__errors_last_24_hours
/mcp__github__create_issue "Fix found bug"
/mcp__vercel__deploy
```

### Process Payment
```
/mcp__stripe__create_payment
/mcp__paypal__verify
```

### Create Team Task
```
/mcp__linear__create_issue "New feature"
/mcp__asana__create_task
```

---

## 📚 Where to Go Next

- **Setup Help:** Read `MCP_DOCS/GUIDES/SETUP_GUIDE.md`
- **All Commands:** See `MCP_DOCS/REFERENCE/COMMANDS.md`
- **Your Services:** Check `MCP_DOCS/SERVICES/MICROSERVICES_WORKFLOWS.md`
- **Issues?** Read `MCP_DOCS/REFERENCE/TROUBLESHOOTING.md`

---

**That's it! You're all set! 🚀**
