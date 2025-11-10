package com.evfleet.fleet.model;

/**
 * TelemetryType Enum
 * Defines the types of telemetry metrics that can be collected from vehicles.
 * Different fuel types support different telemetry categories.
 * 
 * This enum helps categorize and filter telemetry data based on vehicle type:
 * - EV vehicles provide battery and motor metrics
 * - ICE vehicles provide engine and fuel metrics
 * - HYBRID vehicles provide both sets of metrics
 * 
 * @since 2.0.0 (Multi-fuel telemetry support)
 */
public enum TelemetryType {
    /**
     * Common telemetry metrics available for all vehicle types
     * Includes: GPS position, speed, heading, altitude, signal strength
     */
    COMMON,
    
    /**
     * Battery-specific telemetry metrics for EV and HYBRID vehicles
     * Includes: battery SoC, voltage, current, temperature
     */
    BATTERY,
    
    /**
     * Electric motor telemetry metrics for EV and HYBRID vehicles
     * Includes: motor temperature, controller temperature, power consumption, regenerative power
     */
    MOTOR,
    
    /**
     * Engine telemetry metrics for ICE and HYBRID vehicles
     * Includes: engine RPM, temperature, load, hours, error codes
     */
    ENGINE,
    
    /**
     * Fuel system telemetry metrics for ICE and HYBRID vehicles
     * Includes: fuel level, fuel consumption rate
     */
    FUEL,
    
    /**
     * Charging-specific telemetry metrics for EV and HYBRID vehicles
     * Includes: charging status, charging power
     */
    CHARGING
}
