package com.evfleet.charging.presentation.rest;

import com.evfleet.charging.application.command.EndChargingSessionCommand;
import com.evfleet.charging.application.command.StartChargingSessionCommand;
import com.evfleet.charging.application.handler.EndChargingSessionHandler;
import com.evfleet.charging.application.handler.StartChargingSessionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/charging")
@RequiredArgsConstructor
@Tag(name = "Charging Management", description = "Charging session and station management APIs")
public class ChargingController {

    private final StartChargingSessionHandler startHandler;
    private final EndChargingSessionHandler endHandler;

    @PostMapping("/sessions/start")
    @Operation(summary = "Start a charging session")
    public ResponseEntity<Map<String, String>> startSession(@Valid @RequestBody StartChargingSessionCommand command) {
        log.info("Starting charging session for vehicle: {}", command.getVehicleId());
        String sessionId = startHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("sessionId", sessionId, "message", "Charging session started successfully"));
    }

    @PostMapping("/sessions/{id}/end")
    @Operation(summary = "End a charging session")
    public ResponseEntity<Map<String, String>> endSession(
            @PathVariable String id,
            @Valid @RequestBody EndChargingSessionCommand command) {
        log.info("Ending charging session: {}", id);
        command.setSessionId(id);
        endHandler.handle(command);
        return ResponseEntity.ok(Map.of("message", "Charging session ended successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "charging-service"));
    }
}
