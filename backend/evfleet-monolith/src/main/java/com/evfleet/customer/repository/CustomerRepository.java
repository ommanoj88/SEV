package com.evfleet.customer.repository;

import com.evfleet.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find all customers for a company
     */
    List<Customer> findByCompanyId(Long companyId);

    /**
     * Find customers by company and status
     */
    List<Customer> findByCompanyIdAndStatus(Long companyId, Customer.CustomerStatus status);

    /**
     * Find customers by type
     */
    List<Customer> findByCompanyIdAndCustomerType(Long companyId, Customer.CustomerType customerType);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by phone
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    /**
     * Find active customers for a company
     */
    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId AND c.status = 'ACTIVE'")
    List<Customer> findActiveCustomersByCompany(Long companyId);
}
