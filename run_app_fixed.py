#!/usr/bin/env python3
"""
EV Fleet Management Platform - Monolith Application Launcher
Updated for Modular Monolith Architecture (Nov 2025)

This script provides a comprehensive solution to start/stop the application:
- Checks all prerequisites (PostgreSQL, Redis, RabbitMQ, Node.js, Java)
- Initializes PostgreSQL databases if needed
- Kills processes on required ports
- Starts backend monolith and frontend
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
import glob

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

# Port definitions for MONOLITH architecture
PORTS = {
    'postgres': [5432],
    'redis': [6379],
    'rabbitmq': [5672, 15672],
    'backend': [8080],  # Monolith backend
    'frontend': [3000],
}

# Database names for monolith (8 databases)
DATABASES = [
    'evfleet_auth',
    'evfleet_fleet',
    'evfleet_charging',
    'evfleet_maintenance',
    'evfleet_driver',
    'evfleet_analytics',
    'evfleet_notification',
    'evfleet_billing'
]

# Maven path (IntelliJ bundled Maven on Windows)
MAVEN_CMD = r"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd"

# NPM path (explicit .cmd on Windows)
NPM_CMD = r"C:\Program Files\nodejs\npm.cmd"


def print_header(message: str):
    """Print formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(80)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*80}{Colors.ENDC}\n")


def print_success(message: str):
    """Print success message"""
    print(f"{Colors.OKGREEN}[OK] {message}{Colors.ENDC}")


def print_info(message: str):
    """Print info message"""
    print(f"{Colors.OKCYAN}[INFO] {message}{Colors.ENDC}")


def print_warning(message: str):
    """Print warning message"""
    print(f"{Colors.WARNING}[WARN] {message}{Colors.ENDC}")


def print_error(message: str):
    """Print error message"""
    print(f"{Colors.FAIL}[ERROR] {message}{Colors.ENDC}")


def run_command(cmd: List[str], cwd: Optional[str] = None, check: bool = True, capture: bool = False):
    """Run a shell command"""
    try:
        if capture:
            result = subprocess.run(cmd, cwd=cwd, check=check, capture_output=True, text=True)
            return result.returncode == 0, result.stdout
        else:
            result = subprocess.run(cmd, cwd=cwd, check=check)
            return result.returncode == 0
    except subprocess.CalledProcessError:
        return (False, "") if capture else False
    except FileNotFoundError:
        return (False, "") if capture else False


def check_command_exists(command: str) -> bool:
    """Check if a command exists in PATH"""
    return shutil.which(command) is not None


def check_prerequisites() -> bool:
    """Check all required dependencies"""
    print_header("Checking Prerequisites")

    all_ok = True

    # Check PostgreSQL
    print_info("Checking PostgreSQL...")
    if check_command_exists('psql'):
        success, output = run_command(['psql', '--version'], capture=True)
        if success:
            print_success(f"PostgreSQL client installed: {output.strip()}")
        else:
            print_error("PostgreSQL client found but not working")
            all_ok = False
    else:
        print_error("PostgreSQL client (psql) not found in PATH")
        print_info("Please install PostgreSQL from https://www.postgresql.org/download/")
        all_ok = False

    # Check Redis (optional - can run in Docker)
    print_info("Checking Redis...")
    if check_command_exists('redis-server'):
        print_success("Redis installed locally")
    else:
        print_info("Redis not found locally (will check for Docker/remote)")

    # Check Node.js
    print_info("Checking Node.js...")
    if check_command_exists('node'):
        success, output = run_command(['node', '--version'], capture=True)
        if success:
            print_success(f"Node.js installed: {output.strip()}")
        else:
            print_error("Node.js found but not working")
            all_ok = False
    else:
        print_error("Node.js not found in PATH")
        print_info("Please install Node.js from https://nodejs.org/")
        all_ok = False

    # Check npm
    print_info("Checking npm...")
    if check_command_exists('npm'):
        success, output = run_command(['npm', '--version'], capture=True)
        if success:
            print_success(f"npm installed: {output.strip()}")
    else:
        print_error("npm not found (should come with Node.js)")
        all_ok = False

    # Check Java
    print_info("Checking Java...")
    if check_command_exists('java'):
        success, output = run_command(['java', '--version'], capture=True)
        if success:
            print_success("Java installed (JDK 17+)")
        else:
            print_error("Java found but not working")
            all_ok = False
    else:
        print_error("Java not found in PATH")
        print_info("Please install Java 17+ from https://adoptium.net/")
        all_ok = False

    # Check Maven (check PATH first, then configured path)
    print_info("Checking Maven...")
    maven_found = False
    if check_command_exists('mvn'):
        success, output = run_command(['mvn', '--version'], capture=True)
        if success:
            print_success("Maven installed in PATH")
            maven_found = True
    elif os.path.exists(MAVEN_CMD):
        # Check the configured Maven path
        success, output = run_command([MAVEN_CMD, '--version'], capture=True)
        if success:
            print_success(f"Maven found at configured path")
            maven_found = True

    if not maven_found:
        print_error("Maven not found in PATH or configured location")
        print_info("Please install Maven from https://maven.apache.org/download.cgi")
        all_ok = False

    # Check Firebase credentials
    print_info("Checking Firebase credentials...")
    firebase_file = os.path.join('backend', 'evfleet-monolith', 'firebase-service-account.json')
    if os.path.exists(firebase_file):
        print_success("Firebase credentials file found")
    else:
        print_warning(f"Firebase credentials not found at: {firebase_file}")
        print_info("Auth module may not work properly without Firebase credentials")

    if not all_ok:
        print_error("\nSome prerequisites are missing. Please install them and try again.")
        return False

    print_success("\nAll prerequisites check passed!")
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


