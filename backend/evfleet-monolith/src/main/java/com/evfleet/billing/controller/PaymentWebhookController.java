package com.evfleet.billing.controller;

import com.evfleet.billing.config.RazorpayConfig;
import com.evfleet.billing.dto.PaymentFailureRequest;
import com.evfleet.billing.dto.PaymentSuccessRequest;
import com.evfleet.billing.model.WebhookEvent;
import com.evfleet.billing.model.WebhookEvent.WebhookStatus;
import com.evfleet.billing.repository.WebhookEventRepository;
import com.evfleet.billing.service.InvoicePaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Payment Webhook Controller
 * 
 * Handles webhook notifications from Razorpay for payment events.
 * 
 * Features:
 * - Signature verification (HMAC-SHA256)
 * - Idempotent event processing
 * - Event logging and audit trail
 * - Retry handling for failed events
 * - Metrics for monitoring
 * 
 * Supported Events:
 * - payment.captured: Payment was successfully captured
 * - payment.failed: Payment failed
 * - refund.processed: Refund was processed
 * - order.paid: Order was fully paid
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Payment gateway webhook endpoints")
public class PaymentWebhookController {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String RAZORPAY_SIGNATURE_HEADER = "X-Razorpay-Signature";
    private static final String SOURCE_RAZORPAY = "RAZORPAY";
    
    // Supported event types
    private static final String EVENT_PAYMENT_CAPTURED = "payment.captured";
    private static final String EVENT_PAYMENT_FAILED = "payment.failed";
    private static final String EVENT_REFUND_PROCESSED = "refund.processed";
    private static final String EVENT_ORDER_PAID = "order.paid";

    private final RazorpayConfig razorpayConfig;
    private final WebhookEventRepository webhookEventRepository;
    private final InvoicePaymentService invoicePaymentService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private Counter webhookReceivedCounter;
    private Counter webhookProcessedCounter;
    private Counter webhookFailedCounter;
    private Counter webhookDuplicateCounter;
    private Counter signatureInvalidCounter;

    @PostConstruct
    public void initMetrics() {
        webhookReceivedCounter = Counter.builder("webhook.received")
                .description("Number of webhooks received")
                .tag("source", SOURCE_RAZORPAY)
                .register(meterRegistry);
        webhookProcessedCounter = Counter.builder("webhook.processed")
                .description("Number of webhooks successfully processed")
                .tag("source", SOURCE_RAZORPAY)
                .register(meterRegistry);
        webhookFailedCounter = Counter.builder("webhook.failed")
                .description("Number of webhooks that failed processing")
                .tag("source", SOURCE_RAZORPAY)
                .register(meterRegistry);
        webhookDuplicateCounter = Counter.builder("webhook.duplicate")
                .description("Number of duplicate webhooks received")
                .tag("source", SOURCE_RAZORPAY)
                .register(meterRegistry);
        signatureInvalidCounter = Counter.builder("webhook.signature.invalid")
                .description("Number of webhooks with invalid signatures")
                .tag("source", SOURCE_RAZORPAY)
                .register(meterRegistry);
    }

