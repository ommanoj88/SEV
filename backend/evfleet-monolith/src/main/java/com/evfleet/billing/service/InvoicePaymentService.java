package com.evfleet.billing.service;

import com.evfleet.billing.dto.*;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.Invoice.InvoiceStatus;
import com.evfleet.billing.model.Payment;
import com.evfleet.billing.model.Payment.PaymentMethodType;
import com.evfleet.billing.model.Payment.PaymentStatus;
import com.evfleet.billing.model.PaymentOrder;
import com.evfleet.billing.model.PaymentOrder.OrderStatus;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.PaymentOrderRepository;
import com.evfleet.billing.repository.PaymentRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.model.Notification.NotificationType;
import com.evfleet.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Invoice Payment Service
 * 
 * Provides a unified workflow for invoice payments using Razorpay:
 * - Initiates payment for an invoice
 * - Handles payment success/failure callbacks
 * - Supports partial payments
 * - Generates payment receipts
 * - Sends notifications on payment events
 * 
 * Payment Flow:
 * 1. initiatePayment() - Creates Razorpay order for invoice
 * 2. Client completes payment via Razorpay Checkout
 * 3. handlePaymentSuccess() - Processes successful payment
 *    OR handlePaymentFailure() - Handles payment failure
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@Transactional
public class InvoicePaymentService {

    private final RazorpayPaymentService razorpayPaymentService;
    private final PaymentEmailService paymentEmailService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final NotificationRepository notificationRepository;
    private final MeterRegistry meterRegistry;

    public InvoicePaymentService(
            RazorpayPaymentService razorpayPaymentService,
            @Autowired(required = false) PaymentEmailService paymentEmailService,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            PaymentOrderRepository paymentOrderRepository,
            NotificationRepository notificationRepository,
            MeterRegistry meterRegistry) {
        this.razorpayPaymentService = razorpayPaymentService;
        this.paymentEmailService = paymentEmailService;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.notificationRepository = notificationRepository;
        this.meterRegistry = meterRegistry;
    }

    private Counter paymentInitiatedCounter;
    private Counter paymentCompletedCounter;
    private Counter paymentFailedCounter;
    private Counter partialPaymentCounter;

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private static final DateTimeFormatter RECEIPT_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @PostConstruct
    public void initMetrics() {
        paymentInitiatedCounter = Counter.builder("invoice.payment.initiated")
                .description("Number of payment initiations")
                .register(meterRegistry);
        paymentCompletedCounter = Counter.builder("invoice.payment.completed")
                .description("Number of completed payments")
                .register(meterRegistry);
        paymentFailedCounter = Counter.builder("invoice.payment.failed")
                .description("Number of failed payments")
                .register(meterRegistry);
        partialPaymentCounter = Counter.builder("invoice.payment.partial")
                .description("Number of partial payments")
                .register(meterRegistry);
    }

    /**
     * Initiate payment for an invoice
     * 
     * Creates a Razorpay order and returns details for frontend checkout
     * 
     * @param request Payment initiation request
     * @return Payment initiation response with checkout details
     */
    public PaymentInitiationResponse initiatePayment(InvoicePaymentRequest request) {
        log.info("Initiating payment for invoice: {}", request.getInvoiceId());

        // Validate and fetch invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        validateInvoiceForPayment(invoice);

        // Calculate payment amount (remaining amount or custom partial amount)
        BigDecimal paymentAmount = calculatePaymentAmount(invoice, request.getAmount());

        // Validate partial payment
        if (request.getAmount() != null) {
            validatePartialPayment(invoice, request.getAmount());
        }

        // Create Razorpay order
        RazorpayOrderRequest orderRequest = RazorpayOrderRequest.builder()
                .invoiceId(invoice.getId())
                .amount(paymentAmount)
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .notes(request.getNotes())
                .build();

        RazorpayOrderResponse orderResponse = razorpayPaymentService.createOrder(orderRequest);

        paymentInitiatedCounter.increment();
        log.info("Payment initiated for invoice: {}, order: {}", invoice.getId(), orderResponse.getOrderId());

        // Convert amount to paise for response
        long amountInPaise = paymentAmount.multiply(BigDecimal.valueOf(100)).longValue();

        return PaymentInitiationResponse.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(orderResponse.getOrderId())
                .keyId(orderResponse.getKeyId())
                .amount(paymentAmount)
                .amountInPaise(amountInPaise)
                .currency(orderResponse.getCurrency())
                .remainingAmount(invoice.getRemainingAmount())
                .isPartialPayment(paymentAmount.compareTo(invoice.getRemainingAmount()) < 0)
                .checkoutUrl(orderResponse.getCallbackUrl())
                .expiresAt(orderResponse.getExpiresAt())
                .build();
    }

