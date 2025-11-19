package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Trip entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByVehicleId(Long vehicleId);

    List<Trip> findByDriverId(Long driverId);

    List<Trip> findByCompanyId(Long companyId);

    List<Trip> findByStatus(Trip.TripStatus status);

    Optional<Trip> findByVehicleIdAndStatus(Long vehicleId, Trip.TripStatus status);

    Optional<Trip> findByDriverIdAndStatus(Long driverId, Trip.TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.companyId = :companyId " +
           "AND t.startTime BETWEEN :startDate AND :endDate")
    List<Trip> findByCompanyAndDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
}
