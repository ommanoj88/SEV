#!/usr/bin/env python3
"""
SEV Fleet Management System - Comprehensive Chaos Testing Script
Tests all endpoints across all microservices with authentication, load testing, and chaos scenarios
"""

import requests
import json
import time
import sys
from datetime import datetime
from typing import Dict, List, Tuple
from concurrent.futures import ThreadPoolExecutor, as_completed
import random

# Configuration
BASE_URL = "http://localhost:8080"
SERVICES = {
    "api-gateway": "http://localhost:8080",
    "eureka": "http://localhost:8761",
    "auth-service": "http://localhost:8081",
    "fleet-service": "http://localhost:8082",
    "charging-service": "http://localhost:8083",
    "maintenance-service": "http://localhost:8084",
    "driver-service": "http://localhost:8085",
    "analytics-service": "http://localhost:8086",
    "notification-service": "http://localhost:8087",
    "billing-service": "http://localhost:8088",
}

# Test Results
test_results = {
    "total": 0,
    "passed": 0,
    "failed": 0,
    "skipped": 0,
    "warnings": 0,
    "start_time": None,
    "end_time": None,
    "failures": [],
    "warnings_list": [],
}

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    WHITE = '\033[97m'
    BOLD = '\033[1m'
    RESET = '\033[0m'

def print_header(text: str):
    """Print section header"""
    print(f"\n{Colors.BOLD}{Colors.CYAN}{'='*80}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{text.center(80)}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.CYAN}{'='*80}{Colors.RESET}\n")

def print_test(name: str, status: str, message: str = "", response_time: float = 0):
    """Print individual test result"""
    test_results["total"] += 1

    if status == "PASS":
        test_results["passed"] += 1
        symbol = f"{Colors.GREEN}✓{Colors.RESET}"
        color = Colors.GREEN
    elif status == "FAIL":
        test_results["failed"] += 1
        symbol = f"{Colors.RED}✗{Colors.RESET}"
        color = Colors.RED
        test_results["failures"].append(f"{name}: {message}")
    elif status == "WARN":
        test_results["warnings"] += 1
        symbol = f"{Colors.YELLOW}⚠{Colors.RESET}"
        color = Colors.YELLOW
        test_results["warnings_list"].append(f"{name}: {message}")
    else:  # SKIP
        test_results["skipped"] += 1
        symbol = f"{Colors.YELLOW}○{Colors.RESET}"
        color = Colors.YELLOW

    time_str = f"({response_time:.3f}s)" if response_time > 0 else ""
    print(f"{symbol} {color}{name:<60}{Colors.RESET} {time_str} {message}")

def make_request(method: str, url: str, **kwargs) -> Tuple[requests.Response, float]:
    """Make HTTP request and measure response time"""
    start = time.time()
    try:
        response = requests.request(method, url, timeout=10, **kwargs)
        elapsed = time.time() - start
        return response, elapsed
    except Exception as e:
        elapsed = time.time() - start
        return None, elapsed

# ============================================================================
# HEALTH CHECK TESTS
# ============================================================================

def test_service_health():
    """Test health endpoints for all services"""
    print_header("SERVICE HEALTH CHECKS")

    health_endpoints = {
        "API Gateway": f"{SERVICES['api-gateway']}/actuator/health",
        "Eureka Server": f"{SERVICES['eureka']}/actuator/health",
        "Fleet Service": f"{SERVICES['fleet-service']}/actuator/health",
        "Charging Service": f"{SERVICES['charging-service']}/actuator/health",
        "Driver Service": f"{SERVICES['driver-service']}/actuator/health",
        "Maintenance Service": f"{SERVICES['maintenance-service']}/actuator/health",
        "Analytics Service": f"{SERVICES['analytics-service']}/actuator/health",
        "Notification Service": f"{SERVICES['notification-service']}/actuator/health",
        "Billing Service": f"{SERVICES['billing-service']}/actuator/health",
    }

    for service_name, url in health_endpoints.items():
        try:
            response, elapsed = make_request("GET", url)
            if response and response.status_code == 200:
                data = response.json()
                status = data.get("status", "UNKNOWN")
                if status == "UP":
                    print_test(f"{service_name} Health", "PASS", f"Status: {status}", elapsed)
                else:
                    print_test(f"{service_name} Health", "WARN", f"Status: {status}", elapsed)
            elif response and response.status_code == 503:
                print_test(f"{service_name} Health", "WARN", f"Service Unavailable (503)", elapsed)
            else:
                status_code = response.status_code if response else "No Response"
                print_test(f"{service_name} Health", "FAIL", f"Status: {status_code}", elapsed)
        except Exception as e:
            print_test(f"{service_name} Health", "FAIL", f"Exception: {str(e)[:50]}")

