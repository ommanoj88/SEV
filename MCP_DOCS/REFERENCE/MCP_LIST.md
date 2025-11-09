# 25 MCPs - Complete List

## 🎯 All 25 MCPs Configured

### Development & Testing (5)

| MCP | Purpose | URL |
|-----|---------|-----|
| **GitHub** | Code reviews, PR management, issue tracking | https://api.githubcopilot.com/mcp/ |
| **Sentry** | Error monitoring, debug production issues | https://mcp.sentry.dev/mcp |
| **Socket** | Dependency security, CVE scanning | https://mcp.socket.dev/ |
| **Jam** | Debug recordings, video logs | https://mcp.jam.dev/mcp |
| **Hugging Face** | AI models, ML integration | https://huggingface.co/mcp |

### Deployment (3)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Vercel** | Deploy & manage projects | https://mcp.vercel.com/ |
| **Netlify** | Website hosting, deployments | https://netlify-mcp.netlify.app/mcp |
| **Cloudflare** | DNS, CDN, security, analytics | https://mcp.cloudflare.com/mcp |

### Payments (4)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Stripe** | Payment processing, subscriptions | https://mcp.stripe.com |
| **PayPal** | Alternative payments | https://mcp.paypal.com/mcp |
| **Square** | Inventory, orders, payments | https://mcp.squareup.com/sse |
| **Plaid** | Banking data, financial accounts | https://api.dashboard.plaid.com/mcp/sse |

### Project Management (6)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Linear** | Issues, roadmap, features | https://mcp.linear.app/mcp |
| **Asana** | Tasks, projects, workflows | https://mcp.asana.com/sse |
| **Notion** | Docs, databases, pages | https://mcp.notion.com/mcp |
| **Monday** | Boards, automation, tracking | https://mcp.monday.com/mcp |
| **Atlassian** | Jira, Confluence | https://mcp.atlassian.com/v1/sse |
| **Intercom** | Customer tickets, conversations | https://mcp.intercom.com/mcp |

### CRM & Data (2)

| MCP | Purpose | URL |
|-----|---------|-----|
| **HubSpot** | Analytics, CRM data, contacts | https://mcp.hubspot.com/anthropic |
| **Box** | Enterprise content management | https://mcp.box.com/ |

### Design & Media (3)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Figma** | Design systems, code generation | https://mcp.figma.com/mcp |
| **Canva** | Design templates, automation | https://mcp.canva.com/mcp |
| **Cloudinary** | Image management, transformation | https://mcp.cloudinary.com/mcp |

### Security & Auth (1)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Stytch** | Authentication management | http://mcp.stytch.dev/mcp |

### Automation (1)

| MCP | Purpose | URL |
|-----|---------|-----|
| **Workato** | Workflow automation, integrations | https://mcp.workato.com/ |

---

## 📊 Quick Stats

| Category | Count | MCPs |
|----------|-------|------|
| Development | 5 | GitHub, Sentry, Socket, Jam, Hugging Face |
| Deployment | 3 | Vercel, Netlify, Cloudflare |
| Payments | 4 | Stripe, PayPal, Square, Plaid |
| Projects | 6 | Linear, Asana, Notion, Monday, Atlassian, Intercom |
| CRM | 2 | HubSpot, Box |
| Design | 3 | Figma, Canva, Cloudinary |
| Security | 1 | Stytch |
| Automation | 1 | Workato |
| **TOTAL** | **25+** | **All Configured** |

---

## 🚀 Usage by Service Type

### For APIs (HTTP)
Most MCPs use HTTP transport (easiest):
```bash
claude mcp add --transport http service https://mcp.service.com/mcp
```

### For Real-time (SSE)
Some use Server-Sent Events:
- Asana, Atlassian, Plaid, Square (deprecated but still working)

### For Local (stdio)
Some can run locally with Node.js

---

## 🎯 By Your Role

### Frontend Developers
- Figma, Vercel, GitHub, Netlify

### Backend Developers
- GitHub, Sentry, Linear, Socket

### DevOps Engineers
- Vercel, Netlify, Cloudflare, Sentry

### Project Managers
- Linear, Asana, Monday, Notion

### Data Analysts
- HubSpot, Hugging Face, Notion

### Finance
- Stripe, PayPal, Square, Plaid

### Support Team
- Intercom, Linear, Asana, HubSpot

---

## 🔗 All MCPs Quick Reference

```
GitHub          → Code management
Sentry          → Error tracking
Socket          → Security
Jam             → Debug recordings
Hugging Face    → AI/ML
Vercel          → Deploy (frontend)
Netlify         → Deploy (static)
Cloudflare      → Infrastructure
Stripe          → Payments
PayPal          → Payments
Square          → Inventory
Plaid           → Banking
Linear          → Issues
Asana           → Tasks
Notion          → Docs
Monday          → Boards
Atlassian       → Jira/Confluence
Intercom        → Support
HubSpot         → CRM
Box             → Files
Figma           → Design
Canva           → Design
Cloudinary      → Images
Stytch          → Auth
Workato         → Automation
```

---

## ✅ Verify Your MCPs

```bash
claude mcp list
```

Should show 25 MCPs with all details.

---

## 📞 Need More Info?

- **All Commands:** See `COMMANDS.md`
- **Setup Help:** See `SETUP_GUIDE.md`
- **Issues?** See `TROUBLESHOOTING.md`
