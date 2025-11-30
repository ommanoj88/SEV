package com.evfleet.billing.repository;

import com.evfleet.billing.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentOrder entity
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    /**
     * Find by Razorpay Order ID
     */
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Find by Razorpay Payment ID
     */
    Optional<PaymentOrder> findByRazorpayPaymentId(String razorpayPaymentId);

    /**
     * Find by Invoice ID
     */
    List<PaymentOrder> findByInvoiceId(Long invoiceId);

    /**
     * Find by Invoice ID with specific status
     */
    Optional<PaymentOrder> findByInvoiceIdAndStatus(Long invoiceId, PaymentOrder.OrderStatus status);

    /**
     * Find by Company ID
     */
    List<PaymentOrder> findByCompanyId(Long companyId);

    /**
     * Find by Company ID ordered by creation date
     */
    List<PaymentOrder> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    /**
     * Find by status
     */
    List<PaymentOrder> findByStatus(PaymentOrder.OrderStatus status);

    /**
     * Find pending orders for a company
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.companyId = :companyId " +
           "AND po.status IN ('CREATED', 'ATTEMPTED') ORDER BY po.createdAt DESC")
    List<PaymentOrder> findPendingOrdersByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find successful payments for a company within date range
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.companyId = :companyId " +
           "AND po.status = 'PAID' AND po.paidAt BETWEEN :start AND :end ORDER BY po.paidAt DESC")
    List<PaymentOrder> findSuccessfulPayments(
            @Param("companyId") Long companyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find expired orders that need cleanup
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.status = 'CREATED' " +
           "AND po.expiresAt IS NOT NULL AND po.expiresAt < :now")
    List<PaymentOrder> findExpiredOrders(@Param("now") LocalDateTime now);

    /**
     * Find orders created in date range
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.createdAt BETWEEN :start AND :end")
    List<PaymentOrder> findByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Count orders by status for a company
     */
    @Query("SELECT po.status, COUNT(po) FROM PaymentOrder po WHERE po.companyId = :companyId GROUP BY po.status")
    List<Object[]> countByStatusForCompany(@Param("companyId") Long companyId);

    /**
     * Calculate total amount paid by company
     */
    @Query("SELECT COALESCE(SUM(po.amountPaid), 0) FROM PaymentOrder po WHERE po.companyId = :companyId AND po.status = 'PAID'")
    Long sumAmountPaidByCompany(@Param("companyId") Long companyId);

    /**
     * Calculate total refunded amount by company
     */
    @Query("SELECT COALESCE(SUM(po.amountRefunded), 0) FROM PaymentOrder po WHERE po.companyId = :companyId")
    Long sumAmountRefundedByCompany(@Param("companyId") Long companyId);

    /**
     * Check if invoice has any successful payment
     */
    boolean existsByInvoiceIdAndStatus(Long invoiceId, PaymentOrder.OrderStatus status);

    /**
     * Find last successful payment for invoice
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.invoiceId = :invoiceId AND po.status = 'PAID' ORDER BY po.paidAt DESC")
    Optional<PaymentOrder> findLastSuccessfulPaymentForInvoice(@Param("invoiceId") Long invoiceId);

    /**
     * Find refundable orders for invoice
     */
    @Query("SELECT po FROM PaymentOrder po WHERE po.invoiceId = :invoiceId " +
           "AND po.status IN ('PAID', 'PARTIALLY_REFUNDED') " +
           "AND (po.amountPaid - COALESCE(po.amountRefunded, 0)) > 0")
    List<PaymentOrder> findRefundableOrdersForInvoice(@Param("invoiceId") Long invoiceId);

    /**
     * Delete old expired orders (cleanup)
     */
    @Query("DELETE FROM PaymentOrder po WHERE po.status = 'EXPIRED' AND po.createdAt < :before")
    void deleteExpiredOrdersBefore(@Param("before") LocalDateTime before);
}
