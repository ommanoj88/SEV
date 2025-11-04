package com.evfleet.driver.dto;

import com.evfleet.driver.enums.DriverStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {

    @NotBlank(message = "Company ID is required")
    private String companyId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private DriverStatus status;

    private BigDecimal rating;

    private Integer totalTrips;

    private LocalDate joinedDate;
}
