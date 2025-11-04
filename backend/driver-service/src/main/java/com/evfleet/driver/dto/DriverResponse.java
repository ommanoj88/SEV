package com.evfleet.driver.dto;

import com.evfleet.driver.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private String id;
    private String companyId;
    private String name;
    private String licenseNumber;
    private String phone;
    private String email;
    private DriverStatus status;
    private BigDecimal rating;
    private Integer totalTrips;
    private LocalDate joinedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
