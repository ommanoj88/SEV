package com.evfleet.billing.repository;

import com.evfleet.billing.model.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Billing Address Repository
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {

    Optional<BillingAddress> findByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);
}
