"""
Pytest fixtures and configuration for API tests
"""
import pytest
import requests
from typing import Dict, Any
import logging
from config import config, Endpoints

logger = logging.getLogger(__name__)


@pytest.fixture(scope="session")
def api_client():
    """Create API client session"""
    session = requests.Session()
    session.headers.update({
        "Content-Type": "application/json",
        "Accept": "application/json"
    })
    yield session
    session.close()


@pytest.fixture(scope="session")
def base_url():
    """Get base API URL"""
    return config.API_BASE_URL


@pytest.fixture(scope="session")
def api_timeout():
    """Get API timeout"""
    return config.API_TIMEOUT


@pytest.fixture
def auth_headers():
    """Get authentication headers"""
    return {
        "Authorization": f"Bearer {config.FIREBASE_TEST_TOKEN}",
        "Content-Type": "application/json"
    }


@pytest.fixture
def test_user_data():
    """Get test user data"""
    import uuid
    unique_id = str(uuid.uuid4())[:8]

    return {
        "firebaseUid": f"test-{unique_id}",
        "email": f"test-{unique_id}@evfleet.com",
        "name": "Test User",
        "phone": f"90{unique_id[:8]}",
        "companyId": 1,
        "companyName": "Test Company"
    }


@pytest.fixture
def registered_user(api_client, base_url, test_user_data):
    """Register a test user and return user data"""
    response = api_client.post(
        f"{base_url}{Endpoints.AUTH_REGISTER}",
        json=test_user_data,
        timeout=config.API_TIMEOUT
    )

    if response.status_code in [200, 201]:
        yield response.json()
        # Cleanup: delete user after test
        # Note: Add delete endpoint if available
    else:
        pytest.fail(f"Failed to register test user: {response.status_code} {response.text}")


@pytest.fixture(scope="session")
def health_check(api_client, base_url):
    """Verify API is healthy before running tests"""
    try:
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_HEALTH}",
            timeout=5
        )
        if response.status_code != 200:
            pytest.exit(f"API health check failed: {response.status_code}")
        logger.info("✅ API health check passed")
    except requests.exceptions.RequestException as e:
        pytest.exit(f"Cannot connect to API: {e}")


@pytest.fixture
def test_vehicle_data():
    """Get test vehicle data"""
    import uuid
    unique_id = str(uuid.uuid4())[:8]

    return {
        "companyId": 1,
        "vehicleNumber": f"TEST{unique_id}",
        "make": "Tata",
        "model": "Nexon EV",
        "year": 2023,
        "fuelType": "EV",
        "batteryCapacity": 40.5,
        "currentBatterySoc": 85.0,
        "status": "AVAILABLE"
    }


@pytest.fixture
def test_charging_station_data():
    """Get test charging station data"""
    import uuid
    unique_id = str(uuid.uuid4())[:8]

    return {
        "name": f"Test Station {unique_id}",
        "location": "Test Location",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "totalSlots": 10,
        "availableSlots": 10,
        "pricePerKwh": 12.50,
        "status": "AVAILABLE"
    }


@pytest.fixture
def performance_metrics():
    """Track performance metrics for tests"""
    metrics = {
        "requests": [],
        "errors": [],
        "latencies": []
    }
    yield metrics

    # Log metrics after test
    if metrics["requests"]:
        avg_latency = sum(metrics["latencies"]) / len(metrics["latencies"])
        error_rate = len(metrics["errors"]) / len(metrics["requests"]) * 100

        logger.info(f"Performance Metrics:")
        logger.info(f"  Total Requests: {len(metrics['requests'])}")
        logger.info(f"  Errors: {len(metrics['errors'])}")
        logger.info(f"  Error Rate: {error_rate:.2f}%")
        logger.info(f"  Avg Latency: {avg_latency:.2f}ms")


def pytest_configure(config):
    """Configure pytest"""
    # Register custom markers
    config.addinivalue_line(
        "markers", "smoke: Quick smoke tests"
    )
    config.addinivalue_line(
        "markers", "load: Load and performance tests"
    )


def pytest_collection_modifyitems(config, items):
    """Modify test collection"""
    # Add skip marker for tests requiring specific environment
    skip_if_not_staging = pytest.mark.skip(reason="Test requires staging environment")

    for item in items:
        if "staging_only" in item.keywords and config.getoption("--env") != "staging":
            item.add_marker(skip_if_not_staging)
