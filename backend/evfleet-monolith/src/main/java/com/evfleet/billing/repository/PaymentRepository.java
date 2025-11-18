package com.evfleet.billing.repository;

import com.evfleet.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM Payment p JOIN Invoice i ON p.invoiceId = i.id WHERE i.companyId = :companyId")
    List<Payment> findByCompanyId(Long companyId);

    @Query("SELECT p FROM Payment p WHERE p.invoiceId = :invoiceId AND p.status = 'COMPLETED'")
    List<Payment> findSuccessfulPaymentsByInvoiceId(Long invoiceId);

    boolean existsByTransactionId(String transactionId);
}
