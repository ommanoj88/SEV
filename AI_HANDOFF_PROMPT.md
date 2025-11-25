# AI Agent Handoff Prompt: Deep Dive Verification (Analytics & Notifications)

**Context:**
You are an expert Senior Software Engineer conducting a rigorous "Deep Dive Verification" of an EV Fleet Management System (`backend/evfleet-monolith` [Spring Boot] and `frontend` [React]).
Your goal is to verify if features are "Real" (fully implemented backend+frontend), "Ghost Features" (frontend UI only, no backend), or "Broken" (logic gaps, missing validation).

**Current State:**
We have verified Fleet (`1-9`), Charging (`B1-B6`), Maintenance (`C1-C6`), and Driver (`D1-D5`) modules.
You are now responsible for verifying **Analytics (`E`)** and **Notifications (`F`)**.

**Your Task:**
Systematically analyze the following features. For each feature, search the codebase, analyze the logic, and create a detailed report following the naming convention below.

---

## Module 1: Analytics & Reporting (Prefix: `E`)
**Naming Convention:** `E[X].[FEATURE_NAME]_ANALYSIS.md`

1.  **Fleet Summary Dashboards** -> `E1.FLEET_SUMMARY_ANALYSIS.md`
    *   *Verify:* Is the data pre-aggregated or calculated on the fly? Is it performant?
2.  **Utilization Reports** -> `E2.UTILIZATION_REPORT_ANALYSIS.md`
    *   *Verify:* Does it track vehicle uptime/downtime correctly?
3.  **Cost Analytics** -> `E3.COST_ANALYTICS_DEEP_DIVE.md`
    *   *Verify:* Does it include Maintenance + Charging + Fixed Costs? (Note: Previous analysis found gaps here).
4.  **TCO (Total Cost of Ownership)** -> `E4.TCO_ANALYSIS.md`
    *   *Verify:* Is there a formula? Or just a placeholder field?
5.  **Energy Consumption Tracking** -> `E5.ENERGY_TRACKING_ANALYSIS.md`
    *   *Verify:* Does it separate EV energy from ICE fuel?
6.  **Vehicle Reports (PDF Generation)** -> `E6.PDF_GENERATION_ANALYSIS.md`
    *   *Verify:* Is there a PDF library (iText/PDFBox)? Does it actually generate files?
7.  **Historical Data Analysis** -> `E7.HISTORICAL_DATA_ANALYSIS.md`
    *   *Verify:* Is there a history table or audit log?

## Module 2: Notifications & Alerts (Prefix: `F`)
**Naming Convention:** `F[X].[FEATURE_NAME]_ANALYSIS.md`

1.  **System Notifications** -> `F1.SYSTEM_NOTIFICATIONS_ANALYSIS.md`
    *   *Verify:* In-app notification storage and retrieval.
2.  **Alert Management** -> `F2.ALERT_MANAGEMENT_ANALYSIS.md`
    *   *Verify:* Can users configure rules? (e.g., "Alert me if SoC < 20%").
3.  **User Preferences** -> `F3.USER_PREFERENCES_ANALYSIS.md`
    *   *Verify:* Do preferences actually stop notifications?
4.  **Email Notifications** -> `F4.EMAIL_NOTIFICATIONS_ANALYSIS.md`
    *   *Verify:* Is SMTP configured? Is `JavaMailSender` used? Is it tested?
5.  **SMS Notifications** -> `F5.SMS_NOTIFICATIONS_ANALYSIS.md`
    *   *Verify:* Is there a Twilio/SNS adapter? Is it connected?

---

## Report Format (Strict Adherence Required)
For each file, use this template:

```markdown
# Deep Dive: [Feature Name] Analysis

**Date:** 2025-11-19
**Feature:** [Feature Name]
**Status:** [✅ Functional | ⚠️ Partial | ❌ Broken | 👻 Ghost Feature]

## 1. Executive Summary
[Brief overview of findings]

## 2. Backend Analysis
**Status:** [Status]
*   **Entities:** [List entities found]
*   **Logic:** [Analyze the service layer. Look for race conditions, missing validation, fake data]
*   **Missing:** [What is missing?]

## 3. Frontend Analysis
**Status:** [Status]
*   **Components:** [List components]
*   **Integration:** [Does it call the real API? Does it use mock data?]

## 4. Impact
[Business or Technical impact of the findings]

## 5. Recommendations
1.  [Recommendation 1]
2.  [Recommendation 2]
```

**Instructions:**
1.  **Search First:** Use `grep` and `find_by_name` to locate relevant code.
2.  **Read Deeply:** Don't just check if the file exists. Read the code to see *what it does*.
3.  **Be Critical:** If a function returns `null` or `true` hardcoded, report it as a Ghost Feature.
4.  **Proceed Sequentially:** Do E1, then E2, etc.
5.  u also have creative way to search all files and folders mentioned in your way