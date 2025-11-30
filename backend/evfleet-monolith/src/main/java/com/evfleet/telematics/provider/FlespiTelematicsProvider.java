package com.evfleet.telematics.provider;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Telemetry provider for flespi middleware
 * Supports Teltonika, Queclink, and other devices via flespi
 * 
 * Features:
 * - Automatic retry with exponential backoff
 * - Health check monitoring
 * - Configuration validation on startup
 * - Historical data fetching
 *
 * @author SEV Platform Team
 * @version 2.0.0
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "evfleet.telematics.flespi.enabled", havingValue = "true", matchIfMissing = false)
public class FlespiTelematicsProvider implements TelemetryProvider {

    @Value("${evfleet.telematics.flespi.token:}")
    private String flespiToken;

    @Value("${evfleet.telematics.flespi.api-url:https://flespi.io/gw/devices}")
    private String flespiApiUrl;

    @Value("${evfleet.telematics.flespi.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${evfleet.telematics.flespi.max-retries:3}")
    private int maxRetries;

    private final RestTemplate restTemplate;
    
    // Health monitoring
    private final AtomicBoolean isHealthy = new AtomicBoolean(false);
    private final AtomicLong lastSuccessfulCall = new AtomicLong(0);
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);

    public FlespiTelematicsProvider() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Validate configuration on startup
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating flespi telematics provider configuration...");
        
        if (flespiToken == null || flespiToken.trim().isEmpty()) {
            log.error("CRITICAL: flespi token is not configured! Set evfleet.telematics.flespi.token");
            throw new IllegalStateException("Flespi token is required when flespi provider is enabled");
        }
        
        if (flespiToken.length() < 10) {
            log.warn("Flespi token appears to be too short - verify it's correct");
        }
        
        if (flespiApiUrl == null || flespiApiUrl.trim().isEmpty()) {
            log.error("CRITICAL: flespi API URL is not configured!");
            throw new IllegalStateException("Flespi API URL is required");
        }
        
        // Test connection on startup
        boolean connected = testConnection();
        isHealthy.set(connected);
        
        if (connected) {
            log.info("✅ Flespi provider initialized successfully - API connection verified");
        } else {
            log.warn("⚠️ Flespi provider initialized but API connection failed - will retry on demand");
        }
    }

    @Override
    public String getProviderId() {
        return "flespi";
    }

    @Override
    public String getProviderName() {
        return "flespi Telematics Gateway";
    }

    @Override
    public Vehicle.TelemetrySource getSourceType() {
        return Vehicle.TelemetrySource.DEVICE;
    }

    @Override
    public boolean supports(Vehicle vehicle) {
        // Supports vehicles with device IMEI configured
        return vehicle.getTelematicsDeviceImei() != null &&
               !vehicle.getTelematicsDeviceImei().trim().isEmpty() &&
               vehicle.getTelemetrySource() == Vehicle.TelemetrySource.DEVICE;
    }

    /**
     * Fetch latest telemetry data with automatic retry and exponential backoff.
     * Retries up to 3 times with delays of 1s, 2s, 4s
     */
    @Override
    @Retryable(
        value = {ResourceAccessException.class, HttpServerErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Optional<VehicleTelemetryData> fetchLatestData(Vehicle vehicle) {
        if (!supports(vehicle)) {
            return Optional.empty();
        }

        totalCalls.incrementAndGet();
        
        try {
            // Call flespi REST API to get latest device message
            String url = String.format("%s/%s/messages?count=1", flespiApiUrl, vehicle.getTelematicsDeviceImei());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "FlespiToken " + flespiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("result");

                if (messages != null && !messages.isEmpty()) {
                    Map<String, Object> latestMessage = messages.get(0);
                    lastSuccessfulCall.set(System.currentTimeMillis());
                    isHealthy.set(true);
                    return Optional.of(parseFlespiMessage(vehicle, latestMessage));
                }
            }

            log.warn("No data received from flespi for device: {}", vehicle.getTelematicsDeviceImei());
            return Optional.empty();

        } catch (HttpClientErrorException e) {
            failedCalls.incrementAndGet();
            isHealthy.set(false);
            log.error("Client error fetching from flespi ({}): {} - device: {}", 
                e.getStatusCode(), e.getMessage(), vehicle.getTelematicsDeviceImei());
            return Optional.empty();
        } catch (Exception e) {
            failedCalls.incrementAndGet();
            isHealthy.set(false);
            log.error("Error fetching data from flespi for vehicle: {}", vehicle.getId(), e);
            throw e; // Re-throw for retry mechanism
        }
    }

    /**
     * Fetch historical telemetry data from flespi.
     * Uses Unix timestamps for flespi API time range queries.
     */
    @Override
    public List<VehicleTelemetryData> fetchHistoricalData(Vehicle vehicle, LocalDateTime start, LocalDateTime end) {
        if (!supports(vehicle)) {
            return new ArrayList<>();
        }
        
        try {
            // Convert LocalDateTime to Unix timestamps for flespi API
            long startTimestamp = start.toEpochSecond(ZoneOffset.UTC);
            long endTimestamp = end.toEpochSecond(ZoneOffset.UTC);
            
            String url = String.format("%s/%s/messages?begin=%d&end=%d&count=1000",
                flespiApiUrl, vehicle.getTelematicsDeviceImei(), startTimestamp, endTimestamp);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "FlespiToken " + flespiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            List<VehicleTelemetryData> result = new ArrayList<>();
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("result");

                if (messages != null) {
                    for (Map<String, Object> message : messages) {
                        result.add(parseFlespiMessage(vehicle, message));
                    }
                    log.info("Retrieved {} historical telemetry records for device {}", 
                        result.size(), vehicle.getTelematicsDeviceImei());
                }
            }
            
            return result;

        } catch (Exception e) {
            log.error("Error fetching historical data from flespi for vehicle: {}", vehicle.getId(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean testConnection() {
        try {
            String url = flespiApiUrl;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "FlespiToken " + flespiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            boolean connected = response.getStatusCode() == HttpStatus.OK;
            isHealthy.set(connected);
            if (connected) {
                lastSuccessfulCall.set(System.currentTimeMillis());
            }
            return connected;

        } catch (Exception e) {
            log.error("flespi connection test failed", e);
            isHealthy.set(false);
            return false;
        }
    }

    /**
     * Get health status of the flespi provider.
     * Returns detailed health information for monitoring.
     */
    public FlespiHealthStatus getHealthStatus() {
        return FlespiHealthStatus.builder()
            .healthy(isHealthy.get())
            .lastSuccessfulCallMs(lastSuccessfulCall.get())
            .totalCalls(totalCalls.get())
            .failedCalls(failedCalls.get())
            .successRate(totalCalls.get() > 0 
                ? (double)(totalCalls.get() - failedCalls.get()) / totalCalls.get() * 100 
                : 100.0)
            .apiUrl(flespiApiUrl)
            .tokenConfigured(flespiToken != null && !flespiToken.isEmpty())
            .build();
    }

    /**
     * Health status DTO for flespi provider
     */
    @lombok.Builder
    @lombok.Data
    public static class FlespiHealthStatus {
        private boolean healthy;
        private long lastSuccessfulCallMs;
        private long totalCalls;
        private long failedCalls;
        private double successRate;
        private String apiUrl;
        private boolean tokenConfigured;
    }

    @Override
    public int getUpdateIntervalSeconds() {
        return 30; // flespi devices typically report every 30 seconds
    }

    @Override
    public List<String> getSupportedDataFields() {
        return List.of(
            "latitude", "longitude", "speed", "altitude", "heading",
            "odometer", "ignition", "batteryVoltage", "satellites",
            "gsmSignal", "canData"
        );
    }

    /**
     * Parse flespi JSON message into normalized telemetry data
     */
    private VehicleTelemetryData parseFlespiMessage(Vehicle vehicle, Map<String, Object> message) {
        VehicleTelemetryData.VehicleTelemetryDataBuilder builder = VehicleTelemetryData.builder()
            .vehicleId(vehicle.getId())
            .deviceId(vehicle.getTelematicsDeviceImei())
            .source(Vehicle.TelemetrySource.DEVICE)
            .providerName(getProviderName());

        // Timestamp (flespi uses Unix timestamp)
        if (message.containsKey("timestamp")) {
            long timestamp = ((Number) message.get("timestamp")).longValue();
            builder.timestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault()));
        } else {
            builder.timestamp(LocalDateTime.now());
        }

        // GPS coordinates
        if (message.containsKey("position.latitude")) {
            builder.latitude((Double) message.get("position.latitude"));
        }
        if (message.containsKey("position.longitude")) {
            builder.longitude((Double) message.get("position.longitude"));
        }
        if (message.containsKey("position.altitude")) {
            builder.altitude((Double) message.get("position.altitude"));
        }

        // Speed and heading
        if (message.containsKey("position.speed")) {
            builder.speed((Double) message.get("position.speed"));
        }
        if (message.containsKey("position.direction")) {
            builder.heading((Double) message.get("position.direction"));
        }

        // Satellites
        if (message.containsKey("position.satellites")) {
            builder.satellites((Integer) message.get("position.satellites"));
        }

        // Odometer (from GPS or CAN)
        if (message.containsKey("can.vehicle.mileage")) {
            builder.odometer((Double) message.get("can.vehicle.mileage"));
        } else if (message.containsKey("position.mileage")) {
            builder.odometer((Double) message.get("position.mileage"));
        }

        // ===== BATTERY DATA - ONLY FOR 4-WHEELERS (LCV) =====
        // 2-wheelers and 3-wheelers use GPS-only tracking as per 2WHEELER_GPS_ONLY_STRATEGY.md
        // They don't have OBD-II ports or accessible CAN bus for battery data
        if (vehicle.getType() == Vehicle.VehicleType.LCV) {
            // Battery data (from CAN bus for EVs - 4-wheelers only)
            if (message.containsKey("can.battery.soc")) {
                builder.batterySoc((Double) message.get("can.battery.soc"));
            }
            if (message.containsKey("can.vehicle.range")) {
                builder.estimatedRange((Double) message.get("can.vehicle.range"));
            }
            if (message.containsKey("can.battery.voltage")) {
                builder.batteryVoltage((Double) message.get("can.battery.voltage"));
            }
            if (message.containsKey("can.battery.temperature")) {
                builder.batteryTemperature((Double) message.get("can.battery.temperature"));
            }
            if (message.containsKey("can.battery.charging.status")) {
                Boolean charging = (Boolean) message.get("can.battery.charging.status");
                builder.isCharging(charging);
                builder.chargingStatus(charging ? "AC_CHARGING" : "NOT_CHARGING");
            }
        } else {
            // For 2-wheelers and 3-wheelers, mark as GPS-only tracking
            builder.notes("GPS-only tracking - Battery data not available for " + vehicle.getType());
        }

        // Ignition status
        if (message.containsKey("din.1")) { // Digital input 1 typically = ignition
            builder.ignitionOn((Boolean) message.get("din.1"));
        }

        // Movement detection
        if (message.containsKey("position.speed")) {
            Double speed = (Double) message.get("position.speed");
            builder.isMoving(speed != null && speed > 1.0); // Moving if speed > 1 km/h
        }

        // GSM signal strength
        if (message.containsKey("gsm.signal.level")) {
            builder.signalStrength((Integer) message.get("gsm.signal.level"));
        }

        // Data quality assessment
        Vehicle.TelemetryDataQuality quality = Vehicle.TelemetryDataQuality.REAL_TIME;
        if (builder.build().getTimestamp().isBefore(LocalDateTime.now().minusMinutes(5))) {
            quality = Vehicle.TelemetryDataQuality.RECENT;
        }
        if (builder.build().getTimestamp().isBefore(LocalDateTime.now().minusMinutes(30))) {
            quality = Vehicle.TelemetryDataQuality.STALE;
        }
        builder.dataQuality(quality);

        // Check if battery data is estimated (no CAN data available)
        boolean hasCanData = message.containsKey("can.battery.soc");
        builder.isEstimated(!hasCanData);

        if (!hasCanData && message.containsKey("battery.voltage")) {
            // Estimate battery SOC from device battery voltage (basic estimation)
            builder.notes("Battery SOC estimated from voltage - no CAN data available");
        }

        return builder.build();
    }
}
