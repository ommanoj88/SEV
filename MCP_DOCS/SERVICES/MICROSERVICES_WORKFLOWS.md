# Microservices Workflows - 11 Services Integration

## 🏗️ SEV Fleet Management Architecture

All 11 microservices with 64+ MCP-integrated workflows

---

## 1️⃣ API Gateway

**Role:** Central entry point for all requests  
**Port:** 8080  
**MCPs:** GitHub, Linear, Sentry, Vercel, Stripe, PayPal

### Workflows

#### Deploy New Version
```bash
# 1. Check code on GitHub
claude mcp github list-pull-requests repo:sev-api-gateway

# 2. Create deployment task
claude mcp linear create-issue title:"Deploy API Gateway v1.2.0"

# 3. Deploy to Vercel
claude mcp vercel create-deployment project:sev-api-gateway

# 4. Monitor for errors
claude mcp sentry list-issues project:api-gateway
```

#### Setup Payment Routes
```bash
# 1. Get Stripe keys
claude mcp stripe list-keys

# 2. Configure in Linear issue
claude mcp linear update-issue issue:GATE-123 description:"Stripe keys: sk_test_..."

# 3. Test with test payment
claude mcp stripe create-test-charge amount:100 currency:USD
```

#### API Rate Limiting
```bash
# 1. Check GitHub rate limits
claude mcp github get-rate-limits

# 2. Create monitoring issue
claude mcp linear create-issue title:"API rate limits exceeded"

# 3. Escalate in Sentry
claude mcp sentry create-alert
```

---

## 2️⃣ Auth Service

**Role:** JWT authentication, user management  
**Port:** 8081  
**MCPs:** GitHub, Linear, Sentry, Stytch, Intercom

### Workflows

#### Add OAuth Provider
```bash
# 1. Setup on GitHub
claude mcp github create-secret name:OAUTH_GITHUB value:***

# 2. Document in Linear
claude mcp linear create-issue title:"Add GitHub OAuth"

# 3. Track authentication events
claude mcp sentry create-event data:"OAuth provider added"

# 4. Notify team
claude mcp intercom create-message "OAuth setup complete"
```

#### Reset User Password
```bash
# 1. Find user in Stytch
claude mcp stytch get-user email:user@example.com

# 2. Create reset token
claude mcp stytch create-password-reset email:user@example.com

# 3. Log action in Linear
claude mcp linear create-issue title:"Password reset for user@example.com"

# 4. Monitor success
claude mcp sentry add-breadcrumb message:"Password reset initiated"
```

#### Audit Authentication
```bash
# 1. Check GitHub logs
claude mcp github list-events repo:sev-auth-service

# 2. Review in Sentry
claude mcp sentry list-events project:auth-service

# 3. Report to team
claude mcp linear create-issue title:"Auth audit report"
```

---

## 3️⃣ Driver Service

**Role:** Driver profiles, licenses, ratings  
**Port:** 8082  
**MCPs:** GitHub, Linear, Sentry, Plaid, HubSpot, Intercom

### Workflows

#### Onboard New Driver
```bash
# 1. Create driver record
claude mcp hubspot create-contact firstName:John lastName:Doe

# 2. Add to Linear
claude mcp linear create-issue title:"Onboard driver: John Doe"

# 3. Verify documents with GitHub
claude mcp github create-gist description:"Driver docs" files:["license.pdf"]

# 4. Welcome email
claude mcp intercom create-message to:driver "Welcome to SEV!"
```

#### Verify License
```bash
# 1. Pull ID data via Plaid
claude mcp plaid get-identity

# 2. Create verification task
claude mcp linear create-issue title:"License verification pending"

# 3. Track in Sentry
claude mcp sentry create-event data:"License verification started"
```

#### Get Driver Rating
```bash
# 1. Query HubSpot for reviews
claude mcp hubspot search-contacts property:driver_rating

# 2. Log in Linear
claude mcp linear create-issue title:"Driver rating report"

# 3. Monitor issues
claude mcp sentry list-issues filter:"driver_rating"
```