# ============================================================================
# EUREKA SERVICE DISCOVERY TESTS
# ============================================================================

def test_eureka_discovery():
    """Test Eureka service registration"""
    print_header("EUREKA SERVICE DISCOVERY")

    try:
        response, elapsed = make_request("GET", f"{SERVICES['eureka']}/eureka/apps")
        if response and response.status_code == 200:
            print_test("Eureka Apps List", "PASS", "Retrieved service registry", elapsed)

            # Check specific services are registered
            text = response.text
            expected_services = [
                "FLEET-SERVICE", "CHARGING-SERVICE", "DRIVER-SERVICE",
                "MAINTENANCE-SERVICE", "ANALYTICS-SERVICE", "NOTIFICATION-SERVICE",
                "BILLING-SERVICE", "API-GATEWAY"
            ]

            for service in expected_services:
                if service in text:
                    print_test(f"  └─ {service} Registered", "PASS")
                else:
                    print_test(f"  └─ {service} Registered", "WARN", "Not found in registry")
        else:
            print_test("Eureka Apps List", "FAIL", f"Status: {response.status_code if response else 'No Response'}", elapsed)
    except Exception as e:
        print_test("Eureka Apps List", "FAIL", f"Exception: {str(e)[:50]}")

# ============================================================================
# FLEET SERVICE TESTS
# ============================================================================

