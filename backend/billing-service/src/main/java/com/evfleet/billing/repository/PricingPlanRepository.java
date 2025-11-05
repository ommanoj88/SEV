package com.evfleet.billing.repository;

import com.evfleet.billing.entity.PricingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, String> {
    List<PricingPlan> findByIsActiveTrue();
}
