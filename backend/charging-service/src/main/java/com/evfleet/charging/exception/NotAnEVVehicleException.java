package com.evfleet.charging.exception;

/**
 * Exception thrown when an ICE vehicle attempts to charge
 * Only EV and HYBRID vehicles can use charging stations
 * 
 * @since PR-9 (Charging Validation)
 */
public class NotAnEVVehicleException extends RuntimeException {
    
    private final Long vehicleId;
    private final String fuelType;
    
    public NotAnEVVehicleException(Long vehicleId, String fuelType) {
        super(String.format("Vehicle ID %d with fuel type %s does not support charging. Only EV and HYBRID vehicles can charge.", 
                vehicleId, fuelType));
        this.vehicleId = vehicleId;
        this.fuelType = fuelType;
    }
    
    public Long getVehicleId() {
        return vehicleId;
    }
    
    public String getFuelType() {
        return fuelType;
    }
}
