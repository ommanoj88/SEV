package com.evfleet.driver.repository;

import com.evfleet.driver.entity.DriverBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverBehaviorRepository extends JpaRepository<DriverBehavior, String> {

    List<DriverBehavior> findByDriverId(String driverId);

    List<DriverBehavior> findByDriverIdOrderByTimestampDesc(String driverId);

    List<DriverBehavior> findByTripId(String tripId);
}
