package com.evfleet.billing.repository;

import com.evfleet.billing.model.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Pricing Plan Repository
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {

    Optional<PricingPlan> findByName(String name);

    List<PricingPlan> findByIsActiveTrue();

    List<PricingPlan> findByBillingCycle(PricingPlan.BillingCycle billingCycle);

    @Query("SELECT p FROM PricingPlan p WHERE p.isActive = true AND :vehicleCount >= p.minVehicles AND (:vehicleCount <= p.maxVehicles OR p.maxVehicles IS NULL)")
    List<PricingPlan> findPlansForVehicleCount(int vehicleCount);

    @Query("SELECT p FROM PricingPlan p WHERE p.isActive = true AND p.billingCycle = :cycle AND :vehicleCount >= p.minVehicles AND (:vehicleCount <= p.maxVehicles OR p.maxVehicles IS NULL)")
    List<PricingPlan> findPlansForVehicleCountAndCycle(int vehicleCount, PricingPlan.BillingCycle cycle);
}
