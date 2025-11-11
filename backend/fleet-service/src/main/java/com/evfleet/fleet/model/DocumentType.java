package com.evfleet.fleet.model;

public enum DocumentType {
    // Vehicle Documents
    VEHICLE_RC("Vehicle Registration Certificate"),
    VEHICLE_INSURANCE("Vehicle Insurance Policy"),
    VEHICLE_PERMIT("Vehicle Permit"),
    VEHICLE_FITNESS("Fitness Certificate"),
    VEHICLE_PUC("Pollution Under Control Certificate"),
    VEHICLE_TAX("Road Tax Receipt"),
    
    // Driver Documents
    DRIVER_LICENSE("Driving License"),
    DRIVER_BADGE("Driver Badge/ID"),
    DRIVER_AADHAR("Aadhar Card"),
    DRIVER_PAN("PAN Card"),
    DRIVER_PHOTO("Driver Photo"),
    
    // Other
    OTHER("Other Document");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isVehicleDocument() {
        return this.name().startsWith("VEHICLE_");
    }

    public boolean isDriverDocument() {
        return this.name().startsWith("DRIVER_");
    }
}
