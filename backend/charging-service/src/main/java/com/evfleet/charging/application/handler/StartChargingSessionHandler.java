package com.evfleet.charging.application.handler;

import com.evfleet.charging.application.command.StartChargingSessionCommand;
import com.evfleet.charging.application.service.ChargingSessionSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartChargingSessionHandler {
    private final ChargingSessionSaga saga;

    public String handle(StartChargingSessionCommand command) {
        log.info("Handling StartChargingSessionCommand for vehicle: {}", command.getVehicleId());
        return saga.executeStartSessionSaga(command);
    }
}
