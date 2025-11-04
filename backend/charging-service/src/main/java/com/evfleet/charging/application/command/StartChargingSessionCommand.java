package com.evfleet.charging.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartChargingSessionCommand {
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @NotBlank(message = "Station ID is required")
    private String stationId;

    private String userId;
}
