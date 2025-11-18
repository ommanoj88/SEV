package com.evfleet.billing.repository;

import com.evfleet.billing.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Subscription Repository
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCompanyId(Long companyId);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    List<Subscription> findByStatusAndEndDateBefore(Subscription.SubscriptionStatus status, LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate IS NOT NULL AND s.endDate <= :date AND s.autoRenew = true")
    List<Subscription> findSubscriptionsToRenew(LocalDate date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND (s.endDate IS NULL OR s.endDate > CURRENT_DATE)")
    List<Subscription> findAllActiveSubscriptions();

    boolean existsByCompanyIdAndStatus(Long companyId, Subscription.SubscriptionStatus status);
}
