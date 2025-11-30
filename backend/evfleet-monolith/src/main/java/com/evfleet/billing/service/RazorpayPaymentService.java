package com.evfleet.billing.service;

import com.evfleet.billing.config.RazorpayConfig;
import com.evfleet.billing.dto.*;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.PaymentOrder;
import com.evfleet.billing.model.PaymentOrder.OrderStatus;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.PaymentOrderRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.razorpay.Utils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Razorpay Payment Service
 * 
 * Provides integration with Razorpay payment gateway for:
 * - Creating payment orders
 * - Verifying payment signatures
 * - Processing refunds
 * - Fetching payment details
 * 
 * Flow:
 * 1. createOrder() - Creates Razorpay order for an invoice
 * 2. Client completes payment via Razorpay Checkout
 * 3. verifyPayment() - Verifies signature and updates order status
 * 4. refundPayment() - Initiates refund if needed
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RazorpayPaymentService {

    private final RazorpayConfig config;
    private final PaymentOrderRepository paymentOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final MeterRegistry meterRegistry;

    private RazorpayClient razorpayClient;
    private Counter orderCreatedCounter;
    private Counter paymentSuccessCounter;
    private Counter paymentFailureCounter;
    private Counter refundCounter;
    private Timer orderCreationTimer;

    /**
     * Initialize Razorpay client on startup
     */
    @PostConstruct
    public void init() {
        initializeMetrics();
        
        if (!config.isConfigured()) {
            log.warn("Razorpay is not configured. Payment features will be unavailable. " +
                    "Set razorpay.enabled=true and provide credentials to enable.");
            return;
        }

        try {
            razorpayClient = new RazorpayClient(config.getKeyId(), config.getKeySecret());
            log.info("Razorpay client initialized successfully. Key ID: {}, Test Mode: {}", 
                    config.getMaskedKeyId(), config.isTestMode());
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }

    private void initializeMetrics() {
        orderCreatedCounter = Counter.builder("razorpay.orders.created")
                .description("Number of Razorpay orders created")
                .register(meterRegistry);
        paymentSuccessCounter = Counter.builder("razorpay.payments.success")
                .description("Number of successful payments")
                .register(meterRegistry);
        paymentFailureCounter = Counter.builder("razorpay.payments.failure")
                .description("Number of failed payments")
                .register(meterRegistry);
        refundCounter = Counter.builder("razorpay.refunds.initiated")
                .description("Number of refunds initiated")
                .register(meterRegistry);
        orderCreationTimer = Timer.builder("razorpay.order.creation.time")
                .description("Time to create Razorpay order")
                .register(meterRegistry);
    }

    /**
     * Create a Razorpay order for an invoice
     * 
     * @param request Order creation request
     * @return Created order details
     */
    @Retryable(value = RazorpayException.class, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) {
        log.info("Creating Razorpay order for invoice: {}", request.getInvoiceId());
        
        validateConfiguration();
        
        // Validate invoice exists and is payable
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is cancelled");
        }

        // Check for existing pending order
        Optional<PaymentOrder> existingOrder = paymentOrderRepository
                .findByInvoiceIdAndStatus(request.getInvoiceId(), OrderStatus.CREATED);
        if (existingOrder.isPresent()) {
            log.info("Returning existing pending order for invoice: {}", request.getInvoiceId());
            return buildOrderResponse(existingOrder.get(), invoice);
        }

        return orderCreationTimer.record(() -> {
            try {
                // Calculate amount in paise (smallest currency unit)
                BigDecimal amount = request.getAmount() != null ? 
                        request.getAmount() : invoice.getTotalAmount();
                long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

                // Generate receipt number
                String receipt = "INV-" + invoice.getId() + "-" + System.currentTimeMillis();

                // Prepare order options
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amountInPaise);
                orderRequest.put("currency", config.getCurrency());
                orderRequest.put("receipt", receipt);
                orderRequest.put("payment_capture", 1); // Auto-capture payment

                // Add notes
                JSONObject notes = new JSONObject();
                notes.put("invoice_id", invoice.getId().toString());
                notes.put("invoice_number", invoice.getInvoiceNumber());
                notes.put("company_id", invoice.getCompanyId().toString());
                if (request.getNotes() != null) {
                    request.getNotes().forEach(notes::put);
                }
                orderRequest.put("notes", notes);

                // Create order in Razorpay
                Order razorpayOrder = razorpayClient.orders.create(orderRequest);
                String razorpayOrderId = razorpayOrder.get("id");
                log.info("Created Razorpay order: {} for invoice: {}", razorpayOrderId, invoice.getId());

                // Save order in database
                LocalDateTime expiresAt = LocalDateTime.now()
                        .plusMinutes(config.getPaymentLinkExpiryMinutes());

                PaymentOrder paymentOrder = PaymentOrder.builder()
                        .razorpayOrderId(razorpayOrderId)
                        .invoiceId(invoice.getId())
                        .companyId(invoice.getCompanyId())
                        .amount(amountInPaise)
                        .currency(config.getCurrency())
                        .receipt(receipt)
                        .status(OrderStatus.CREATED)
                        .customerEmail(request.getCustomerEmail())
                        .customerPhone(request.getCustomerPhone())
                        .notes(notes.toString())
                        .expiresAt(expiresAt)
                        .build();

                PaymentOrder saved = paymentOrderRepository.save(paymentOrder);
                orderCreatedCounter.increment();

                return buildOrderResponse(saved, invoice);

            } catch (RazorpayException e) {
                log.error("Failed to create Razorpay order for invoice: {}: {}", 
                        request.getInvoiceId(), e.getMessage());
                throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Verify payment signature and update order status
     * 
     * This should be called after client receives payment callback
     * 
     * @param request Payment verification request
     * @return Verification result
     */
    public RazorpayPaymentVerifyResponse verifyPayment(RazorpayPaymentVerifyRequest request) {
        log.info("Verifying payment for order: {}", request.getRazorpayOrderId());

        // Find the order
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentOrder", "razorpayOrderId", request.getRazorpayOrderId()));

        // Verify signature
        boolean signatureValid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        order.setSignature(request.getRazorpaySignature());
        order.setSignatureVerified(signatureValid);

        if (!signatureValid) {
            order.markAsFailed("SIGNATURE_INVALID", "Payment signature verification failed", "Invalid signature");
            paymentOrderRepository.save(order);
            paymentFailureCounter.increment();
            
            log.error("Signature verification failed for order: {}", order.getRazorpayOrderId());
            return RazorpayPaymentVerifyResponse.builder()
                    .success(false)
                    .orderId(order.getRazorpayOrderId())
                    .message("Signature verification failed")
                    .build();
        }

        try {
            // Fetch payment details from Razorpay
            Payment payment = razorpayClient.payments.fetch(request.getRazorpayPaymentId());
            
            String status = payment.get("status");
            Long amountPaid = Long.parseLong(payment.get("amount").toString());

            if ("captured".equals(status)) {
                order.markAsPaid(request.getRazorpayPaymentId(), amountPaid);
                order.setPaymentMethod(payment.get("method"));
                order.setBank(payment.has("bank") ? payment.get("bank") : null);
                order.setWallet(payment.has("wallet") ? payment.get("wallet") : null);
                order.setVpa(payment.has("vpa") ? payment.get("vpa") : null);
                order.setRazorpayFee(payment.has("fee") ? Long.parseLong(payment.get("fee").toString()) : null);
                order.setRazorpayTax(payment.has("tax") ? Long.parseLong(payment.get("tax").toString()) : null);

                // Update invoice status
                updateInvoiceOnPayment(order);
                
                paymentSuccessCounter.increment();
                log.info("Payment verified successfully for order: {}", order.getRazorpayOrderId());

                paymentOrderRepository.save(order);
                
                return RazorpayPaymentVerifyResponse.builder()
                        .success(true)
                        .orderId(order.getRazorpayOrderId())
                        .paymentId(request.getRazorpayPaymentId())
                        .message("Payment verified successfully")
                        .amountPaid(order.getAmountPaidInRupees())
                        .build();
            } else {
                order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                order.incrementAttempts();
                paymentOrderRepository.save(order);
                paymentFailureCounter.increment();

                return RazorpayPaymentVerifyResponse.builder()
                        .success(false)
                        .orderId(order.getRazorpayOrderId())
                        .paymentId(request.getRazorpayPaymentId())
                        .message("Payment status: " + status)
                        .build();
            }

        } catch (RazorpayException e) {
            log.error("Failed to fetch payment details: {}", e.getMessage());
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }

    /**
     * Initiate refund for a payment
     * 
     * @param request Refund request
     * @return Refund result
     */
    @Retryable(value = RazorpayException.class, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public RazorpayRefundResponse refundPayment(RazorpayRefundRequest request) {
        log.info("Initiating refund for payment: {}", request.getPaymentId());
        
        validateConfiguration();

        // Find order by payment ID
        PaymentOrder order = paymentOrderRepository.findByRazorpayPaymentId(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentOrder", "razorpayPaymentId", request.getPaymentId()));

        if (!order.isRefundable()) {
            throw new IllegalStateException("Order is not refundable. Current status: " + order.getStatus());
        }

        // Calculate refund amount
        long refundAmountPaise;
        if (request.getAmount() != null) {
            refundAmountPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        } else {
            // Full refund
            refundAmountPaise = order.getAmountPaid() - (order.getAmountRefunded() != null ? order.getAmountRefunded() : 0L);
        }

        // Validate refund amount
        long maxRefundable = order.getAmountPaid() - (order.getAmountRefunded() != null ? order.getAmountRefunded() : 0L);
        if (refundAmountPaise > maxRefundable) {
            throw new IllegalArgumentException("Refund amount exceeds maximum refundable amount");
        }

        try {
            // Prepare refund request
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", refundAmountPaise);
            refundRequest.put("speed", request.isInstantRefund() ? "optimum" : "normal");
            
            if (request.getReason() != null) {
                JSONObject notes = new JSONObject();
                notes.put("reason", request.getReason());
                refundRequest.put("notes", notes);
            }

            // Create refund in Razorpay
            Refund refund = razorpayClient.payments.refund(request.getPaymentId(), refundRequest);
            String refundId = refund.get("id");
            String refundStatus = refund.get("status");

            log.info("Refund initiated: {} for payment: {}", refundId, request.getPaymentId());

            // Update order
            order.markAsRefunded(refundId, refundAmountPaise);
            paymentOrderRepository.save(order);
            refundCounter.increment();

            return RazorpayRefundResponse.builder()
                    .success(true)
                    .refundId(refundId)
                    .paymentId(request.getPaymentId())
                    .amount(BigDecimal.valueOf(refundAmountPaise).divide(BigDecimal.valueOf(100)))
                    .status(refundStatus)
                    .message("Refund initiated successfully")
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to initiate refund: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate refund: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch payment details from Razorpay
     */
    public RazorpayPaymentDetailsResponse getPaymentDetails(String paymentId) {
        validateConfiguration();

        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            return mapPaymentToResponse(payment);
        } catch (RazorpayException e) {
            log.error("Failed to fetch payment details: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch payment details: " + e.getMessage(), e);
        }
    }

    /**
     * Get order details from database
     */
    public PaymentOrder getOrder(String razorpayOrderId) {
        return paymentOrderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentOrder", "razorpayOrderId", razorpayOrderId));
    }

    /**
     * Get orders for an invoice
     */
    public List<PaymentOrder> getOrdersForInvoice(Long invoiceId) {
        return paymentOrderRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Get orders for a company
     */
    public List<PaymentOrder> getOrdersForCompany(Long companyId) {
        return paymentOrderRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    /**
     * Get payment statistics for a company
     */
    public Map<String, Object> getPaymentStats(Long companyId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalPaid = paymentOrderRepository.sumAmountPaidByCompany(companyId);
        Long totalRefunded = paymentOrderRepository.sumAmountRefundedByCompany(companyId);
        List<Object[]> statusCounts = paymentOrderRepository.countByStatusForCompany(companyId);

        stats.put("totalPaidAmount", BigDecimal.valueOf(totalPaid != null ? totalPaid : 0L)
                .divide(BigDecimal.valueOf(100)));
        stats.put("totalRefundedAmount", BigDecimal.valueOf(totalRefunded != null ? totalRefunded : 0L)
                .divide(BigDecimal.valueOf(100)));
        stats.put("currency", config.getCurrency());

        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("ordersByStatus", statusMap);

        return stats;
    }

    /**
     * Mark expired orders
     */
    @Transactional
    public int markExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentOrder> expiredOrders = paymentOrderRepository.findExpiredOrders(now);
        
        for (PaymentOrder order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            paymentOrderRepository.save(order);
        }
        
        if (!expiredOrders.isEmpty()) {
            log.info("Marked {} orders as expired", expiredOrders.size());
        }
        
        return expiredOrders.size();
    }

    /**
     * Verify Razorpay signature using HMAC SHA256
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (config.getWebhookSecret() == null || config.getWebhookSecret().isBlank()) {
            // Use key secret if webhook secret not configured
            return verifySignatureInternal(orderId, paymentId, signature, config.getKeySecret());
        }
        return verifySignatureInternal(orderId, paymentId, signature, config.getWebhookSecret());
    }

    private boolean verifySignatureInternal(String orderId, String paymentId, String signature, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = generateHmacSha256(payload, secret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }

    private String generateHmacSha256(String data, String secret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (config.getWebhookSecret() == null || config.getWebhookSecret().isBlank()) {
            log.warn("Webhook secret not configured, skipping signature verification");
            return true;
        }

        try {
            String generatedSignature = generateHmacSha256(payload, config.getWebhookSecret());
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private void validateConfiguration() {
        if (!config.isConfigured()) {
            throw new IllegalStateException(
                    "Razorpay is not configured. Enable and provide credentials in application.yml");
        }
        if (razorpayClient == null) {
            throw new IllegalStateException("Razorpay client not initialized");
        }
    }

    private void updateInvoiceOnPayment(PaymentOrder order) {
        Invoice invoice = invoiceRepository.findById(order.getInvoiceId()).orElse(null);
        if (invoice != null) {
            BigDecimal paidAmount = order.getAmountPaidInRupees();
            invoice.markAsPaid(java.time.LocalDate.now(), paidAmount);
            invoiceRepository.save(invoice);
            log.info("Updated invoice {} status to {}", invoice.getId(), invoice.getStatus());
        }
    }

    private RazorpayOrderResponse buildOrderResponse(PaymentOrder order, Invoice invoice) {
        return RazorpayOrderResponse.builder()
                .orderId(order.getRazorpayOrderId())
                .invoiceId(order.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(order.getAmountInRupees())
                .currency(order.getCurrency())
                .receipt(order.getReceipt())
                .status(order.getStatus().name())
                .keyId(config.getKeyId())
                .companyName(config.getCompanyName())
                .description(config.getOrderDescriptionPrefix() + invoice.getInvoiceNumber())
                .callbackUrl(config.getCallbackUrl())
                .expiresAt(order.getExpiresAt())
                .build();
    }

    private RazorpayPaymentDetailsResponse mapPaymentToResponse(Payment payment) {
        return RazorpayPaymentDetailsResponse.builder()
                .paymentId(payment.get("id"))
                .orderId(payment.get("order_id"))
                .status(payment.get("status"))
                .method(payment.get("method"))
                .amount(BigDecimal.valueOf(Long.parseLong(payment.get("amount").toString()))
                        .divide(BigDecimal.valueOf(100)))
                .currency(payment.get("currency"))
                .bank(payment.has("bank") ? payment.get("bank") : null)
                .wallet(payment.has("wallet") ? payment.get("wallet") : null)
                .vpa(payment.has("vpa") ? payment.get("vpa") : null)
                .email(payment.has("email") ? payment.get("email") : null)
                .contact(payment.has("contact") ? payment.get("contact") : null)
                .fee(payment.has("fee") ? 
                        BigDecimal.valueOf(Long.parseLong(payment.get("fee").toString()))
                                .divide(BigDecimal.valueOf(100)) : null)
                .tax(payment.has("tax") ? 
                        BigDecimal.valueOf(Long.parseLong(payment.get("tax").toString()))
                                .divide(BigDecimal.valueOf(100)) : null)
                .captured(payment.get("captured"))
                .createdAt(Long.parseLong(payment.get("created_at").toString()))
                .build();
    }

    /**
     * Check if Razorpay is configured and ready
     */
    public boolean isConfigured() {
        return config.isConfigured() && razorpayClient != null;
    }

    /**
     * Get configuration status for health check
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", config.isConfigured());
        status.put("enabled", config.isEnabled());
        status.put("testMode", config.isTestMode());
        status.put("keyId", config.getMaskedKeyId());
        status.put("currency", config.getCurrency());
        status.put("clientInitialized", razorpayClient != null);
        return status;
    }
}