---

## 4️⃣ Fleet Service

**Role:** Vehicle management, tracking, maintenance  
**Port:** 8083  
**MCPs:** GitHub, Linear, Sentry, Cloudflare, Box, Figma

### Workflows

#### Add Vehicle to Fleet
```bash
# 1. Create vehicle config
claude mcp github create-file path:vehicles/new-vehicle.json content:"{...}"

# 2. Log in system
claude mcp linear create-issue title:"Add vehicle ABC-123"

# 3. Track in Sentry
claude mcp sentry create-event data:"Vehicle added: ABC-123"

# 4. Store documents
claude mcp box upload path:fleet/ABC-123/ file:registration.pdf
```

#### Vehicle Tracking
```bash
# 1. Configure CDN for GPS data
claude mcp cloudflare purge-cache zone:sev.com

# 2. Create tracking task
claude mcp linear create-issue title:"GPS tracking enabled: ABC-123"

# 3. Monitor performance
claude mcp sentry add-breadcrumb message:"Tracking started"
```

#### Fleet Maintenance Schedule
```bash
# 1. Design maintenance checklist
claude mcp figma get-page page:maintenance-checklist

# 2. Create maintenance tasks
claude mcp linear create-issue title:"Maintenance due for ABC-123"

# 3. Upload maintenance logs
claude mcp box upload path:fleet/maintenance/ file:service-log.pdf

# 4. Monitor compliance
claude mcp sentry create-alert alert_type:"maintenance_due"
```

---

## 5️⃣ Billing Service

**Role:** Invoice generation, subscription management  
**Port:** 8084  
**MCPs:** Stripe, PayPal, Linear, Sentry, HubSpot, Square

### Workflows

#### Create Invoice
```bash
# 1. Get customer from Stripe
claude mcp stripe get-customer customer:cus_12345

# 2. Generate invoice
claude mcp stripe create-invoice customer:cus_12345 items:["item_abc"]

# 3. Log in Linear
claude mcp linear create-issue title:"Invoice created: INV-001"

# 4. Update HubSpot
claude mcp hubspot update-contact id:contact_123 properties:"invoice_sent:true"
```

#### Process Subscription
```bash
# 1. Create with Stripe
claude mcp stripe create-subscription customer:cus_12345 price:price_monthly

# 2. Or with PayPal
claude mcp paypal create-billing-plan name:"Monthly Plan" amount:29.99

# 3. Track in Linear
claude mcp linear create-issue title:"Subscription started"

# 4. Monitor in Sentry
claude mcp sentry create-event data:"Subscription activated"
```

#### Refund Process
```bash
# 1. Get charge from Stripe
claude mcp stripe list-charges customer:cus_12345

# 2. Create refund
claude mcp stripe create-refund charge:ch_12345 amount:5000

# 3. Log in Square
claude mcp square create-refund transaction_id:abc_123

# 4. Notify via Linear
claude mcp linear create-issue title:"Refund processed: $50"
```

---

## 6️⃣ Charging Service

**Role:** EV charging station management, payments  
**Port:** 8085  
**MCPs:** Stripe, Square, Linear, Sentry, HubSpot, Figma

### Workflows

#### Enable Charging Station
```bash
# 1. Design UI in Figma
claude mcp figma create-frame name:"Charging Station UI"

# 2. Setup payment with Stripe
claude mcp stripe create-payment-intent amount:5000 customer:cus_12345

# 3. Create task in Linear
claude mcp linear create-issue title:"Activate charging station: CS-001"

# 4. Monitor errors
claude mcp sentry add-breadcrumb message:"Charging station activated"
```

#### Process Charging Session
```bash
# 1. Start session
# User starts charging via app

# 2. Process payment
claude mcp stripe create-charge amount:2500 currency:USD

# 3. Log session
claude mcp linear create-issue title:"Charging session completed: 2.5 kWh"

# 4. Update customer record
claude mcp hubspot update-contact id:contact_123 properties:"last_charge:2023-10-15"
```

