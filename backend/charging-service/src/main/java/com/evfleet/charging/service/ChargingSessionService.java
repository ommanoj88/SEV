package com.evfleet.charging.service;

import com.evfleet.charging.dto.ChargingSessionRequest;
import com.evfleet.charging.dto.ChargingSessionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ChargingSessionService {
    ChargingSessionResponse startSession(ChargingSessionRequest request);
    ChargingSessionResponse endSession(Long sessionId, BigDecimal endBatteryLevel);
    ChargingSessionResponse getSessionById(Long id);
    List<ChargingSessionResponse> getSessionsByVehicle(Long vehicleId);
    List<ChargingSessionResponse> getSessionsByStation(Long stationId);
    ChargingSessionResponse cancelSession(Long sessionId);
}
