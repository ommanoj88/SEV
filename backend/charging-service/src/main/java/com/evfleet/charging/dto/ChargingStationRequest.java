package com.evfleet.charging.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStationRequest {

    @NotBlank(message = "Station name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotNull(message = "Total slots is required")
    @Min(value = 1, message = "Total slots must be at least 1")
    private Integer totalSlots;

    @NotNull(message = "Charging rate is required")
    @DecimalMin(value = "0.1", message = "Charging rate must be positive")
    private BigDecimal chargingRate;

    @NotNull(message = "Price per kWh is required")
    @DecimalMin(value = "0.01", message = "Price per kWh must be positive")
    private BigDecimal pricePerKwh;

    private String connectorType;
    private String amenities;
    private String operatingHours;
    private String contactPhone;
    private String contactEmail;
    private String apiEndpoint;
    private String apiKey;
}
