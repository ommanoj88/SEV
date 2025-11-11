# Vehicle Report Generation User Guide (v.report)

## Overview

The Vehicle Report Generation feature (v.report) allows you to generate comprehensive PDF reports for any vehicle in your fleet. These reports include genealogy and historical data, providing complete visibility into a vehicle's operational history.

---

## Accessing the Report Generation Page

1. Log in to the Smart Fleet Management Platform
2. Navigate to **Reports** from the sidebar menu (icon: 📄)
3. You will be taken to the Vehicle Report Generation page

---

## Generating a Report

### Step 1: Select a Vehicle

1. Click on the **"Select Vehicle"** dropdown
2. Choose the vehicle you want to generate a report for
3. The dropdown shows the vehicle number, make, and model for easy identification

### Step 2: Choose Date Range

1. **Start Date**: Select the beginning date for the report period
2. **End Date**: Select the ending date for the report period
3. Default range is set to the last 30 days

### Step 3: Select Report Sections

Choose which sections to include in your report:

- ☑ **Vehicle Information**: Basic vehicle details, specifications, and configuration
- ☑ **Event History**: Complete timeline of all vehicle events (genealogy)
- ☑ **Trip History**: All trips taken during the selected period
- ☑ **Maintenance History**: Maintenance records and schedules
- ☑ **Charging History**: Charging sessions (for EV and Hybrid vehicles)
- ☑ **Alert History**: All alerts raised during the period
- ☑ **Performance Metrics**: Efficiency scores, utilization rates, etc.
- ☑ **Cost Analysis**: Operating costs, energy/fuel costs, and TCO

### Step 4: Generate Report

Two types of reports are available:

#### Comprehensive Report
- Includes all selected sections
- Best for detailed analysis and record-keeping
- Click **"Generate Comprehensive Report"** button
- Report will be downloaded as `vehicle_report_{vehicleId}_{timestamp}.pdf`

#### Genealogy Report
- Focuses on complete event history and timeline
- Includes vehicle info, events, trips, maintenance, charging, and alerts
- Excludes performance metrics and cost analysis
- Click **"Generate Genealogy Report"** button
- Report will be downloaded as `vehicle_genealogy_{vehicleId}_{timestamp}.pdf`

---

## Report Contents

### 1. Vehicle Information
- Vehicle Number
- Make, Model, Year
- Vehicle Type (EV, ICE, Hybrid)
- Fuel/Power Type
- Battery Capacity / Fuel Tank Capacity
- Current Status
- VIN, License Plate, Color

### 2. Event History (Genealogy)
Complete chronological timeline of all events:
- **Trip Events**: Started, ended, paused, resumed
- **Charging Events**: Started, completed, stopped, failed
- **Maintenance Events**: Scheduled, started, completed
- **Alert Events**: Raised, resolved, acknowledged
- **Status Events**: Vehicle started, stopped, idle
- **Battery/Fuel Events**: Low battery/fuel warnings, refueling
- **Geofence Events**: Entered, exited geofences
- **Driver Events**: Assignments, harsh braking, speeding
- **System Events**: Connected, disconnected, firmware updates

### 3. Trip History
- Total number of trips
- Total distance traveled
- Average trip duration
- List of recent trips with:
  - Start/end times and locations
  - Distance covered
  - Energy/fuel consumed
  - Average speed

### 4. Maintenance History
- Total maintenance records
- Last maintenance date
- Next maintenance due date
- List of maintenance events with:
  - Maintenance type
  - Date performed
  - Description and notes

### 5. Charging History (EV/Hybrid)
- Total charging sessions
- Total energy consumed (kWh)
- Average charging duration
- List of charging sessions with:
  - Date and time
  - Energy added
  - Duration
  - Charging station

### 6. Alert History
- Total number of alerts
- Critical alerts count
- List of alerts with:
  - Alert type
  - Severity level
  - Timestamp
  - Resolution status

### 7. Performance Metrics
- Utilization rate
- Average speed
- Efficiency score
- Battery health
- Idle time
- Energy/fuel efficiency

### 8. Cost Analysis
- Total operating cost
- Energy/fuel cost
- Maintenance cost
- Cost per kilometer
- Monthly average

---

## Use Cases

