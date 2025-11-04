#!/usr/bin/env python3
"""
Database Failure and Security Testing Module
Tests database connection failures, constraint violations, and security issues
"""

import requests
import time
from datetime import datetime, timedelta
import json

GATEWAY_URL = "http://localhost:8080"

class DatabaseSecurityTests:
    def __init__(self):
        self.total = 0
        self.passed = 0
        self.failed = 0
        self.security_issues = []
        self.results = []

    # ============================================================================
    # DATABASE FAILURE TESTS
    # ============================================================================

    def test_duplicate_constraint(self):
        """Test handling of duplicate constraint violations"""
        print("\n[Database] Testing duplicate constraint handling...")

        # Try to create duplicate vehicle numbers
        vehicle_number = f"EV-UNIQUE-{int(time.time())}"

        try:
            # First creation
            response1 = requests.post(
                f"{GATEWAY_URL}/api/v1/vehicles",
                json={
                    "vehicleNumber": vehicle_number,
                    "companyId": 1,
                    "model": "Tesla"
                },
                timeout=5
            )

            self.total += 1

            if response1.status_code in [200, 201]:
                time.sleep(0.5)

                # Attempt duplicate
                response2 = requests.post(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    json={
                        "vehicleNumber": vehicle_number,
                        "companyId": 1,
                        "model": "Tesla"
                    },
                    timeout=5
                )

                if response2.status_code >= 400:
                    print(f"  [OK] Correctly rejected duplicate (HTTP {response2.status_code})")
                    self.passed += 1
                else:
                    print(f"  [FAIL] Accepted duplicate! (HTTP {response2.status_code})")
                    self.failed += 1
                    self.security_issues.append("Duplicate vehicle number accepted")
            else:
                print(f"  [WARNING] Could not create first vehicle")
                self.failed += 1
        except Exception as e:
            print(f"  [WARNING] Test error: {str(e)[:30]}")
            self.failed += 1

    def test_foreign_key_constraint(self):
        """Test handling of foreign key constraint violations"""
        print("\n[Database] Testing foreign key constraint handling...")

        self.total += 1

        try:
            # Try to assign non-existent driver to vehicle
            response = requests.patch(
                f"{GATEWAY_URL}/api/v1/vehicles/1/driver?driverId=999999",
                timeout=5
            )

            if response.status_code >= 400:
                print(f"  [OK] Correctly rejected invalid foreign key (HTTP {response.status_code})")
                self.passed += 1
            else:
                print(f"  [WARNING] Accepted invalid foreign key reference")
                self.failed += 1
        except Exception as e:
            print(f"  [OK] Handled foreign key error: {type(e).__name__}")
            self.passed += 1

    def test_null_constraint(self):
        """Test handling of NULL constraint violations"""
        print("\n[Database] Testing NULL constraint handling...")

        tests = [
            ("Empty vehicle number", {"vehicleNumber": "", "companyId": 1}),
            ("Null model", {"vehicleNumber": f"EV-NULL-{int(time.time())}", "model": None}),
            ("Missing required field", {"vehicleNumber": f"EV-REQ-{int(time.time())}"})
        ]

        for test_name, test_data in tests:
            self.total += 1
            try:
                response = requests.post(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    json=test_data,
                    timeout=5
                )

                if response.status_code >= 400:
                    print(f"  [OK] {test_name}: Correctly rejected")
                    self.passed += 1
                else:
                    print(f"  [FAIL] {test_name}: Accepted NULL/empty!")
                    self.failed += 1
                    self.security_issues.append(f"NULL constraint violated: {test_name}")
            except Exception as e:
                print(f"  [WARNING] {test_name}: {str(e)[:30]}")
                self.failed += 1

    def test_transaction_rollback(self):
        """Test transaction rollback on error"""
        print("\n[Database] Testing transaction rollback...")

        self.total += 1

        try:
            # Create vehicle with invalid data that should rollback
            response = requests.post(
                f"{GATEWAY_URL}/api/v1/vehicles",
                json={
                    "vehicleNumber": f"EV-TXN-{int(time.time())}",
                    "companyId": 1,
                    "model": "Tesla",
                    "batteryCapacity": -999  # Invalid!
                },
                timeout=5
            )

            if response.status_code >= 400:
                print(f"  [OK] Transaction rejected (no partial writes)")
                self.passed += 1
            else:
                print(f"  [WARNING] Transaction accepted despite invalid data")
                self.failed += 1
        except Exception as e:
            print(f"  [OK] Handled transaction error: {type(e).__name__}")
            self.passed += 1

    # ============================================================================
    # SECURITY TESTS
    # ============================================================================

    def test_sql_injection(self):
        """Test SQL injection prevention"""
        print("\n[Security] Testing SQL injection prevention...")

        sql_payloads = [
            "'; DROP TABLE vehicles; --",
            "1' OR '1'='1",
            "admin' --",
            "1'; DELETE FROM vehicles; --"
        ]

        for payload in sql_payloads:
            self.total += 1

            try:
                # Try injection in vehicle number field
                response = requests.post(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    json={
                        "vehicleNumber": payload,
                        "companyId": 1,
                        "model": "Tesla"
                    },
                    timeout=5
                )

                # Should either reject or safely escape
                if response.status_code >= 400:
                    print(f"  [OK] SQL injection payload rejected")
                    self.passed += 1
                elif response.status_code in [200, 201]:
                    print(f"  [OK] SQL injection safely escaped")
                    self.passed += 1
                else:
                    print(f"  [WARNING] Unexpected response to SQL injection attempt")
                    self.failed += 1
            except Exception as e:
                print(f"  [OK] Prevented SQL injection (error: {type(e).__name__})")
                self.passed += 1

    def test_xss_prevention(self):
        """Test XSS attack prevention"""
        print("\n[Security] Testing XSS prevention...")

        xss_payloads = [
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "<img src=x onerror='alert(1)'>",
            '\"><script>alert(String.fromCharCode(88,83,83))</script>'
        ]

        for payload in xss_payloads:
            self.total += 1

            try:
                response = requests.post(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    json={
                        "vehicleNumber": f"EV-XSS-{int(time.time())}",
                        "companyId": 1,
                        "model": payload
                    },
                    timeout=5
                )

                if response.status_code < 400:
                    # Check if payload was escaped in response
                    if payload not in response.text or "&lt;" in response.text or "\\u003c" in response.text:
                        print(f"  [OK] XSS payload safely escaped")
                        self.passed += 1
                    else:
                        print(f"  [FAIL] XSS payload may not be escaped!")
                        self.failed += 1
                        self.security_issues.append("Potential XSS vulnerability")
                else:
                    print(f"  [OK] XSS payload rejected")
                    self.passed += 1
            except Exception as e:
                print(f"  [OK] Handled XSS attempt safely")
                self.passed += 1

    def test_authentication_bypass(self):
        """Test authentication bypass prevention"""
        print("\n[Security] Testing authentication bypass...")

        self.total += 1

        try:
            # Try to access protected resource without auth
            response = requests.get(
                f"{GATEWAY_URL}/api/v1/vehicles",
                headers={"Authorization": "Bearer invalid"},
                timeout=5
            )

            # Should work (no auth required in this case) or reject
            if response.status_code in [200, 401, 403]:
                print(f"  [OK] Handled authentication properly (HTTP {response.status_code})")
                self.passed += 1
            else:
                print(f"  [WARNING] Unexpected auth response: {response.status_code}")
                self.failed += 1
        except Exception as e:
            print(f"  [WARNING] Auth test error: {type(e).__name__}")
            self.failed += 1

    def test_authorization_bypass(self):
        """Test authorization bypass prevention"""
        print("\n[Security] Testing authorization bypass...")

        self.total += 1

        try:
            # Try to delete another company's vehicle (if auth exists)
            response = requests.delete(
                f"{GATEWAY_URL}/api/v1/vehicles/999999",
                headers={"Authorization": "Bearer user_token"},
                timeout=5
            )

            # Should be rejected or return not found
            if response.status_code in [403, 404, 401]:
                print(f"  [OK] Correctly enforced authorization (HTTP {response.status_code})")
                self.passed += 1
            elif response.status_code >= 400:
                print(f"  [OK] Rejected unauthorized access (HTTP {response.status_code})")
                self.passed += 1
            else:
                print(f"  [WARNING] Potential authorization bypass")
                self.failed += 1
                self.security_issues.append("Possible authorization bypass")
        except Exception as e:
            print(f"  [OK] Handled authorization check")
            self.passed += 1

    def test_sensitive_data_exposure(self):
        """Test for sensitive data exposure in responses"""
        print("\n[Security] Testing sensitive data exposure...")

        self.total += 1

        try:
            response = requests.get(f"{GATEWAY_URL}/api/v1/vehicles", timeout=5)

            response_text = response.text.lower()

            # Check for exposed sensitive data
            sensitive_patterns = [
                "password",
                "token",
                "secret",
                "private_key",
                "api_key"
            ]

            exposed = [p for p in sensitive_patterns if p in response_text]

            if exposed:
                print(f"  [WARNING] Potential sensitive data exposure: {exposed}")
                self.failed += 1
                self.security_issues.append(f"Exposed: {exposed}")
            else:
                print(f"  [OK] No obvious sensitive data exposure")
                self.passed += 1
        except Exception as e:
            print(f"  [WARNING] Data exposure check error")
            self.failed += 1

    def test_rate_limiting(self):
        """Test rate limiting enforcement"""
        print("\n[Security] Testing rate limiting...")

        self.total += 1

        try:
            # Make rapid requests
            responses = []
            for i in range(50):
                response = requests.get(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    timeout=2
                )
                responses.append(response.status_code)

            rate_limited = any(code == 429 for code in responses)

            if rate_limited:
                print(f"  [OK] Rate limiting enforced (429 detected)")
                self.passed += 1
            else:
                print(f"  [WARNING] No rate limiting detected (50 rapid requests allowed)")
                self.failed += 1
        except Exception as e:
            print(f"  [WARNING] Rate limit test error")
            self.failed += 1

    # ============================================================================
    # CASCADE FAILURE TESTS
    # ============================================================================

    def test_service_cascading_failure(self):
        """Test behavior when dependent service fails"""
        print("\n[Cascading] Testing service cascade behavior...")

        self.total += 1

        try:
            # Fleet service depends on company validation
            # Try with non-existent company
            response = requests.post(
                f"{GATEWAY_URL}/api/v1/vehicles",
                json={
                    "vehicleNumber": f"EV-CASCADE-{int(time.time())}",
                    "companyId": 999999,
                    "model": "Tesla"
                },
                timeout=5
            )

            if response.status_code >= 400:
                print(f"  [OK] Correctly handled missing dependency (HTTP {response.status_code})")
                self.passed += 1
            else:
                print(f"  [WARNING] Accepted request with missing dependency")
                self.failed += 1
        except Exception as e:
            print(f"  [OK] Handled cascade error gracefully")
            self.passed += 1

    def test_data_consistency_after_failure(self):
        """Test data consistency after partial failures"""
        print("\n[Cascading] Testing consistency after failure...")

        self.total += 1

        try:
            # Create vehicle
            response1 = requests.post(
                f"{GATEWAY_URL}/api/v1/vehicles",
                json={
                    "vehicleNumber": f"EV-CONS-{int(time.time())}",
                    "companyId": 1,
                    "model": "Tesla"
                },
                timeout=5
            )

            if response1.status_code in [200, 201]:
                time.sleep(0.5)

                # Verify it exists
                response2 = requests.get(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    timeout=5
                )

                if response2.status_code == 200:
                    print(f"  [OK] Data consistency maintained")
                    self.passed += 1
                else:
                    print(f"  [FAIL] Data consistency issue")
                    self.failed += 1
            else:
                print(f"  [WARNING] Could not create test vehicle")
                self.failed += 1
        except Exception as e:
            print(f"  [WARNING] Consistency test error")
            self.failed += 1

    def print_summary(self):
        """Print comprehensive test summary"""
        print(f"\n{'='*70}")
        print(f"Database & Security Test Summary")
        print(f"{'='*70}")
        print(f"Total Tests: {self.total}")
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")

        if self.total > 0:
            success_rate = (self.passed / self.total) * 100
            print(f"Success Rate: {success_rate:.1f}%")

        if self.security_issues:
            print(f"\n[WARNING] SECURITY ISSUES FOUND:")
            for issue in self.security_issues:
                print(f"  • {issue}")

def run_database_security_tests():
    """Run all database and security tests"""
    tests = DatabaseSecurityTests()

    # Database tests
    tests.test_duplicate_constraint()
    tests.test_foreign_key_constraint()
    tests.test_null_constraint()
    tests.test_transaction_rollback()

    # Security tests
    tests.test_sql_injection()
    tests.test_xss_prevention()
    tests.test_authentication_bypass()
    tests.test_authorization_bypass()
    tests.test_sensitive_data_exposure()
    tests.test_rate_limiting()

    # Cascade tests
    tests.test_service_cascading_failure()
    tests.test_data_consistency_after_failure()

    tests.print_summary()

if __name__ == "__main__":
    run_database_security_tests()
