package com.evfleet.charging.service;

import com.evfleet.charging.dto.ChargingSessionRequest;
import com.evfleet.charging.dto.ChargingSessionResponse;
import com.evfleet.charging.entity.ChargingSession;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.event.EventPublisher;
import com.evfleet.charging.exception.ResourceNotFoundException;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.validation.VehicleTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingSessionServiceImpl implements ChargingSessionService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingStationService stationService;
    private final EventPublisher eventPublisher;
    private final VehicleTypeValidator vehicleTypeValidator;

    @Override
    @Transactional
    public ChargingSessionResponse startSession(ChargingSessionRequest request) {
        log.info("Starting charging session for vehicle: {}", request.getVehicleId());

        // PR-9: Validate that vehicle can charge (only EV/HYBRID)
        vehicleTypeValidator.validateVehicleCanCharge(request.getVehicleId());

        // Check for active session
        sessionRepository.findActiveSessionByVehicleId(request.getVehicleId())
            .ifPresent(session -> {
                throw new IllegalStateException("Vehicle already has an active charging session");
            });

        // Reserve slot at station
        ChargingStation station = stationService.reserveSlot(request.getStationId());

        ChargingSession session = new ChargingSession();
        session.setVehicleId(request.getVehicleId());
        session.setStationId(request.getStationId());
        session.setStartTime(LocalDateTime.now());
        session.setStartBatteryLevel(request.getStartBatteryLevel());
        session.setPaymentMethod(request.getPaymentMethod());
        session.setStatus(ChargingSession.SessionStatus.CHARGING);
        session.setTransactionId(UUID.randomUUID().toString());

        ChargingSession savedSession = sessionRepository.save(session);
        log.info("Charging session started with ID: {}", savedSession.getId());

        // Publish event
        eventPublisher.publishChargingSessionStarted(savedSession);

        return ChargingSessionResponse.from(savedSession);
    }

    @Override
    @Transactional
    public ChargingSessionResponse endSession(Long sessionId, BigDecimal endBatteryLevel) {
        log.info("Ending charging session: {}", sessionId);

        ChargingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with ID: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Session is not active");
        }

        // Get station details for cost calculation
        var stationResponse = stationService.getStationById(session.getStationId());

        // Calculate energy consumed (simplified calculation)
        BigDecimal batteryGain = endBatteryLevel.subtract(session.getStartBatteryLevel());
        // Assume average battery capacity of 60 kWh for calculation
        BigDecimal energyConsumed = batteryGain.multiply(new BigDecimal("0.6"))
            .setScale(2, RoundingMode.HALF_UP);

        // Calculate cost
        BigDecimal cost = energyConsumed.multiply(stationResponse.getPricePerKwh())
            .setScale(2, RoundingMode.HALF_UP);

        session.completeSession(endBatteryLevel, energyConsumed, cost);
        session.setPaymentStatus("COMPLETED");

        ChargingSession updatedSession = sessionRepository.save(session);

        // Release slot at station
        stationService.releaseSlot(session.getStationId());

        // Publish event
        eventPublisher.publishChargingSessionCompleted(updatedSession);

        log.info("Charging session completed: {}, Energy: {} kWh, Cost: ${}",
                 sessionId, energyConsumed, cost);

        return ChargingSessionResponse.from(updatedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingSessionResponse getSessionById(Long id) {
        log.info("Fetching charging session with ID: {}", id);

        ChargingSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with ID: " + id));

        return ChargingSessionResponse.from(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingSessionResponse> getSessionsByVehicle(Long vehicleId) {
        log.info("Fetching charging sessions for vehicle: {}", vehicleId);

        return sessionRepository.findByVehicleId(vehicleId).stream()
            .map(ChargingSessionResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingSessionResponse> getSessionsByStation(Long stationId) {
        log.info("Fetching charging sessions for station: {}", stationId);

        return sessionRepository.findByStationId(stationId).stream()
            .map(ChargingSessionResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChargingSessionResponse cancelSession(Long sessionId) {
        log.info("Cancelling charging session: {}", sessionId);

        ChargingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with ID: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Session is not active");
        }

        session.cancelSession();
        ChargingSession updatedSession = sessionRepository.save(session);

        // Release slot at station
        stationService.releaseSlot(session.getStationId());

        log.info("Charging session cancelled: {}", sessionId);

        return ChargingSessionResponse.from(updatedSession);
    }
}
