package com.evfleet.billing.controller;

import com.evfleet.billing.config.RazorpayConfig;
import com.evfleet.billing.model.WebhookEvent;
import com.evfleet.billing.model.WebhookEvent.WebhookStatus;
import com.evfleet.billing.repository.WebhookEventRepository;
import com.evfleet.billing.service.InvoicePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentWebhookController
 * 
 * Tests cover:
 * - Signature verification
 * - Idempotency handling
 * - Event dispatching
 * - Error handling
 * - Metrics recording
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentWebhookController Tests")
class PaymentWebhookControllerTest {

    @Mock
    private RazorpayConfig razorpayConfig;

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private InvoicePaymentService invoicePaymentService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private PaymentWebhookController controller;

    private MockHttpServletRequest request;
    
    private static final String TEST_WEBHOOK_SECRET = "test_webhook_secret_12345";
    private static final String TEST_EVENT_ID = "evt_1234567890";
    private static final String TEST_PAYMENT_ID = "pay_1234567890";
    private static final String TEST_ORDER_ID = "order_1234567890";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.addHeader("User-Agent", "RazorpayWebhook/1.0");
        
        // Initialize metrics
        controller.initMetrics();
    }

    // ==================== Signature Verification Tests ====================

    @Nested
    @DisplayName("Signature Verification Tests")
    class SignatureVerificationTests {

        @Test
        @DisplayName("Should accept valid signature")
        void shouldAcceptValidSignature() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(razorpayConfig.isTestMode()).thenReturn(false);
            when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "success");
            
            // Verify webhook event was saved with valid signature
            ArgumentCaptor<WebhookEvent> eventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);
            verify(webhookEventRepository, atLeast(1)).save(eventCaptor.capture());
            WebhookEvent savedEvent = eventCaptor.getAllValues().get(eventCaptor.getAllValues().size() - 1);
            assertThat(savedEvent.getSignatureValid()).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid signature")
        void shouldRejectInvalidSignature() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String invalidSignature = "invalid_signature_12345";
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(razorpayConfig.isTestMode()).thenReturn(false);
            when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, invalidSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).containsEntry("status", "error");
            assertThat(response.getBody()).containsEntry("message", "Invalid signature");
        }

        @Test
        @DisplayName("Should skip signature verification in test mode when secret not configured")
        void shouldSkipVerificationInTestModeWithoutSecret() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(null);
            when(razorpayConfig.isTestMode()).thenReturn(true);
            when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, null, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should reject missing signature when secret is configured and not test mode")
        void shouldRejectMissingSignature() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(razorpayConfig.isTestMode()).thenReturn(false);
            when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, null, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    // ==================== Idempotency Tests ====================

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should detect and handle duplicate completed events")
        void shouldHandleDuplicateCompletedEvent() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            WebhookEvent existingEvent = WebhookEvent.builder()
                    .eventId(TEST_EVENT_ID)
                    .eventType("payment.captured")
                    .status(WebhookStatus.COMPLETED)
                    .build();
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(webhookEventRepository.findByEventIdAndSource(TEST_EVENT_ID, "RAZORPAY"))
                    .thenReturn(Optional.of(existingEvent));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("message", "Event already processed");
            
            // Should not create a new event
            verify(webhookEventRepository, never()).save(any(WebhookEvent.class));
        }

        @Test
        @DisplayName("Should detect and handle duplicate processing events")
        void shouldHandleDuplicateProcessingEvent() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            WebhookEvent existingEvent = WebhookEvent.builder()
                    .eventId(TEST_EVENT_ID)
                    .eventType("payment.captured")
                    .status(WebhookStatus.PROCESSING)
                    .build();
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(webhookEventRepository.findByEventIdAndSource(TEST_EVENT_ID, "RAZORPAY"))
                    .thenReturn(Optional.of(existingEvent));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("message", "Event is currently being processed");
        }

        @Test
        @DisplayName("Should process new event and save to repository")
        void shouldProcessNewEvent() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(razorpayConfig.isTestMode()).thenReturn(false);
            when(webhookEventRepository.findByEventIdAndSource(TEST_EVENT_ID, "RAZORPAY"))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            // Verify event was saved multiple times (received, processing, completed)
            verify(webhookEventRepository, atLeast(3)).save(any(WebhookEvent.class));
        }
    }

    // ==================== Event Dispatching Tests ====================

    @Nested
    @DisplayName("Event Dispatching Tests")
    class EventDispatchingTests {

        @Test
        @DisplayName("Should dispatch payment.captured to InvoicePaymentService")
        void shouldDispatchPaymentCaptured() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("eventType", "payment.captured");
            verify(invoicePaymentService).handlePaymentSuccess(any());
        }

        @Test
        @DisplayName("Should dispatch payment.failed to InvoicePaymentService")
        void shouldDispatchPaymentFailed() throws Exception {
            // Arrange
            String payload = createPaymentFailedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("eventType", "payment.failed");
            verify(invoicePaymentService).handlePaymentFailure(any());
        }

        @Test
        @DisplayName("Should handle unknown event type gracefully")
        void shouldHandleUnknownEventType() throws Exception {
            // Arrange
            String payload = createUnknownEventPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("eventType", "custom.event");
            
            // Should not call any payment service methods
            verifyNoInteractions(invoicePaymentService);
        }

        @Test
        @DisplayName("Should handle refund.processed event")
        void shouldHandleRefundEvent() throws Exception {
            // Arrange
            String payload = createRefundProcessedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("eventType", "refund.processed");
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return error for invalid payload")
        void shouldReturnErrorForInvalidPayload() {
            // Arrange
            String invalidPayload = "not json";

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    invalidPayload, null, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("message", "Invalid payload format");
        }

        @Test
        @DisplayName("Should return error for missing event ID")
        void shouldReturnErrorForMissingEventId() {
            // Arrange
            String payload = "{\"event\": \"payment.captured\"}";

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, null, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("message", "Missing event ID");
        }

        @Test
        @DisplayName("Should accept event even if processing fails")
        void shouldAcceptEventEvenIfProcessingFails() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
            when(razorpayConfig.isTestMode()).thenReturn(false);
            when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(webhookEventRepository.save(any(WebhookEvent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            doThrow(new RuntimeException("Processing failed"))
                    .when(invoicePaymentService).handlePaymentSuccess(any());

            // Act
            ResponseEntity<Map<String, Object>> response = controller.handleRazorpayWebhook(
                    payload, validSignature, request);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "accepted");
        }
    }

    // ==================== Health Check Tests ====================

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return healthy when webhook configured")
        void shouldReturnHealthyWhenConfigured() {
            // Arrange
            when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);

            // Act
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("status", "healthy");
            assertThat(response.getBody()).containsEntry("webhookConfigured", true);
        }

        @Test
        @DisplayName("Should indicate when webhook not configured")
        void shouldIndicateWebhookNotConfigured() {
            // Arrange
            when(razorpayConfig.getWebhookSecret()).thenReturn(null);

            // Act
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("webhookConfigured", false);
        }
    }

    // ==================== Metadata Extraction Tests ====================

    @Nested
    @DisplayName("Metadata Extraction Tests")
    class MetadataExtractionTests {

        @Test
        @DisplayName("Should extract source IP from X-Forwarded-For header")
        void shouldExtractSourceIpFromForwardedHeader() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1, 172.16.0.1");
            
            setupSuccessfulWebhookScenario();

            // Act
            controller.handleRazorpayWebhook(payload, validSignature, request);

            // Assert
            ArgumentCaptor<WebhookEvent> eventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);
            verify(webhookEventRepository, atLeast(1)).save(eventCaptor.capture());
            
            WebhookEvent firstSavedEvent = eventCaptor.getAllValues().get(0);
            assertThat(firstSavedEvent.getSourceIp()).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("Should capture user agent")
        void shouldCaptureUserAgent() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            controller.handleRazorpayWebhook(payload, validSignature, request);

            // Assert
            ArgumentCaptor<WebhookEvent> eventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);
            verify(webhookEventRepository, atLeast(1)).save(eventCaptor.capture());
            
            WebhookEvent firstSavedEvent = eventCaptor.getAllValues().get(0);
            assertThat(firstSavedEvent.getUserAgent()).isEqualTo("RazorpayWebhook/1.0");
        }

        @Test
        @DisplayName("Should extract payment and order IDs from payload")
        void shouldExtractPaymentAndOrderIds() throws Exception {
            // Arrange
            String payload = createPaymentCapturedPayload();
            String validSignature = calculateSignature(payload, TEST_WEBHOOK_SECRET);
            
            setupSuccessfulWebhookScenario();

            // Act
            controller.handleRazorpayWebhook(payload, validSignature, request);

            // Assert
            ArgumentCaptor<WebhookEvent> eventCaptor = ArgumentCaptor.forClass(WebhookEvent.class);
            verify(webhookEventRepository, atLeast(1)).save(eventCaptor.capture());
            
            WebhookEvent firstSavedEvent = eventCaptor.getAllValues().get(0);
            assertThat(firstSavedEvent.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
            assertThat(firstSavedEvent.getOrderId()).isEqualTo(TEST_ORDER_ID);
        }
    }

    // ==================== Helper Methods ====================

    private void setupSuccessfulWebhookScenario() {
        when(razorpayConfig.getWebhookSecret()).thenReturn(TEST_WEBHOOK_SECRET);
        when(razorpayConfig.isTestMode()).thenReturn(false);
        when(webhookEventRepository.findByEventIdAndSource(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(WebhookEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private String createPaymentCapturedPayload() {
        return """
            {
                "id": "%s",
                "event": "payment.captured",
                "payload": {
                    "payment": {
                        "entity": {
                            "id": "%s",
                            "order_id": "%s",
                            "amount": 50000,
                            "currency": "INR",
                            "status": "captured",
                            "method": "card"
                        }
                    }
                },
                "created_at": 1609459200
            }
            """.formatted(TEST_EVENT_ID, TEST_PAYMENT_ID, TEST_ORDER_ID);
    }

    private String createPaymentFailedPayload() {
        return """
            {
                "id": "%s",
                "event": "payment.failed",
                "payload": {
                    "payment": {
                        "entity": {
                            "id": "%s",
                            "order_id": "%s",
                            "amount": 50000,
                            "currency": "INR",
                            "status": "failed",
                            "error_code": "BAD_REQUEST_ERROR",
                            "error_description": "Card declined",
                            "error_reason": "insufficient_funds"
                        }
                    }
                },
                "created_at": 1609459200
            }
            """.formatted(TEST_EVENT_ID, TEST_PAYMENT_ID, TEST_ORDER_ID);
    }

    private String createRefundProcessedPayload() {
        return """
            {
                "id": "%s",
                "event": "refund.processed",
                "payload": {
                    "refund": {
                        "entity": {
                            "id": "rfnd_1234567890",
                            "payment_id": "%s",
                            "amount": 50000,
                            "status": "processed"
                        }
                    }
                },
                "created_at": 1609459200
            }
            """.formatted(TEST_EVENT_ID, TEST_PAYMENT_ID);
    }

    private String createUnknownEventPayload() {
        return """
            {
                "id": "%s",
                "event": "custom.event",
                "payload": {
                    "data": {
                        "id": "custom_123"
                    }
                },
                "created_at": 1609459200
            }
            """.formatted(TEST_EVENT_ID);
    }

    private String calculateSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
