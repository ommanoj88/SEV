package com.evfleet.billing.service;

import com.evfleet.billing.entity.Invoice;
import com.evfleet.billing.entity.Payment;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.PaymentRepository;
import com.evfleet.billing.exception.BillingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for processing payments against invoices.
 * Supports multiple payment methods and handles payment status tracking.
 *
 * PR 18: Invoice Generation - Payment processing
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Process payment for an invoice.
     * Validates amount and updates invoice status.
     */
    @Transactional
    public Payment processPayment(String invoiceId, BigDecimal amount, String paymentMethod) {
        log.info("Processing payment for invoice {}: amount=₹{}, method={}",
                invoiceId, amount, paymentMethod);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        // Validate payment amount
        if (amount.compareTo(invoice.getTotalAmount()) < 0) {
            throw new BillingException(
                    String.format("Payment amount ₹%s is less than invoice amount ₹%s",
                            amount, invoice.getTotalAmount()));
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("PROCESSING");
        payment.setProcessedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        try {
            // Call payment gateway
            boolean paymentSuccessful = processWithPaymentGateway(payment);

            if (paymentSuccessful) {
                payment.setStatus("SUCCESS");
                invoice.setStatus("PAID");
                invoice.setPaidDate(java.time.LocalDate.now());
                log.info("Payment successful for invoice {}", invoiceId);
            } else {
                payment.setStatus("FAILED");
                payment.setFailureReason("Payment gateway rejected transaction");
                log.warn("Payment failed for invoice {}", invoiceId);
            }
        } catch (Exception e) {
            payment.setStatus("FAILED");
            payment.setFailureReason(e.getMessage());
            log.error("Payment processing error for invoice {}", invoiceId, e);
            throw new BillingException("Payment processing failed", e);
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        return payment;
    }

    /**
     * Process payment with external payment gateway.
     * This is a mock implementation - replace with actual gateway integration (Razorpay, Stripe, etc.)
     */
    private boolean processWithPaymentGateway(Payment payment) {
        log.info("Calling payment gateway for ₹{} via {}", payment.getAmount(), payment.getPaymentMethod());

        // Mock gateway response
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
        payment.setTransactionId(transactionId);

        // Simulate successful payment
        return true;
    }

    /**
     * Retry failed payment for an invoice.
     */
    @Transactional
    public Payment retryPayment(String invoiceId, String paymentMethod) {
        log.info("Retrying payment for invoice {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        if ("PAID".equals(invoice.getStatus())) {
            throw new BillingException("Invoice is already paid");
        }

        return processPayment(invoiceId, invoice.getTotalAmount(), paymentMethod);
    }

    /**
     * Get payment history for an invoice.
     */
    public List<Payment> getPaymentHistory(String invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Handle overdue invoice with late fee.
     * Applies 5% late fee if invoice is > 30 days past due.
     */
    @Transactional
    public void handleOverdueInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        if (!"FINALIZED".equals(invoice.getStatus())) {
            return; // Only apply to unpaid invoices
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        if (today.isAfter(invoice.getDueDate().plusDays(30))) {
            BigDecimal lateFee = invoice.getTotalAmount().multiply(BigDecimal.valueOf(0.05));
            invoice.setTotalAmount(invoice.getTotalAmount().add(lateFee));
            invoice.setStatus("OVERDUE");
            invoiceRepository.save(invoice);

            log.warn("Invoice {} marked overdue with late fee ₹{}", invoiceId, lateFee);
        }
    }

    /**
     * Verify payment was processed successfully.
     */
    public boolean verifyPaymentStatus(String invoiceId) {
        List<Payment> payments = getPaymentHistory(invoiceId);
        return payments.stream()
                .anyMatch(p -> "SUCCESS".equals(p.getStatus()));
    }
}
