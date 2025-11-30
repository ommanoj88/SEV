package com.evfleet.telematics.provider;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for FlespiTelematicsProvider
 * Tests API calls, retry logic, and data parsing
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FlespiTelematicsProviderTest {

    @Mock
    private RestTemplate restTemplate;

    private FlespiTelematicsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new FlespiTelematicsProvider();
        // Inject mock RestTemplate
        ReflectionTestUtils.setField(provider, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(provider, "flespiToken", "test-token-12345");
        ReflectionTestUtils.setField(provider, "flespiApiUrl", "https://flespi.io/gw/devices");
        ReflectionTestUtils.setField(provider, "timeoutSeconds", 30);
        ReflectionTestUtils.setField(provider, "maxRetries", 3);
    }

    @Test
    @DisplayName("getProviderId returns 'flespi'")
    void getProviderId_ReturnsFlespi() {
        assertThat(provider.getProviderId()).isEqualTo("flespi");
    }

    @Test
    @DisplayName("getProviderName returns human-readable name")
    void getProviderName_ReturnsReadableName() {
        assertThat(provider.getProviderName()).isEqualTo("flespi Telematics Gateway");
    }

    @Test
    @DisplayName("supports returns true for vehicle with IMEI and DEVICE source")
    void supports_ReturnsTrueForValidVehicle() {
        Vehicle vehicle = createTestVehicle();
        assertThat(provider.supports(vehicle)).isTrue();
    }

    @Test
    @DisplayName("supports returns false for vehicle without IMEI")
    void supports_ReturnsFalseWithoutImei() {
        Vehicle vehicle = createTestVehicle();
        vehicle.setTelematicsDeviceImei(null);
        assertThat(provider.supports(vehicle)).isFalse();
    }

    @Test
    @DisplayName("supports returns false for OEM source vehicle")
    void supports_ReturnsFalseForOemSource() {
        Vehicle vehicle = createTestVehicle();
        vehicle.setTelemetrySource(Vehicle.TelemetrySource.OEM_API);
        assertThat(provider.supports(vehicle)).isFalse();
    }

    @Test
    @DisplayName("fetchLatestData returns telemetry data on success")
    void fetchLatestData_ReturnsData_OnSuccess() {
        // Arrange
        Vehicle vehicle = createTestVehicle();
        Map<String, Object> message = createMockFlespiMessage();
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", List.of(message));
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Act
        Optional<VehicleTelemetryData> result = provider.fetchLatestData(vehicle);

        // Assert
        assertThat(result).isPresent();
        VehicleTelemetryData data = result.get();
        assertThat(data.getVehicleId()).isEqualTo(vehicle.getId());
        assertThat(data.getLatitude()).isEqualTo(18.5204);
        assertThat(data.getLongitude()).isEqualTo(73.8567);
        assertThat(data.getSpeed()).isEqualTo(45.5);
    }

    @Test
    @DisplayName("fetchLatestData returns empty when no messages")
    void fetchLatestData_ReturnsEmpty_WhenNoMessages() {
        // Arrange
        Vehicle vehicle = createTestVehicle();
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", List.of());
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Act
        Optional<VehicleTelemetryData> result = provider.fetchLatestData(vehicle);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fetchLatestData returns empty for unsupported vehicle")
    void fetchLatestData_ReturnsEmpty_ForUnsupportedVehicle() {
        // Arrange
        Vehicle vehicle = createTestVehicle();
        vehicle.setTelematicsDeviceImei(null); // Make unsupported

        // Act
        Optional<VehicleTelemetryData> result = provider.fetchLatestData(vehicle);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("testConnection returns true on successful API call")
    void testConnection_ReturnsTrue_OnSuccess() {
        // Arrange
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(String.class)
        )).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Act
        boolean result = provider.testConnection();

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("testConnection returns false on API failure")
    void testConnection_ReturnsFalse_OnFailure() {
        // Arrange
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(String.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act
        boolean result = provider.testConnection();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("fetchHistoricalData returns list of telemetry data")
    void fetchHistoricalData_ReturnsList() {
        // Arrange
        Vehicle vehicle = createTestVehicle();
        Map<String, Object> message1 = createMockFlespiMessage();
        Map<String, Object> message2 = createMockFlespiMessage();
        message2.put("timestamp", 1701350000L);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", List.of(message1, message2));
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Act
        List<VehicleTelemetryData> result = provider.fetchHistoricalData(
            vehicle, 
            LocalDateTime.now().minusHours(24), 
            LocalDateTime.now()
        );

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getHealthStatus returns valid status object")
    void getHealthStatus_ReturnsValidStatus() {
        // Act
        FlespiTelematicsProvider.FlespiHealthStatus status = provider.getHealthStatus();

        // Assert
        assertThat(status).isNotNull();
        assertThat(status.isTokenConfigured()).isTrue();
        assertThat(status.getApiUrl()).isEqualTo("https://flespi.io/gw/devices");
        assertThat(status.getTotalCalls()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("getUpdateIntervalSeconds returns 30 seconds")
    void getUpdateIntervalSeconds_Returns30() {
        assertThat(provider.getUpdateIntervalSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("getSupportedDataFields returns expected fields")
    void getSupportedDataFields_ReturnsExpectedFields() {
        List<String> fields = provider.getSupportedDataFields();
        
        assertThat(fields).contains(
            "latitude", "longitude", "speed", "altitude", "heading",
            "odometer", "ignition", "batteryVoltage", "satellites"
        );
    }

    // ============= Helper Methods =============

    private Vehicle createTestVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setRegistrationNumber("MH12AB1234");
        vehicle.setTelematicsDeviceImei("123456789012345");
        vehicle.setTelemetrySource(Vehicle.TelemetrySource.DEVICE);
        vehicle.setVehicleType(Vehicle.VehicleType.LCV);
        vehicle.setCompanyId(1L);
        return vehicle;
    }

    private Map<String, Object> createMockFlespiMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("timestamp", 1701349800L); // Unix timestamp
        message.put("position.latitude", 18.5204);
        message.put("position.longitude", 73.8567);
        message.put("position.altitude", 560.0);
        message.put("position.speed", 45.5);
        message.put("position.direction", 180.0);
        message.put("position.satellites", 12);
        message.put("can.vehicle.mileage", 15432.5);
        message.put("can.battery.soc", 85.0);
        message.put("can.vehicle.range", 250.0);
        message.put("can.battery.voltage", 400.0);
        message.put("din.1", true); // Ignition on
        message.put("gsm.signal.level", 80);
        return message;
    }
}
