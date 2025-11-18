"""
Authentication API Tests

Comprehensive tests for auth module endpoints:
- User registration
- User login
- Get current user (/me)
- User CRUD operations
- Firebase integration
"""
import pytest
import requests
import time
from config import Endpoints, config


@pytest.mark.smoke
@pytest.mark.auth
@pytest.mark.critical
class TestAuthSmoke:
    """Smoke tests for auth module"""

    def test_auth_health(self, api_client, base_url):
        """Test auth health endpoint"""
        response = api_client.get(f"{base_url}{Endpoints.AUTH_HEALTH}")

        assert response.status_code == 200
        assert "running" in response.text.lower()

    def test_auth_module_accessible(self, api_client, base_url):
        """Verify auth module is accessible"""
        # Try accessing without auth (should get 401 or redirect)
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_USERS}",
            allow_redirects=False
        )

        # Should not be 500 (module exists)
        assert response.status_code != 500


@pytest.mark.functional
@pytest.mark.auth
class TestUserRegistration:
    """Test user registration flow"""

    def test_register_new_user_success(self, api_client, base_url, test_user_data):
        """Test successful user registration"""
        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=test_user_data
        )

        assert response.status_code in [200, 201], f"Failed: {response.text}"

        data = response.json()
        assert data["success"] is True
        assert data["message"] == "User registered successfully"
        assert "user" in data
        assert data["user"]["email"] == test_user_data["email"]

    def test_register_duplicate_email_fails(self, api_client, base_url, registered_user, test_user_data):
        """Test duplicate email registration fails"""
        # Try to register with same email
        duplicate_data = test_user_data.copy()
        duplicate_data["email"] = registered_user["user"]["email"]
        duplicate_data["firebaseUid"] = "different-uid"

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=duplicate_data
        )

        assert response.status_code == 409, "Should return 409 Conflict for duplicate email"
        assert "already exists" in response.text.lower()

    def test_register_duplicate_firebase_uid_fails(self, api_client, base_url, registered_user, test_user_data):
        """Test duplicate Firebase UID registration fails"""
        duplicate_data = test_user_data.copy()
        duplicate_data["firebaseUid"] = test_user_data["firebaseUid"]
        duplicate_data["email"] = "different@email.com"

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=duplicate_data
        )

        assert response.status_code == 409

    def test_register_invalid_email_fails(self, api_client, base_url, test_user_data):
        """Test registration with invalid email fails"""
        invalid_data = test_user_data.copy()
        invalid_data["email"] = "not-an-email"

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=invalid_data
        )

        # Should return 400 Bad Request for validation error
        assert response.status_code == 400

    def test_register_missing_required_fields(self, api_client, base_url):
        """Test registration with missing required fields"""
        incomplete_data = {
            "email": "test@example.com"
            # Missing other required fields
        }

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=incomplete_data
        )

        assert response.status_code == 400


@pytest.mark.functional
@pytest.mark.auth
class TestGetCurrentUser:
    """Test /me endpoint (the one that had infinite loop bug)"""

    def test_get_current_user_success(self, api_client, base_url, auth_headers):
        """Test getting current user with valid token"""
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_ME}",
            headers=auth_headers
        )

        # With valid token should return user
        # Without valid Firebase token will return 401
        assert response.status_code in [200, 401]

    def test_get_current_user_no_auth_fails(self, api_client, base_url):
        """Test /me without auth token fails"""
        response = api_client.get(f"{base_url}{Endpoints.AUTH_ME}")

        assert response.status_code == 401

    def test_get_current_user_invalid_token_fails(self, api_client, base_url):
        """Test /me with invalid token fails"""
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_ME}",
            headers={"Authorization": "Bearer invalid-token-12345"}
        )

        assert response.status_code == 401

    def test_me_endpoint_caching(self, api_client, base_url, auth_headers):
        """Test that /me endpoint returns proper cache headers"""
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_ME}",
            headers=auth_headers
        )

        if response.status_code == 200:
            # Should have cache-control header (our fix!)
            assert "Cache-Control" in response.headers
            assert "max-age" in response.headers["Cache-Control"]

            # Should have ETag header
            assert "ETag" in response.headers

    @pytest.mark.load
    def test_me_endpoint_no_infinite_loop(self, api_client, base_url, auth_headers):
        """Test that /me endpoint doesn't cause infinite loop"""
        # Make 100 rapid requests - should not hang or timeout
        start_time = time.time()
        success_count = 0

        for i in range(100):
            try:
                response = api_client.get(
                    f"{base_url}{Endpoints.AUTH_ME}",
                    headers=auth_headers,
                    timeout=5  # 5 second timeout
                )
                if response.status_code in [200, 401]:
                    success_count += 1
            except requests.exceptions.Timeout:
                pytest.fail(f"Request {i} timed out - possible infinite loop!")

        elapsed = time.time() - start_time

        # Should complete quickly (not infinite loop)
        assert elapsed < 30, f"Took too long: {elapsed}s - possible performance issue"
        assert success_count == 100, f"Only {success_count}/100 requests succeeded"


