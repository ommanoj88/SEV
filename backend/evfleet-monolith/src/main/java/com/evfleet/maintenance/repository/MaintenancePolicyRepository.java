package com.evfleet.maintenance.repository;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.maintenance.model.MaintenancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MaintenancePolicy entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface MaintenancePolicyRepository extends JpaRepository<MaintenancePolicy, Long> {

    List<MaintenancePolicy> findByCompanyIdAndActiveTrue(Long companyId);

    List<MaintenancePolicy> findByVehicleTypeAndActiveTrue(Vehicle.VehicleType vehicleType);

    List<MaintenancePolicy> findByCompanyIdAndVehicleTypeAndActiveTrue(Long companyId, Vehicle.VehicleType vehicleType);
}
