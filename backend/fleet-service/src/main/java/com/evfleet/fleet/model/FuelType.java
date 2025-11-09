package com.evfleet.fleet.model;

/**
 * FuelType Enum
 * Defines the types of fuel/power sources supported by vehicles in the fleet.
 * 
 * This enum is central to the multi-fuel fleet management system:
 * - ICE: Internal Combustion Engine vehicles (traditional fuel-based)
 * - EV: Electric Vehicles (battery-powered)
 * - HYBRID: Vehicles with both electric and fuel capabilities
 * 
 * @since 2.0.0 (Multi-fuel support)
 */
public enum FuelType {
    /**
     * Internal Combustion Engine vehicles
     * Requires: fuelTankCapacity, fuelLevel
     * Features: Fuel consumption tracking, fuel station discovery
     */
    ICE,
    
    /**
     * Electric Vehicles
     * Requires: batteryCapacity, currentBatterySoc
     * Features: Charging management, battery health, energy consumption
     */
    EV,
    
    /**
     * Hybrid vehicles with both fuel and electric capabilities
     * Requires: Both batteryCapacity+currentBatterySoc AND fuelTankCapacity+fuelLevel
     * Features: All features from both ICE and EV
     */
    HYBRID
}
