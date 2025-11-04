package com.evfleet.charging.application.handler;

import com.evfleet.charging.application.command.EndChargingSessionCommand;
import com.evfleet.charging.application.service.ChargingSessionSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndChargingSessionHandler {
    private final ChargingSessionSaga saga;

    public void handle(EndChargingSessionCommand command) {
        log.info("Handling EndChargingSessionCommand for session: {}", command.getSessionId());
        saga.executeEndSessionSaga(command);
    }
}