def test_fleet_endpoints():
    """Test Fleet Service endpoints"""
    print_header("FLEET SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/vehicles", "List All Vehicles"),
        ("GET", "/api/v1/vehicles/1", "Get Vehicle by ID"),
        ("GET", "/api/v1/vehicles/active", "Get Active Vehicles"),
        ("GET", "/api/v1/vehicles/1/telemetry", "Get Vehicle Telemetry"),
        ("GET", "/api/v1/vehicles/1/location", "Get Vehicle Location"),
        ("GET", "/api/v1/trips", "List All Trips"),
        ("GET", "/api/v1/trips/active", "Get Active Trips"),
        ("GET", "/api/v1/geofences", "List All Geofences"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                # 401/403 means endpoint exists but needs auth (expected)
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Fleet: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Fleet: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Fleet: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Fleet: {description}", "FAIL", "No response")

# ============================================================================
# CHARGING SERVICE TESTS
# ============================================================================

def test_charging_endpoints():
    """Test Charging Service endpoints"""
    print_header("CHARGING SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/charging/stations", "List Charging Stations"),
        ("GET", "/api/v1/charging/stations/nearby", "Find Nearby Stations"),
        ("GET", "/api/v1/charging/stations/STAT001", "Get Station by ID"),
        ("GET", "/api/v1/charging/sessions", "List Charging Sessions"),
        ("GET", "/api/v1/charging/sessions/active", "Get Active Sessions"),
        ("GET", "/api/v1/charging/networks", "List Charging Networks"),
        ("GET", "/api/v1/charging/route-optimization", "Route Optimization"),
        ("GET", "/api/v1/charging/reservations", "List Reservations"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Charging: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Charging: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Charging: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Charging: {description}", "FAIL", "No response")

# ============================================================================
# DRIVER SERVICE TESTS
# ============================================================================

def test_driver_endpoints():
    """Test Driver Service endpoints"""
    print_header("DRIVER SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/drivers", "List All Drivers"),
        ("GET", "/api/v1/drivers/1", "Get Driver by ID"),
        ("GET", "/api/v1/drivers/available", "Get Available Drivers"),
        ("GET", "/api/v1/drivers/1/behavior", "Get Driver Behavior"),
        ("GET", "/api/v1/drivers/1/assignments", "Get Driver Assignments"),
        ("GET", "/api/v1/drivers/1/attendance", "Get Driver Attendance"),
        ("GET", "/api/v1/drivers/1/performance", "Get Driver Performance"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Driver: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Driver: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Driver: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Driver: {description}", "FAIL", "No response")

# ============================================================================
# MAINTENANCE SERVICE TESTS
# ============================================================================

def test_maintenance_endpoints():
    """Test Maintenance Service endpoints"""
    print_header("MAINTENANCE SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/maintenance/schedules", "List Maintenance Schedules"),
        ("GET", "/api/v1/maintenance/service-history", "Get Service History"),
        ("GET", "/api/v1/maintenance/battery-health", "Get Battery Health"),
        ("GET", "/api/v1/maintenance/warranties", "List Warranties"),
        ("GET", "/api/v1/maintenance/due-soon", "Get Due Soon Maintenance"),
        ("GET", "/api/v1/maintenance/overdue", "Get Overdue Maintenance"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Maintenance: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Maintenance: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Maintenance: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Maintenance: {description}", "FAIL", "No response")

# ============================================================================
# ANALYTICS SERVICE TESTS
# ============================================================================

def test_analytics_endpoints():
    """Test Analytics Service endpoints"""
    print_header("ANALYTICS SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/analytics/fleet", "Fleet Analytics"),
        ("GET", "/api/v1/analytics/costs", "Cost Analytics"),
        ("GET", "/api/v1/analytics/utilization", "Utilization Reports"),
        ("GET", "/api/v1/analytics/carbon-footprint", "Carbon Footprint"),
        ("GET", "/api/v1/analytics/performance", "Performance Metrics"),
        ("GET", "/api/v1/analytics/energy-trends", "Energy Consumption Trends"),
        ("GET", "/api/v1/analytics/fleet-summary", "Fleet Summary"),
        ("GET", "/api/v1/analytics/tco", "Total Cost of Ownership"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Analytics: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Analytics: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Analytics: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Analytics: {description}", "FAIL", "No response")

# ============================================================================
# NOTIFICATION SERVICE TESTS
# ============================================================================

def test_notification_endpoints():
    """Test Notification Service endpoints"""
    print_header("NOTIFICATION SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/notifications", "List Notifications"),
        ("GET", "/api/v1/notifications/alerts", "Get Alerts"),
        ("GET", "/api/v1/notifications/unread", "Get Unread Notifications"),
        ("GET", "/api/v1/notifications/rules", "List Alert Rules"),
        ("GET", "/api/v1/notifications/templates", "List Templates"),
        ("GET", "/api/v1/notifications/preferences", "Get Preferences"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Notification: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Notification: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Notification: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Notification: {description}", "FAIL", "No response")

# ============================================================================
# BILLING SERVICE TESTS
# ============================================================================

def test_billing_endpoints():
    """Test Billing Service endpoints"""
    print_header("BILLING SERVICE ENDPOINTS")

    endpoints = [
        ("GET", "/api/v1/billing/subscriptions", "List Subscriptions"),
        ("GET", "/api/v1/billing/invoices", "List Invoices"),
        ("GET", "/api/v1/billing/payments", "List Payments"),
        ("GET", "/api/v1/billing/pricing-plans", "List Pricing Plans"),
        ("GET", "/api/v1/billing/usage", "Get Usage Records"),
        ("GET", "/api/v1/billing/credits", "Get Credits"),
    ]

    for method, path, description in endpoints:
        url = f"{BASE_URL}{path}"
        response, elapsed = make_request(method, url)

        if response:
            if response.status_code in [200, 401, 403]:
                status = "PASS" if response.status_code == 200 else "WARN"
                msg = f"HTTP {response.status_code}"
                if response.status_code in [401, 403]:
                    msg += " (Auth required - expected)"
                print_test(f"Billing: {description}", status, msg, elapsed)
            elif response.status_code == 404:
                print_test(f"Billing: {description}", "FAIL", "Endpoint not found (404)", elapsed)
            else:
                print_test(f"Billing: {description}", "FAIL", f"HTTP {response.status_code}", elapsed)
        else:
            print_test(f"Billing: {description}", "FAIL", "No response")

# ============================================================================
# LOAD TESTING
# ============================================================================

def test_load_concurrent():
    """Test concurrent requests to simulate load"""
    print_header("CONCURRENT LOAD TESTING")

    endpoints = [
        f"{SERVICES['fleet-service']}/actuator/health",
        f"{SERVICES['charging-service']}/actuator/health",
        f"{SERVICES['driver-service']}/actuator/health",
        f"{BASE_URL}/api/v1/vehicles",
        f"{BASE_URL}/api/v1/charging/stations",
    ]

    num_requests = 50
    print(f"Sending {num_requests} concurrent requests...")

    start = time.time()
    success_count = 0
    failed_count = 0

    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = []
        for i in range(num_requests):
            endpoint = random.choice(endpoints)
            futures.append(executor.submit(make_request, "GET", endpoint))

        for future in as_completed(futures):
            response, _ = future.result()
            if response and response.status_code < 500:
                success_count += 1
            else:
                failed_count += 1

    elapsed = time.time() - start
    requests_per_sec = num_requests / elapsed

    if failed_count == 0:
        print_test(f"Concurrent Load Test ({num_requests} requests)", "PASS",
                  f"{requests_per_sec:.1f} req/s, 0% failures", elapsed)
    elif failed_count < num_requests * 0.1:
        print_test(f"Concurrent Load Test ({num_requests} requests)", "WARN",
                  f"{requests_per_sec:.1f} req/s, {failed_count} failures", elapsed)
    else:
        print_test(f"Concurrent Load Test ({num_requests} requests)", "FAIL",
                  f"{requests_per_sec:.1f} req/s, {failed_count} failures", elapsed)

# ============================================================================
# CHAOS TESTING
# ============================================================================

def test_chaos_scenarios():
    """Test chaos engineering scenarios"""
    print_header("CHAOS ENGINEERING TESTS")

    # Test 1: Invalid Request Bodies
    print("\n" + Colors.BOLD + "Invalid Request Tests:" + Colors.RESET)
    invalid_tests = [
        ("POST", f"{BASE_URL}/api/v1/vehicles", {"invalid": "data"}),
        ("PUT", f"{BASE_URL}/api/v1/vehicles/999999", {"invalid": "data"}),
        ("DELETE", f"{BASE_URL}/api/v1/vehicles/999999", None),
    ]

    for method, url, data in invalid_tests:
        response, elapsed = make_request(method, url, json=data)
        if response:
            # We expect 401 (auth), 400 (bad request), or 404
            if response.status_code in [400, 401, 403, 404, 405]:
                print_test(f"  Chaos: Invalid {method} request", "PASS",
                          f"Properly rejected with {response.status_code}", elapsed)
            else:
                print_test(f"  Chaos: Invalid {method} request", "FAIL",
                          f"Unexpected status {response.status_code}", elapsed)
        else:
            print_test(f"  Chaos: Invalid {method} request", "FAIL", "No response")

    # Test 2: Large Payload
    print("\n" + Colors.BOLD + "Large Payload Tests:" + Colors.RESET)
    large_payload = {"data": "x" * 1000000}  # 1MB payload
    response, elapsed = make_request("POST", f"{BASE_URL}/api/v1/vehicles", json=large_payload)
    if response:
        if response.status_code in [400, 401, 403, 413]:
            print_test("  Chaos: Large Payload Handling", "PASS",
                      f"Rejected with {response.status_code}", elapsed)
        else:
            print_test("  Chaos: Large Payload Handling", "WARN",
                      f"Accepted large payload ({response.status_code})", elapsed)
    else:
        print_test("  Chaos: Large Payload Handling", "PASS", "Request timeout (expected)")

    # Test 3: Rapid Sequential Requests
    print("\n" + Colors.BOLD + "Rate Limiting Tests:" + Colors.RESET)
    url = f"{BASE_URL}/api/v1/vehicles"
    start = time.time()
    responses = []
    for i in range(20):
        response, _ = make_request("GET", url)
        responses.append(response)
    elapsed = time.time() - start

    rate_limited = any(r and r.status_code == 429 for r in responses if r)
    if rate_limited:
        print_test("  Chaos: Rate Limiting", "PASS", "Rate limiter active", elapsed)
    else:
        print_test("  Chaos: Rate Limiting", "WARN", "No rate limiting detected", elapsed)

# ============================================================================
# DATABASE CONNECTIVITY
# ============================================================================

def test_database_connectivity():
    """Test database connectivity through services"""
    print_header("DATABASE CONNECTIVITY")

    # Check database health through service health endpoints
    services_with_db = [
        ("Fleet Service", f"{SERVICES['fleet-service']}/actuator/health"),
        ("Charging Service", f"{SERVICES['charging-service']}/actuator/health"),
        ("Driver Service", f"{SERVICES['driver-service']}/actuator/health"),
        ("Maintenance Service", f"{SERVICES['maintenance-service']}/actuator/health"),
        ("Analytics Service", f"{SERVICES['analytics-service']}/actuator/health"),
        ("Notification Service", f"{SERVICES['notification-service']}/actuator/health"),
        ("Billing Service", f"{SERVICES['billing-service']}/actuator/health"),
    ]

    for service_name, url in services_with_db:
        try:
            response, elapsed = make_request("GET", url)
            if response and response.status_code == 200:
                data = response.json()
                db_status = data.get("components", {}).get("db", {}).get("status", "UNKNOWN")
                if db_status == "UP":
                    print_test(f"{service_name} DB Connection", "PASS", "Database connected", elapsed)
                else:
                    print_test(f"{service_name} DB Connection", "FAIL", f"Status: {db_status}", elapsed)
            else:
                print_test(f"{service_name} DB Connection", "FAIL", "Cannot check status", elapsed)
        except Exception as e:
            print_test(f"{service_name} DB Connection", "FAIL", f"Exception: {str(e)[:50]}")

# ============================================================================
# FRONTEND TESTING
# ============================================================================

def test_frontend():
    """Test Frontend availability"""
    print_header("FRONTEND APPLICATION")

    frontend_url = "http://localhost:3000"

    try:
        response, elapsed = make_request("GET", frontend_url)
        if response and response.status_code == 200:
            print_test("Frontend Application", "PASS", "Frontend accessible", elapsed)

            # Check for key assets
            if "<!DOCTYPE html>" in response.text or "<html" in response.text:
                print_test("  └─ HTML Structure", "PASS", "Valid HTML found")
            else:
                print_test("  └─ HTML Structure", "WARN", "HTML not found in response")
        else:
            status = response.status_code if response else "No Response"
            print_test("Frontend Application", "FAIL", f"Status: {status}", elapsed)
    except Exception as e:
        print_test("Frontend Application", "FAIL", f"Exception: {str(e)[:50]}")

# ============================================================================
# MAIN EXECUTION
# ============================================================================

def print_summary():
    """Print test summary"""
    duration = test_results["end_time"] - test_results["start_time"]

    print_header("TEST SUMMARY")

    print(f"{Colors.BOLD}Duration:{Colors.RESET} {duration:.2f} seconds")
    print(f"{Colors.BOLD}Total Tests:{Colors.RESET} {test_results['total']}")
    print(f"{Colors.GREEN}✓ Passed:{Colors.RESET} {test_results['passed']}")
    print(f"{Colors.RED}✗ Failed:{Colors.RESET} {test_results['failed']}")
    print(f"{Colors.YELLOW}⚠ Warnings:{Colors.RESET} {test_results['warnings']}")
    print(f"{Colors.YELLOW}○ Skipped:{Colors.RESET} {test_results['skipped']}")

    success_rate = (test_results['passed'] / test_results['total'] * 100) if test_results['total'] > 0 else 0
    print(f"\n{Colors.BOLD}Success Rate:{Colors.RESET} {success_rate:.1f}%")

    if test_results['failures']:
        print(f"\n{Colors.RED}{Colors.BOLD}FAILURES:{Colors.RESET}")
        for failure in test_results['failures']:
            print(f"  {Colors.RED}✗{Colors.RESET} {failure}")

    if test_results['warnings_list']:
        print(f"\n{Colors.YELLOW}{Colors.BOLD}WARNINGS:{Colors.RESET}")
        for warning in test_results['warnings_list'][:10]:  # Show first 10
            print(f"  {Colors.YELLOW}⚠{Colors.RESET} {warning}")
        if len(test_results['warnings_list']) > 10:
            print(f"  ... and {len(test_results['warnings_list']) - 10} more")

    # Overall status
    print(f"\n{Colors.BOLD}{'='*80}{Colors.RESET}")
    if test_results['failed'] == 0 and test_results['warnings'] <= test_results['total'] * 0.3:
        print(f"{Colors.GREEN}{Colors.BOLD}✓ OVERALL STATUS: HEALTHY{Colors.RESET}")
        print(f"{Colors.GREEN}Application is functioning correctly!{Colors.RESET}")
    elif test_results['failed'] <= test_results['total'] * 0.1:
        print(f"{Colors.YELLOW}{Colors.BOLD}⚠ OVERALL STATUS: ACCEPTABLE{Colors.RESET}")
        print(f"{Colors.YELLOW}Application is mostly functional with minor issues.{Colors.RESET}")
    else:
        print(f"{Colors.RED}{Colors.BOLD}✗ OVERALL STATUS: UNHEALTHY{Colors.RESET}")
        print(f"{Colors.RED}Application has significant issues that need attention.{Colors.RESET}")
    print(f"{Colors.BOLD}{'='*80}{Colors.RESET}\n")

def main():
    """Main execution"""
    print(f"\n{Colors.BOLD}{Colors.MAGENTA}{'='*80}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.MAGENTA}SEV Fleet Management System - Comprehensive Chaos Testing{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.MAGENTA}{'='*80}{Colors.RESET}\n")
    print(f"Test Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")

    test_results["start_time"] = time.time()

    try:
        # Run all test suites
        test_service_health()
        test_eureka_discovery()
        test_database_connectivity()
        test_fleet_endpoints()
        test_charging_endpoints()
        test_driver_endpoints()
        test_maintenance_endpoints()
        test_analytics_endpoints()
        test_notification_endpoints()
        test_billing_endpoints()
        test_frontend()
        test_load_concurrent()
        test_chaos_scenarios()

    except KeyboardInterrupt:
        print(f"\n\n{Colors.YELLOW}Test interrupted by user{Colors.RESET}")
    except Exception as e:
        print(f"\n\n{Colors.RED}Fatal error: {str(e)}{Colors.RESET}")
    finally:
        test_results["end_time"] = time.time()
        print_summary()

        # Exit code based on results
        if test_results['failed'] > 0:
            sys.exit(1)
        else:
            sys.exit(0)

if __name__ == "__main__":
    main()
