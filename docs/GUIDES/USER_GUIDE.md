# SEV Fleet Management - User Guide

**Version:** 2.0.0  
**Last Updated:** November 2025

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Dashboard Overview](#dashboard-overview)
3. [Fleet Management](#fleet-management)
4. [Driver Management](#driver-management)
5. [Charging Management](#charging-management)
6. [Maintenance Management](#maintenance-management)
7. [Analytics & Reports](#analytics--reports)
8. [Billing & Invoicing](#billing--invoicing)
9. [Settings & Configuration](#settings--configuration)
10. [FAQ](#frequently-asked-questions)

---

## Getting Started

### System Requirements

- **Browser:** Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **Screen Resolution:** 1366x768 minimum, 1920x1080 recommended
- **Internet:** Stable broadband connection

### Logging In

1. Navigate to `https://app.sevfleet.com`
2. Enter your company email address
3. Enter your password
4. Click **Sign In**
5. Complete two-factor authentication if enabled

### First-Time Setup

After your first login, complete these steps:

1. **Update Profile**
   - Click your avatar in the top right
   - Select "Profile Settings"
   - Add your phone number for notifications
   - Set your notification preferences

2. **Configure Company Settings**
   - Navigate to Settings > Company
   - Upload your company logo
   - Set default currency and timezone
   - Configure working hours

3. **Add Your First Vehicle**
   - Go to Fleet > Vehicles
   - Click "+ Add Vehicle"
   - Follow the guided setup wizard

---

## Dashboard Overview

The dashboard provides a real-time overview of your fleet operations.

### Key Metrics

| Metric | Description |
|--------|-------------|
| **Total Vehicles** | All vehicles in your fleet |
| **Active Vehicles** | Currently in operation |
| **Charging** | Vehicles currently charging |
| **Maintenance** | Vehicles under maintenance |
| **Fleet Utilization** | Percentage of fleet in active use |

### Live Fleet Map

The interactive map shows:
- 🟢 **Green markers**: Available vehicles
- 🔵 **Blue markers**: Vehicles in trip
- 🟡 **Yellow markers**: Vehicles charging
- 🔴 **Red markers**: Vehicles requiring attention

**Map Controls:**
- **Zoom:** Mouse scroll or +/- buttons
- **Pan:** Click and drag
- **Vehicle Details:** Click any marker
- **Cluster View:** Zoom out to group nearby vehicles

### Quick Actions

- **Start Trip**: Initiate a new trip for a vehicle
- **Start Charging**: Begin a charging session
- **Schedule Maintenance**: Create maintenance appointment
- **Generate Report**: Quick access to common reports

---

## Fleet Management

### Adding a Vehicle

1. Navigate to **Fleet > Vehicles**
2. Click **+ Add Vehicle**
3. Select vehicle type:
   - **Electric Vehicle (EV)**: Battery-powered vehicles
   - **Internal Combustion (ICE)**: Traditional fuel vehicles
   - **Hybrid**: Combined electric and fuel

4. Enter vehicle details:
   - Vehicle number (unique identifier)
   - Make and model
   - Year of manufacture
   - License plate number
   - VIN (Vehicle Identification Number)

5. For **EV vehicles**, provide:
   - Battery capacity (kWh)
   - Default charger type (CCS, CCS2, CHAdeMO, Type2)
   - Current battery level (%)

6. For **ICE/Hybrid vehicles**, provide:
   - Fuel tank capacity (liters)
   - Engine type
   - Current fuel level (%)

7. Click **Save Vehicle**

### Managing Vehicle Status

| Status | Meaning | Actions Available |
|--------|---------|-------------------|
| Available | Ready for use | Assign driver, start trip |
| In Trip | Currently on a journey | View trip, track location |
| Charging | At charging station | View session, end charging |
| Maintenance | Under service | View work order, mark complete |
| Offline | No connectivity | Troubleshoot, manual update |

### Viewing Vehicle Details

Click any vehicle to see:
- **Overview Tab**: Current status, location, battery/fuel level
- **Trips Tab**: Recent trip history with routes
- **Maintenance Tab**: Service history and upcoming schedules
- **Analytics Tab**: Fuel efficiency, utilization metrics
- **Documents Tab**: Registration, insurance, permits

### Bulk Operations

Select multiple vehicles to:
- Update status
- Assign/unassign drivers
- Schedule maintenance
- Export data

---

## Driver Management

### Adding a Driver

1. Navigate to **Drivers > All Drivers**
2. Click **+ Add Driver**
3. Enter driver information:
   - Full name
   - Phone number
   - Email address
   - License number
   - License expiry date
   - License class/category

4. Click **Save Driver**

### Driver Assignment

**To assign a driver to a vehicle:**

1. Go to **Fleet > Vehicles**
2. Click on the vehicle
3. Click **Assign Driver**
4. Select an available driver from the list
5. Confirm assignment

**Automatic validation checks:**
- Driver license is valid (not expired)
- Driver is not already assigned
- License class matches vehicle type

### Safety Scores

Each driver has a safety score (0-100):

| Score Range | Rating | Meaning |
|-------------|--------|---------|
| 90-100 | Excellent | Exemplary driving behavior |
| 75-89 | Good | Minor improvements needed |
| 60-74 | Fair | Training recommended |
| Below 60 | Needs Improvement | Immediate attention required |

**Score Factors:**
- Harsh braking events (-5 points each)
- Speeding incidents (-10 points each)
- Rapid acceleration (-3 points each)
- Excessive idling (-1 point per 10 minutes)

### Driver Leaderboard

View and reward your best drivers:
1. Navigate to **Drivers > Leaderboard**
2. Filter by time period (week, month, quarter)
3. See rankings by safety score, efficiency, trip count

---

## Charging Management

### Starting a Charging Session

1. Navigate to **Charging > Sessions**
2. Click **+ Start Session**
3. Select the vehicle
4. Choose a charging station
5. Verify initial battery level
6. Click **Start Charging**

### Monitoring Active Sessions

The charging dashboard shows:
- Active sessions in progress
- Current charge level (%)
- Estimated time to full
- Energy consumed
- Real-time cost

### Completing a Session

**Automatic completion:**
Sessions auto-complete when:
- Vehicle is unplugged
- Target SOC is reached

**Manual completion:**
1. Click on the active session
2. Click **End Session**
3. Review session summary
4. Click **Confirm**

### Charging Station Management

**Adding a Station:**
1. Go to **Charging > Stations**
2. Click **+ Add Station**
3. Enter station details:
   - Name and address
   - Charger type (CCS, CCS2, CHAdeMO, Type2)
   - Power output (kW)
   - Number of charging slots
   - Price per kWh

### Charging Analytics

View insights on:
- Energy consumption trends
- Cost per vehicle
- Peak usage times
- Station utilization rates

---

## Maintenance Management

### Scheduling Maintenance

1. Navigate to **Maintenance > Schedule**
2. Click **+ Schedule Service**
3. Select the vehicle
4. Choose maintenance type:
   - **Routine**: Tire rotation, brake inspection
   - **EV Specific**: Battery check, motor service
   - **ICE Specific**: Oil change, engine tune-up
5. Set scheduled date
6. Add service provider details
7. Enter estimated cost
8. Click **Schedule**

### Maintenance Types

| Type | Frequency | Applicable Vehicles |
|------|-----------|---------------------|
| Tire Rotation | 10,000 km | All |
| Brake Inspection | 20,000 km | All |
| Oil Change | 10,000 km | ICE, Hybrid |
| Battery Health Check | 25,000 km | EV, Hybrid |
| Coolant Flush | 50,000 km | All |
| Air Filter | 20,000 km | ICE, Hybrid |

### Completing Maintenance

1. Go to **Maintenance > In Progress**
2. Click on the maintenance record
3. Enter actual cost
4. Add completion notes
5. Upload invoices/receipts
6. Click **Mark Complete**

### Maintenance Alerts

The system automatically alerts you:
- 7 days before scheduled service
- When odometer-based service is due
- When battery health degrades below threshold

---

## Analytics & Reports

### Available Reports

| Report | Description | Frequency |
|--------|-------------|-----------|
| Fleet Summary | Overview of all vehicles | Real-time |
| Trip Report | Detailed trip data | Daily/Weekly |
| Energy Report | Charging and consumption | Weekly/Monthly |
| Driver Performance | Safety scores and metrics | Monthly |
| Maintenance Report | Service costs and history | Monthly |
| TCO Analysis | Total cost of ownership | Quarterly |
| ESG Report | Environmental impact | Quarterly |

### Generating Reports

1. Navigate to **Analytics > Reports**
2. Select report type
3. Choose date range
4. Apply filters (vehicle, driver, location)
5. Click **Generate**
6. Download as PDF, Excel, or CSV

### Dashboard Customization

Personalize your dashboard:
1. Click **Customize** on dashboard
2. Drag and drop widgets
3. Resize panels as needed
4. Save layout

---

## Billing & Invoicing

### Viewing Invoices

1. Navigate to **Billing > Invoices**
2. Filter by status:
   - **Pending**: Awaiting payment
   - **Paid**: Completed payments
   - **Overdue**: Past due date

### Making Payments

**Online Payment (Razorpay):**
1. Click on the invoice
2. Click **Pay Now**
3. Select payment method:
   - Credit/Debit Card
   - UPI
   - Net Banking
   - Wallet
4. Complete payment
5. Download receipt

### Payment History

View all transactions in **Billing > Payments**

---

## Settings & Configuration

### User Settings
- Profile information
- Password change
- Two-factor authentication
- Notification preferences

### Company Settings
- Company profile
- Branding (logo, colors)
- Timezone and currency
- Working hours

### Fleet Settings
- Default charging preferences
- Maintenance intervals
- Alert thresholds
- Geofence boundaries

### Integration Settings
- Telematics providers
- Payment gateways
- Notification channels

---

## Frequently Asked Questions

### General

**Q: How do I reset my password?**
A: Click "Forgot Password" on the login page, enter your email, and follow the reset link.

**Q: Can I use the app on mobile?**
A: Yes, the web app is fully responsive. Native mobile apps coming soon.

**Q: How is data secured?**
A: All data is encrypted in transit (TLS 1.3) and at rest (AES-256).

### Vehicles

**Q: Why can't I delete a vehicle?**
A: Vehicles in active trips or with assigned drivers cannot be deleted. First end the trip or unassign the driver.

**Q: How do I transfer a vehicle between locations?**
A: Update the vehicle's assigned location in Settings > Locations.

### Charging

**Q: Why doesn't my 2-wheeler show charging options?**
A: For 2-wheelers and 3-wheelers, we track GPS location only. Charging management is available for 4-wheeler EVs.

**Q: How is charging cost calculated?**
A: Cost = Energy consumed (kWh) × Price per kWh (set at station level).

### Maintenance

**Q: How do I add a custom maintenance type?**
A: Go to Settings > Maintenance > Custom Types and add your own.

**Q: Can I set different maintenance schedules for different vehicles?**
A: Yes, override the default schedule on individual vehicle pages.

### Analytics

**Q: How often is data updated?**
A: Telematics data syncs every 60 seconds. Analytics are calculated in real-time.

**Q: Can I export all my data?**
A: Yes, use the bulk export feature in Settings > Data Export.

---

## Support

**Email:** support@sevfleet.com  
**Phone:** +91 1800-XXX-XXXX (Toll-free)  
**Hours:** Monday-Saturday, 9 AM - 6 PM IST

**Knowledge Base:** https://help.sevfleet.com  
**Community Forum:** https://community.sevfleet.com

---

*PR #46: User Guide Documentation*
