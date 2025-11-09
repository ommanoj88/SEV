package com.evfleet.fleet.exception;

/**
 * Exception thrown when a requested feature is not available for a vehicle's fuel type.
 * This exception is used in conjunction with the @RequireFeature annotation
 * to enforce feature availability checks.
 * 
 * @since 2.0.0 (Feature flag system)
 */
public class FeatureNotAvailableException extends RuntimeException {

    private final String featureName;
    private final String fuelType;

    /**
     * Constructor with feature name and fuel type
     * 
     * @param featureName the name of the unavailable feature
     * @param fuelType the fuel type of the vehicle
     */
    public FeatureNotAvailableException(String featureName, String fuelType) {
        super(String.format("Feature '%s' is not available for fuel type '%s'", featureName, fuelType));
        this.featureName = featureName;
        this.fuelType = fuelType;
    }

    /**
     * Constructor with custom message
     * 
     * @param message the custom error message
     */
    public FeatureNotAvailableException(String message) {
        super(message);
        this.featureName = null;
        this.fuelType = null;
    }

    /**
     * Constructor with feature name, fuel type, and custom message
     * 
     * @param featureName the name of the unavailable feature
     * @param fuelType the fuel type of the vehicle
     * @param message the custom error message
     */
    public FeatureNotAvailableException(String featureName, String fuelType, String message) {
        super(message);
        this.featureName = featureName;
        this.fuelType = fuelType;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFuelType() {
        return fuelType;
    }
}
