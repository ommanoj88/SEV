#!/usr/bin/env python3
"""
Enhanced Chaos Testing Runner with Authentication & Retry Logic
Includes:
- Firebase/Token-based authentication
- Exponential backoff retry logic
- Circuit breaker pattern
- Rate limit awareness
"""

import subprocess
import sys
import time
import os
from datetime import datetime
import requests
from functools import wraps
import random

# Configuration
GATEWAY_URL = os.getenv("API_GATEWAY_URL", "http://localhost:8080")
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://localhost:8081")
TEST_EMAIL = os.getenv("TEST_EMAIL", "test@evfleet.com")
TEST_PASSWORD = os.getenv("TEST_PASSWORD", "Test@1234567")

# Global token storage
current_token = None
token_expiry = None

class RetryConfig:
    """Retry configuration with exponential backoff"""
    MAX_RETRIES = 3
    INITIAL_DELAY = 0.5  # seconds
    MAX_DELAY = 10  # seconds
    EXPONENTIAL_BASE = 2


def get_exponential_backoff_delay(retry_count):
    """Calculate exponential backoff delay"""
    delay = RetryConfig.INITIAL_DELAY * (RetryConfig.EXPONENTIAL_BASE ** retry_count)
    delay = min(delay, RetryConfig.MAX_DELAY)
    # Add jitter to prevent thundering herd
    delay = delay * (0.5 + random.random())
    return delay


def get_auth_token():
    """Get authentication token from auth service"""
    global current_token, token_expiry

    # Check if token is still valid
    if current_token and token_expiry and time.time() < token_expiry - 60:
        return current_token

    try:
        response = requests.post(
            f"{AUTH_SERVICE_URL}/api/auth/token",
            json={
                "email": TEST_EMAIL,
                "password": TEST_PASSWORD
            },
            timeout=5
        )

        if response.status_code == 200:
            data = response.json()
            current_token = data.get('token') or data.get('idToken')
            token_expiry = time.time() + data.get('expiresIn', 3600)
            return current_token
        else:
            print(f"[WARNING] Failed to get auth token: {response.status_code}")
            return None
    except Exception as e:
        print(f"[WARNING] Auth service error: {str(e)[:50]}")
        return None


def retry_with_backoff(func):
    """Decorator to add retry logic with exponential backoff"""
    @wraps(func)
    def wrapper(*args, **kwargs):
        last_exception = None

        for attempt in range(RetryConfig.MAX_RETRIES):
            try:
                return func(*args, **kwargs)
            except requests.exceptions.Timeout:
                last_exception = "Timeout"
                if attempt < RetryConfig.MAX_RETRIES - 1:
                    delay = get_exponential_backoff_delay(attempt)
                    time.sleep(delay)
            except requests.exceptions.ConnectionError:
                last_exception = "Connection Error"
                if attempt < RetryConfig.MAX_RETRIES - 1:
                    delay = get_exponential_backoff_delay(attempt)
                    time.sleep(delay)
            except Exception as e:
                last_exception = str(e)
                if attempt < RetryConfig.MAX_RETRIES - 1:
                    delay = get_exponential_backoff_delay(attempt)
                    time.sleep(delay)

        raise Exception(f"Failed after {RetryConfig.MAX_RETRIES} retries: {last_exception}")

    return wrapper


class AuthenticatedRequest:
    """Helper class for making authenticated requests"""

    @staticmethod
    @retry_with_backoff
    def get(url, **kwargs):
        """GET request with authentication"""
        token = get_auth_token()
        headers = kwargs.get('headers', {})
        if token:
            headers['Authorization'] = f"Bearer {token}"
        kwargs['headers'] = headers
        return requests.get(url, **kwargs)

    @staticmethod
    @retry_with_backoff
    def post(url, **kwargs):
        """POST request with authentication"""
        token = get_auth_token()
        headers = kwargs.get('headers', {})
        if token:
            headers['Authorization'] = f"Bearer {token}"
        kwargs['headers'] = headers
        return requests.post(url, **kwargs)

    @staticmethod
    @retry_with_backoff
    def put(url, **kwargs):
        """PUT request with authentication"""
        token = get_auth_token()
        headers = kwargs.get('headers', {})
        if token:
            headers['Authorization'] = f"Bearer {token}"
        kwargs['headers'] = headers
        return requests.put(url, **kwargs)

    @staticmethod
    @retry_with_backoff
    def delete(url, **kwargs):
        """DELETE request with authentication"""
        token = get_auth_token()
        headers = kwargs.get('headers', {})
        if token:
            headers['Authorization'] = f"Bearer {token}"
        kwargs['headers'] = headers
        return requests.delete(url, **kwargs)

    @staticmethod
    @retry_with_backoff
    def patch(url, **kwargs):
        """PATCH request with authentication"""
        token = get_auth_token()
        headers = kwargs.get('headers', {})
        if token:
            headers['Authorization'] = f"Bearer {token}"
        kwargs['headers'] = headers
        return requests.patch(url, **kwargs)


class CircuitBreaker:
    """Simple circuit breaker implementation"""

    def __init__(self, failure_threshold=5, timeout=30):
        self.failure_count = 0
        self.failure_threshold = failure_threshold
        self.timeout = timeout
        self.last_failure_time = None
        self.state = "CLOSED"  # CLOSED, OPEN, HALF_OPEN

    def call(self, func, *args, **kwargs):
        """Execute function with circuit breaker protection"""
        if self.state == "OPEN":
            if time.time() - self.last_failure_time > self.timeout:
                self.state = "HALF_OPEN"
            else:
                raise Exception("Circuit breaker is OPEN")

        try:
            result = func(*args, **kwargs)
            self.on_success()
            return result
        except Exception as e:
            self.on_failure()
            raise e

    def on_success(self):
        """Reset circuit breaker on success"""
        self.failure_count = 0
        self.state = "CLOSED"

    def on_failure(self):
        """Increment failure count and open circuit if threshold exceeded"""
        self.failure_count += 1
        self.last_failure_time = time.time()
        if self.failure_count >= self.failure_threshold:
            self.state = "OPEN"


