# Copilot Agent Quick Reference - 50 PR Guide

**How to Use:** Copy the relevant PR section when assigning work to Copilot Coding Agent

---

## PR Quick Index

| PR | Title | ~Time | Main Files |
|----|-------|-------|------------|
| **BATCH 1: Research** ||||
| 1 | Validate Core Features (1-9) | 45min | docs/ANALYSIS/CORE_FEATURES/*.md |
| 2 | Validate Charging (B1-B6) | 45min | docs/ANALYSIS/CHARGING/*.md |
| 3 | Validate Maintenance & Driver (C1-C6, D1-D5) | 45min | docs/ANALYSIS/MAINTENANCE/*.md, DRIVER/*.md |
| 4 | Validate Analytics + Gap Analysis | 45min | docs/ANALYSIS/ANALYTICS/*.md |
| **BATCH 2: Backend Validation** ||||
| 5 | Multi-Fuel Validation | 50min | VehicleService.java |
| 6 | Trip Teleportation Prevention | 50min | TripService.java |
| 7 | 2-Wheeler Charging Restriction | 40min | ChargingSessionService.java |
| 8 | Driver Assignment Validation | 50min | DriverService.java |
| **BATCH 3: Driver Backend** ||||
| 9 | Create DrivingEvent Entity | 45min | driver/model/, driver/repository/ |
| 10 | Create DrivingEventService | 50min | driver/service/ |
| 11 | Create DrivingEventController | 45min | driver/controller/ |
| 12 | License Alert Scheduler | 40min | driver/scheduler/ |
| **BATCH 4: Telematics Backend** ||||
| 13 | Flespi Integration | 50min | telematics/provider/ |
| 14 | Telemetry History Storage | 45min | telematics/model/, repository/ |
| 15 | Telemetry Sync Scheduler | 45min | telematics/scheduler/ |
| 16 | Telemetry Alert System | 50min | telematics/service/ |
| **BATCH 5: Billing Backend** ||||
| 17 | Razorpay Integration | 60min | billing/service/RazorpayPaymentService.java |
| 18 | Invoice Payment Workflow | 50min | billing/service/InvoicePaymentService.java |
| 19 | Payment Webhook Controller | 45min | billing/controller/ |
| 20 | Subscription Auto-Renewal | 50min | billing/scheduler/ |
| **BATCH 6: Analytics Backend** ||||
| 21 | Historical Data Aggregation | 50min | analytics/service/ |
| 22 | Multi-Fuel TCO Analysis | 50min | analytics/service/TCOAnalysisService.java |
| 23 | ESG Reporting Module | 55min | analytics/service/ESGReportService.java |
| 24 | Dashboard Metrics Endpoint | 45min | analytics/controller/ |
| **BATCH 7: Driver Frontend** ||||
| 25 | Driver Behavior Dashboard | 50min | components/drivers/DriverBehaviorDashboard.tsx |
| 26 | Safety Leaderboard | 45min | components/drivers/SafetyLeaderboard.tsx |
| 27 | License Management UI | 45min | components/drivers/LicenseManagement.tsx |
| 28 | Assignment Calendar | 50min | components/drivers/AssignmentCalendar.tsx |
| **BATCH 8: Charging Frontend** ||||
| 29 | Charging Station Map | 55min | components/charging/ChargingStationMap.tsx |
| 30 | Cost Comparison Component | 45min | components/charging/CostComparison.tsx |
| 31 | Session History Component | 45min | components/charging/SessionHistory.tsx |
| 32 | Payment Methods UI | 50min | components/billing/PaymentMethods.tsx |
| **BATCH 9: Analytics Frontend** ||||
| 33 | Fleet Summary Dashboard | 55min | components/analytics/FleetSummaryDashboard.tsx |
| 34 | Utilization Heatmap | 50min | components/analytics/UtilizationHeatmap.tsx |
| 35 | Cost Analytics Dashboard | 50min | components/analytics/CostAnalyticsDashboard.tsx |
| 36 | ESG Report Generator | 50min | components/analytics/ESGReportGenerator.tsx |
| **BATCH 10: Maintenance Frontend** ||||
| 37 | Maintenance Calendar | 50min | components/maintenance/MaintenanceCalendar.tsx |
| 38 | Predictive Alerts Component | 45min | components/maintenance/PredictiveAlerts.tsx |
| 39 | Battery Health Dashboard | 50min | components/maintenance/BatteryHealthDashboard.tsx |
| 40 | Service History Timeline | 45min | components/maintenance/ServiceHistoryTimeline.tsx |
| **BATCH 11: Testing** ||||
| 41 | Backend Controller Tests | 60min | test/*/controller/*Test.java |
| 42 | Backend Service Tests | 60min | test/*/service/*Test.java |
| 43 | Frontend Component Tests | 60min | **/*.test.tsx |
| 44 | Integration Tests | 60min | test/integration/*IT.java |
| **BATCH 12: Documentation** ||||
| 45 | API Documentation | 50min | docs/API/ |
| 46 | User Guide | 50min | docs/GUIDES/USER_GUIDE.md |
| 47 | Developer Guide | 50min | docs/GUIDES/DEVELOPER_GUIDE.md |
| 48 | Operations Runbook | 50min | docs/GUIDES/RUNBOOK.md |
| **BATCH 13: Infrastructure** ||||
| 49 | Docker Production Config | 55min | docker/ |
| 50 | GitHub Actions CI/CD | 55min | .github/workflows/ |

---

## Copy-Paste Instructions for Each PR

### Template for Starting a PR

```
@copilot Please work on PR #[NUMBER] from docs/PLANNING/PR_WORK_BREAKDOWN_50.md

Scope: [TITLE]
Files to modify: [FILES]

Please:
1. Follow existing code patterns in the repository
2. Add appropriate tests
3. Update documentation if needed
4. Mark the PR as complete in PR_WORK_BREAKDOWN_50.md when done
```

---

## Parallel Execution Groups

**Run these 4 PRs together (no conflicts):**

- **Batch 1:** PRs 1, 2, 3, 4
- **Batch 2:** PRs 5, 6, 7, 8
- **Batch 3:** PRs 9, 10, 11, 12
- **Batch 4:** PRs 13, 14, 15, 16
- **Batch 5:** PRs 17, 18, 19, 20
- **Batch 6:** PRs 21, 22, 23, 24
- **Batch 7:** PRs 25, 26, 27, 28
- **Batch 8:** PRs 29, 30, 31, 32
- **Batch 9:** PRs 33, 34, 35, 36
- **Batch 10:** PRs 37, 38, 39, 40
- **Batch 11:** PRs 41, 42, 43, 44
- **Batch 12:** PRs 45, 46, 47, 48
- **Batch 13:** PRs 49, 50

---

## Status Tracking

When a PR is complete, update `docs/PLANNING/PR_WORK_BREAKDOWN_50.md`:

```markdown
### PR #X: [Title]
**Status:** ✅ DONE - [Date]
```

---

## Dependencies Between Batches

```
Batch 1 (Research) → Independent, start first
Batch 2 (Validation) → Independent
Batch 3 (Driver Backend) → After Batch 2 ideally
Batch 4 (Telematics) → Independent
Batch 5 (Billing) → Independent
Batch 6 (Analytics) → Independent
Batch 7 (Driver Frontend) → After Batch 3 ideally
Batch 8 (Charging Frontend) → Independent
Batch 9 (Analytics Frontend) → After Batch 6 ideally
Batch 10 (Maintenance Frontend) → Independent
Batch 11 (Testing) → After most code complete
Batch 12 (Docs) → After most PRs complete
Batch 13 (Infra) → Independent
```

---

## Priority Order (if doing sequentially)

**High Priority (Critical Features):**
1. PR 9-11 (Driver Behavior - Ghost Feature)
2. PR 17-19 (Payment Processing - Revenue)
3. PR 13-16 (Telemetry - Core Feature)

**Medium Priority (Enhancements):**
4. PR 5-8 (Backend Validation)
5. PR 25-28 (Driver Frontend)
6. PR 21-24 (Analytics Backend)

**Lower Priority (Nice-to-Have):**
7. PR 1-4 (Documentation Validation)
8. PR 33-40 (Frontend Components)
9. PR 41-50 (Testing, Docs, Infra)

---

**Last Updated:** November 29, 2025
