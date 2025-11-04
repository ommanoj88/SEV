package com.evfleet.charging.service;

import com.evfleet.charging.dto.ChargingStationRequest;
import com.evfleet.charging.dto.ChargingStationResponse;
import com.evfleet.charging.entity.ChargingStation;

import java.util.List;

public interface ChargingStationService {
    ChargingStationResponse createStation(ChargingStationRequest request);
    ChargingStationResponse updateStation(Long id, ChargingStationRequest request);
    ChargingStationResponse getStationById(Long id);
    List<ChargingStationResponse> getAllStations();
    List<ChargingStationResponse> getAvailableStations();
    List<ChargingStationResponse> getNearestStations(Double latitude, Double longitude, Integer limit);
    List<ChargingStationResponse> getStationsByProvider(String provider);
    void deleteStation(Long id);
    ChargingStation reserveSlot(Long stationId);
    void releaseSlot(Long stationId);
}
