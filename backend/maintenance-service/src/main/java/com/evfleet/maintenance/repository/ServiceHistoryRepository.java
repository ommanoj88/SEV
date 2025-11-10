package com.evfleet.maintenance.repository;

import com.evfleet.maintenance.entity.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, String> {

    List<ServiceHistory> findByVehicleId(String vehicleId);

    List<ServiceHistory> findByServiceType(String serviceType);

    List<ServiceHistory> findByServiceDateBetween(LocalDate startDate, LocalDate endDate);

    List<ServiceHistory> findByVehicleIdOrderByServiceDateDesc(String vehicleId);
    
    // ========== PR 12: Maintenance Cost Tracking ==========
    
    /**
     * Find service history by vehicle ID and date range
     */
    List<ServiceHistory> findByVehicleIdAndServiceDateBetween(
            String vehicleId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate total cost for a vehicle
     */
    @Query("SELECT COALESCE(SUM(sh.cost), 0) FROM ServiceHistory sh WHERE sh.vehicleId = :vehicleId")
    BigDecimal calculateTotalCostForVehicle(@Param("vehicleId") String vehicleId);
    
    /**
     * Calculate total cost for a vehicle within a date range
     */
    @Query("SELECT COALESCE(SUM(sh.cost), 0) FROM ServiceHistory sh " +
           "WHERE sh.vehicleId = :vehicleId " +
           "AND sh.serviceDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalCostForVehicleInPeriod(
            @Param("vehicleId") String vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Calculate total cost by service type
     */
    @Query("SELECT COALESCE(SUM(sh.cost), 0) FROM ServiceHistory sh " +
           "WHERE sh.serviceType = :serviceType")
    BigDecimal calculateTotalCostByServiceType(@Param("serviceType") String serviceType);
    
    /**
     * Calculate total cost by service type within a date range
     */
    @Query("SELECT COALESCE(SUM(sh.cost), 0) FROM ServiceHistory sh " +
           "WHERE sh.serviceType = :serviceType " +
           "AND sh.serviceDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalCostByServiceTypeInPeriod(
            @Param("serviceType") String serviceType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Find all service history in a date range
     */
    @Query("SELECT sh FROM ServiceHistory sh " +
           "WHERE sh.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sh.serviceDate DESC")
    List<ServiceHistory> findAllInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Count service records for a vehicle
     */
    @Query("SELECT COUNT(sh) FROM ServiceHistory sh WHERE sh.vehicleId = :vehicleId")
    Long countByVehicleId(@Param("vehicleId") String vehicleId);
    
    /**
     * Find the most expensive service for a vehicle
     */
    @Query("SELECT sh FROM ServiceHistory sh WHERE sh.vehicleId = :vehicleId " +
           "ORDER BY sh.cost DESC")
    List<ServiceHistory> findMostExpensiveServiceForVehicle(@Param("vehicleId") String vehicleId);
}
