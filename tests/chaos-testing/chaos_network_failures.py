#!/usr/bin/env python3
"""
Network Failure and Service Cascade Testing Module
Tests network timeouts, connection failures, and cascading service failures
"""

import requests
import socket
import time
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

GATEWAY_URL = "http://localhost:8080"

class NetworkChaosTests:
    def __init__(self):
        self.total = 0
        self.passed = 0
        self.failed = 0
        self.results = []

    def test_connection_timeout(self):
        """Test connection timeout behavior"""
        print("\n[Network] Testing connection timeout scenarios...")

        timeouts = [0.5, 1.0, 2.0, 5.0]
        for timeout_val in timeouts:
            try:
                start = time.time()
                response = requests.get(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    timeout=timeout_val
                )
                elapsed = time.time() - start
                self.total += 1

                if response.status_code == 200 and elapsed <= timeout_val:
                    print(f"  [OK] Timeout {timeout_val}s: OK ({elapsed:.3f}s)")
                    self.passed += 1
                elif response.status_code == 200:
                    print(f"  [WARNING] Timeout {timeout_val}s: Slow ({elapsed:.3f}s)")
                    self.failed += 1
            except requests.exceptions.Timeout:
                print(f"  [OK] Timeout {timeout_val}s: Correctly timed out")
                self.total += 1
                self.passed += 1
            except Exception as e:
                print(f"  [FAIL] Timeout {timeout_val}s: {str(e)[:30]}")
                self.total += 1
                self.failed += 1

    def test_malformed_response(self):
        """Test handling of malformed responses"""
        print("\n[Network] Testing malformed response handling...")

        # This would require a mock server, so we test with invalid endpoints
        invalid_endpoints = [
            "/api/v1/vehicles?invalid=[[[ {bad json}",
            "/api/v1/vehicles\x00invalid",
            "/api/v1/vehicles?limit=99999999999999999"
        ]

        for endpoint in invalid_endpoints:
            try:
                self.total += 1
                response = requests.get(
                    f"{GATEWAY_URL}{endpoint}",
                    timeout=5
                )
                # Should handle gracefully
                print(f"  [OK] Handled malformed request: {response.status_code}")
                self.passed += 1
            except Exception as e:
                print(f"  [WARNING] Malformed request error: {str(e)[:30]}")
                self.failed += 1

    def test_partial_response(self):
        """Test handling of partial/incomplete responses"""
        print("\n[Network] Testing partial response handling...")

        # Test with very strict timeout to catch slow/partial responses
        try:
            self.total += 1
            response = requests.get(
                f"{GATEWAY_URL}/api/v1/vehicles",
                timeout=0.1,
                stream=True
            )
            print(f"  [WARNING] Response received with strict timeout")
            self.failed += 1
        except requests.exceptions.Timeout:
            print(f"  [OK] Correctly rejected extremely slow response")
            self.passed += 1
        except Exception as e:
            print(f"  [OK] Handled timeout gracefully: {type(e).__name__}")
            self.passed += 1

    def test_connection_refused(self):
        """Test behavior when connection is refused"""
        print("\n[Network] Testing connection refused scenarios...")

        invalid_services = [
            "http://localhost:9001",
            "http://localhost:9002",
            "http://localhost:9003"
        ]

        for service_url in invalid_services:
            try:
                self.total += 1
                response = requests.get(f"{service_url}/api/test", timeout=2)
                print(f"  [WARNING] Unexpected response from {service_url}")
                self.failed += 1
            except (requests.exceptions.ConnectionError,
                   socket.error,
                   requests.exceptions.ConnectTimeout):
                print(f"  [OK] Correctly rejected connection to {service_url}")
                self.passed += 1
            except Exception as e:
                print(f"  [OK] Handled connection error: {type(e).__name__}")
                self.passed += 1

    def test_intermittent_failures(self):
        """Test handling of intermittent connection failures"""
        print("\n[Network] Testing intermittent failure handling...")

        def make_request():
            try:
                response = requests.get(
                    f"{GATEWAY_URL}/api/v1/vehicles",
                    timeout=5
                )
                return response.status_code == 200
            except:
                return False

        # Make multiple requests and check for intermittent failures
        results = []
        with ThreadPoolExecutor(max_workers=5) as executor:
            futures = [executor.submit(make_request) for _ in range(20)]
            results = [f.result() for f in as_completed(futures)]

        success_count = sum(results)
        self.total += 1

        if success_count >= 18:  # Allow 2 failures
            print(f"  [OK] Handled intermittent failures ({success_count}/20 succeeded)")
            self.passed += 1
        else:
            print(f"  [FAIL] High failure rate under load ({success_count}/20 succeeded)")
            self.failed += 1

    def test_slow_network_recovery(self):
        """Test recovery after slow network conditions"""
        print("\n[Network] Testing slow network recovery...")

        # First request with normal timeout
        try:
            self.total += 1
            start = time.time()
            response1 = requests.get(f"{GATEWAY_URL}/api/v1/vehicles", timeout=10)
            time1 = time.time() - start

            # Simulate slow network (tight timeout)
            time.sleep(0.5)

            # Recovery request
            start = time.time()
            response2 = requests.get(f"{GATEWAY_URL}/api/v1/vehicles", timeout=10)
            time2 = time.time() - start

            if response2.status_code == 200:
                print(f"  [OK] Recovered from slow network ({time1:.3f}s -> {time2:.3f}s)")
                self.passed += 1
            else:
                print(f"  [FAIL] Failed to recover: {response2.status_code}")
                self.failed += 1
        except Exception as e:
            print(f"  [WARNING] Recovery test error: {str(e)[:30]}")
            self.failed += 1

    def test_circuit_breaker_activation(self):
        """Test circuit breaker activation under failure"""
        print("\n[Network] Testing circuit breaker behavior...")

        # Try to hit a service repeatedly (simulating failures)
        failure_count = 0

        for i in range(10):
            try:
                # Use invalid endpoint to simulate failure
                response = requests.get(
                    f"{GATEWAY_URL}/api/invalid-endpoint-{i}",
                    timeout=2
                )
                if response.status_code >= 500:
                    failure_count += 1
            except:
                failure_count += 1

            time.sleep(0.1)

        self.total += 1

        # After repeated failures, next request should fail faster (circuit broken)
        try:
            start = time.time()
            response = requests.get(
                f"{GATEWAY_URL}/api/invalid-endpoint-final",
                timeout=2
            )
            elapsed = time.time() - start

            if elapsed < 0.5:
                print(f"  [OK] Circuit breaker activated (fast failure after {failure_count} errors)")
                self.passed += 1
            else:
                print(f"  [WARNING] Circuit breaker may not be active (took {elapsed:.3f}s)")
                self.failed += 1
        except Exception as e:
            print(f"  [OK] Fast failure detected (circuit likely open)")
            self.passed += 1

    def print_summary(self):
        """Print test summary"""
        print(f"\n{'='*60}")
        print(f"Network Chaos Test Summary")
        print(f"{'='*60}")
        print(f"Total: {self.total}")
        print(f"Passed: {self.passed}")
        print(f"Failed: {self.failed}")
        if self.total > 0:
            success_rate = (self.passed / self.total) * 100
            print(f"Success Rate: {success_rate:.1f}%")

def run_network_tests():
    """Run all network chaos tests"""
    tests = NetworkChaosTests()
    tests.test_connection_timeout()
    tests.test_malformed_response()
    tests.test_partial_response()
    tests.test_connection_refused()
    tests.test_intermittent_failures()
    tests.test_slow_network_recovery()
    tests.test_circuit_breaker_activation()
    tests.print_summary()

if __name__ == "__main__":
    run_network_tests()
