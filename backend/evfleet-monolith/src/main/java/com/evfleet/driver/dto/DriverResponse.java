package com.evfleet.driver.dto;

import com.evfleet.driver.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Driver Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {

    private Long id;
    private Long companyId;
    private String name;
    private String phone;
    private String email;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private String status;
    private Long currentVehicleId;
    private Integer totalTrips;
    private Double totalDistance;
    private Boolean isLicenseExpired;
    
    // Performance metrics
    private Double safetyScore;
    private Double fuelEfficiency;
    private Integer harshBrakingEvents;
    private Integer speedingEvents;
    private Integer idlingTimeMinutes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DriverResponse fromEntity(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .companyId(driver.getCompanyId())
                .name(driver.getName())
                .phone(driver.getPhone())
                .email(driver.getEmail())
                .licenseNumber(driver.getLicenseNumber())
                .licenseExpiry(driver.getLicenseExpiry())
                .status(driver.getStatus().name())
                .currentVehicleId(driver.getCurrentVehicleId())
                .totalTrips(driver.getTotalTrips() != null ? driver.getTotalTrips() : 0)
                .totalDistance(driver.getTotalDistance() != null ? driver.getTotalDistance() : 0.0)
                .isLicenseExpired(driver.getLicenseExpiry().isBefore(LocalDate.now()))
                .safetyScore(driver.getSafetyScore() != null ? driver.getSafetyScore() : 0.0)
                .fuelEfficiency(driver.getFuelEfficiency() != null ? driver.getFuelEfficiency() : 0.0)
                .harshBrakingEvents(driver.getHarshBrakingEvents() != null ? driver.getHarshBrakingEvents() : 0)
                .speedingEvents(driver.getSpeedingEvents() != null ? driver.getSpeedingEvents() : 0)
                .idlingTimeMinutes(driver.getIdlingTimeMinutes() != null ? driver.getIdlingTimeMinutes() : 0)
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
