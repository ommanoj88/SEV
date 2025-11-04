package com.evfleet.charging.domain.model.valueobject;

import lombok.Value;
import java.io.Serializable;

/**
 * Location Value Object - Immutable representation of geographical coordinates
 * Part of Domain-Driven Design - Value Objects are immutable and defined by their attributes
 */
@Value
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    Double latitude;
    Double longitude;

    public Location(Double latitude, Double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private void validateLatitude(Double latitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
    }

    private void validateLongitude(Double longitude) {
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
    }

    /**
     * Calculate distance to another location using Haversine formula
     * @param other The other location
     * @return Distance in kilometers
     */
    public double distanceTo(Location other) {
        final int EARTH_RADIUS_KM = 6371;

        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    public boolean isWithinRadius(Location center, double radiusKm) {
        return this.distanceTo(center) <= radiusKm;
    }

    @Override
    public String toString() {
        return String.format("Location(%.6f, %.6f)", latitude, longitude);
    }
}
