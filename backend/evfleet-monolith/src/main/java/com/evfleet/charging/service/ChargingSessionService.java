package com.evfleet.charging.service;

import com.evfleet.charging.event.ChargingSessionCompletedEvent;
import com.evfleet.charging.event.ChargingSessionStartedEvent;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.model.ChargingStation;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.repository.ChargingStationRepository;
import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Charging Session Service
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChargingSessionService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingStationRepository stationRepository;
    private final EventPublisher eventPublisher;

    public ChargingSession startSession(Long vehicleId, Long stationId, Long companyId, Double initialSoc) {
        log.info("Starting charging session - Vehicle: {}, Station: {}", vehicleId, stationId);

        // Check if vehicle already has active session
        sessionRepository.findByVehicleIdAndStatus(vehicleId, ChargingSession.SessionStatus.ACTIVE)
            .ifPresent(s -> {
                throw new IllegalStateException("Vehicle already has an active charging session");
            });

        // Get and reserve station slot
        ChargingStation station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", stationId));

        if (!station.hasAvailableSlots()) {
            throw new IllegalStateException("No available slots at this station");
        }

        station.reserveSlot();
        stationRepository.save(station);

        // Create session
        ChargingSession session = ChargingSession.builder()
            .vehicleId(vehicleId)
            .stationId(stationId)
            .companyId(companyId)
            .startTime(LocalDateTime.now())
            .initialSoc(initialSoc)
            .status(ChargingSession.SessionStatus.ACTIVE)
            .build();

        ChargingSession saved = sessionRepository.save(session);

        // Publish event
        eventPublisher.publish(new ChargingSessionStartedEvent(this, saved.getId(), vehicleId, stationId));

        log.info("Charging session started with ID: {}", saved.getId());
        return saved;
    }

    public ChargingSession completeSession(Long sessionId, BigDecimal energyConsumed, Double finalSoc) {
        log.info("Completing charging session: {}", sessionId);

        ChargingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingSession", "id", sessionId));

        if (session.getStatus() != ChargingSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        // Get station and calculate cost
        ChargingStation station = stationRepository.findById(session.getStationId())
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", session.getStationId()));

        BigDecimal cost = energyConsumed.multiply(BigDecimal.valueOf(station.getPricePerKwh()));

        // Complete session
        session.complete(energyConsumed, cost, finalSoc);
        ChargingSession completed = sessionRepository.save(session);

        // Release station slot
        station.releaseSlot();
        stationRepository.save(station);

        // Publish event
        eventPublisher.publish(new ChargingSessionCompletedEvent(
            this, sessionId, session.getVehicleId(), energyConsumed, cost
        ));

        log.info("Charging session completed - Energy: {} kWh, Cost: ₹{}", energyConsumed, cost);
        return completed;
    }

    @Transactional(readOnly = true)
    public List<ChargingSession> getSessionsByVehicle(Long vehicleId) {
        return sessionRepository.findByVehicleId(vehicleId);
    }

    @Transactional(readOnly = true)
    public List<ChargingSession> getSessionsByCompany(Long companyId) {
        return sessionRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public ChargingSession getSessionById(Long id) {
        return sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingSession", "id", id));
    }
}