### Fleet Management
- **Monthly Reports**: Generate comprehensive reports at the end of each month for record-keeping
- **Performance Review**: Analyze vehicle performance metrics quarterly
- **Cost Analysis**: Review operating costs to identify optimization opportunities

### Maintenance Planning
- **Service History**: Review complete maintenance history before scheduling service
- **Predictive Maintenance**: Analyze event patterns to predict maintenance needs
- **Warranty Claims**: Generate detailed reports for warranty documentation

### Compliance and Auditing
- **Regulatory Compliance**: Maintain complete vehicle operation records
- **Audit Trails**: Provide comprehensive documentation for audits
- **Insurance Claims**: Generate detailed event history for insurance purposes

### Operations Analysis
- **Route Optimization**: Analyze trip patterns and distances
- **Driver Performance**: Review driver-related events and behaviors
- **Energy Management**: Track charging patterns and energy consumption

### Client Reporting
- **Customer Reports**: Provide detailed reports to fleet customers
- **Billing Support**: Generate reports to support billing and invoicing
- **Performance SLAs**: Document service level agreement compliance

---

## Tips and Best Practices

### Report Generation
1. **Select Relevant Sections**: Only include sections relevant to your analysis to keep reports concise
2. **Choose Appropriate Date Ranges**: 
   - Daily reports for active monitoring
   - Weekly reports for operational review
   - Monthly reports for performance analysis
   - Quarterly/Annual reports for strategic planning

3. **Save Reports Systematically**: Use a consistent naming and filing system for easy retrieval

### Data Quality
1. **Regular Generation**: Generate reports regularly to maintain up-to-date records
2. **Verify Data**: Cross-check important data points with real-time dashboard
3. **Complete Time Periods**: Use complete time periods (full months, quarters) for accurate analysis

### Performance
1. **Avoid Very Large Ranges**: For vehicles with high activity, limit date ranges to avoid very large reports
2. **Off-Peak Hours**: Generate large reports during off-peak hours if possible
3. **Multiple Vehicles**: Generate reports for multiple vehicles separately rather than all at once

---

## Troubleshooting

### Report Generation Failed
- **Check Vehicle Selection**: Ensure a vehicle is selected
- **Verify Date Range**: Ensure start date is before end date
- **Network Issues**: Check your internet connection
- **Try Again**: Some temporary issues may resolve on retry

### Missing Data in Report
- **Date Range**: Verify the date range includes the expected events
- **Data Availability**: Some data may not be available for all time periods
- **Vehicle Type**: Some sections (e.g., charging) may not apply to all vehicle types

### Report Takes Long Time
- **Large Date Range**: Reduce the date range for faster generation
- **Many Sections**: Deselect unnecessary sections
- **Peak Usage**: Try generating during off-peak hours

---

## API Integration

For programmatic report generation, use the Analytics Service API:

### Comprehensive Report
```bash
POST /api/v1/analytics/reports/vehicle
Content-Type: application/json

{
  "vehicleId": 123,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "includeVehicleInfo": true,
  "includeEventHistory": true,
  ...
}
```

### Genealogy Report
```bash
GET /api/v1/analytics/reports/vehicle/{vehicleId}/genealogy?startDate={ISO_DATE}&endDate={ISO_DATE}
```

---

## Frequently Asked Questions

**Q: How far back can I generate reports?**
A: You can generate reports for any date range where data is available. However, consider data retention policies of your organization.

**Q: Can I schedule automatic report generation?**
A: Currently, reports must be generated manually. Scheduled report generation is planned for a future release.

**Q: What format are the reports in?**
A: Reports are generated as PDF files, which can be viewed, printed, or shared easily.

**Q: Can I customize the report format?**
A: Currently, the report format is standardized. Custom report templates are planned for a future release.

**Q: How long are reports stored?**
A: Reports are generated on-demand and downloaded directly to your device. The platform does not store generated reports.

**Q: Can I generate reports for multiple vehicles at once?**
A: Currently, reports are generated one vehicle at a time. Bulk report generation is planned for a future release.

---

## Support

For additional help or to report issues:
- Contact your fleet administrator
- Refer to the main documentation at [README.md](README.md)
- Check the Database Schema documentation at [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)

---

Last Updated: November 2024
Version: 1.0.0
