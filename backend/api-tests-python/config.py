"""
Configuration for EVFleet API Tests
"""
import os
from pydantic import BaseSettings, Field


class TestConfig(BaseSettings):
    """Test configuration"""

    # API Configuration
    API_BASE_URL: str = Field(default="http://localhost:8080", env="API_BASE_URL")
    API_TIMEOUT: int = Field(default=30, env="API_TIMEOUT")

    # Firebase Test Credentials
    FIREBASE_TEST_UID: str = Field(default="test-uid-123", env="FIREBASE_TEST_UID")
    FIREBASE_TEST_TOKEN: str = Field(default="", env="FIREBASE_TEST_TOKEN")

    # Test User Credentials
    TEST_USER_EMAIL: str = Field(default="test@evfleet.com", env="TEST_USER_EMAIL")
    TEST_USER_NAME: str = Field(default="Test User", env="TEST_USER_NAME")
    TEST_USER_PHONE: str = Field(default="9876543210", env="TEST_USER_PHONE")

    # Database Configuration (for validation)
    DB_HOST: str = Field(default="localhost", env="DB_HOST")
    DB_PORT: int = Field(default=5432, env="DB_PORT")
    DB_USER: str = Field(default="postgres", env="DB_USER")
    DB_PASSWORD: str = Field(default="Shobharain11@", env="DB_PASSWORD")

    # Redis Configuration (for cache tests)
    REDIS_HOST: str = Field(default="localhost", env="REDIS_HOST")
    REDIS_PORT: int = Field(default=6379, env="REDIS_PORT")

    # Load Testing Configuration
    LOAD_TEST_USERS: int = Field(default=100, env="LOAD_TEST_USERS")
    LOAD_TEST_SPAWN_RATE: int = Field(default=10, env="LOAD_TEST_SPAWN_RATE")
    LOAD_TEST_DURATION: int = Field(default=300, env="LOAD_TEST_DURATION")

    # Chaos Testing Configuration
    CHAOS_FAILURE_RATE: float = Field(default=0.1, env="CHAOS_FAILURE_RATE")
    CHAOS_LATENCY_MIN_MS: int = Field(default=50, env="CHAOS_LATENCY_MIN_MS")
    CHAOS_LATENCY_MAX_MS: int = Field(default=500, env="CHAOS_LATENCY_MAX_MS")

    # Test Environment
    TEST_ENV: str = Field(default="local", env="TEST_ENV")  # local, staging, prod

    # Logging
    LOG_LEVEL: str = Field(default="INFO", env="LOG_LEVEL")

    class Config:
        env_file = ".env"
        case_sensitive = True


# Global config instance
config = TestConfig()


# API Endpoints
class Endpoints:
    """API endpoint constants"""

    # Auth endpoints
    AUTH_REGISTER = "/api/v1/auth/register"
    AUTH_LOGIN = "/api/v1/auth/login"
    AUTH_ME = "/api/v1/auth/me"
    AUTH_USERS = "/api/v1/auth/users"
    AUTH_SYNC = "/api/v1/auth/sync"
    AUTH_HEALTH = "/api/v1/auth/health"

    # Fleet endpoints
    FLEET_VEHICLES = "/api/v1/fleet/vehicles"
    FLEET_TRIPS = "/api/v1/fleet/trips"
    FLEET_HEALTH = "/api/v1/fleet/health"

    # Charging endpoints
    CHARGING_STATIONS = "/api/v1/charging/stations"
    CHARGING_SESSIONS = "/api/v1/charging/sessions"
    CHARGING_HEALTH = "/api/v1/charging/health"

    # Driver endpoints
    DRIVER_LIST = "/api/v1/driver/drivers"
    DRIVER_HEALTH = "/api/v1/driver/health"

    # Analytics endpoints
    ANALYTICS_SUMMARY = "/api/v1/analytics/summary"
    ANALYTICS_HEALTH = "/api/v1/analytics/health"

    # Billing endpoints
    BILLING_SUBSCRIPTION = "/api/v1/billing/subscription"
    BILLING_INVOICES = "/api/v1/billing/invoices"
    BILLING_HEALTH = "/api/v1/billing/health"

    # Maintenance endpoints
    MAINTENANCE_RECORDS = "/api/v1/maintenance/records"
    MAINTENANCE_HEALTH = "/api/v1/maintenance/health"

    # Notification endpoints
    NOTIFICATION_LIST = "/api/v1/notification/notifications"
    NOTIFICATION_HEALTH = "/api/v1/notification/health"


# Test data constants
class TestData:
    """Test data constants"""

    VALID_EMAIL = "test@evfleet.com"
    VALID_PHONE = "9876543210"
    VALID_COMPANY_ID = 1
    VALID_COMPANY_NAME = "Test Fleet Company"

    INVALID_EMAIL = "not-an-email"
    INVALID_PHONE = "123"

    VEHICLE_MAKES = ["Tata", "MG", "Mahindra", "Toyota", "Hyundai"]
    FUEL_TYPES = ["EV", "ICE", "HYBRID"]
    VEHICLE_STATUSES = ["AVAILABLE", "IN_USE", "MAINTENANCE", "CHARGING"]
