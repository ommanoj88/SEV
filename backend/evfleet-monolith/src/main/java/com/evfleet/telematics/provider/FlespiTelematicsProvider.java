package com.evfleet.telematics.provider;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Telemetry provider for flespi middleware
 * Supports Teltonika, Queclink, and other devices via flespi
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "evfleet.telematics.flespi.enabled", havingValue = "true", matchIfMissing = false)
public class FlespiTelematicsProvider implements TelemetryProvider {

    @Value("${evfleet.telematics.flespi.token:}")
    private String flespiToken;

    @Value("${evfleet.telematics.flespi.api-url:https://flespi.io/gw/devices}")
    private String flespiApiUrl;

    private final RestTemplate restTemplate;

    public FlespiTelematicsProvider() {
        this.restTemplate = new RestTemplate();
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

    @Override
    public Optional<VehicleTelemetryData> fetchLatestData(Vehicle vehicle) {
        if (!supports(vehicle)) {
            return Optional.empty();
        }

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
                    return Optional.of(parseFlespiMessage(vehicle, latestMessage));
                }
            }

            log.warn("No data received from flespi for device: {}", vehicle.getTelematicsDeviceImei());
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching data from flespi for vehicle: {}", vehicle.getId(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<VehicleTelemetryData> fetchHistoricalData(Vehicle vehicle, LocalDateTime start, LocalDateTime end) {
        // TODO: Implement historical data fetching from flespi
        // flespi supports time range queries: /devices/{id}/messages?begin={timestamp}&end={timestamp}
        log.warn("Historical data fetching not yet implemented for flespi provider");
        return new ArrayList<>();
    }

    @Override
    public boolean testConnection() {
        try {
            String url = flespiApiUrl;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "FlespiToken " + flespiToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.error("flespi connection test failed", e);
            return false;
        }
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

        // Battery data (from CAN bus for EVs)
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
