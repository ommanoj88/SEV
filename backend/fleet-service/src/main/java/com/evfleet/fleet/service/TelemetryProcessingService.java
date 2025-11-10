package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.TelemetryData;
import com.evfleet.fleet.model.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for processing telemetry data based on vehicle fuel type.
 * Routes and processes EV-specific metrics vs ICE-specific metrics appropriately.
 * 
 * Processing Strategy:
 * - EV metrics: Battery-related fields (SOC, voltage, current, temperature, charging)
 * - ICE metrics: Engine-related fields (fuel level, RPM, temperature, load, hours)
 * - HYBRID metrics: Both EV and ICE fields are processed
 * 
 * @since 2.0.0 (Multi-fuel support - PR 6)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryProcessingService {

    /**
     * Processes telemetry data and maps it to TelemetryData entity based on vehicle fuel type.
     * 
     * @param request Telemetry request data
     * @param vehicle Vehicle for which telemetry is being processed
     * @return TelemetryData entity ready to be saved
     */
    public TelemetryData processTelemetryData(TelemetryRequest request, Vehicle vehicle) {
        FuelType fuelType = vehicle.getFuelType();
        
        // Default to EV if fuel type is null (backward compatibility)
        if (fuelType == null) {
            fuelType = FuelType.EV;
        }

        log.debug("Processing telemetry for vehicle {} with fuel type {}", vehicle.getId(), fuelType);

        TelemetryData telemetry = new TelemetryData();
        
        // Set common fields (applicable to all vehicle types)
        setCommonFields(telemetry, request);

        // Set fuel-type-specific fields
        switch (fuelType) {
            case EV:
                processEVMetrics(telemetry, request);
                break;
            case ICE:
                processICEMetrics(telemetry, request);
                break;
            case HYBRID:
                processHybridMetrics(telemetry, request);
                break;
        }

        return telemetry;
    }

    /**
     * Sets common telemetry fields that apply to all vehicle types.
     */
    private void setCommonFields(TelemetryData telemetry, TelemetryRequest request) {
        telemetry.setVehicleId(request.getVehicleId());
        telemetry.setLatitude(request.getLatitude());
        telemetry.setLongitude(request.getLongitude());
        telemetry.setSpeed(request.getSpeed());
        telemetry.setOdometer(request.getOdometer());
        telemetry.setTimestamp(request.getTimestamp());
        telemetry.setHeading(request.getHeading());
        telemetry.setAltitude(request.getAltitude());
        telemetry.setIsIgnitionOn(request.getIsIgnitionOn());
        telemetry.setTripId(request.getTripId());
        telemetry.setErrorCodes(request.getErrorCodes());
        telemetry.setSignalStrength(request.getSignalStrength());
    }

    /**
     * Processes EV-specific metrics (battery and motor data).
     */
    private void processEVMetrics(TelemetryData telemetry, TelemetryRequest request) {
        log.debug("Processing EV metrics for vehicle {}", request.getVehicleId());
        
        // Battery metrics
        telemetry.setBatterySoc(request.getBatterySoc());
        telemetry.setBatteryVoltage(request.getBatteryVoltage());
        telemetry.setBatteryCurrent(request.getBatteryCurrent());
        telemetry.setBatteryTemperature(request.getBatteryTemperature());
        
        // Power consumption metrics
        telemetry.setPowerConsumption(request.getPowerConsumption());
        telemetry.setRegenerativePower(request.getRegenerativePower());
        
        // Motor and controller metrics
        telemetry.setMotorTemperature(request.getMotorTemperature());
        telemetry.setControllerTemperature(request.getControllerTemperature());
        
        // Charging status
        telemetry.setIsCharging(request.getIsCharging());
        
        // ICE fields should be null for EV vehicles
        telemetry.setFuelLevel(null);
        telemetry.setEngineRpm(null);
        telemetry.setEngineTemperature(null);
        telemetry.setEngineLoad(null);
        telemetry.setEngineHours(null);
    }

    /**
     * Processes ICE-specific metrics (fuel and engine data).
     */
    private void processICEMetrics(TelemetryData telemetry, TelemetryRequest request) {
        log.debug("Processing ICE metrics for vehicle {}", request.getVehicleId());
        
        // Fuel metrics
        telemetry.setFuelLevel(request.getFuelLevel());
        
        // Engine metrics
        telemetry.setEngineRpm(request.getEngineRpm());
        telemetry.setEngineTemperature(request.getEngineTemperature());
        telemetry.setEngineLoad(request.getEngineLoad());
        telemetry.setEngineHours(request.getEngineHours());
        
        // Battery-related fields should be null for ICE vehicles
        telemetry.setBatterySoc(null);
        telemetry.setBatteryVoltage(null);
        telemetry.setBatteryCurrent(null);
        telemetry.setBatteryTemperature(null);
        telemetry.setPowerConsumption(null);
        telemetry.setRegenerativePower(null);
        telemetry.setMotorTemperature(null);
        telemetry.setControllerTemperature(null);
        telemetry.setIsCharging(null);
    }

    /**
     * Processes HYBRID metrics (both battery and engine data).
     */
    private void processHybridMetrics(TelemetryData telemetry, TelemetryRequest request) {
        log.debug("Processing HYBRID metrics for vehicle {}", request.getVehicleId());
        
        // Process both EV and ICE metrics for hybrid vehicles
        // Battery metrics
        telemetry.setBatterySoc(request.getBatterySoc());
        telemetry.setBatteryVoltage(request.getBatteryVoltage());
        telemetry.setBatteryCurrent(request.getBatteryCurrent());
        telemetry.setBatteryTemperature(request.getBatteryTemperature());
        telemetry.setPowerConsumption(request.getPowerConsumption());
        telemetry.setRegenerativePower(request.getRegenerativePower());
        telemetry.setMotorTemperature(request.getMotorTemperature());
        telemetry.setControllerTemperature(request.getControllerTemperature());
        telemetry.setIsCharging(request.getIsCharging());
        
        // Engine metrics
        telemetry.setFuelLevel(request.getFuelLevel());
        telemetry.setEngineRpm(request.getEngineRpm());
        telemetry.setEngineTemperature(request.getEngineTemperature());
        telemetry.setEngineLoad(request.getEngineLoad());
        telemetry.setEngineHours(request.getEngineHours());
    }
}