class RateLimitAwarenessTracker:
    """Track and handle rate limiting"""

    def __init__(self):
        self.rate_limit_remaining = float('inf')
        self.rate_limit_reset = None
        self.requests_count = 0
        self.rate_limited_count = 0

    def update_from_response(self, response):
        """Update rate limit info from response headers"""
        if 'X-RateLimit-Remaining' in response.headers:
            self.rate_limit_remaining = int(response.headers['X-RateLimit-Remaining'])
        if 'X-RateLimit-Reset' in response.headers:
            self.rate_limit_reset = int(response.headers['X-RateLimit-Reset'])

        if response.status_code == 429:
            self.rate_limited_count += 1
            # Back off aggressively on rate limit
            reset_time = self.rate_limit_reset or int(time.time()) + 60
            wait_time = max(1, reset_time - int(time.time()))
            print(f"[RATE LIMIT] Waiting {wait_time}s before retry")
            time.sleep(wait_time)

        self.requests_count += 1

    def should_throttle(self):
        """Check if we should throttle requests"""
        return self.rate_limit_remaining < 10


# Create circuit breakers for each service
fleet_breaker = CircuitBreaker()
charging_breaker = CircuitBreaker()
driver_breaker = CircuitBreaker()
maintenance_breaker = CircuitBreaker()

rate_limiter = RateLimitAwarenessTracker()


def run_enhanced_tests():
    """Run chaos tests with authentication and resilience"""
    print("\n" + "="*70)
    print("ENHANCED CHAOS TESTING WITH AUTHENTICATION & RETRY LOGIC")
    print("="*70)

    print(f"\nConfiguration:")
    print(f"  API Gateway: {GATEWAY_URL}")
    print(f"  Auth Service: {AUTH_SERVICE_URL}")
    print(f"  Test User: {TEST_EMAIL}")
    print(f"  Max Retries: {RetryConfig.MAX_RETRIES}")
    print(f"  Initial Backoff: {RetryConfig.INITIAL_DELAY}s")

    # Get initial token
    print("\n[Auth] Obtaining authentication token...")
    token = get_auth_token()
    if not token:
        print("[ERROR] Failed to obtain auth token. Exiting.")
        return 1

    print(f"[OK] Token obtained. Expires in ~1 hour")

    # Test 1: Basic connectivity with retry
    print("\n[Test 1] Testing basic connectivity with retry logic...")
    try:
        response = AuthenticatedRequest.get(
            f"{GATEWAY_URL}/api/v1/vehicles",
            timeout=5
        )
        print(f"[OK] Connected successfully ({response.status_code})")
    except Exception as e:
        print(f"[FAIL] Could not connect after retries: {str(e)[:50]}")
        return 1

    # Test 2: Load test with rate limit awareness
    print("\n[Test 2] Load testing with rate limit awareness...")
    success_count = 0
    fail_count = 0

    for i in range(50):
        if rate_limiter.should_throttle():
            print(f"  [THROTTLE] Rate limit approaching, backing off...")
            time.sleep(1)

        try:
            response = AuthenticatedRequest.get(
                f"{GATEWAY_URL}/api/v1/vehicles",
                timeout=5
            )
            rate_limiter.update_from_response(response)

            if response.status_code == 200:
                success_count += 1
            else:
                fail_count += 1
        except Exception as e:
            fail_count += 1

        if (i + 1) % 10 == 0:
            print(f"  Progress: {i+1}/50 (Success: {success_count}, Failed: {fail_count})")

    success_rate = (success_count / (success_count + fail_count) * 100) if (success_count + fail_count) > 0 else 0
    print(f"[OK] Load test completed: {success_rate:.1f}% success rate")

    # Test 3: Circuit breaker test
    print("\n[Test 3] Testing circuit breaker behavior...")
    breaker_test_passed = True
    for i in range(3):
        try:
            def test_request():
                return AuthenticatedRequest.get(
                    f"{GATEWAY_URL}/api/invalid-endpoint-{i}",
                    timeout=2
                )
            fleet_breaker.call(test_request)
        except Exception:
            pass

    if fleet_breaker.state == "OPEN":
        print(f"[OK] Circuit breaker opened after failures")
    else:
        print(f"[WARNING] Circuit breaker did not open as expected")

    # Test 4: Token refresh
    print("\n[Test 4] Testing automatic token refresh...")
    old_token = current_token
    # Force token expiry
    global token_expiry
    token_expiry = time.time() - 10

    new_token = get_auth_token()
    if new_token and new_token != old_token:
        print(f"[OK] Token refreshed successfully")
    else:
        print(f"[WARNING] Token refresh may have issues")

    # Summary
    print("\n" + "="*70)
    print("TEST SUMMARY")
    print("="*70)
    print(f"Total Requests: {rate_limiter.requests_count}")
    print(f"Rate Limited: {rate_limiter.rate_limited_count}")
    print(f"Success Rate: {success_rate:.1f}%")
    print(f"Circuit Breaker State: {fleet_breaker.state}")

    if success_rate >= 90:
        print("\n[OK] Chaos tests with authentication PASSED")
        return 0
    else:
        print("\n[WARNING] Some tests failed - review output above")
        return 1


if __name__ == "__main__":
    sys.exit(run_enhanced_tests())
