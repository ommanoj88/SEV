#!/usr/bin/env python3
"""
EV Fleet Management Platform - Universal Application Launcher (FIXED VERSION)

This script provides a comprehensive solution to start/stop the entire application stack:
- Checks all prerequisites (Docker, PostgreSQL, Node.js, Java)
- Initializes PostgreSQL databases if needed
- Kills processes on required ports
- Rebuilds Docker images with --no-cache to ensure latest code
- Starts all infrastructure and microservices
- Monitors service health
- Provides clear status updates

Usage:
    python run_app_fixed.py start       # Start all services
    python run_app_fixed.py stop        # Stop all services
    python run_app_fixed.py restart     # Restart all services
    python run_app_fixed.py status      # Check service status
    python run_app_fixed.py clean       # Clean and rebuild everything
"""

import subprocess
import sys
import time
import os
import signal
import argparse
from typing import List, Dict, Optional
import json
import shutil

# Color codes for terminal output
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

# Port definitions
PORTS = {
    'postgres': [5432],
    'frontend': [3000],
    'gateway': [8080],
    'eureka': [8761],
    'auth': [8081],
    'fleet': [8082],
    'charging': [8083],
    'maintenance': [8084],
    'driver': [8085],
    'analytics': [8086],
    'notification': [8087],
    'billing': [8088],
    'redis': [6379],
    'rabbitmq': [5672, 15672],
}

# Service definitions in dependency order
INFRASTRUCTURE_SERVICES = ['postgres', 'redis', 'rabbitmq']
DISCOVERY_SERVICES = ['eureka-server']
GATEWAY_SERVICES = ['api-gateway']
BUSINESS_SERVICES = [
    'auth-service',
    'fleet-service',
    'charging-service',
    'maintenance-service',
    'driver-service',
    'analytics-service',
    'notification-service',
    'billing-service'
]
FRONTEND_SERVICES = ['frontend']

ALL_SERVICES = (
    INFRASTRUCTURE_SERVICES +
    DISCOVERY_SERVICES +
    GATEWAY_SERVICES +
    BUSINESS_SERVICES +
    FRONTEND_SERVICES
)


