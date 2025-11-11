package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Find by customer code
    Optional<Customer> findByCustomerCode(String customerCode);

    // Find by customer type
    List<Customer> findByCustomerType(String customerType);

    // Find active customers
    List<Customer> findByIsActive(Boolean isActive);

    // Find by city
    List<Customer> findByCity(String city);

    // Find by state
    List<Customer> findByState(String state);

    // Search by name
    @Query("SELECT c FROM Customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> searchByName(@Param("name") String name);

    // Find customers with credit limit
    @Query("SELECT c FROM Customer c WHERE c.creditLimit IS NOT NULL AND c.creditLimit > 0")
    List<Customer> findCustomersWithCreditLimit();

    // Find customers over credit limit
    @Query("SELECT c FROM Customer c WHERE c.outstandingBalance > c.creditLimit")
    List<Customer> findCustomersOverCreditLimit();

    // Find top rated customers
    @Query("SELECT c FROM Customer c WHERE c.serviceRating >= :minRating ORDER BY c.serviceRating DESC")
    List<Customer> findTopRatedCustomers(@Param("minRating") BigDecimal minRating);

    // Find customers by rating range
    @Query("SELECT c FROM Customer c WHERE c.serviceRating BETWEEN :minRating AND :maxRating")
    List<Customer> findByRatingRange(@Param("minRating") BigDecimal minRating, @Param("maxRating") BigDecimal maxRating);

    // Find customers with coordinates
    @Query("SELECT c FROM Customer c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Customer> findCustomersWithCoordinates();

    // Find customers by phone
    @Query("SELECT c FROM Customer c WHERE c.primaryPhone = :phone OR c.secondaryPhone = :phone")
    Optional<Customer> findByPhone(@Param("phone") String phone);

    // Find customers by email
    Optional<Customer> findByEmail(String email);

    // Count active customers
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true")
    Long countActiveCustomers();

    // Find customers with high success rate
    @Query("SELECT c FROM Customer c WHERE c.totalDeliveries > 0 " +
           "AND (CAST(c.successfulDeliveries AS float) / c.totalDeliveries) >= :minRate")
    List<Customer> findCustomersWithHighSuccessRate(@Param("minRate") Double minRate);

    // Find business customers
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'BUSINESS' AND c.gstin IS NOT NULL")
    List<Customer> findBusinessCustomers();

    // Check if customer code exists
    boolean existsByCustomerCode(String customerCode);
}