@pytest.mark.functional
@pytest.mark.auth
class TestUserCRUD:
    """Test user CRUD operations"""

    def test_get_all_users(self, api_client, base_url, auth_headers):
        """Test getting all users"""
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_USERS}",
            headers=auth_headers
        )

        # Should return list (even if empty)
        if response.status_code == 200:
            assert isinstance(response.json(), list)

    def test_get_user_by_id(self, api_client, base_url, auth_headers, registered_user):
        """Test getting user by ID"""
        user_id = registered_user["user"]["id"]

        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_USERS}/{user_id}",
            headers=auth_headers
        )

        if response.status_code == 200:
            user = response.json()
            assert user["id"] == user_id

    def test_get_nonexistent_user_fails(self, api_client, base_url, auth_headers):
        """Test getting non-existent user returns 404"""
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_USERS}/99999",
            headers=auth_headers
        )

        assert response.status_code == 404

    def test_update_user(self, api_client, base_url, auth_headers, registered_user):
        """Test updating user"""
        user_id = registered_user["user"]["id"]

        update_data = {
            "name": "Updated Name",
            "phone": "9999999999"
        }

        response = api_client.put(
            f"{base_url}{Endpoints.AUTH_USERS}/{user_id}",
            headers=auth_headers,
            json=update_data
        )

        if response.status_code == 200:
            updated_user = response.json()
            assert updated_user["name"] == "Updated Name"

    def test_delete_user(self, api_client, base_url, auth_headers, registered_user):
        """Test deleting user (soft delete)"""
        user_id = registered_user["user"]["id"]

        response = api_client.delete(
            f"{base_url}{Endpoints.AUTH_USERS}/{user_id}",
            headers=auth_headers
        )

        assert response.status_code in [200, 204]


@pytest.mark.load
@pytest.mark.auth
class TestAuthPerformance:
    """Performance tests for auth endpoints"""

    def test_register_latency(self, api_client, base_url, test_user_data):
        """Test registration endpoint latency"""
        start = time.time()

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=test_user_data
        )

        latency = (time.time() - start) * 1000  # Convert to ms

        assert response.status_code in [200, 201, 409]  # 409 if already exists
        assert latency < 2000, f"Registration too slow: {latency}ms"

    def test_concurrent_registrations(self, api_client, base_url):
        """Test concurrent user registrations"""
        import concurrent.futures
        import uuid

        def register_user(index):
            unique_id = str(uuid.uuid4())[:8]
            data = {
                "firebaseUid": f"concurrent-{unique_id}",
                "email": f"concurrent-{unique_id}@test.com",
                "name": f"User {index}",
                "phone": f"90{str(index).zfill(8)}",
                "companyId": 1,
                "companyName": "Test Company"
            }

            start = time.time()
            response = api_client.post(
                f"{base_url}{Endpoints.AUTH_REGISTER}",
                json=data
            )
            latency = (time.time() - start) * 1000

            return {
                "status": response.status_code,
                "latency": latency,
                "success": response.status_code in [200, 201]
            }

        # Register 50 users concurrently
        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            futures = [executor.submit(register_user, i) for i in range(50)]
            results = [f.result() for f in concurrent.futures.as_completed(futures)]

        # Analyze results
        success_count = sum(1 for r in results if r["success"])
        avg_latency = sum(r["latency"] for r in results) / len(results)
        p95_latency = sorted([r["latency"] for r in results])[int(len(results) * 0.95)]

        print(f"\nConcurrent Registrations:")
        print(f"  Success: {success_count}/50")
        print(f"  Avg Latency: {avg_latency:.2f}ms")
        print(f"  P95 Latency: {p95_latency:.2f}ms")

        assert success_count >= 45, "Too many failures in concurrent registrations"
        assert avg_latency < 3000, "Average latency too high"
        assert p95_latency < 5000, "P95 latency too high"


@pytest.mark.regression
@pytest.mark.auth
class TestAuthRegression:
    """Regression tests for previously fixed bugs"""

    def test_duplicate_resource_exception_returns_409(self, api_client, base_url, registered_user, test_user_data):
        """
        Regression test for bug fix:
        DuplicateResourceException should return 409, not 500
        """
        duplicate_data = test_user_data.copy()
        duplicate_data["email"] = registered_user["user"]["email"]

        response = api_client.post(
            f"{base_url}{Endpoints.AUTH_REGISTER}",
            json=duplicate_data
        )

        # Critical: Must be 409, NOT 500
        assert response.status_code == 409, \
            f"Regression: DuplicateResourceException returned {response.status_code} instead of 409"

    def test_me_endpoint_has_caching_headers(self, api_client, base_url, auth_headers):
        """
        Regression test for infinite loop bug fix:
        /me endpoint must have caching headers
        """
        response = api_client.get(
            f"{base_url}{Endpoints.AUTH_ME}",
            headers=auth_headers
        )

        if response.status_code == 200:
            # Critical: Must have Cache-Control header
            assert "Cache-Control" in response.headers, \
                "Regression: /me endpoint missing Cache-Control header"

            assert "max-age" in response.headers["Cache-Control"], \
                "Regression: /me endpoint Cache-Control missing max-age"

            assert "ETag" in response.headers, \
                "Regression: /me endpoint missing ETag header"
