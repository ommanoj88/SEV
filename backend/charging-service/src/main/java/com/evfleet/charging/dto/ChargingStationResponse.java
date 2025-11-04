package com.evfleet.charging.dto;

import com.evfleet.charging.entity.ChargingStation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStationResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String provider;
    private Integer availableSlots;
    private Integer totalSlots;
    private BigDecimal chargingRate;
    private BigDecimal pricePerKwh;
    private String status;
    private String connectorType;
    private String amenities;
    private String operatingHours;
    private String contactPhone;
    private String contactEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChargingStationResponse from(ChargingStation station) {
        ChargingStationResponse response = new ChargingStationResponse();
        response.setId(station.getId());
        response.setName(station.getName());
        response.setAddress(station.getAddress());
        response.setLatitude(station.getLatitude());
        response.setLongitude(station.getLongitude());
        response.setProvider(station.getProvider());
        response.setAvailableSlots(station.getAvailableSlots());
        response.setTotalSlots(station.getTotalSlots());
        response.setChargingRate(station.getChargingRate());
        response.setPricePerKwh(station.getPricePerKwh());
        response.setStatus(station.getStatus().name());
        response.setConnectorType(station.getConnectorType());
        response.setAmenities(station.getAmenities());
        response.setOperatingHours(station.getOperatingHours());
        response.setContactPhone(station.getContactPhone());
        response.setContactEmail(station.getContactEmail());
        response.setCreatedAt(station.getCreatedAt());
        response.setUpdatedAt(station.getUpdatedAt());
        return response;
    }
}