    /**
     * Razorpay webhook endpoint
     * 
     * Receives and processes webhook events from Razorpay.
     * This endpoint should be registered in Razorpay Dashboard.
     * 
     * @param payload Raw JSON payload from Razorpay
     * @param signature X-Razorpay-Signature header for verification
     * @param request HTTP request for metadata extraction
     * @return Response indicating success or failure
     */
    @PostMapping("/razorpay")
    @Operation(summary = "Razorpay webhook endpoint", 
               description = "Receives payment events from Razorpay. This endpoint is called by Razorpay servers.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or missing event ID"),
        @ApiResponse(responseCode = "401", description = "Invalid signature"),
        @ApiResponse(responseCode = "409", description = "Duplicate event (already processed)")
    })
    public ResponseEntity<Map<String, Object>> handleRazorpayWebhook(
            @RequestBody String payload,
            @Parameter(description = "Razorpay signature for verification", hidden = true)
            @RequestHeader(value = RAZORPAY_SIGNATURE_HEADER, required = false) String signature,
            HttpServletRequest request) {
        
        log.info("Received Razorpay webhook");
        webhookReceivedCounter.increment();

        // Extract metadata
        String sourceIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Parse payload to extract event details
        JsonNode payloadNode;
        String eventId;
        String eventType;
        
        try {
            payloadNode = objectMapper.readTree(payload);
            eventId = extractEventId(payloadNode);
            eventType = extractEventType(payloadNode);
        } catch (Exception e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Invalid payload format"));
        }

        if (eventId == null || eventId.isBlank()) {
            log.warn("Webhook received without event ID");
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Missing event ID"));
        }

        // Check for duplicate (idempotency)
        Optional<WebhookEvent> existingEvent = webhookEventRepository.findByEventIdAndSource(eventId, SOURCE_RAZORPAY);
        if (existingEvent.isPresent()) {
            WebhookEvent existing = existingEvent.get();
            log.info("Duplicate webhook received. Event ID: {}, Original status: {}", eventId, existing.getStatus());
            webhookDuplicateCounter.increment();
            
            // If already processed successfully, return success
            if (existing.getStatus() == WebhookStatus.PROCESSED) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Event already processed",
                        "eventId", eventId
                ));
            }
            
            // If still processing, return conflict
            if (existing.getStatus() == WebhookStatus.PROCESSING) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "status", "processing",
                                "message", "Event is currently being processed",
                                "eventId", eventId
                        ));
            }
            
            // Mark as duplicate if it was a failed event (will be retried)
            existing.setStatus(WebhookStatus.DUPLICATE);
            webhookEventRepository.save(existing);
        }

        // Create webhook event record
        WebhookEvent webhookEvent = createWebhookEvent(eventId, eventType, payload, signature, sourceIp, userAgent, payloadNode);
        webhookEventRepository.save(webhookEvent);

        // Verify signature
        boolean signatureValid = verifySignature(payload, signature);
        webhookEvent.setSignatureValid(signatureValid);
        
        if (!signatureValid) {
            log.warn("Invalid webhook signature for event: {}", eventId);
            signatureInvalidCounter.increment();
            webhookEvent.markFailed("Signature verification failed");
            webhookEventRepository.save(webhookEvent);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid signature"));
        }

        // Process the event
        try {
            webhookEvent.startProcessing();
            webhookEventRepository.save(webhookEvent);

            processEvent(eventType, payloadNode);

            webhookEvent.markProcessed();
            webhookEventRepository.save(webhookEvent);
            webhookProcessedCounter.increment();

            log.info("Webhook processed successfully. Event ID: {}, Type: {}", eventId, eventType);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Event processed successfully",
                    "eventId", eventId,
                    "eventType", eventType
            ));

        } catch (Exception e) {
            log.error("Failed to process webhook event {}: {}", eventId, e.getMessage(), e);
            webhookEvent.markFailed(e.getMessage());
            webhookEventRepository.save(webhookEvent);
            webhookFailedCounter.increment();

            // Return 200 OK even on processing failure to prevent Razorpay retries
            // (we'll handle retries internally)
            return ResponseEntity.ok(Map.of(
                    "status", "accepted",
                    "message", "Event accepted for processing",
                    "eventId", eventId
            ));
        }
    }

    /**
     * Health check endpoint for webhook service
     */
    @GetMapping("/razorpay/health")
    @Operation(summary = "Webhook health check", description = "Check if webhook endpoint is operational")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean configured = razorpayConfig.getWebhookSecret() != null 
                && !razorpayConfig.getWebhookSecret().isBlank();
        
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "webhookConfigured", configured,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Verify the webhook signature using HMAC-SHA256
     * 
     * @param payload The raw request body
     * @param signature The X-Razorpay-Signature header value
     * @return true if signature is valid, false otherwise
     */
    private boolean verifySignature(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            log.warn("No signature provided in webhook request");
            return false;
        }

        String webhookSecret = razorpayConfig.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Webhook secret not configured - skipping signature verification");
            // In development, allow without signature if secret is not configured
            return razorpayConfig.isTestMode();
        }

        try {
            String expectedSignature = calculateHmacSha256(payload, webhookSecret);
            boolean valid = signature.equals(expectedSignature);
            
            if (!valid) {
                log.debug("Signature mismatch. Expected: {}, Received: {}", 
                         maskSignature(expectedSignature), maskSignature(signature));
            }
            
            return valid;
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate HMAC-SHA256 signature
     */
    private String calculateHmacSha256(String data, String secret) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    /**
     * Convert bytes to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Mask signature for logging
     */
    private String maskSignature(String signature) {
        if (signature == null || signature.length() < 10) {
            return "***";
        }
        return signature.substring(0, 5) + "..." + signature.substring(signature.length() - 5);
    }

    /**
     * Extract event ID from payload
     */
    private String extractEventId(JsonNode payload) {
        // Razorpay provides event ID in the "id" field
        JsonNode idNode = payload.get("id");
        if (idNode != null && !idNode.isNull()) {
            return idNode.asText();
        }
        
        // Fallback: Check in payload.entity for older webhook formats
        JsonNode entityNode = payload.path("payload").path("entity");
        if (!entityNode.isMissingNode() && entityNode.has("id")) {
            return entityNode.get("id").asText();
        }
        
        return null;
    }

    /**
     * Extract event type from payload
     */
    private String extractEventType(JsonNode payload) {
        JsonNode eventNode = payload.get("event");
        return eventNode != null ? eventNode.asText() : "unknown";
    }

    /**
     * Extract payment ID from payload
     */
    private String extractPaymentId(JsonNode payload) {
        // payment.captured: payload.payment.entity.id
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");
        if (!paymentEntity.isMissingNode() && paymentEntity.has("id")) {
            return paymentEntity.get("id").asText();
        }
        return null;
    }

    /**
     * Extract order ID from payload
     */
    private String extractOrderId(JsonNode payload) {
        // payment.captured: payload.payment.entity.order_id
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");
        if (!paymentEntity.isMissingNode() && paymentEntity.has("order_id")) {
            return paymentEntity.get("order_id").asText();
        }
        
        // order.paid: payload.order.entity.id
        JsonNode orderEntity = payload.path("payload").path("order").path("entity");
        if (!orderEntity.isMissingNode() && orderEntity.has("id")) {
            return orderEntity.get("id").asText();
        }
        
        return null;
    }

    /**
     * Extract client IP address
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Create a WebhookEvent record
     */
    private WebhookEvent createWebhookEvent(String eventId, String eventType, String payload,
                                             String signature, String sourceIp, String userAgent,
                                             JsonNode payloadNode) {
        return WebhookEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .source(SOURCE_RAZORPAY)
                .payload(payload)
                .signature(signature)
                .signatureValid(false) // Will be updated after verification
                .paymentId(extractPaymentId(payloadNode))
                .orderId(extractOrderId(payloadNode))
                .status(WebhookStatus.RECEIVED)
                .sourceIp(sourceIp)
                .userAgent(userAgent)
                .attempts(0)
                .build();
    }

    /**
     * Process the webhook event based on event type
     */
    private void processEvent(String eventType, JsonNode payload) {
        log.debug("Processing event type: {}", eventType);

        switch (eventType) {
            case EVENT_PAYMENT_CAPTURED -> processPaymentCaptured(payload);
            case EVENT_PAYMENT_FAILED -> processPaymentFailed(payload);
            case EVENT_REFUND_PROCESSED -> processRefundProcessed(payload);
            case EVENT_ORDER_PAID -> processOrderPaid(payload);
            default -> log.info("Unhandled event type: {}. Acknowledging without processing.", eventType);
        }
    }

    /**
     * Process payment.captured event
     */
    private void processPaymentCaptured(JsonNode payload) {
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");
        
        if (paymentEntity.isMissingNode()) {
            log.warn("payment.captured event missing payment entity");
            return;
        }

        String paymentId = paymentEntity.path("id").asText();
        String orderId = paymentEntity.path("order_id").asText();
        
        log.info("Processing payment captured. Payment: {}, Order: {}", paymentId, orderId);

        // Build success request from webhook data
        // Note: Webhook doesn't provide signature for payment verification,
        // as the payment has already been verified by Razorpay
        PaymentSuccessRequest request = PaymentSuccessRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                // Signature is not provided in webhooks; we skip verification since
                // the webhook itself is already verified via signature header
                .razorpaySignature("webhook-verified")
                .build();

        // Delegate to invoice payment service
        // Note: This may throw if order not found or already processed
        try {
            invoicePaymentService.handlePaymentSuccess(request);
        } catch (Exception e) {
            log.warn("Payment already processed or not found: {}", e.getMessage());
            // This is acceptable - the payment may have been processed via client callback
        }
    }

    /**
     * Process payment.failed event
     */
    private void processPaymentFailed(JsonNode payload) {
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");
        
        if (paymentEntity.isMissingNode()) {
            log.warn("payment.failed event missing payment entity");
            return;
        }

        String paymentId = paymentEntity.path("id").asText();
        String orderId = paymentEntity.path("order_id").asText();
        String errorCode = paymentEntity.path("error_code").asText(null);
        String errorDescription = paymentEntity.path("error_description").asText("Payment failed");
        String errorReason = paymentEntity.path("error_reason").asText(null);
        
        log.info("Processing payment failed. Payment: {}, Order: {}, Error: {}", 
                paymentId, orderId, errorCode);

        PaymentFailureRequest request = PaymentFailureRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .errorCode(errorCode)
                .errorDescription(errorDescription)
                .errorReason(errorReason)
                .build();

        try {
            invoicePaymentService.handlePaymentFailure(request);
        } catch (Exception e) {
            log.warn("Failed to process payment failure event: {}", e.getMessage());
        }
    }

    /**
     * Process refund.processed event
     */
    private void processRefundProcessed(JsonNode payload) {
        JsonNode refundEntity = payload.path("payload").path("refund").path("entity");
        
        if (refundEntity.isMissingNode()) {
            log.warn("refund.processed event missing refund entity");
            return;
        }

        String refundId = refundEntity.path("id").asText();
        String paymentId = refundEntity.path("payment_id").asText();
        long amountPaise = refundEntity.path("amount").asLong(0);
        
        log.info("Refund processed. Refund: {}, Payment: {}, Amount: {} paise", 
                refundId, paymentId, amountPaise);

        // TODO: Implement refund handling when refund service is available
        // For now, just log the event
        log.info("Refund event logged. Implementation pending for refund service integration.");
    }

    /**
     * Process order.paid event
     */
    private void processOrderPaid(JsonNode payload) {
        JsonNode orderEntity = payload.path("payload").path("order").path("entity");
        
        if (orderEntity.isMissingNode()) {
            log.warn("order.paid event missing order entity");
            return;
        }

        String orderId = orderEntity.path("id").asText();
        long amountPaid = orderEntity.path("amount_paid").asLong(0);
        
        log.info("Order paid. Order: {}, Amount: {} paise", orderId, amountPaid);

        // order.paid is typically sent along with payment.captured
        // The payment.captured handler should have already processed this
        log.debug("Order.paid event acknowledged. Processing delegated to payment.captured handler.");
    }
}
