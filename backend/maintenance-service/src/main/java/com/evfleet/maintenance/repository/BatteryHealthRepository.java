package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.entity.BatteryHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatteryHealthRepository extends JpaRepository<BatteryHealth, String> {

    List<BatteryHealth> findByVehicleId(String vehicleId);

    List<BatteryHealth> findByVehicleIdOrderByTimestampDesc(String vehicleId);

    Optional<BatteryHealth> findFirstByVehicleIdOrderByTimestampDesc(String vehicleId);

    List<BatteryHealth> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}