    /**
     * Handle successful payment callback
     * 
     * Verifies payment, updates invoice status, creates payment record, sends notification
     * 
     * @param request Payment success request
     * @return Payment receipt response
     */
    public PaymentReceiptResponse handlePaymentSuccess(PaymentSuccessRequest request) {
        log.info("Handling payment success for order: {}", request.getRazorpayOrderId());

        // Verify payment with Razorpay
        RazorpayPaymentVerifyRequest verifyRequest = RazorpayPaymentVerifyRequest.builder()
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .razorpaySignature(request.getRazorpaySignature())
                .build();

        RazorpayPaymentVerifyResponse verifyResponse = razorpayPaymentService.verifyPayment(verifyRequest);

        if (!verifyResponse.isSuccess()) {
            log.error("Payment verification failed for order: {}", request.getRazorpayOrderId());
            throw new IllegalStateException("Payment verification failed: " + verifyResponse.getMessage());
        }

        // Get payment order
        PaymentOrder paymentOrder = paymentOrderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "razorpayOrderId", request.getRazorpayOrderId()));

        // Get invoice
        Invoice invoice = invoiceRepository.findById(paymentOrder.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", paymentOrder.getInvoiceId()));

        // Calculate paid amount in rupees
        BigDecimal paidAmount = paymentOrder.getAmountPaidInRupees();

        // Create payment record
        Payment payment = createPaymentRecord(invoice, paymentOrder, paidAmount);
        
        // Update invoice status
        updateInvoiceStatus(invoice, paidAmount);

        paymentCompletedCounter.increment();

        // Check if this is a partial payment
        boolean isPartialPayment = invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID;
        if (isPartialPayment) {
            partialPaymentCounter.increment();
        }

        // Generate receipt
        PaymentReceiptResponse receipt = generateReceipt(invoice, payment, paymentOrder);

        // Send in-app notification
        sendPaymentSuccessNotification(invoice, payment, isPartialPayment);

        // Send email notification with receipt (async)
        if (paymentEmailService != null && paymentOrder.getCustomerEmail() != null) {
            paymentEmailService.sendPaymentSuccessEmail(paymentOrder.getCustomerEmail(), receipt);
        }

        log.info("Payment processed successfully. Invoice: {}, Payment: {}, Status: {}", 
                invoice.getId(), payment.getId(), invoice.getStatus());

        return receipt;
    }

    /**
     * Handle payment failure callback
     * 
     * Updates payment order status, creates failed payment record, sends notification
     * 
     * @param request Payment failure request
     */
    public void handlePaymentFailure(PaymentFailureRequest request) {
        log.info("Handling payment failure for order: {}", request.getRazorpayOrderId());

        // Get payment order
        PaymentOrder paymentOrder = paymentOrderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentOrder", "razorpayOrderId", request.getRazorpayOrderId()));

        // Update order status
        String errorCode = request.getErrorCode() != null ? request.getErrorCode() : "UNKNOWN";
        String errorDescription = request.getErrorDescription() != null ? request.getErrorDescription() : "Payment failed";
        String errorReason = request.getErrorReason() != null ? request.getErrorReason() : "Unknown reason";

        paymentOrder.markAsFailed(errorCode, errorDescription, errorReason);
        paymentOrderRepository.save(paymentOrder);

        // Get invoice
        Invoice invoice = invoiceRepository.findById(paymentOrder.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", paymentOrder.getInvoiceId()));

        // Create failed payment record for audit
        Payment failedPayment = Payment.builder()
                .invoiceId(invoice.getId())
                .amount(paymentOrder.getAmountInRupees())
                .paymentMethod(PaymentMethodType.RAZORPAY)
                .status(PaymentStatus.FAILED)
                .failureReason(errorCode + ": " + errorDescription)
                .remarks("Order: " + request.getRazorpayOrderId())
                .build();
        paymentRepository.save(failedPayment);

        paymentFailedCounter.increment();

        // Send in-app notification
        sendPaymentFailureNotification(invoice, errorDescription);

        // Send email notification (async)
        if (paymentEmailService != null && paymentOrder.getCustomerEmail() != null) {
            paymentEmailService.sendPaymentFailureEmail(
                    paymentOrder.getCustomerEmail(),
                    invoice.getInvoiceNumber(),
                    paymentOrder.getAmountInRupees(),
                    errorDescription
            );
        }

        log.warn("Payment failed for invoice: {}, error: {}", invoice.getId(), errorDescription);
    }

    /**
     * Get payment status for an invoice
     * 
     * @param invoiceId Invoice ID
     * @return Current payment status details
     */
    @Transactional(readOnly = true)
    public InvoicePaymentStatusResponse getPaymentStatus(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        List<PaymentOrder> pendingOrders = paymentOrderRepository.findByInvoiceIdAndStatus(invoiceId, OrderStatus.CREATED)
                .map(List::of)
                .orElse(List.of());

        return InvoicePaymentStatusResponse.builder()
                .invoiceId(invoiceId)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceStatus(invoice.getStatus().name())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .paymentCount(payments.size())
                .successfulPayments((int) payments.stream()
                        .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                        .count())
                .hasPendingOrder(!pendingOrders.isEmpty())
                .isFullyPaid(invoice.getStatus() == InvoiceStatus.PAID)
                .isOverdue(invoice.isOverdue())
                .build();
    }

    /**
     * Get payment history for an invoice
     * 
     * @param invoiceId Invoice ID
     * @return List of payment records
     */
    @Transactional(readOnly = true)
    public List<InvoicePaymentHistoryResponse> getPaymentHistory(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice", "id", invoiceId);
        }

        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);

        return payments.stream()
                .map(p -> InvoicePaymentHistoryResponse.builder()
                        .paymentId(p.getId())
                        .transactionId(p.getTransactionId())
                        .amount(p.getAmount())
                        .paymentMethod(p.getPaymentMethod().name())
                        .status(p.getStatus().name())
                        .paymentDate(p.getPaymentDate())
                        .processedAt(p.getProcessedAt())
                        .failureReason(p.getFailureReason())
                        .build())
                .toList();
    }

    /**
     * Retry payment for an invoice with failed/expired order
     * 
     * @param request Payment retry request
     * @return New payment initiation response
     */
    public PaymentInitiationResponse retryPayment(InvoicePaymentRequest request) {
        log.info("Retrying payment for invoice: {}", request.getInvoiceId());

        // Cancel any existing pending orders
        Optional<PaymentOrder> existingOrder = paymentOrderRepository
                .findByInvoiceIdAndStatus(request.getInvoiceId(), OrderStatus.CREATED);
        
        if (existingOrder.isPresent()) {
            PaymentOrder order = existingOrder.get();
            order.markAsExpired();
            paymentOrderRepository.save(order);
            log.info("Expired existing pending order: {}", order.getRazorpayOrderId());
        }

        // Create new payment
        return initiatePayment(request);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void validateInvoiceForPayment(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already fully paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay a cancelled invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Cannot pay a draft invoice. Invoice must be finalized first.");
        }
    }

    private BigDecimal calculatePaymentAmount(Invoice invoice, BigDecimal requestedAmount) {
        if (requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return requestedAmount;
        }
        return invoice.getRemainingAmount();
    }

    private void validatePartialPayment(Invoice invoice, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (amount.compareTo(invoice.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Payment amount (%.2f) exceeds remaining amount (%.2f)", 
                            amount, invoice.getRemainingAmount()));
        }
    }

    private Payment createPaymentRecord(Invoice invoice, PaymentOrder paymentOrder, BigDecimal paidAmount) {
        Payment payment = Payment.builder()
                .invoiceId(invoice.getId())
                .amount(paidAmount)
                .paymentMethod(PaymentMethodType.RAZORPAY)
                .transactionId(paymentOrder.getRazorpayPaymentId())
                .status(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .remarks("Razorpay Order: " + paymentOrder.getRazorpayOrderId())
                .build();
        
        return paymentRepository.save(payment);
    }

    private void updateInvoiceStatus(Invoice invoice, BigDecimal paidAmount) {
        BigDecimal newPaidAmount = invoice.getPaidAmount() != null ?
                invoice.getPaidAmount().add(paidAmount) : paidAmount;
        
        invoice.markAsPaid(LocalDate.now(), newPaidAmount);
        invoiceRepository.save(invoice);
    }

    private PaymentReceiptResponse generateReceipt(Invoice invoice, Payment payment, PaymentOrder order) {
        String receiptNumber = generateReceiptNumber();
        LocalDateTime receiptDate = LocalDateTime.now();

        return PaymentReceiptResponse.builder()
                .receiptNumber(receiptNumber)
                .receiptDate(receiptDate)
                .formattedReceiptDate(receiptDate.format(RECEIPT_DATE_FORMAT))
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(order.getRazorpayOrderId())
                .razorpayPaymentId(order.getRazorpayPaymentId())
                .amountPaid(payment.getAmount())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "Razorpay")
                .currency("INR")
                .invoiceTotal(invoice.getTotalAmount())
                .previouslyPaid(invoice.getPaidAmount().subtract(payment.getAmount()))
                .totalPaidNow(payment.getAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .isFullyPaid(invoice.getStatus() == InvoiceStatus.PAID)
                .companyId(invoice.getCompanyId())
                .bank(order.getBank())
                .wallet(order.getWallet())
                .vpa(order.getVpa())
                .razorpayFee(order.getRazorpayFeeInRupees())
                .build();
    }

    private String generateReceiptNumber() {
        return "RCP-" + LocalDateTime.now().format(RECEIPT_NUMBER_FORMAT) + 
                "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void sendPaymentSuccessNotification(Invoice invoice, Payment payment, boolean isPartialPayment) {
        String title = isPartialPayment ? "Partial Payment Received" : "Payment Successful";
        String message = String.format(
                "Payment of ₹%.2f received for Invoice %s. %s",
                payment.getAmount(),
                invoice.getInvoiceNumber(),
                isPartialPayment ? 
                        String.format("Remaining balance: ₹%.2f", invoice.getRemainingAmount()) :
                        "Invoice is now fully paid."
        );

        // Create notification - userId should be fetched from company or invoice context
        // For now, we'll use companyId as a placeholder for company admin notification
        Notification notification = Notification.builder()
                .userId(invoice.getCompanyId()) // This should be company admin user ID
                .title(title)
                .message(message)
                .type(NotificationType.SUCCESS)
                .referenceId("PAYMENT-" + payment.getId())
                .build();

        notificationRepository.save(notification);
        log.info("Payment success notification sent for invoice: {}", invoice.getId());
    }

    private void sendPaymentFailureNotification(Invoice invoice, String errorDescription) {
        String title = "Payment Failed";
        String message = String.format(
                "Payment failed for Invoice %s. Reason: %s. Please try again.",
                invoice.getInvoiceNumber(),
                errorDescription
        );

        Notification notification = Notification.builder()
                .userId(invoice.getCompanyId()) // This should be company admin user ID
                .title(title)
                .message(message)
                .type(NotificationType.ALERT)
                .referenceId("INVOICE-" + invoice.getId())
                .build();

        notificationRepository.save(notification);
        log.info("Payment failure notification sent for invoice: {}", invoice.getId());
    }
}
