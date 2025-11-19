package com.evfleet.customer.repository;

import com.evfleet.customer.model.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CustomerFeedback entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {

    /**
     * Find all feedback for a customer
     */
    List<CustomerFeedback> findByCustomerId(Long customerId);

    /**
     * Find feedback for a trip
     */
    List<CustomerFeedback> findByTripId(Long tripId);

    /**
     * Find feedback for a driver
     */
    List<CustomerFeedback> findByDriverId(Long driverId);

    /**
     * Find feedback by rating
     */
    List<CustomerFeedback> findByRating(Integer rating);

    /**
     * Get average rating for a driver
     */
    @Query("SELECT AVG(f.rating) FROM CustomerFeedback f WHERE f.driverId = :driverId")
    Double getAverageRatingForDriver(Long driverId);

    /**
     * Get average rating for a vehicle
     */
    @Query("SELECT AVG(f.rating) FROM CustomerFeedback f WHERE f.vehicleId = :vehicleId")
    Double getAverageRatingForVehicle(Long vehicleId);
}
