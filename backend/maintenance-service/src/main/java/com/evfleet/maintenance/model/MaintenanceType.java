package com.evfleet.maintenance.model;

/**
 * MaintenanceType Enum
 * Defines all types of maintenance services supported by the fleet management system.
 * 
 * This enum covers maintenance for all fuel types:
 * - ICE: Internal Combustion Engine specific maintenance
 * - EV: Electric Vehicle specific maintenance
 * - HYBRID: Applicable to both ICE and EV maintenance
 * - COMMON: Universal maintenance applicable to all vehicle types
 * 
 * @since 2.0.0 (Multi-fuel support - PR 11)
 */
public enum MaintenanceType {
    
    // ========== ICE-SPECIFIC MAINTENANCE ==========
    /**
     * Engine oil change service
     * Applicable to: ICE, HYBRID
     * Frequency: Every 5,000-10,000 km or 6 months
     */
    OIL_CHANGE("Oil Change", "ICE", 5000, 6),
    
    /**
     * Engine oil filter replacement
     * Applicable to: ICE, HYBRID
     * Frequency: Every 5,000-10,000 km or 6 months
     */
    OIL_FILTER("Oil Filter Replacement", "ICE", 5000, 6),
    
    /**
     * Engine air filter replacement
     * Applicable to: ICE, HYBRID
     * Frequency: Every 15,000-30,000 km or 12 months
     */
    AIR_FILTER("Air Filter Replacement", "ICE", 15000, 12),
    
    /**
     * Fuel filter replacement
     * Applicable to: ICE, HYBRID
     * Frequency: Every 20,000-40,000 km or 24 months
     */
    FUEL_FILTER("Fuel Filter Replacement", "ICE", 20000, 24),
    
    /**
     * Transmission fluid change
     * Applicable to: ICE, HYBRID
     * Frequency: Every 40,000-60,000 km or 36 months
     */
    TRANSMISSION_FLUID("Transmission Fluid Change", "ICE", 40000, 36),
    
    /**
     * Coolant/antifreeze flush
     * Applicable to: ICE, HYBRID
     * Frequency: Every 40,000-60,000 km or 24 months
     */
    COOLANT_FLUSH("Coolant Flush", "ICE", 40000, 24),
    
    /**
     * Spark plug replacement (for petrol engines)
     * Applicable to: ICE (Petrol), HYBRID
     * Frequency: Every 30,000-50,000 km or 36 months
     */
    SPARK_PLUGS("Spark Plug Replacement", "ICE", 30000, 36),
    
    /**
     * Timing belt replacement
     * Applicable to: ICE, HYBRID
     * Frequency: Every 60,000-100,000 km or 60 months
     */
    TIMING_BELT("Timing Belt Replacement", "ICE", 60000, 60),
    
    /**
     * Engine tune-up
     * Applicable to: ICE, HYBRID
     * Frequency: Every 20,000-30,000 km or 24 months
     */
    ENGINE_TUNE_UP("Engine Tune-Up", "ICE", 20000, 24),
    
    /**
     * Exhaust system inspection and repair
     * Applicable to: ICE, HYBRID
     * Frequency: Every 15,000-20,000 km or 12 months
     */
    EXHAUST_SYSTEM("Exhaust System Service", "ICE", 15000, 12),
    
    /**
     * Fuel injector cleaning
     * Applicable to: ICE, HYBRID
     * Frequency: Every 25,000-30,000 km or 24 months
     */
    FUEL_INJECTOR_CLEANING("Fuel Injector Cleaning", "ICE", 25000, 24),
    
    // ========== EV-SPECIFIC MAINTENANCE ==========
    /**
     * Battery health check and diagnostics
     * Applicable to: EV, HYBRID
     * Frequency: Every 10,000 km or 6 months
     */
    BATTERY_CHECK("Battery Health Check", "EV", 10000, 6),
    
    /**
     * Battery pack inspection and maintenance
     * Applicable to: EV, HYBRID
     * Frequency: Every 20,000 km or 12 months
     */
    BATTERY_MAINTENANCE("Battery Pack Maintenance", "EV", 20000, 12),
    
    /**
     * Battery cooling system check
     * Applicable to: EV, HYBRID
     * Frequency: Every 15,000 km or 12 months
     */
    BATTERY_COOLING("Battery Cooling System Check", "EV", 15000, 12),
    
    /**
     * Electric motor inspection
     * Applicable to: EV, HYBRID
     * Frequency: Every 20,000 km or 12 months
     */
    MOTOR_INSPECTION("Electric Motor Inspection", "EV", 20000, 12),
    
    /**
     * High voltage system check
     * Applicable to: EV, HYBRID
     * Frequency: Every 15,000 km or 12 months
     */
    HIGH_VOLTAGE_CHECK("High Voltage System Check", "EV", 15000, 12),
    
