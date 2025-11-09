package com.evfleet.fleet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * FuelConsumption Entity
 * Tracks fuel consumption metrics for ICE and Hybrid vehicles.
 * 
 * This entity stores historical fuel consumption data, enabling:
 * - Cost tracking and TCO calculations
 * - Fuel efficiency analysis
 * - Carbon footprint calculations for ICE vehicles
 * - Comparison between vehicle performance
 * 
 * @since 2.0.0 (Multi-fuel support)
 */
@Entity
@Table(name = "fuel_consumption", indexes = {
    @Index(name = "idx_fuel_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_fuel_timestamp", columnList = "timestamp"),
    @Index(name = "idx_fuel_trip_id", columnList = "trip_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the vehicle
     */
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /**
     * Optional trip reference if consumption is tied to a specific trip
     */
    @Column(name = "trip_id")
    private Long tripId;

    /**
     * Timestamp when the fuel consumption was recorded
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Amount of fuel consumed in liters
     */
    @Column(name = "fuel_consumed_liters", nullable = false)
    private Double fuelConsumedLiters;

    /**
     * Distance covered during this consumption period in kilometers
     */
    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    /**
     * Fuel efficiency: km per liter
     * Calculated as: distanceKm / fuelConsumedLiters
     */
    @Column(name = "fuel_efficiency_kmpl")
    private Double fuelEfficiencyKmpl;

    /**
     * Cost of fuel consumed in INR
     * Calculated as: fuelConsumedLiters * fuelPricePerLiter
     */
    @Column(name = "cost_inr")
    private Double costInr;

    /**
     * Fuel price per liter at the time of consumption (in INR)
     */
    @Column(name = "fuel_price_per_liter")
    private Double fuelPricePerLiter;

    /**
     * Estimated CO2 emissions in kg
     * Calculated using standard emission factors for fuel type
     */
    @Column(name = "co2_emissions_kg")
    private Double co2EmissionsKg;

    /**
     * Type of fuel used (Petrol, Diesel, CNG, etc.)
     */
    @Column(name = "fuel_type_detail", length = 50)
    private String fuelTypeDetail;

    /**
     * Optional location where refueling occurred
     */
    @Column(name = "refuel_location", length = 255)
    private String refuelLocation;

    /**
     * Latitude of refuel location
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude of refuel location
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Additional notes or remarks
     */
    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calculate fuel efficiency based on distance and fuel consumed
     */
    @PrePersist
    @PreUpdate
    public void calculateMetrics() {
        if (fuelConsumedLiters != null && fuelConsumedLiters > 0 && distanceKm != null) {
            this.fuelEfficiencyKmpl = distanceKm / fuelConsumedLiters;
        }
        
        if (fuelConsumedLiters != null && fuelPricePerLiter != null) {
            this.costInr = fuelConsumedLiters * fuelPricePerLiter;
        }

        // Standard CO2 emission factors (kg CO2 per liter)
        // Petrol: 2.31 kg CO2/liter, Diesel: 2.68 kg CO2/liter
        if (fuelConsumedLiters != null && fuelTypeDetail != null) {
            double emissionFactor = switch (fuelTypeDetail.toUpperCase()) {
                case "DIESEL" -> 2.68;
                case "PETROL", "GASOLINE" -> 2.31;
                case "CNG" -> 1.89;
                default -> 2.5; // Average
            };
            this.co2EmissionsKg = fuelConsumedLiters * emissionFactor;
        }
    }
}