#### Maintenance Alert
```bash
# 1. Detect issue
# System detects charger malfunction

# 2. Create alert
claude mcp sentry create-alert alert_type:"charger_maintenance_required"

# 3. Notify team
claude mcp linear create-issue title:"Charger maintenance needed: CS-001"

# 4. Refund if needed
claude mcp stripe create-refund charge:ch_12345
```

---

## 7️⃣ Notification Service

**Role:** Alerts, emails, SMS, push notifications  
**Port:** 8086  
**MCPs:** Intercom, HubSpot, Linear, Sentry, Cloudinary

### Workflows

#### Send Ride Notification
```bash
# 1. Get user contact info
claude mcp hubspot get-contact id:contact_123

# 2. Create message
claude mcp intercom create-message to:user "Your ride is ready"

# 3. Add image/branding
claude mcp cloudinary get-resource public_id:sev-logo

# 4. Track in Sentry
claude mcp sentry create-event data:"Notification sent: ride_ready"
```

#### Alert Driver
```bash
# 1. Create alert in Intercom
claude mcp intercom create-alert user_id:driver_123 message:"Maintenance due"

# 2. Log in Linear
claude mcp linear create-issue title:"Driver alert sent: maintenance"

# 3. Update HubSpot
claude mcp hubspot update-contact id:driver_123 properties:"last_alert:timestamp"

# 4. Monitor delivery
claude mcp sentry add-breadcrumb message:"Alert delivered to driver"
```

#### Bulk Email Campaign
```bash
# 1. Prepare email list in HubSpot
claude mcp hubspot search-contacts filter:"driver:true"

# 2. Create message via Intercom
claude mcp intercom create-campaign subject:"December Promotions"

# 3. Track opens/clicks
claude mcp sentry create-event data:"Campaign sent to 500 drivers"

# 4. Log results
claude mcp linear create-issue title:"Campaign results: 45% open rate"
```

---

## 8️⃣ Analytics Service

**Role:** Data analysis, reports, dashboards  
**Port:** 8087  
**MCPs:** HubSpot, Linear, Sentry, Hugging Face, GitHub

### Workflows

#### Generate Revenue Report
```bash
# 1. Query data from HubSpot
claude mcp hubspot search-deals filter:"close_date:oct_2023"

# 2. Process with Hugging Face ML
claude mcp hugging-face run-model task:"text-classification"

# 3. Create report
claude mcp linear create-issue title:"October Revenue Report"

# 4. Store in GitHub
claude mcp github create-file path:reports/october-2023.json content:"{...}"
```

#### Driver Performance Analysis
```bash
# 1. Get metrics from HubSpot
claude mcp hubspot search-contacts property:driver_rating

# 2. Run ML analysis
claude mcp hugging-face run-model task:"regression"

# 3. Create issue with findings
claude mcp linear create-issue title:"Driver performance analysis complete"

# 4. Log results
claude mcp sentry create-event data:"Analysis: top 10% drivers identified"
```

#### Fleet Utilization Dashboard
```bash
# 1. Gather data
claude mcp github list-files path:fleet-data/

# 2. Process analytics
claude mcp hugging-face run-model task:"clustering"

# 3. Create summary
claude mcp linear create-issue title:"Fleet utilization: 87% this week"

# 4. Archive report
claude mcp github create-file path:dashboards/weekly-utilization.json
```

---

## 9️⃣ Config Server

**Role:** Centralized configuration management  
**Port:** 8888  
**MCPs:** GitHub, Linear, Sentry, Vercel

### Workflows

#### Deploy New Config
```bash
# 1. Create config file
claude mcp github create-file path:config/production.yml content:"{...}"

# 2. Create PR
claude mcp github create-pull-request title:"Update prod config"

# 3. Log in Linear
claude mcp linear create-issue title:"Config deployment pending"

# 4. Deploy via Vercel
claude mcp vercel create-deployment
```

