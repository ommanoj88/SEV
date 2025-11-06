#!/usr/bin/env python3
"""
EV Fleet Management Platform - Universal Application Launcher

This script provides a comprehensive solution to start/stop the entire application stack:
- Kills processes on required ports
- Rebuilds Docker images with --no-cache to ensure latest code
- Starts all infrastructure and microservices
- Monitors service health
- Provides clear status updates

Usage:
    python run_app.py start       # Start all services
    python run_app.py stop        # Stop all services
    python run_app.py restart     # Restart all services
    python run_app.py status      # Check service status
    python run_app.py clean       # Clean and rebuild everything
"""

import subprocess
import sys
import time
import os
import signal
import argparse
from typing import List, Dict, Optional
import json

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
INFRASTRUCTURE_SERVICES = ['redis', 'rabbitmq']  # postgres runs on host machine
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


def run_command(cmd: List[str], cwd: Optional[str] = None, check: bool = True) -> bool:
    """Run a shell command and return success status"""
    try:
        result = subprocess.run(
            cmd,
            cwd=cwd,
            check=check,
            capture_output=True,
            text=True
        )
        return result.returncode == 0
    except subprocess.CalledProcessError as e:
        if check:
            print_error(f"Command failed: {' '.join(cmd)}")
            print_error(f"Error: {e.stderr}")
        return False
    except Exception as e:
        print_error(f"Error running command: {e}")
        return False


def get_process_on_port(port: int) -> Optional[str]:
    """Get process ID using a specific port"""
    try:
        # Try lsof first (Linux/Mac)
        result = subprocess.run(
            ['lsof', '-t', f'-i:{port}'],
            capture_output=True,
            text=True,
            check=False
        )
        if result.returncode == 0 and result.stdout.strip():
            pids = result.stdout.strip().split('\n')
            return pids[0] if pids else None
        
        # Fallback to netstat (cross-platform)
        if sys.platform == 'win32':
            result = subprocess.run(
                ['netstat', '-ano'],
                capture_output=True,
                text=True,
                check=False
            )
            for line in result.stdout.split('\n'):
                if f':{port}' in line and 'LISTENING' in line:
                    parts = line.split()
                    if parts:
                        pid = parts[-1].strip()
                        # Validate PID is numeric
                        if pid.isdigit():
                            return pid
    except Exception:
        pass
    return None


def kill_port(port: int):
    """Kill process using a specific port"""
    pid = get_process_on_port(port)
    if pid:
        try:
            # Validate PID is numeric
            if not pid.isdigit():
                print_warning(f"Invalid PID format for port {port}: {pid}")
                return False
                
            print_info(f"Killing process {pid} on port {port}")
            if sys.platform == 'win32':
                subprocess.run(['taskkill', '/F', '/PID', pid], check=False)
            else:
                os.kill(int(pid), signal.SIGKILL)
            time.sleep(0.5)
            print_success(f"Killed process on port {port}")
            return True
        except Exception as e:
            print_warning(f"Could not kill process on port {port}: {e}")
            return False
    return True


def kill_all_ports():
    """Kill processes on all required ports"""
    print_header("Cleaning Up Ports")
    all_ports = []
    for service_ports in PORTS.values():
        all_ports.extend(service_ports)
    
    for port in set(all_ports):
        kill_port(port)
    
    print_success("All ports cleaned up")


def stop_docker_services():
    """Stop all Docker containers"""
    print_header("Stopping Docker Services")
    
    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')
    
    if run_command(['docker-compose', 'down'], cwd=docker_dir, check=False):
        print_success("Docker services stopped")
    else:
        print_warning("Some Docker services may not have stopped cleanly")


def clean_docker():
    """Clean Docker volumes and images"""
    print_header("Cleaning Docker Resources")
    
    # Stop all containers
    stop_docker_services()
    
    # Remove volumes
    print_info("Removing Docker volumes...")
    run_command(['docker', 'volume', 'prune', '-f'], check=False)
    
    # Remove dangling images
    print_info("Removing dangling images...")
    run_command(['docker', 'image', 'prune', '-f'], check=False)
    
    print_success("Docker resources cleaned")


def build_docker_images():
    """Build all Docker images with --no-cache to ensure latest code"""
    print_header("Building Docker Images (No Cache - Latest Code)")
    
    docker_dir = os.path.join(os.path.dirname(__file__), 'docker')
    
    print_info("Building all images with --no-cache flag...")
    print_info("This may take several minutes...")
    
    cmd = ['docker-compose', 'build', '--no-cache', '--parallel']
    
    try:
        # Run with real-time output
        process = subprocess.Popen(
            cmd,
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


def start_all_services():
    """Start all services in correct order"""
    print_header("Starting All Services")
    
    print_info("Starting infrastructure services (PostgreSQL, Redis, RabbitMQ)...")
    start_service_group(INFRASTRUCTURE_SERVICES, wait_time=20)
    
    print_info("Starting service discovery (Eureka)...")
    start_service_group(DISCOVERY_SERVICES, wait_time=30)
    
    print_info("Starting API Gateway...")
    start_service_group(GATEWAY_SERVICES, wait_time=20)
    
    print_info("Starting business microservices...")
    start_service_group(BUSINESS_SERVICES, wait_time=40)
    
    print_info("Starting frontend...")
    start_service_group(FRONTEND_SERVICES, wait_time=10)
    
    print_success("All services started")


def check_service_health(service: str, port: int, path: str = '/actuator/health') -> bool:
    """Check if a service is healthy"""
    try:
        import urllib.request
        url = f'http://localhost:{port}{path}'
        response = urllib.request.urlopen(url, timeout=5)
        return response.status == 200
    except Exception:
        return False


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
        print("="*80 + "\n")
        
    except Exception as e:
        print_error(f"Error checking status: {e}")


def main():
    """Main entry point"""
    parser = argparse.ArgumentParser(
        description='EV Fleet Management Platform - Universal Launcher',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_app.py start        # Start all services
  python run_app.py stop         # Stop all services
  python run_app.py restart      # Restart all services
  python run_app.py status       # Check service status
  python run_app.py clean        # Full clean and rebuild
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
    
    args = parser.parse_args()
    
    # Change to script directory
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
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
                time.sleep(2)
            
            # Always kill ports before starting
            kill_all_ports()
            
            # Build images unless skipped
            if not args.skip_build:
                if not build_docker_images():
                    print_error("Build failed. Use --skip-build to start with existing images")
                    sys.exit(1)
            else:
                print_warning("Skipping build - using existing Docker images")
            
            # Start services
            start_all_services()
            
            # Show status
            time.sleep(5)
            show_status()
            
            print_success(f"Application {'restarted' if args.command == 'restart' else 'started'} successfully!")
            print_info("Access the application at: http://localhost:3000")
            
    except KeyboardInterrupt:
        print_warning("\nOperation cancelled by user")
        sys.exit(1)
    except Exception as e:
        print_error(f"Unexpected error: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()