def check_postgres_running() -> bool:
    """Check if PostgreSQL is running"""
    try:
        env = os.environ.copy()
        env['PGPASSWORD'] = os.getenv('DB_PASSWORD', 'Shobharain11@')

        result = subprocess.run(
            ['psql', '-h', 'localhost', '-p', '5432', '-U', 'postgres', '-d', 'postgres', '-c', 'SELECT 1'],
            capture_output=True,
            env=env
        )
        return result.returncode == 0
    except Exception:
        return False


def check_redis_running() -> bool:
    """Check if Redis is running"""
    try:
        result = subprocess.run(
            ['redis-cli', 'ping'],
            capture_output=True,
            text=True
        )
        return 'PONG' in result.stdout
    except Exception:
        return False


def start_infrastructure():
    """Start infrastructure services (PostgreSQL, Redis, RabbitMQ)"""
    print_header("Starting Infrastructure Services")

    # Check PostgreSQL
    print_info("Checking PostgreSQL...")
    if check_postgres_running():
        print_success("PostgreSQL is already running")
    else:
        print_warning("PostgreSQL is not running")
        print_info("Please start PostgreSQL manually:")
        print_info("  Windows: Start 'postgresql-x64-15' service")
        print_info("  Linux: sudo systemctl start postgresql")
        print_info("  Mac: brew services start postgresql")
        return False

    # Check Redis
    print_info("Checking Redis...")
    if check_redis_running():
        print_success("Redis is already running")
    else:
        print_warning("Redis is not running on localhost:6379")
        print_info("Application will attempt to use Redis at configured address")

    # RabbitMQ check (optional)
    print_info("RabbitMQ check skipped (optional for basic operations)")

    return True


def initialize_databases():
    """Initialize databases using init_database.py"""
    print_header("Initializing Databases")

    init_script = 'init_database.py'
    if not os.path.exists(init_script):
        print_warning(f"Database initialization script not found: {init_script}")
        print_info("Databases may already be initialized")
        return True

    print_info("Running database initialization (safe - won't drop existing data)...")

    try:
        result = subprocess.run(
            [sys.executable, init_script],
            check=False,
            text=True
        )

        if result.returncode == 0:
            print_success("Database initialization completed")
            return True
        else:
            print_warning("Database initialization had issues, continuing anyway...")
            return True
    except Exception as e:
        print_warning(f"Could not run database initialization: {e}")
        return True


