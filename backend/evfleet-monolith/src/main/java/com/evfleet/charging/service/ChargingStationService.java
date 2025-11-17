package com.evfleet.charging.service;

import com.evfleet.charging.model.ChargingStation;
import com.evfleet.charging.repository.ChargingStationRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Charging Station Service
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChargingStationService {

    private final ChargingStationRepository stationRepository;

    public ChargingStation createStation(ChargingStation station) {
        log.info("Creating charging station: {}", station.getName());
        station.setAvailableSlots(station.getTotalSlots());
        station.setStatus(ChargingStation.StationStatus.AVAILABLE);
        ChargingStation saved = stationRepository.save(station);
        log.info("Charging station created with ID: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ChargingStation> getAvailableStations() {
        return stationRepository.findAvailableStations();
    }

    @Transactional(readOnly = true)
    public List<ChargingStation> getNearbyStations(Double latitude, Double longitude, int limit) {
        return stationRepository.findNearbyStations(latitude, longitude, limit);
    }

    @Transactional(readOnly = true)
    public ChargingStation getStationById(Long id) {
        return stationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ChargingStation", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ChargingStation> getAllStations() {
        return stationRepository.findAll();
    }

    public ChargingStation updateStation(Long id, ChargingStation updates) {
        ChargingStation station = getStationById(id);

        if (updates.getName() != null) station.setName(updates.getName());
        if (updates.getAddress() != null) station.setAddress(updates.getAddress());
        if (updates.getTotalSlots() != null) station.setTotalSlots(updates.getTotalSlots());
        if (updates.getPricePerKwh() != null) station.setPricePerKwh(updates.getPricePerKwh());
        if (updates.getStatus() != null) station.setStatus(updates.getStatus());

        return stationRepository.save(station);
    }
}
