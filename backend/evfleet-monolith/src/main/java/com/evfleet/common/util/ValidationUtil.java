package com.evfleet.common.util;

import java.util.regex.Pattern;

/**
 * Validation Utility Class
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10}$"
    );

    private static final Pattern VEHICLE_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$"
    );

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number (10 digits)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("[\\s-]", "")).matches();
    }

    /**
     * Validate Indian vehicle number (e.g., MH12AB1234)
     */
    public static boolean isValidVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return false;
        }
        return VEHICLE_NUMBER_PATTERN.matcher(vehicleNumber.toUpperCase()).matches();
    }

    /**
     * Validate battery SOC (0-100)
     */
    public static boolean isValidBatterySoc(Double soc) {
        if (soc == null) {
            return false;
        }
        return soc >= 0 && soc <= 100;
    }

    /**
     * Validate coordinates
     */
    public static boolean isValidLatitude(Double latitude) {
        if (latitude == null) {
            return false;
        }
        return latitude >= -90 && latitude <= 90;
    }

    public static boolean isValidLongitude(Double longitude) {
        if (longitude == null) {
            return false;
        }
        return longitude >= -180 && longitude <= 180;
    }

    /**
     * Validate positive number
     */
    public static boolean isPositive(Double value) {
        return value != null && value > 0;
    }

    /**
     * Validate non-negative number
     */
    public static boolean isNonNegative(Double value) {
        return value != null && value >= 0;
    }
}
