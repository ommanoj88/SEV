package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FeatureToggle entity operations.
 * Provides methods to query feature availability by fuel type.
 * 
 * @since 2.0.0 (Feature flag system)
 */
@Repository
public interface FeatureToggleRepository extends JpaRepository<FeatureToggle, Long> {

    /**
     * Find a feature toggle by its unique name
     * 
     * @param featureName the name of the feature
     * @return Optional containing the feature toggle if found
     */
    Optional<FeatureToggle> findByFeatureName(String featureName);

    /**
     * Find all enabled feature toggles
     * 
     * @return list of enabled feature toggles
     */
    List<FeatureToggle> findByEnabledTrue();

    /**
     * Find all feature toggles that support a specific fuel type
     * 
     * @param fuelType the fuel type to check
     * @return list of feature toggles supporting the fuel type
     */
    @Query("SELECT ft FROM FeatureToggle ft JOIN ft.supportedFuelTypes sft WHERE sft = :fuelType AND ft.enabled = true")
    List<FeatureToggle> findByFuelType(@Param("fuelType") FuelType fuelType);

    /**
     * Check if a specific feature is available for a fuel type
     * 
     * @param featureName the name of the feature
     * @param fuelType the fuel type to check
     * @return true if the feature is enabled and supports the fuel type
     */
    @Query("SELECT CASE WHEN COUNT(ft) > 0 THEN true ELSE false END " +
           "FROM FeatureToggle ft JOIN ft.supportedFuelTypes sft " +
           "WHERE ft.featureName = :featureName AND sft = :fuelType AND ft.enabled = true")
    boolean isFeatureAvailableForFuelType(
        @Param("featureName") String featureName,
        @Param("fuelType") FuelType fuelType
    );

    /**
     * Check if a feature toggle exists by name
     * 
     * @param featureName the name of the feature
     * @return true if exists
     */
    boolean existsByFeatureName(String featureName);
}
