package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.entity.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, String> {

    List<ServiceHistory> findByVehicleId(String vehicleId);

    List<ServiceHistory> findByServiceType(String serviceType);

    List<ServiceHistory> findByServiceDateBetween(LocalDate startDate, LocalDate endDate);

    List<ServiceHistory> findByVehicleIdOrderByServiceDateDesc(String vehicleId);
}
