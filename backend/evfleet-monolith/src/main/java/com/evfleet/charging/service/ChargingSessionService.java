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

        // Validate initialSoc
        if (initialSoc != null && (initialSoc < 0.0 || initialSoc > 100.0)) {
            throw new IllegalArgumentException("Initial SOC must be between 0 and 100");
        }

        // Check if vehicle already has active session
        sessionRepository.findByVehicleIdAndStatus(vehicleId, ChargingSession.SessionStatus.ACTIVE)
            .ifPresent(s -> {
                throw new IllegalStateException("Vehicle already has an active charging session");
            });

        // Get station (check before attempting to reserve)
        ChargingStation station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", stationId));

        if (!station.hasAvailableSlots()) {
            throw new IllegalStateException("No available slots at this station");
        }

        // Atomically reserve slot using database-level operation (thread-safe)
        int rowsUpdated = stationRepository.decrementAvailableSlots(stationId);
        if (rowsUpdated == 0) {
            // Slot was taken by another thread between check and reserve
            throw new IllegalStateException("No available slots at this station");
        }

        // Refresh station to get updated slot count
        stationRepository.flush();
        station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", stationId));

        // Update station status if full
        if (station.getAvailableSlots() == 0 && station.getStatus() != ChargingStation.StationStatus.FULL) {
            station.setStatus(ChargingStation.StationStatus.FULL);
            stationRepository.save(station);
        }

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

        // Validate inputs
        if (energyConsumed != null && energyConsumed.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Energy consumed must be zero or positive");
        }
        if (finalSoc != null && (finalSoc < 0.0 || finalSoc > 100.0)) {
            throw new IllegalArgumentException("Final SOC must be between 0 and 100");
        }

        ChargingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingSession", "id", sessionId));

        if (session.getStatus() != ChargingSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }

        // Get station and calculate cost
        ChargingStation station = stationRepository.findById(session.getStationId())
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", session.getStationId()));

        // Fix: Use BigDecimal throughout to avoid precision errors
        BigDecimal cost = BigDecimal.ZERO;
        if (energyConsumed != null && station.getPricePerKwh() != null) {
            cost = energyConsumed.multiply(station.getPricePerKwh());
        }

        // Complete session
        session.complete(energyConsumed, cost, finalSoc);
        ChargingSession completed = sessionRepository.save(session);

        // Atomically release station slot using database-level operation (thread-safe)
        int rowsUpdated = stationRepository.incrementAvailableSlots(session.getStationId());
        
        // Refresh station to get updated slot count
        if (rowsUpdated > 0) {
            stationRepository.flush();
            station = stationRepository.findById(session.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", session.getStationId()));
            
            // Update station status if it was full
            if (station.getStatus() == ChargingStation.StationStatus.FULL && station.getAvailableSlots() > 0) {
                station.setStatus(ChargingStation.StationStatus.AVAILABLE);
                stationRepository.save(station);
            }
        }

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
