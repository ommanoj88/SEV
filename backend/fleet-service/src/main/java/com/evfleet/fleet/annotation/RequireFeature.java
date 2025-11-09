package com.evfleet.fleet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require a specific feature to be available.
 * This annotation can be used on controller methods or service methods to enforce
 * feature availability checks before method execution.
 * 
 * Example usage:
 * <pre>
 * {@code
 * @RequireFeature("BATTERY_TRACKING")
 * public BatteryHealthResponse getBatteryHealth(Long vehicleId) {
 *     // This method will only execute if BATTERY_TRACKING is available
 *     // for the vehicle's fuel type
 * }
 * }
 * </pre>
 * 
 * The actual enforcement of this annotation would typically be done via:
 * - AOP (Aspect-Oriented Programming) interceptor
 * - Custom Spring Security expression
 * - Manual checks in service methods
 * 
 * @since 2.0.0 (Feature flag system)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireFeature {
    
    /**
     * The name of the required feature.
     * Should match one of the constants in FeatureToggle.Features
     * 
     * @return the feature name
     */
    String value();
    
    /**
     * Optional message to be included in the exception when the feature is not available
     * 
     * @return the error message
     */
    String message() default "This feature is not available for the vehicle's fuel type";
}
