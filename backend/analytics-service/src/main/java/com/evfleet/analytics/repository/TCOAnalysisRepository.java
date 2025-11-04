package com.evfleet.analytics.repository;

import com.evfleet.analytics.entity.TCOAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TCOAnalysisRepository extends JpaRepository<TCOAnalysis, String> {

    Optional<TCOAnalysis> findByVehicleId(String vehicleId);
}
