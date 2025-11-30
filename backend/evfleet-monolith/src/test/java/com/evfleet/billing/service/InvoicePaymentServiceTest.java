package com.evfleet.billing.service;

import com.evfleet.billing.dto.*;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.Invoice.InvoiceStatus;
import com.evfleet.billing.model.Payment;
import com.evfleet.billing.model.Payment.PaymentStatus;
import com.evfleet.billing.model.PaymentOrder;
import com.evfleet.billing.model.PaymentOrder.OrderStatus;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.PaymentOrderRepository;
import com.evfleet.billing.repository.PaymentRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoicePaymentService
 * 
 * Tests cover:
 * - Payment initiation
 * - Payment success handling
 * - Payment failure handling
 * - Partial payments
 * - Payment status retrieval
 * - Payment history
 * - Retry payments
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class InvoicePaymentServiceTest {

    @Mock
    private RazorpayPaymentService razorpayPaymentService;

    @Mock
    private PaymentEmailService paymentEmailService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private InvoicePaymentService invoicePaymentService;

    private Invoice testInvoice;
    private PaymentOrder testPaymentOrder;

    @BeforeEach
    void setUp() {
        // Initialize metrics
        invoicePaymentService.initMetrics();

        // Create test invoice
        testInvoice = Invoice.builder()
                .id(1L)
                .companyId(100L)
                .invoiceNumber("INV-2025-001")
                .totalAmount(BigDecimal.valueOf(5000.00))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.PENDING)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        // Create test payment order
        testPaymentOrder = PaymentOrder.builder()
                .id(1L)
                .razorpayOrderId("order_ABC123")
                .invoiceId(1L)
                .companyId(100L)
                .amount(500000L) // 5000.00 in paise
                .amountPaid(500000L)
                .currency("INR")
                .status(OrderStatus.PAID)
                .customerEmail("test@example.com")
                .razorpayPaymentId("pay_XYZ789")
                .paymentMethod("upi")
                .paidAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Payment Initiation Tests")
    class PaymentInitiationTests {

        @Test
        @DisplayName("Should initiate payment for pending invoice")
        void shouldInitiatePaymentForPendingInvoice() {
            // Given
            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .customerEmail("customer@example.com")
                    .customerPhone("+919876543210")
                    .build();

            RazorpayOrderResponse orderResponse = RazorpayOrderResponse.builder()
                    .orderId("order_NEW123")
                    .keyId("rzp_test_xxx")
                    .amount(BigDecimal.valueOf(5000.00))
                    .currency("INR")
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build();

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(razorpayPaymentService.createOrder(any())).thenReturn(orderResponse);

            // When
            PaymentInitiationResponse response = invoicePaymentService.initiatePayment(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getInvoiceId()).isEqualTo(1L);
            assertThat(response.getOrderId()).isEqualTo("order_NEW123");
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
            assertThat(response.isPartialPayment()).isFalse();

            verify(razorpayPaymentService).createOrder(argThat(req -> 
                req.getInvoiceId().equals(1L) &&
                req.getCustomerEmail().equals("customer@example.com")
            ));
        }

        @Test
        @DisplayName("Should initiate partial payment")
        void shouldInitiatePartialPayment() {
            // Given
            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .amount(BigDecimal.valueOf(2000.00)) // Partial payment
                    .build();

            RazorpayOrderResponse orderResponse = RazorpayOrderResponse.builder()
                    .orderId("order_PARTIAL123")
                    .keyId("rzp_test_xxx")
                    .amount(BigDecimal.valueOf(2000.00))
                    .currency("INR")
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build();

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(razorpayPaymentService.createOrder(any())).thenReturn(orderResponse);

            // When
            PaymentInitiationResponse response = invoicePaymentService.initiatePayment(request);

            // Then
            assertThat(response.isPartialPayment()).isTrue();
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        }

        @Test
        @DisplayName("Should throw exception for already paid invoice")
        void shouldThrowExceptionForAlreadyPaidInvoice() {
            // Given
            testInvoice.setStatus(InvoiceStatus.PAID);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .build();

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.initiatePayment(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already fully paid");
        }

        @Test
        @DisplayName("Should throw exception for cancelled invoice")
        void shouldThrowExceptionForCancelledInvoice() {
            // Given
            testInvoice.setStatus(InvoiceStatus.CANCELLED);
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .build();

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.initiatePayment(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cancelled invoice");
        }

        @Test
        @DisplayName("Should throw exception when amount exceeds remaining")
        void shouldThrowExceptionWhenAmountExceedsRemaining() {
            // Given
            testInvoice.setPaidAmount(BigDecimal.valueOf(4000.00)); // Already paid 4000
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .amount(BigDecimal.valueOf(2000.00)) // Trying to pay 2000 but only 1000 remaining
                    .build();

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.initiatePayment(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds remaining amount");
        }

        @Test
        @DisplayName("Should throw exception for non-existent invoice")
        void shouldThrowExceptionForNonExistentInvoice() {
            // Given
            when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(999L)
                    .build();

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.initiatePayment(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Payment Success Handling Tests")
    class PaymentSuccessTests {

        @Test
        @DisplayName("Should process successful full payment")
        void shouldProcessSuccessfulFullPayment() {
            // Given
            PaymentSuccessRequest request = PaymentSuccessRequest.builder()
                    .razorpayOrderId("order_ABC123")
                    .razorpayPaymentId("pay_XYZ789")
                    .razorpaySignature("valid_signature")
                    .build();

            RazorpayPaymentVerifyResponse verifyResponse = RazorpayPaymentVerifyResponse.builder()
                    .success(true)
                    .orderId("order_ABC123")
                    .paymentId("pay_XYZ789")
                    .message("Payment verified successfully")
                    .build();

            when(razorpayPaymentService.verifyPayment(any())).thenReturn(verifyResponse);
            when(paymentOrderRepository.findByRazorpayOrderId("order_ABC123"))
                    .thenReturn(Optional.of(testPaymentOrder));
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
                Payment p = i.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            PaymentReceiptResponse receipt = invoicePaymentService.handlePaymentSuccess(request);

            // Then
            assertThat(receipt).isNotNull();
            assertThat(receipt.getInvoiceId()).isEqualTo(1L);
            assertThat(receipt.getTransactionId()).isEqualTo("pay_XYZ789");
            assertThat(receipt.getReceiptNumber()).isNotNull();
            assertThat(receipt.getAmountPaid()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));

            // Verify invoice was updated
            verify(invoiceRepository).save(argThat(inv -> 
                inv.getStatus() == InvoiceStatus.PAID
            ));

            // Verify notification was sent
            verify(notificationRepository).save(any());

            // Verify email was sent
            verify(paymentEmailService).sendPaymentSuccessEmail(eq("test@example.com"), any());
        }

        @Test
        @DisplayName("Should process successful partial payment")
        void shouldProcessSuccessfulPartialPayment() {
            // Given
            testPaymentOrder.setAmount(200000L); // 2000 in paise
            testPaymentOrder.setAmountPaid(200000L);

            PaymentSuccessRequest request = PaymentSuccessRequest.builder()
                    .razorpayOrderId("order_ABC123")
                    .razorpayPaymentId("pay_XYZ789")
                    .razorpaySignature("valid_signature")
                    .build();

            RazorpayPaymentVerifyResponse verifyResponse = RazorpayPaymentVerifyResponse.builder()
                    .success(true)
                    .build();

            when(razorpayPaymentService.verifyPayment(any())).thenReturn(verifyResponse);
            when(paymentOrderRepository.findByRazorpayOrderId("order_ABC123"))
                    .thenReturn(Optional.of(testPaymentOrder));
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
                Payment p = i.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            PaymentReceiptResponse receipt = invoicePaymentService.handlePaymentSuccess(request);

            // Then
            assertThat(receipt).isNotNull();
            assertThat(receipt.getAmountPaid()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
            assertThat(receipt.isFullyPaid()).isFalse();

            // Verify invoice updated with partial payment
            ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(invoiceCaptor.capture());
            assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        }

        @Test
        @DisplayName("Should throw exception when payment verification fails")
        void shouldThrowExceptionWhenVerificationFails() {
            // Given
            PaymentSuccessRequest request = PaymentSuccessRequest.builder()
                    .razorpayOrderId("order_ABC123")
                    .razorpayPaymentId("pay_XYZ789")
                    .razorpaySignature("invalid_signature")
                    .build();

            RazorpayPaymentVerifyResponse verifyResponse = RazorpayPaymentVerifyResponse.builder()
                    .success(false)
                    .message("Signature verification failed")
                    .build();

            when(razorpayPaymentService.verifyPayment(any())).thenReturn(verifyResponse);

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.handlePaymentSuccess(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("verification failed");
        }
    }

    @Nested
    @DisplayName("Payment Failure Handling Tests")
    class PaymentFailureTests {

        @Test
        @DisplayName("Should handle payment failure correctly")
        void shouldHandlePaymentFailure() {
            // Given
            testPaymentOrder.setStatus(OrderStatus.CREATED);

            PaymentFailureRequest request = PaymentFailureRequest.builder()
                    .razorpayOrderId("order_ABC123")
                    .errorCode("PAYMENT_FAILED")
                    .errorDescription("Insufficient funds")
                    .errorReason("Bank declined the transaction")
                    .build();

            when(paymentOrderRepository.findByRazorpayOrderId("order_ABC123"))
                    .thenReturn(Optional.of(testPaymentOrder));
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(paymentOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            invoicePaymentService.handlePaymentFailure(request);

            // Then
            // Verify order was marked as failed
            verify(paymentOrderRepository).save(argThat(order ->
                order.getStatus() == OrderStatus.FAILED &&
                order.getErrorCode().equals("PAYMENT_FAILED")
            ));

            // Verify failed payment record was created
            verify(paymentRepository).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.FAILED &&
                payment.getFailureReason().contains("PAYMENT_FAILED")
            ));

            // Verify notification was sent
            verify(notificationRepository).save(any());

            // Verify email was sent
            verify(paymentEmailService).sendPaymentFailureEmail(
                    eq("test@example.com"),
                    eq("INV-2025-001"),
                    any(BigDecimal.class),
                    anyString()
            );
        }

        @Test
        @DisplayName("Should handle failure with missing error details")
        void shouldHandleFailureWithMissingErrorDetails() {
            // Given
            testPaymentOrder.setStatus(OrderStatus.CREATED);

            PaymentFailureRequest request = PaymentFailureRequest.builder()
                    .razorpayOrderId("order_ABC123")
                    // Missing error details
                    .build();

            when(paymentOrderRepository.findByRazorpayOrderId("order_ABC123"))
                    .thenReturn(Optional.of(testPaymentOrder));
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(paymentOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            invoicePaymentService.handlePaymentFailure(request);

            // Then
            verify(paymentOrderRepository).save(argThat(order ->
                order.getErrorCode().equals("UNKNOWN") &&
                order.getErrorDescription().equals("Payment failed")
            ));
        }
    }

    @Nested
    @DisplayName("Payment Status Tests")
    class PaymentStatusTests {

        @Test
        @DisplayName("Should get payment status for invoice")
        void shouldGetPaymentStatus() {
            // Given
            testInvoice.setPaidAmount(BigDecimal.valueOf(2000.00));
            testInvoice.setStatus(InvoiceStatus.PARTIALLY_PAID);

            Payment completedPayment = Payment.builder()
                    .id(1L)
                    .invoiceId(1L)
                    .amount(BigDecimal.valueOf(2000.00))
                    .status(PaymentStatus.COMPLETED)
                    .build();

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.findByInvoiceId(1L)).thenReturn(List.of(completedPayment));
            when(paymentOrderRepository.findByInvoiceIdAndStatus(1L, OrderStatus.CREATED))
                    .thenReturn(Optional.empty());

            // When
            InvoicePaymentStatusResponse status = invoicePaymentService.getPaymentStatus(1L);

            // Then
            assertThat(status).isNotNull();
            assertThat(status.getInvoiceId()).isEqualTo(1L);
            assertThat(status.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
            assertThat(status.getRemainingAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000.00));
            assertThat(status.isFullyPaid()).isFalse();
            assertThat(status.getSuccessfulPayments()).isEqualTo(1);
            assertThat(status.isHasPendingOrder()).isFalse();
        }

        @Test
        @DisplayName("Should indicate pending order when exists")
        void shouldIndicatePendingOrder() {
            // Given
            PaymentOrder pendingOrder = PaymentOrder.builder()
                    .id(2L)
                    .razorpayOrderId("order_PENDING")
                    .status(OrderStatus.CREATED)
                    .build();

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(paymentRepository.findByInvoiceId(1L)).thenReturn(List.of());
            when(paymentOrderRepository.findByInvoiceIdAndStatus(1L, OrderStatus.CREATED))
                    .thenReturn(Optional.of(pendingOrder));

            // When
            InvoicePaymentStatusResponse status = invoicePaymentService.getPaymentStatus(1L);

            // Then
            assertThat(status.isHasPendingOrder()).isTrue();
        }
    }

    @Nested
    @DisplayName("Payment History Tests")
    class PaymentHistoryTests {

        @Test
        @DisplayName("Should get payment history for invoice")
        void shouldGetPaymentHistory() {
            // Given
            Payment payment1 = Payment.builder()
                    .id(1L)
                    .invoiceId(1L)
                    .amount(BigDecimal.valueOf(2000.00))
                    .transactionId("txn_001")
                    .status(PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .build();

            Payment payment2 = Payment.builder()
                    .id(2L)
                    .invoiceId(1L)
                    .amount(BigDecimal.valueOf(1000.00))
                    .status(PaymentStatus.FAILED)
                    .failureReason("Card declined")
                    .paymentDate(LocalDateTime.now().minusDays(2))
                    .build();

            when(invoiceRepository.existsById(1L)).thenReturn(true);
            when(paymentRepository.findByInvoiceId(1L)).thenReturn(List.of(payment1, payment2));

            // When
            List<InvoicePaymentHistoryResponse> history = invoicePaymentService.getPaymentHistory(1L);

            // Then
            assertThat(history).hasSize(2);
            assertThat(history.get(0).getPaymentId()).isEqualTo(1L);
            assertThat(history.get(0).isSuccessful()).isTrue();
            assertThat(history.get(1).isFailed()).isTrue();
            assertThat(history.get(1).getFailureReason()).isEqualTo("Card declined");
        }

        @Test
        @DisplayName("Should throw exception for non-existent invoice")
        void shouldThrowExceptionForNonExistentInvoice() {
            // Given
            when(invoiceRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> invoicePaymentService.getPaymentHistory(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Retry Payment Tests")
    class RetryPaymentTests {

        @Test
        @DisplayName("Should expire existing order and create new one")
        void shouldExpireExistingOrderAndCreateNew() {
            // Given
            PaymentOrder existingOrder = PaymentOrder.builder()
                    .id(1L)
                    .razorpayOrderId("order_OLD")
                    .invoiceId(1L)
                    .status(OrderStatus.CREATED)
                    .build();

            InvoicePaymentRequest request = InvoicePaymentRequest.builder()
                    .invoiceId(1L)
                    .build();

            RazorpayOrderResponse newOrderResponse = RazorpayOrderResponse.builder()
                    .orderId("order_NEW")
                    .keyId("rzp_test_xxx")
                    .amount(BigDecimal.valueOf(5000.00))
                    .currency("INR")
                    .build();

            when(paymentOrderRepository.findByInvoiceIdAndStatus(1L, OrderStatus.CREATED))
                    .thenReturn(Optional.of(existingOrder));
            when(paymentOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
            when(razorpayPaymentService.createOrder(any())).thenReturn(newOrderResponse);

            // When
            PaymentInitiationResponse response = invoicePaymentService.retryPayment(request);

            // Then
            // Verify old order was expired
            verify(paymentOrderRepository).save(argThat(order ->
                order.getStatus() == OrderStatus.EXPIRED
            ));

            // Verify new order was created
            assertThat(response.getOrderId()).isEqualTo("order_NEW");
        }
    }
}