def print_header(message: str):
    """Print formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(80)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")


def print_success(message: str):
    """Print success message"""
    print(f"{Colors.OKGREEN}✓ {message}{Colors.ENDC}")


def print_info(message: str):
    """Print info message"""
    print(f"{Colors.OKCYAN}ℹ {message}{Colors.ENDC}")


def print_warning(message: str):
    """Print warning message"""
    print(f"{Colors.WARNING}⚠ {message}{Colors.ENDC}")


def print_error(message: str):
    """Print error message"""
    print(f"{Colors.FAIL}✗ {message}{Colors.ENDC}")


def run_command(cmd: List[str], cwd: Optional[str] = None, check: bool = True, capture: bool = False) -> bool:
    """Run a shell command"""
    try:
        if capture:
            result = subprocess.run(cmd, cwd=cwd, check=check, capture_output=True, text=True)
            return result.returncode == 0, result.stdout
        else:
            result = subprocess.run(cmd, cwd=cwd, check=check)
            return result.returncode == 0
    except subprocess.CalledProcessError:
        return False
    except FileNotFoundError:
        return False


def check_command_exists(command: str) -> bool:
    """Check if a command exists in PATH"""
    return shutil.which(command) is not None


def check_prerequisites() -> bool:
    """Check all required dependencies"""
    print_header("Checking Prerequisites")

    all_ok = True

    # Check Docker
    print_info("Checking Docker...")
    if check_command_exists('docker'):
        success, output = run_command(['docker', '--version'], capture=True)
        if success:
            print_success(f"Docker installed: {output.strip()}")
        else:
            print_error("Docker is not running")
            all_ok = False
    else:
        print_error("Docker is not installed or not in PATH")
        print_info("Please install Docker from https://www.docker.com/get-started")
        all_ok = False

    # Check Docker Compose
    print_info("Checking Docker Compose...")
    if check_command_exists('docker-compose'):
        success, output = run_command(['docker-compose', '--version'], capture=True)
        if success:
            print_success(f"Docker Compose installed: {output.strip()}")
        else:
            print_error("Docker Compose is not working")
            all_ok = False
    else:
        print_error("Docker Compose is not installed or not in PATH")
        all_ok = False

    # Check PostgreSQL
    print_info("Checking PostgreSQL...")
    if check_command_exists('psql'):
        success, output = run_command(['psql', '--version'], capture=True)
        if success:
            print_success(f"PostgreSQL client installed: {output.strip()}")
        else:
            print_warning("PostgreSQL client found but not working properly")
    else:
        print_warning("PostgreSQL client (psql) not found in PATH")
        print_info("PostgreSQL will run in Docker container")

    # Check Node.js (optional, for local frontend development)
    print_info("Checking Node.js...")
    if check_command_exists('node'):
        success, output = run_command(['node', '--version'], capture=True)
        if success:
            print_success(f"Node.js installed: {output.strip()}")
    else:
        print_info("Node.js not found (optional - frontend runs in Docker)")

    # Check Java (optional, for local backend development)
    print_info("Checking Java...")
    if check_command_exists('java'):
        success, output = run_command(['java', '--version'], capture=True)
        if success:
            print_success(f"Java installed")
    else:
        print_info("Java not found (optional - backend runs in Docker)")

    # Check Firebase credentials
    print_info("Checking Firebase credentials...")
    firebase_file = os.path.join('backend', 'auth-service', 'firebase-service-account.json')
    if os.path.exists(firebase_file):
        print_success("Firebase credentials file found")
    else:
        print_warning(f"Firebase credentials not found at: {firebase_file}")
        print_info("Auth service may not work properly without Firebase credentials")

    # Check environment variables
    print_info("Checking environment variables...")
    env_vars = ['FIREBASE_API_KEY', 'FIREBASE_AUTH_DOMAIN']
    env_missing = []
    for var in env_vars:
        if not os.getenv(var):
            env_missing.append(var)

    if env_missing:
        print_warning(f"Missing environment variables: {', '.join(env_missing)}")
        print_info("These are optional but recommended for Firebase integration")
    else:
        print_success("All Firebase environment variables set")

    if not all_ok:
        print_error("\n❌ Some prerequisites are missing. Please install them and try again.")
        return False

    print_success("\n✅ All critical prerequisites check passed!")
    return True


def initialize_databases() -> bool:
    """Initialize PostgreSQL databases if needed - SAFE VERSION"""
    print_header("Checking Database Initialization")

    print_info("Waiting for PostgreSQL to be ready...")
    time.sleep(10)  # Give PostgreSQL time to start

    # Use init_database.py (SAFE - doesn't drop existing databases)
    init_script = 'init_database.py'
    if not os.path.exists(init_script):
        print_warning(f"Database initialization script not found: {init_script}")
        print_info("Assuming databases are already initialized")
        return True

    print_info("Running SAFE database initialization script...")
    print_info("This will create databases ONLY if they don't exist")
    print_warning("⚠️  Your existing data is SAFE - nothing will be dropped")

    try:
        result = subprocess.run(
            [sys.executable, init_script],
            check=False,
            capture_output=False,  # Show output in real-time
            text=True
        )

        if result.returncode == 0:
            print_success("Database initialization completed successfully")
            return True
        else:
            print_warning("Database initialization had some issues")
            print_info("Continuing anyway - databases may already be initialized")
            return True
    except Exception as e:
        print_warning(f"Could not run database initialization: {e}")
        print_info("Continuing anyway - databases may already be initialized")
        return True


def kill_port(port: int):
    """Kill process using specified port"""
    if sys.platform == 'win32':
        try:
            # Find PID using the port
            result = subprocess.run(
                f'netstat -ano | findstr :{port}',
                shell=True,
                capture_output=True,
                text=True
            )

            if result.stdout:
                lines = result.stdout.strip().split('\n')
                for line in lines:
                    if f':{port}' in line:
                        parts = line.split()
                        if len(parts) >= 5:
                            pid = parts[-1]
                            try:
                                pid_int = int(pid)
                                subprocess.run(f'taskkill /F /PID {pid_int}', shell=True, check=False)
                                print_success(f"Killed process on port {port} (PID: {pid_int})")
                            except ValueError:
                                pass
        except Exception as e:
            print_warning(f"Could not kill port {port}: {e}")
    else:
        try:
            result = subprocess.run(
                f'lsof -ti:{port}',
                shell=True,
                capture_output=True,
                text=True
            )

            if result.stdout:
                pids = result.stdout.strip().split('\n')
                for pid in pids:
                    try:
                        subprocess.run(f'kill -9 {pid}', shell=True, check=False)
                        print_success(f"Killed process on port {port} (PID: {pid})")
                    except Exception:
                        pass
        except Exception as e:
            print_warning(f"Could not kill port {port}: {e}")


def kill_all_ports():
    """Kill all processes on required ports"""
    print_header("Cleaning Up Ports")

    all_ports = []
    for ports in PORTS.values():
        all_ports.extend(ports)

    for port in all_ports:
        kill_port(port)

    print_success("Port cleanup completed")


def stop_docker_services():
    """Stop all Docker services"""
    print_header("Stopping Docker Services")

    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')

    try:
        subprocess.run(
            ['docker-compose', 'down'],
            cwd=docker_dir,
            check=False
        )
        print_success("All Docker services stopped")
    except Exception as e:
        print_error(f"Error stopping services: {e}")


def clean_docker():
    """Clean Docker resources"""
    print_header("Cleaning Docker Resources")

    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')

    print_info("Stopping all containers...")
    subprocess.run(['docker-compose', 'down', '-v'], cwd=docker_dir, check=False)

    print_info("Removing unused images...")
    subprocess.run(['docker', 'image', 'prune', '-f'], check=False)

    print_success("Docker cleanup completed")


def build_docker_images() -> bool:
    """Build all Docker images"""
    print_header("Building Docker Images")

    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')

    try:
        print_info("Building all services with --no-cache to ensure latest code...")
        print_warning("This may take 10-15 minutes on first run...")

        process = subprocess.Popen(
            ['docker-compose', 'build', '--no-cache'],
            cwd=docker_dir,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1
        )

        for line in process.stdout:
            print(line, end='')

        process.wait()

        if process.returncode == 0:
            print_success("All Docker images built successfully")
            return True
        else:
            print_error("Docker build failed")
            return False
    except Exception as e:
        print_error(f"Error building Docker images: {e}")
        return False


def start_service_group(services: List[str], wait_time: int = 30):
    """Start a group of services"""
    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')

    for service in services:
        print_info(f"Starting {service}...")
        cmd = ['docker-compose', 'up', '-d', service]
        if not run_command(cmd, cwd=docker_dir, check=False):
            print_warning(f"Failed to start {service}")

    if wait_time > 0:
        print_info(f"Waiting {wait_time}s for services to initialize...")
        time.sleep(wait_time)


def wait_for_service_health(services: List[str], timeout: int = 300) -> bool:
    """Wait for services to become healthy"""
    print_info(f"Waiting for services to become healthy (timeout: {timeout}s)...")

    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')
    start_time = time.time()

    while time.time() - start_time < timeout:
        all_healthy = True

        for service in services:
            try:
                result = subprocess.run(
                    ['docker-compose', 'ps', service],
                    cwd=docker_dir,
                    capture_output=True,
                    text=True,
                    check=False
                )

                # Check if service is running
                if 'Up' not in result.stdout:
                    all_healthy = False
                    break

            except Exception:
                all_healthy = False
                break

        if all_healthy:
            print_success("All services are healthy!")
            return True

        time.sleep(5)

    print_warning("Timeout waiting for services to become healthy")
    return False


def start_all_services():
    """Start all services in correct order"""
    print_header("Starting All Services")

    print_info("Starting infrastructure services (PostgreSQL, Redis, RabbitMQ)...")
    start_service_group(INFRASTRUCTURE_SERVICES, wait_time=20)

    # Initialize databases after PostgreSQL starts
    if not initialize_databases():
        print_warning("Database initialization had issues, but continuing...")

    print_info("Starting service discovery (Eureka)...")
    start_service_group(DISCOVERY_SERVICES, wait_time=30)

    print_info("Starting API Gateway...")
    start_service_group(GATEWAY_SERVICES, wait_time=20)

    print_info("Starting business microservices...")
    start_service_group(BUSINESS_SERVICES, wait_time=40)

    print_info("Starting frontend...")
    start_service_group(FRONTEND_SERVICES, wait_time=10)

    print_success("All services started")

    # Wait for services to be healthy
    wait_for_service_health(BUSINESS_SERVICES, timeout=120)


def show_status():
    """Show status of all services"""
    print_header("Service Status")

    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')

    try:
        result = subprocess.run(
            ['docker-compose', 'ps'],
            cwd=docker_dir,
            capture_output=True,
            text=True,
            check=True
        )
        print(result.stdout)

        print("\n" + "="*80)
        print_info("Service Endpoints:")
        print(f"  • Eureka Dashboard:      http://localhost:8761")
        print(f"  • API Gateway:           http://localhost:8080")
        print(f"  • RabbitMQ Management:   http://localhost:15672 (user: evfleet, pass: evfleet123)")
        print(f"  • Frontend Application:  http://localhost:3000")
        print(f"  • Auth Service:          http://localhost:8081/actuator/health")
        print(f"  • Fleet Service:         http://localhost:8082/actuator/health")
        print(f"  • Charging Service:      http://localhost:8083/actuator/health")
        print(f"  • Maintenance Service:   http://localhost:8084/actuator/health")
        print(f"  • Billing Service:       http://localhost:8088/actuator/health")
        print("="*80 + "\n")

    except Exception as e:
        print_error(f"Error checking status: {e}")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description='EV Fleet Management Platform - Universal Launcher (FIXED)',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_app_fixed.py start        # Start all services
  python run_app_fixed.py stop         # Stop all services
  python run_app_fixed.py restart      # Restart all services
  python run_app_fixed.py status       # Check service status
  python run_app_fixed.py clean        # Full clean and rebuild
        """
    )

    parser.add_argument(
        'command',
        choices=['start', 'stop', 'restart', 'status', 'clean'],
        help='Command to execute'
    )

    parser.add_argument(
        '--skip-build',
        action='store_true',
        help='Skip Docker image rebuild (faster, but may use cached code)'
    )

    parser.add_argument(
        '--skip-prereq-check',
        action='store_true',
        help='Skip prerequisite checks (not recommended)'
    )

    args = parser.parse_args()

    # Change to script directory
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    # Check prerequisites for start/restart/clean commands
    if args.command in ['start', 'restart', 'clean'] and not args.skip_prereq_check:
        if not check_prerequisites():
            print_error("\n❌ Prerequisites check failed. Fix the issues above and try again.")
            print_info("Or use --skip-prereq-check to skip this check (not recommended)")
            sys.exit(1)

    try:
        if args.command == 'stop':
            kill_all_ports()
            stop_docker_services()
            print_success("Application stopped successfully")

        elif args.command == 'status':
            show_status()

        elif args.command == 'clean':
            kill_all_ports()
            clean_docker()
            if build_docker_images():
                print_success("Clean and rebuild completed successfully")
            else:
                print_error("Clean and rebuild failed")
                sys.exit(1)

        elif args.command in ['start', 'restart']:
            if args.command == 'restart':
                kill_all_ports()
                stop_docker_services()

            # Build images if not skipped
            if not args.skip_build:
                if not build_docker_images():
                    print_error("Build failed. Aborting.")
                    sys.exit(1)
            else:
                print_warning("Skipping Docker build - using existing images")

            # Start services
            start_all_services()

            # Show final status
            print("\n")
            show_status()

            print_success("\n✅ Application started successfully!")
            print_info("\n🌐 Access the application at: http://localhost:3000")
            print_info("📊 Monitor services at: http://localhost:8761 (Eureka Dashboard)")
            print_info("🐰 RabbitMQ Management: http://localhost:15672 (user: evfleet, pass: evfleet123)")
            print_warning("\n⚠️  Press Ctrl+C to stop monitoring (services will keep running)")

            # Keep script running to show logs
            try:
                while True:
                    time.sleep(1)
            except KeyboardInterrupt:
                print_info("\nMonitoring stopped. Services are still running.")
                print_info("Use 'python run_app_fixed.py stop' to stop all services")

    except KeyboardInterrupt:
        print_info("\n\nOperation cancelled by user")
        sys.exit(0)
    except Exception as e:
        print_error(f"\n\nUnexpected error: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