    /**
     * Charging port and cable inspection
     * Applicable to: EV, HYBRID
     * Frequency: Every 10,000 km or 6 months
     */
    CHARGING_PORT_CHECK("Charging Port Inspection", "EV", 10000, 6),
    
    /**
     * Power electronics cooling system
     * Applicable to: EV, HYBRID
     * Frequency: Every 20,000 km or 12 months
     */
    POWER_ELECTRONICS_COOLING("Power Electronics Cooling", "EV", 20000, 12),
    
    // ========== COMMON MAINTENANCE (ALL VEHICLES) ==========
    /**
     * Tire rotation and balancing
     * Applicable to: ALL
     * Frequency: Every 8,000-10,000 km or 6 months
     */
    TIRE_ROTATION("Tire Rotation", "COMMON", 8000, 6),
    
    /**
     * Tire replacement
     * Applicable to: ALL
     * Frequency: Every 40,000-50,000 km or as needed
     */
    TIRE_REPLACEMENT("Tire Replacement", "COMMON", 40000, 36),
    
    /**
     * Brake pad inspection and replacement
     * Applicable to: ALL
     * Frequency: Every 15,000-20,000 km or 12 months
     */
    BRAKE_PADS("Brake Pad Service", "COMMON", 15000, 12),
    
    /**
     * Brake fluid replacement
     * Applicable to: ALL
     * Frequency: Every 20,000-30,000 km or 24 months
     */
    BRAKE_FLUID("Brake Fluid Replacement", "COMMON", 20000, 24),
    
    /**
     * Brake system inspection
     * Applicable to: ALL
     * Frequency: Every 10,000 km or 6 months
     */
    BRAKE_INSPECTION("Brake System Inspection", "COMMON", 10000, 6),
    
    /**
     * Suspension system check
     * Applicable to: ALL
     * Frequency: Every 15,000-20,000 km or 12 months
     */
    SUSPENSION_CHECK("Suspension System Check", "COMMON", 15000, 12),
    
    /**
     * Wheel alignment
     * Applicable to: ALL
     * Frequency: Every 15,000-20,000 km or 12 months
     */
    WHEEL_ALIGNMENT("Wheel Alignment", "COMMON", 15000, 12),
    
    /**
     * Steering system inspection
     * Applicable to: ALL
     * Frequency: Every 15,000 km or 12 months
     */
    STEERING_CHECK("Steering System Check", "COMMON", 15000, 12),
    
    /**
     * HVAC system service
     * Applicable to: ALL
     * Frequency: Every 15,000 km or 12 months
     */
    HVAC_SERVICE("HVAC System Service", "COMMON", 15000, 12),
    
    /**
     * Cabin air filter replacement
     * Applicable to: ALL
     * Frequency: Every 15,000 km or 12 months
     */
    CABIN_FILTER("Cabin Air Filter Replacement", "COMMON", 15000, 12),
    
    /**
     * Windshield wiper replacement
     * Applicable to: ALL
     * Frequency: Every 6-12 months or as needed
     */
    WIPER_REPLACEMENT("Wiper Replacement", "COMMON", 0, 6),
    
    /**
     * Lighting system check
     * Applicable to: ALL
     * Frequency: Every 10,000 km or 6 months
     */
    LIGHTING_CHECK("Lighting System Check", "COMMON", 10000, 6),
    
    /**
     * General safety inspection
     * Applicable to: ALL
     * Frequency: Every 10,000 km or 6 months
     */
    SAFETY_INSPECTION("General Safety Inspection", "COMMON", 10000, 6),
    
    /**
     * Software update
     * Applicable to: ALL (especially EV)
     * Frequency: As available/needed
     */
    SOFTWARE_UPDATE("Software Update", "COMMON", 0, 12);
    
    private final String displayName;
    private final String category; // ICE, EV, or COMMON
    private final int defaultMileageInterval; // in kilometers
    private final int defaultTimeInterval; // in months
    
    MaintenanceType(String displayName, String category, int defaultMileageInterval, int defaultTimeInterval) {
        this.displayName = displayName;
        this.category = category;
        this.defaultMileageInterval = defaultMileageInterval;
        this.defaultTimeInterval = defaultTimeInterval;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public int getDefaultMileageInterval() {
        return defaultMileageInterval;
    }
    
    public int getDefaultTimeInterval() {
        return defaultTimeInterval;
    }
    
    /**
     * Check if this maintenance type is applicable to a given fuel type
     * 
     * @param fuelType The fuel type (ICE, EV, HYBRID)
     * @return true if applicable, false otherwise
     */
    public boolean isApplicableToFuelType(String fuelType) {
        if (fuelType == null) {
            return false;
        }
        
        switch (this.category) {
            case "COMMON":
                return true; // Common maintenance applies to all
            case "ICE":
                return "ICE".equals(fuelType) || "HYBRID".equals(fuelType);
            case "EV":
                return "EV".equals(fuelType) || "HYBRID".equals(fuelType);
            default:
                return false;
        }
    }
}
