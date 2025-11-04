package com.evfleet.charging.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSessionRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Station ID is required")
    private Long stationId;

    @DecimalMin(value = "0.0", message = "Battery level cannot be negative")
    @DecimalMax(value = "100.0", message = "Battery level cannot exceed 100")
    private BigDecimal startBatteryLevel;

    private String paymentMethod;
}