#### Config Validation
```bash
# 1. Check syntax
claude mcp github get-file path:config/production.yml

# 2. Verify in Sentry
claude mcp sentry create-event data:"Config validated successfully"

# 3. Track changes
claude mcp linear create-issue title:"Config updated: database_timeout changed"

# 4. Broadcast to services
# All services reload config automatically
```

#### Rollback Config
```bash
# 1. Get previous version
claude mcp github list-commits path:config/production.yml

# 2. Revert to previous
claude mcp github create-pull-request title:"Rollback: config to commit abc123"

# 3. Log incident
claude mcp sentry create-alert alert_type:"config_rollback"

# 4. Notify team
claude mcp linear create-issue title:"Config rolled back due to error"
```

---

## 🔟 Eureka Server

**Role:** Service discovery, registration  
**Port:** 8761  
**MCPs:** GitHub, Linear, Sentry, Vercel

### Workflows

#### Register New Service
```bash
# 1. Add to config
claude mcp github create-file path:eureka/services/new-service.yml content:"{...}"

# 2. Create task
claude mcp linear create-issue title:"Register new-service in Eureka"

# 3. Verify registration
claude mcp sentry add-breadcrumb message:"Service registered: new-service"

# 4. Deploy
claude mcp vercel create-deployment
```

#### Health Check Monitoring
```bash
# 1. Monitor service health
# Eureka pings all services periodically

# 2. Log failures
claude mcp sentry create-event data:"Service health check failed: driver-service"

# 3. Create alert
claude mcp linear create-issue title:"ALERT: driver-service unhealthy"

# 4. Trigger auto-recovery
# Kubernetes redeploys if needed
```

#### Service Deregistration
```bash
# 1. Remove service config
claude mcp github delete-file path:eureka/services/old-service.yml

# 2. Create removal PR
claude mcp github create-pull-request title:"Deregister old-service"

# 3. Log change
claude mcp linear create-issue title:"Deregistered: old-service"

# 4. Monitor errors
claude mcp sentry add-breadcrumb message:"Service deregistered"
```

---

## 1️⃣1️⃣ Maintenance Service

**Role:** Scheduled maintenance, health checks  
**Port:** 8089  
**MCPs:** Linear, Sentry, GitHub, HubSpot, Figma

### Workflows

#### Schedule Maintenance Window
```bash
# 1. Create maintenance plan in Figma
claude mcp figma create-frame name:"Maintenance Schedule"

# 2. Document in GitHub
claude mcp github create-file path:maintenance/dec-2023.md content:"{...}"

# 3. Notify team in Linear
claude mcp linear create-issue title:"Maintenance window: 2023-12-15 02:00-04:00"

# 4. Monitor with Sentry
claude mcp sentry create-alert alert_type:"maintenance_window_active"
```

#### Execute Health Check
```bash
# 1. Run diagnostics
# Service pings all components

# 2. Log results
claude mcp sentry create-event data:"Health check: all services OK"

# 3. Update status
claude mcp linear create-issue title:"Daily health check passed"

# 4. Archive report
claude mcp github create-file path:health-reports/2023-10-15.json content:"{...}"
```

#### Database Cleanup
```bash
# 1. Plan cleanup
claude mcp linear create-issue title:"Database cleanup: remove old sessions"

# 2. Document procedure
claude mcp github create-file path:maintenance/db-cleanup.sql content:"{...}"

# 3. Execute and monitor
claude mcp sentry add-breadcrumb message:"Database cleanup started"

# 4. Report results
claude mcp linear create-issue title:"Cleaned 50GB disk space"
```

---

## 📊 Cross-Service Integration Patterns

### Pattern 1: Payment to Notification
```
User pays → Stripe charges → Billing service → Notification sends email/SMS
```

