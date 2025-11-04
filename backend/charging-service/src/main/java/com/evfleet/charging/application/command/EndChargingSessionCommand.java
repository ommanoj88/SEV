package com.evfleet.charging.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndChargingSessionCommand {
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    private BigDecimal energyConsumed;
}
