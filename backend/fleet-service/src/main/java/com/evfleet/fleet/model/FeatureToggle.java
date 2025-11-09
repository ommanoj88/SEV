package com.evfleet.fleet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * FeatureToggle Entity
 * Manages which features are available for different vehicle fuel types.
 * This enables conditional feature availability based on vehicle capabilities.
 * 
 * Examples:
 * - BATTERY_TRACKING is only available for EV and HYBRID
 * - FUEL_CONSUMPTION is only available for ICE and HYBRID
 * - CHARGING_MANAGEMENT is only available for EV and HYBRID
 * 
 * @since 2.0.0 (Feature flag system)
 */
@Entity
@Table(name = "feature_toggles", indexes = {
    @Index(name = "idx_feature_name", columnList = "feature_name", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureToggle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique name of the feature (e.g., BATTERY_TRACKING, FUEL_CONSUMPTION)
     */
    @Column(name = "feature_name", nullable = false, unique = true, length = 100)
    private String featureName;

    /**
     * Human-readable description of the feature
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Whether this feature is globally enabled
     * If false, the feature is disabled for all vehicle types
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * Set of fuel types that support this feature
     * Stored as comma-separated values in the database (e.g., "EV,HYBRID")
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "feature_toggle_fuel_types",
        joinColumns = @JoinColumn(name = "feature_toggle_id")
    )
    @Column(name = "fuel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<FuelType> supportedFuelTypes = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Predefined feature names for the system
     */
    public static class Features {
        public static final String BATTERY_TRACKING = "BATTERY_TRACKING";
        public static final String CHARGING_MANAGEMENT = "CHARGING_MANAGEMENT";
        public static final String FUEL_CONSUMPTION = "FUEL_CONSUMPTION";
        public static final String ENGINE_DIAGNOSTICS = "ENGINE_DIAGNOSTICS";
        public static final String ENERGY_OPTIMIZATION = "ENERGY_OPTIMIZATION";
        public static final String CARBON_FOOTPRINT = "CARBON_FOOTPRINT";
        public static final String RANGE_PREDICTION = "RANGE_PREDICTION";
        public static final String REGENERATIVE_BRAKING = "REGENERATIVE_BRAKING";
        public static final String BATTERY_HEALTH = "BATTERY_HEALTH";
        public static final String FUEL_STATION_DISCOVERY = "FUEL_STATION_DISCOVERY";
        public static final String CHARGING_STATION_DISCOVERY = "CHARGING_STATION_DISCOVERY";
        public static final String TRIP_COST_ANALYSIS = "TRIP_COST_ANALYSIS";
    }

    /**
     * Check if this feature is available for a specific fuel type
     * 
     * @param fuelType the fuel type to check
     * @return true if the feature is enabled and supports the fuel type
     */
    public boolean isAvailableFor(FuelType fuelType) {
        return enabled && supportedFuelTypes.contains(fuelType);
    }

    /**
     * Constructor for creating a new feature toggle
     * 
     * @param featureName the unique name of the feature
     * @param description the description of the feature
     * @param enabled whether the feature is globally enabled
     * @param supportedFuelTypes set of fuel types that support this feature
     */
    public FeatureToggle(String featureName, String description, Boolean enabled, Set<FuelType> supportedFuelTypes) {
        this.featureName = featureName;
        this.description = description;
        this.enabled = enabled;
        this.supportedFuelTypes = supportedFuelTypes;
    }
}
