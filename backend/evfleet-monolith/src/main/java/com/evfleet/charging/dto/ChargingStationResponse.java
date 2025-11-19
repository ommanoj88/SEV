package com.evfleet.charging.dto;

import com.evfleet.charging.model.ChargingStation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStationResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer totalSlots;
    private Integer availableSlots;
    private ChargingStation.StationStatus status;
    private String chargerType;
    private Double powerOutput;
    private BigDecimal pricePerKwh;
    private String operatorName;
    private String phone;

    public static ChargingStationResponse from(ChargingStation station) {
        return ChargingStationResponse.builder()
            .id(station.getId())
            .name(station.getName())
            .address(station.getAddress())
            .latitude(station.getLatitude())
            .longitude(station.getLongitude())
            .totalSlots(station.getTotalSlots())
            .availableSlots(station.getAvailableSlots())
            .status(station.getStatus())
            .chargerType(station.getChargerType())
            .powerOutput(station.getPowerOutput())
            .pricePerKwh(station.getPricePerKwh())
            .operatorName(station.getOperatorName())
            .phone(station.getPhone())
            .build();
    }
}
