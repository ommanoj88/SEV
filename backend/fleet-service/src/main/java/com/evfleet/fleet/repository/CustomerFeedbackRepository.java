package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {

    // Find by customer
    List<CustomerFeedback> findByCustomerId(Long customerId);

    // Find by waypoint
    List<CustomerFeedback> findByWaypointId(Long waypointId);

    // Find by route plan
    List<CustomerFeedback> findByRoutePlanId(Long routePlanId);

    // Find by rating
    List<CustomerFeedback> findByRating(Integer rating);

    // Find unaddressed feedback
    List<CustomerFeedback> findByIsAddressed(Boolean isAddressed);

    // Find by feedback category
    List<CustomerFeedback> findByFeedbackCategory(String category);

    // Find negative feedback (rating <= 2)
    @Query("SELECT f FROM CustomerFeedback f WHERE f.rating <= 2 ORDER BY f.createdAt DESC")
    List<CustomerFeedback> findNegativeFeedback();

    // Find positive feedback (rating >= 4)
    @Query("SELECT f FROM CustomerFeedback f WHERE f.rating >= 4 ORDER BY f.createdAt DESC")
    List<CustomerFeedback> findPositiveFeedback();

    // Find recent unaddressed feedback
    @Query("SELECT f FROM CustomerFeedback f WHERE f.isAddressed = false " +
           "ORDER BY f.createdAt DESC")
    List<CustomerFeedback> findRecentUnaddressedFeedback();

    // Find feedback by date range
    List<CustomerFeedback> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Calculate average rating for customer
    @Query("SELECT AVG(f.rating) FROM CustomerFeedback f WHERE f.customerId = :customerId")
    Double calculateAverageRatingForCustomer(@Param("customerId") Long customerId);

    // Count feedback by rating
    @Query("SELECT COUNT(f) FROM CustomerFeedback f WHERE f.rating = :rating")
    Long countByRating(@Param("rating") Integer rating);

    // Find feedback requiring action (negative and unaddressed)
    @Query("SELECT f FROM CustomerFeedback f WHERE f.rating <= 2 AND f.isAddressed = false " +
           "ORDER BY f.createdAt ASC")
    List<CustomerFeedback> findFeedbackRequiringAction();

    // Count unaddressed feedback
    @Query("SELECT COUNT(f) FROM CustomerFeedback f WHERE f.isAddressed = false")
    Long countUnaddressedFeedback();
}