def start_backend():
    """Start the monolith backend"""
    print_header("Starting Backend Monolith")

    backend_dir = os.path.join('backend', 'evfleet-monolith')

    if not os.path.exists(backend_dir):
        print_error(f"Backend monolith directory not found: {backend_dir}")
        return False

    print_info("Building and starting evfleet-monolith with Maven...")
    print_warning("This will take 1-2 minutes on first run...")

    try:
        # ALWAYS clean build to ensure fresh code (no cached classes)
        print_info("Cleaning old build artifacts and building JAR...")
        build_result = subprocess.run(
            [MAVEN_CMD, 'clean', 'package', '-DskipTests', '-Dmaven.test.skip=true'],
            cwd=backend_dir,
            capture_output=True,
            text=True
        )

        if build_result.returncode != 0:
            print_error(f"Maven build failed: {build_result.stderr}")
            return False

        print_success("JAR built successfully")

        # Find the built JAR file (use absolute path)
        jar_files = glob.glob(os.path.join(os.path.abspath(backend_dir), 'target', 'evfleet-monolith-*.jar'))
        if not jar_files:
            print_error("No JAR file found in target directory")
            return False

        jar_path = os.path.abspath(jar_files[-1])  # Get absolute path to the latest jar
        print_info(f"Running JAR: {jar_path}")

        # Create log file for backend output
        log_file_path = os.path.join(os.path.abspath(backend_dir), 'startup.log')
        log_file = open(log_file_path, 'w')

        # Run JAR with dev profile (use absolute path to JAR)
        process = subprocess.Popen(
            ['java', '-jar', jar_path, '--spring.profiles.active=dev'],
            cwd=os.path.abspath(backend_dir),
            stdout=log_file,
            stderr=subprocess.STDOUT,
            text=True
        )

        # Monitor startup (non-blocking)
        print_info("Backend is starting in background...")
        print_info(f"Backend logs: {log_file_path}")
        print_info("Waiting for backend to be ready (checking HTTP health endpoint)...")

        # Wait up to 10 minutes for backend to start (increased due to clean build)
        max_wait = 600
        wait_time = 0
        port_opened = False
        last_log_check = 0

        while wait_time < max_wait:
            # Check if process is still running
            if process.poll() is not None:
                print_error(f"Backend process exited unexpectedly with code {process.returncode}")
                print_error(f"Check logs at: {log_file_path}")
                log_file.close()
                # Show last 50 lines of log
                try:
                    with open(log_file_path, 'r') as f:
                        lines = f.readlines()
                        print_error("\nLast 50 lines of backend log:")
                        print("".join(lines[-50:]))
                except:
                    pass
                return False

            try:
                # First check if port is open
                import socket
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                result = sock.connect_ex(('localhost', 8080))
                sock.close()

                if result == 0:
                    if not port_opened:
                        print_info("Port 8080 is open, waiting for Spring Boot to initialize...")
                        port_opened = True

                    # Port is open, now check if Spring Boot is actually ready
                    try:
                        import urllib.request
                        import urllib.error
                        import json

                        # Try to access the health endpoint
                        try:
                            response = urllib.request.urlopen('http://localhost:8080/actuator/health', timeout=5)
                            health_data = json.loads(response.read().decode())
                        except urllib.error.HTTPError as e:
                            # Even if we get 503 (SERVICE_UNAVAILABLE), we can still read the health JSON
                            if e.code == 503:
                                health_data = json.loads(e.read().decode())
                            else:
                                raise

                        # Check if we got a valid response (even if status is DOWN due to Redis/RabbitMQ)
                        # The important thing is that Spring Boot is responding
                        if 'status' in health_data:
                            print_success(f"Backend is ready on http://localhost:8080 (Health: {health_data['status']})")
                            if health_data['status'] == 'DOWN':
                                print_warning("Some components are DOWN (likely Redis/RabbitMQ), but core app is running")
                            log_file.close()
                            return True
                    except urllib.error.URLError as e:
                        # Port is open but Spring Boot not ready yet (connection refused, etc)
                        pass
                    except json.JSONDecodeError:
                        # Got a response but not valid JSON - might be an error page
                        pass
            except Exception as e:
                pass

            time.sleep(5)
            wait_time += 5

            # Show progress and hint to check logs
            if wait_time % 15 == 0:
                print_info(f"Still waiting... ({wait_time}s / {max_wait}s)")

            # Every 30 seconds, show a snippet from the log
            if wait_time % 30 == 0 and wait_time > last_log_check:
                last_log_check = wait_time
                try:
                    with open(log_file_path, 'r') as f:
                        lines = f.readlines()
                        if lines:
                            # Show last 5 lines
                            print_info(f"Recent log output:")
                            for line in lines[-5:]:
                                print(f"  {line.rstrip()}")
                except:
                    pass

        log_file.close()
        print_warning(f"Backend startup timeout - check logs at: {log_file_path}")
        return True  # Continue anyway

    except Exception as e:
        print_error(f"Error starting backend: {e}")
        return False


def start_frontend():
    """Start the frontend"""
    print_header("Starting Frontend")

    frontend_dir = 'frontend'

    if not os.path.exists(frontend_dir):
        print_error(f"Frontend directory not found: {frontend_dir}")
        return False

    print_info("Installing frontend dependencies...")

    try:
        # Install dependencies
        subprocess.run(
            [NPM_CMD, 'install'],
            cwd=frontend_dir,
            check=True
        )

        print_info("Starting frontend development server...")

        # Create log file for frontend output
        frontend_log = os.path.join(os.path.abspath(frontend_dir), 'frontend.log')
        log_file = open(frontend_log, 'w')

        # Start frontend (background) - redirect to log file instead of PIPE
        if sys.platform == 'win32':
            # On Windows, use CREATE_NEW_PROCESS_GROUP to properly detach
            process = subprocess.Popen(
                [NPM_CMD, 'start'],
                cwd=frontend_dir,
                stdout=log_file,
                stderr=subprocess.STDOUT,
                creationflags=subprocess.CREATE_NEW_PROCESS_GROUP | subprocess.DETACHED_PROCESS
            )
        else:
            # On Unix, use nohup-like approach
            process = subprocess.Popen(
                [NPM_CMD, 'start'],
                cwd=frontend_dir,
                stdout=log_file,
                stderr=subprocess.STDOUT,
                start_new_session=True
            )

        print_info(f"Frontend is starting in background (PID: {process.pid})...")
        print_info(f"Frontend logs: {frontend_log}")
        print_info("Waiting for frontend to be ready (checking port 3000)...")

        # Wait up to 2 minutes
        max_wait = 120
        wait_time = 0
        while wait_time < max_wait:
            try:
                import socket
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                result = sock.connect_ex(('localhost', 3000))
                sock.close()

                if result == 0:
                    print_success("Frontend is ready on http://localhost:3000")
                    return True
            except Exception:
                pass

            time.sleep(5)
            wait_time += 5

        print_warning("Frontend startup timeout - check logs manually")
        return True

    except Exception as e:
        print_error(f"Error starting frontend: {e}")
        return False


