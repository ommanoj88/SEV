package com.evfleet.driver.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverCommand {
    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    private String assignedBy;
}
