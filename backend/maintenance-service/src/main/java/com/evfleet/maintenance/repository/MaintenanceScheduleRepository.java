package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, String> {

    List<MaintenanceSchedule> findByVehicleId(String vehicleId);

    List<MaintenanceSchedule> findByStatus(String status);

    List<MaintenanceSchedule> findByDueDateBefore(LocalDate date);

    List<MaintenanceSchedule> findByVehicleIdAndStatus(String vehicleId, String status);
}