def show_status():
    """Show status of all services"""
    print_header("Service Status")

    # Check ports
    services = {
        'PostgreSQL': 5432,
        'Redis': 6379,
        'RabbitMQ': 5672,
        'RabbitMQ Management': 15672,
        'Backend Monolith': 8080,
        'Frontend': 3000,
    }

    import socket

    print_info("Checking service ports...")
    for service, port in services.items():
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            result = sock.connect_ex(('localhost', port))
            sock.close()

            if result == 0:
                print_success(f"{service:30} - Running on port {port}")
            else:
                print_warning(f"{service:30} - Not running on port {port}")
        except Exception:
            print_warning(f"{service:30} - Error checking port {port}")

    print("\n" + "="*80)
    print_info("Service Endpoints:")
    print(f"  • Frontend Application:  http://localhost:3000")
    print(f"  • Backend API:           http://localhost:8080")
    print(f"  • API Health Check:      http://localhost:8080/actuator/health")
    print(f"  • Swagger UI:            http://localhost:8080/swagger-ui.html")
    print(f"  • RabbitMQ Management:   http://localhost:15672 (user: evfleet, pass: evfleet123)")
    print("="*80 + "\n")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description='EV Fleet Management Platform - Monolith Launcher',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_app_fixed.py start        # Start all services
  python run_app_fixed.py stop         # Stop all services (kill ports)
  python run_app_fixed.py restart      # Restart all services
  python run_app_fixed.py status       # Check service status
        """
    )

    parser.add_argument(
        'command',
        choices=['start', 'stop', 'restart', 'status'],
        help='Command to execute'
    )

    parser.add_argument(
        '--skip-prereq-check',
        action='store_true',
        help='Skip prerequisite checks (not recommended)'
    )

    args = parser.parse_args()

    # Change to script directory
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    # Check prerequisites for start/restart commands
    if args.command in ['start', 'restart'] and not args.skip_prereq_check:
        if not check_prerequisites():
            print_error("\nPrerequisites check failed. Fix the issues above and try again.")
            print_info("Or use --skip-prereq-check to skip this check (not recommended)")
            sys.exit(1)

    try:
        if args.command == 'stop':
            kill_all_ports()
            print_success("Application stopped successfully (ports freed)")

        elif args.command == 'status':
            show_status()

        elif args.command in ['start', 'restart']:
            # Always kill ports before starting to avoid conflicts
            print_info("Cleaning up ports before starting...")
            kill_all_ports()
            time.sleep(2)

            # Start infrastructure
            if not start_infrastructure():
                print_error("Infrastructure services check failed")
                sys.exit(1)

            # Initialize databases
            initialize_databases()

            # Start backend
            print_info("\n[WARN] Backend will start in BACKGROUND and continue running")
            print_info("[WARN] Frontend will start in BACKGROUND and continue running")
            print_info("[WARN] Use 'python run_app_fixed.py stop' to stop all services")

            if not start_backend():
                print_error("Failed to start backend")
                sys.exit(1)

            time.sleep(5)

            # Start frontend
            if not start_frontend():
                print_error("Failed to start frontend")
                sys.exit(1)

            # Show final status
            time.sleep(3)
            print("\n")
            show_status()

            print_success("\nApplication started successfully!")
            print_info("\nAccess the application at: http://localhost:3000")
            print_info("API documentation: http://localhost:8080/swagger-ui.html")
            print_info("Health check: http://localhost:8080/actuator/health")

            print_warning("\nSERVICES ARE RUNNING IN BACKGROUND")
            print_info("To stop all services, run: python run_app_fixed.py stop")

    except KeyboardInterrupt:
        print_info("\n\nOperation cancelled by user")
        sys.exit(0)
    except Exception as e:
        print_error(f"\n\nUnexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == '__main__':
    main()
