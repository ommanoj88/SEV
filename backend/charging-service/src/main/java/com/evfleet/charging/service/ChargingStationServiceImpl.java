package com.evfleet.charging.service;

import com.evfleet.charging.dto.ChargingStationRequest;
import com.evfleet.charging.dto.ChargingStationResponse;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.exception.ResourceNotFoundException;
import com.evfleet.charging.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingStationServiceImpl implements ChargingStationService {

    private final ChargingStationRepository stationRepository;

    @Override
    @Transactional
    public ChargingStationResponse createStation(ChargingStationRequest request) {
        log.info("Creating new charging station: {}", request.getName());

        ChargingStation station = new ChargingStation();
        station.setName(request.getName());
        station.setAddress(request.getAddress());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setProvider(request.getProvider());
        station.setAvailableSlots(request.getTotalSlots());
        station.setTotalSlots(request.getTotalSlots());
        station.setChargingRate(request.getChargingRate());
        station.setPricePerKwh(request.getPricePerKwh());
        station.setConnectorType(request.getConnectorType());
        station.setAmenities(request.getAmenities());
        station.setOperatingHours(request.getOperatingHours());
        station.setContactPhone(request.getContactPhone());
        station.setContactEmail(request.getContactEmail());
        station.setApiEndpoint(request.getApiEndpoint());
        station.setApiKey(request.getApiKey());
        station.setStatus(ChargingStation.StationStatus.ACTIVE);

        ChargingStation savedStation = stationRepository.save(station);
        log.info("Charging station created successfully with ID: {}", savedStation.getId());

        return ChargingStationResponse.from(savedStation);
    }

    @Override
    @Transactional
    public ChargingStationResponse updateStation(Long id, ChargingStationRequest request) {
        log.info("Updating charging station with ID: {}", id);

        ChargingStation station = stationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + id));

        station.setName(request.getName());
        station.setAddress(request.getAddress());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setProvider(request.getProvider());
        station.setTotalSlots(request.getTotalSlots());
        station.setChargingRate(request.getChargingRate());
        station.setPricePerKwh(request.getPricePerKwh());
        station.setConnectorType(request.getConnectorType());
        station.setAmenities(request.getAmenities());
        station.setOperatingHours(request.getOperatingHours());
        station.setContactPhone(request.getContactPhone());
        station.setContactEmail(request.getContactEmail());

        ChargingStation updatedStation = stationRepository.save(station);
        log.info("Charging station updated successfully: {}", id);

        return ChargingStationResponse.from(updatedStation);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargingStationResponse getStationById(Long id) {
        log.info("Fetching charging station with ID: {}", id);

        ChargingStation station = stationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + id));

        return ChargingStationResponse.from(station);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationResponse> getAllStations() {
        log.info("Fetching all charging stations");

        return stationRepository.findAll().stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationResponse> getAvailableStations() {
        log.info("Fetching available charging stations");

        return stationRepository.findAvailableStations().stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationResponse> getNearestStations(Double latitude, Double longitude, Integer limit) {
        log.info("Finding nearest stations to coordinates: {}, {}", latitude, longitude);

        return stationRepository.findNearestStations(latitude, longitude, limit).stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargingStationResponse> getStationsByProvider(String provider) {
        log.info("Fetching stations for provider: {}", provider);

        return stationRepository.findByProvider(provider).stream()
            .map(ChargingStationResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStation(Long id) {
        log.info("Deleting charging station with ID: {}", id);

        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Charging station not found with ID: " + id);
        }

        stationRepository.deleteById(id);
        log.info("Charging station deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public ChargingStation reserveSlot(Long stationId) {
        log.info("Reserving slot at station: {}", stationId);

        ChargingStation station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + stationId));

        if (!station.hasAvailableSlots()) {
            throw new IllegalStateException("No available slots at station: " + stationId);
        }

        station.reserveSlot();
        return stationRepository.save(station);
    }

    @Override
    @Transactional
    public void releaseSlot(Long stationId) {
        log.info("Releasing slot at station: {}", stationId);

        ChargingStation station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + stationId));

        station.releaseSlot();
        stationRepository.save(station);
    }
}
