package com.evfleet.charging.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveChargingSlotCommand {
    @NotBlank(message = "Station ID is required")
    private String stationId;

    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    private LocalDateTime reservationTime;
}