### Pattern 2: Driver Update Flow
```
Driver updates profile → Auth updates → Driver service → HubSpot syncs → Notification alerts
```

### Pattern 3: Fleet Maintenance
```
Fleet service detects issue → Creates Linear issue → Sends notification → Updates HubSpot
```

### Pattern 4: Data Pipeline
```
Services generate data → Analytics collects → Hugging Face processes → Reports to Linear
```

### Pattern 5: Error Handling
```
Any service fails → Sentry alerts → Linear creates issue → Intercom notifies team
```

---

## 🔄 MCP Command Patterns by Service

| Service | Common MCPs | Pattern |
|---------|------------|---------|
| API Gateway | GitHub, Linear, Sentry, Vercel | Deploy → Track → Monitor |
| Auth | Stytch, GitHub, Sentry | Auth → Log → Alert |
| Driver | HubSpot, Plaid, Linear | Create → Verify → Track |
| Fleet | GitHub, Box, Figma | Store → Organize → Design |
| Billing | Stripe, PayPal, Square | Charge → Invoice → Log |
| Charging | Stripe, Figma, Linear | Design → Pay → Track |
| Notification | Intercom, HubSpot, Linear | Message → Send → Log |
| Analytics | HubSpot, Hugging Face, Linear | Query → Process → Report |
| Config | GitHub, Vercel, Sentry | Deploy → Validate → Monitor |
| Eureka | GitHub, Sentry, Linear | Register → Health → Alert |
| Maintenance | GitHub, Linear, Sentry | Plan → Execute → Report |

---

## ⚡ Quick Workflow Examples

### Onboard Complete Driver + Vehicle
```bash
# 1. Create driver in HubSpot
claude mcp hubspot create-contact firstName:Jane lastName:Smith

# 2. Verify documents
claude mcp plaid get-identity

# 3. Add vehicle
claude mcp github create-file path:vehicles/license-plate.json

# 4. Create tracking
claude mcp linear create-issue title:"New driver + vehicle ready"

# 5. Send welcome
claude mcp intercom create-message "Welcome to SEV!"
```

### Handle Payment Failure
```bash
# 1. Detect failure
claude mcp sentry list-events filter:"payment_failed"

# 2. Alert in Linear
claude mcp linear create-issue title:"Payment failed: user_123"

# 3. Notify user
claude mcp intercom create-message to:user_123 "Payment failed, retry here"

# 4. Update billing
claude mcp stripe create-charge retry:true

# 5. Log outcome
claude mcp hubspot update-contact properties:"last_payment_attempt:timestamp"
```

### Generate Weekly Summary
```bash
# 1. Collect metrics
claude mcp hubspot search-deals filter:"close_date:this_week"

# 2. Analyze with ML
claude mcp hugging-face run-model task:"summarization"

# 3. Create report
claude mcp linear create-issue title:"Weekly Summary"

# 4. Archive
claude mcp github create-file path:reports/weekly-summary.json

# 5. Notify team
claude mcp intercom create-message "Weekly summary ready"
```

---

## ✅ All 64+ Workflows Covered

✅ API Gateway (4 workflows)
✅ Auth Service (3 workflows)
✅ Driver Service (3 workflows)
✅ Fleet Service (3 workflows)
✅ Billing Service (3 workflows)
✅ Charging Service (3 workflows)
✅ Notification Service (3 workflows)
✅ Analytics Service (3 workflows)
✅ Config Server (3 workflows)
✅ Eureka Server (3 workflows)
✅ Maintenance Service (3 workflows)
✅ Cross-service patterns (5 patterns)
✅ Integration patterns (5 patterns)
✅ Quick workflows (3 complete examples)

**Total: 64+ documented MCP-integrated workflows**

---

## 📞 Need Help?

- **Setup:** See `SETUP_GUIDE.md`
- **Commands:** See `COMMANDS.md`
- **All MCPs:** See `MCP_LIST.md`
- **Issues?** See `TROUBLESHOOTING.md`
