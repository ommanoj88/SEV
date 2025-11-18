package com.evfleet.common.constants;

/**
 * Application-wide constants
 *
 * Centralized location for all common constants used across modules.
 * Prevents magic strings and provides type-safety.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * API versioning and paths
     */
    public static final class Api {
        public static final String BASE_PATH = "/api";
        public static final String AUTH_PATH = BASE_PATH + "/auth";
        public static final String FLEET_PATH = BASE_PATH + "/fleet";
        public static final String CHARGING_PATH = BASE_PATH + "/charging";
        public static final String MAINTENANCE_PATH = BASE_PATH + "/maintenance";
        public static final String DRIVER_PATH = BASE_PATH + "/driver";
        public static final String ANALYTICS_PATH = BASE_PATH + "/analytics";
        public static final String NOTIFICATION_PATH = BASE_PATH + "/notification";
        public static final String BILLING_PATH = BASE_PATH + "/billing";

        private Api() {}
    }

    /**
     * Pagination defaults
     */
    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_PAGE_NUMBER = "0";
        public static final String DEFAULT_SORT_DIRECTION = "DESC";
        public static final String DEFAULT_SORT_BY = "createdAt";

        private Pagination() {}
    }

    /**
     * Cache names for Redis
     */
    public static final class Cache {
        public static final String USERS = "users";
        public static final String VEHICLES = "vehicles";
        public static final String TRIPS = "trips";
        public static final String CHARGING_STATIONS = "charging_stations";
        public static final String DRIVERS = "drivers";
        public static final String ANALYTICS = "analytics";

        // Cache TTLs in seconds
        public static final long DEFAULT_TTL = 600; // 10 minutes
        public static final long USER_TTL = 1800; // 30 minutes
        public static final long VEHICLE_TTL = 600; // 10 minutes
        public static final long ANALYTICS_TTL = 3600; // 1 hour

        private Cache() {}
    }

    /**
     * Date and time formats
     */
    public static final class DateTime {
        public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String ISO_DATE = "yyyy-MM-dd";
        public static final String DISPLAY_DATE_TIME = "dd MMM yyyy, hh:mm a";
        public static final String DISPLAY_DATE = "dd MMM yyyy";
        public static final String TIME_ZONE_IST = "Asia/Kolkata";

        private DateTime() {}
    }

    /**
     * Security and authentication
     */
    public static final class Security {
        public static final String FIREBASE_TOKEN_HEADER = "X-Firebase-Token";
        public static final String USER_ID_HEADER = "X-User-Id";
        public static final String COMPANY_ID_HEADER = "X-Company-Id";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";

        // Role names
        public static final String ROLE_ADMIN = "ADMIN";
        public static final String ROLE_FLEET_MANAGER = "FLEET_MANAGER";
        public static final String ROLE_DRIVER = "DRIVER";
        public static final String ROLE_VIEWER = "VIEWER";

        private Security() {}
    }

    /**
     * Validation patterns and limits
     */
    public static final class Validation {
        public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
        public static final String PHONE_PATTERN = "^[0-9]{10}$";
        public static final String VEHICLE_NUMBER_PATTERN = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$";

        public static final int NAME_MIN_LENGTH = 2;
        public static final int NAME_MAX_LENGTH = 100;
        public static final int EMAIL_MAX_LENGTH = 255;
        public static final int PHONE_MAX_LENGTH = 20;
        public static final int DESCRIPTION_MAX_LENGTH = 500;

        private Validation() {}
    }

    /**
     * Event types for inter-module communication
     */
    public static final class Events {
        // Auth events
        public static final String USER_REGISTERED = "user.registered";
        public static final String USER_LOGGED_IN = "user.logged.in";

        // Fleet events
        public static final String VEHICLE_CREATED = "vehicle.created";
        public static final String VEHICLE_LOCATION_UPDATED = "vehicle.location.updated";
        public static final String TRIP_STARTED = "trip.started";
        public static final String TRIP_COMPLETED = "trip.completed";

        // Charging events
        public static final String CHARGING_SESSION_STARTED = "charging.session.started";
        public static final String CHARGING_SESSION_COMPLETED = "charging.session.completed";
        public static final String BATTERY_LOW = "battery.low";

        // Maintenance events
        public static final String MAINTENANCE_DUE = "maintenance.due";
        public static final String BATTERY_HEALTH_WARNING = "battery.health.warning";

        private Events() {}
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String UNAUTHORIZED = "Unauthorized access";
        public static final String FORBIDDEN = "You don't have permission to access this resource";
        public static final String NOT_FOUND = "Resource not found";
        public static final String BAD_REQUEST = "Invalid request";
        public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
        public static final String VEHICLE_NOT_FOUND = "Vehicle not found";
        public static final String DRIVER_NOT_FOUND = "Driver not found";
        public static final String TRIP_NOT_FOUND = "Trip not found";

        private ErrorMessages() {}
    }

    /**
     * Success messages
     */
    public static final class SuccessMessages {
        public static final String CREATED = "Resource created successfully";
        public static final String UPDATED = "Resource updated successfully";
        public static final String DELETED = "Resource deleted successfully";
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String LOGOUT_SUCCESS = "Logout successful";
        public static final String REGISTRATION_SUCCESS = "Registration successful";

        private SuccessMessages() {}
    }
}
