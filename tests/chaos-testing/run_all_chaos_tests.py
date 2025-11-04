#!/usr/bin/env python3
"""
EV Fleet Management - Master Chaos Testing Orchestrator
Runs all chaos testing modules and generates comprehensive report
"""

import subprocess
import sys
import time
from datetime import datetime
import io

# Force UTF-8 encoding for output
if sys.stdout.encoding.lower() != 'utf-8':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

def print_header(text):
    """Print formatted header"""
    print(f"\n{Colors.BOLD}{Colors.MAGENTA}")
    print("=" * 100)
    print(f"{text:^100}")
    print("=" * 100)
    print(f"{Colors.RESET}")

def run_test_module(module_name, script_name):
    """Run a test module and capture results"""
    print(f"\n{Colors.BOLD}{Colors.CYAN}Running: {module_name}{Colors.RESET}")
    print("-" * 100)

    try:
        result = subprocess.run(
            [sys.executable, script_name],
            cwd="C:\\Users\\omman\\Desktop\\SEV",
            capture_output=True,
            text=True,
            timeout=300
        )

        print(result.stdout)
        if result.stderr:
            print(f"{Colors.YELLOW}Warnings/Errors:{Colors.RESET}")
            print(result.stderr)

        return result.returncode == 0
    except subprocess.TimeoutExpired:
        print(f"{Colors.RED}✗ Test module timed out{Colors.RESET}")
        return False
    except Exception as e:
        print(f"{Colors.RED}✗ Error running test: {str(e)}{Colors.RESET}")
        return False

def main():
    """Main test orchestrator"""
    print_header("EV FLEET MANAGEMENT SYSTEM - COMPREHENSIVE CHAOS TESTING")

    start_time = datetime.now()

    test_modules = [
        ("Advanced Chaos Testing", "advanced_chaos_testing.py"),
        ("Network Failure Testing", "chaos_network_failures.py"),
        ("Database & Security Testing", "chaos_database_security.py"),
    ]

    results = {}

    for module_name, script_name in test_modules:
        success = run_test_module(module_name, script_name)
        results[module_name] = "PASSED" if success else "FAILED"
        time.sleep(1)  # Brief pause between modules

    # Print final summary
    print_header("CHAOS TESTING FINAL REPORT")

    print(f"\n{Colors.BOLD}Test Execution Summary:{Colors.RESET}\n")

    passed_count = 0
    failed_count = 0

    for module_name, status in results.items():
        if status == "PASSED":
            print(f"  {Colors.GREEN}✓{Colors.RESET} {module_name:<50} {Colors.GREEN}{status}{Colors.RESET}")
            passed_count += 1
        else:
            print(f"  {Colors.RED}✗{Colors.RESET} {module_name:<50} {Colors.RED}{status}{Colors.RESET}")
            failed_count += 1

    end_time = datetime.now()
    duration = (end_time - start_time).total_seconds()

    print(f"\n{Colors.BOLD}Execution Statistics:{Colors.RESET}")
    print(f"  Total Modules: {len(test_modules)}")
    print(f"  Passed: {Colors.GREEN}{passed_count}{Colors.RESET}")
    print(f"  Failed: {Colors.RED}{failed_count}{Colors.RESET}")
    print(f"  Duration: {duration:.1f}s")

    print(f"\n{Colors.BOLD}Test Coverage:{Colors.RESET}")
    print(f"""
  ✓ Fleet Service (Invalid inputs, edge cases, race conditions)
  ✓ Charging Service (Invalid scenarios, resource exhaustion)
  ✓ Maintenance Service (Data validation)
  ✓ Driver Service (Data validation)
  ✓ Load Testing (Concurrent & sequential)
  ✓ Network Failures (Timeouts, connection issues, recovery)
  ✓ Database Failures (Constraints, transactions)
  ✓ Security Tests (SQL injection, XSS, auth bypass)
  ✓ Service Cascading Failures
  ✓ Data Consistency Verification
    """)

    print(f"\n{Colors.BOLD}What Was Tested:{Colors.RESET}")
    print("""
  ✓ Invalid input validation (null, empty, special chars)
  ✓ Boundary conditions (min/max values, out of range)
  ✓ Duplicate key constraints
  ✓ Foreign key constraints
  ✓ NULL constraints
  ✓ Race conditions (concurrent requests)
  ✓ Resource exhaustion scenarios
  ✓ Timeout handling
  ✓ Connection failures
  ✓ Service unavailability
  ✓ Intermittent failures
  ✓ Circuit breaker activation
  ✓ Transaction rollback
  ✓ SQL injection prevention
  ✓ XSS prevention
  ✓ Authentication bypass prevention
  ✓ Authorization bypass prevention
  ✓ Rate limiting enforcement
  ✓ Sensitive data exposure
  ✓ Data consistency after failures
  ✓ Service cascade failures
    """)

    if failed_count == 0:
        print(f"\n{Colors.GREEN}{Colors.BOLD}✓ All chaos tests completed successfully!{Colors.RESET}")
        print(f"{Colors.GREEN}The system is resilient to failures and edge cases.{Colors.RESET}\n")
        return 0
    else:
        print(f"\n{Colors.YELLOW}{Colors.BOLD}⚠ Some tests failed - review detailed output above{Colors.RESET}\n")
        return 1

if __name__ == "__main__":
    sys.exit(main())
