package com.evfleet.driver.repository;

import com.evfleet.driver.entity.DriverAssignment;
import com.evfleet.driver.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, String> {

    List<DriverAssignment> findByDriverId(String driverId);

    List<DriverAssignment> findByVehicleId(String vehicleId);

    List<DriverAssignment> findByDriverIdAndStatus(String driverId, AssignmentStatus status);

    List<DriverAssignment> findByVehicleIdAndStatus(String vehicleId, AssignmentStatus status);

    Optional<DriverAssignment> findFirstByDriverIdAndStatusOrderByShiftStartDesc(String driverId, AssignmentStatus status);
}
