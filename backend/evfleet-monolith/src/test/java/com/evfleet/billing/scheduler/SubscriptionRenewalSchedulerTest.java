package com.evfleet.billing.scheduler;

import com.evfleet.billing.dto.InvoiceResponse;
import com.evfleet.billing.model.BillingAddress;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.Invoice.InvoiceStatus;
import com.evfleet.billing.model.Subscription;
import com.evfleet.billing.model.Subscription.SubscriptionStatus;
import com.evfleet.billing.repository.BillingAddressRepository;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.SubscriptionRepository;
import com.evfleet.billing.service.BillingService;
import com.evfleet.billing.service.PaymentEmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionRenewalScheduler
 * 
 * Tests cover:
 * - Expiry reminder emails (7 days before)
 * - Renewal invoice generation
 * - Grace period warnings
 * - Service suspension after grace period
 * - Auto-renewal processing
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription Renewal Scheduler Tests")
class SubscriptionRenewalSchedulerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BillingAddressRepository billingAddressRepository;

    @Mock
    private BillingService billingService;

    @Mock
    private PaymentEmailService paymentEmailService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private SubscriptionRenewalScheduler scheduler;
    
    private Clock fixedClock;
    private LocalDate today;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> dateCaptor;

    @BeforeEach
    void setUp() {
        // Fixed clock for predictable testing
        today = LocalDate.of(2024, 6, 15);
        fixedClock = Clock.fixed(
                Instant.parse("2024-06-15T06:00:00Z"),
                ZoneId.of("Asia/Kolkata")
        );

        // Create scheduler with fixed clock
        scheduler = new SubscriptionRenewalScheduler(
                subscriptionRepository,
                invoiceRepository,
                billingAddressRepository,
                billingService,
                paymentEmailService,
                meterRegistry,
                fixedClock
        );

        // Set configuration values
        ReflectionTestUtils.setField(scheduler, "reminderDaysBefore", 7);
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 3);
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);

        // Mock counter creation
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        when(meterRegistry.counter(anyString())).thenReturn(counter);
        when(Counter.builder(anyString())).thenReturn(Counter.builder("test"));
        
        // Initialize metrics
        scheduler.initMetrics();
    }

    // ==================== Test Data Helpers ====================

    private Subscription createActiveSubscription(Long id, Long companyId, LocalDate endDate) {
        return Subscription.builder()
                .id(id)
                .companyId(companyId)
                .planType("PRO")
                .vehicleCount(50)
                .amount(new BigDecimal("9999.00"))
                .billingCycle("MONTHLY")
                .startDate(endDate.minusMonths(1))
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(true)
                .build();
    }

    private BillingAddress createBillingAddress(Long companyId) {
        return BillingAddress.builder()
                .id(companyId)
                .companyId(companyId)
                .companyName("Test Company " + companyId)
                .contactEmail("billing" + companyId + "@example.com")
                .contactPhone("+91-9876543210")
                .addressLine1("123 Test Street")
                .city("Mumbai")
                .state("Maharashtra")
                .pincode("400001")
                .country("India")
                .build();
    }

    private Invoice createInvoice(Long id, Long companyId, InvoiceStatus status) {
        return Invoice.builder()
                .id(id)
                .companyId(companyId)
                .invoiceNumber("INV-2024-" + id)
                .invoiceDate(today)
                .dueDate(today.plusDays(7))
                .subtotal(new BigDecimal("9999.00"))
                .taxAmount(new BigDecimal("1799.82"))
                .totalAmount(new BigDecimal("11798.82"))
                .status(status)
                .build();
    }

    // ==================== Scheduler Disabled Tests ====================

    @Nested
    @DisplayName("Scheduler Disabled")
    class SchedulerDisabledTests {

        @Test
        @DisplayName("Should skip processing when scheduler is disabled")
        void shouldSkipWhenDisabled() {
            // Given
            ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);

            // When
            scheduler.processSubscriptions();

            // Then
            verifyNoInteractions(subscriptionRepository);
            verifyNoInteractions(paymentEmailService);
        }
    }

    // ==================== Expiry Reminder Tests ====================

    @Nested
    @DisplayName("Expiry Reminders")
    class ExpiryReminderTests {

        @Test
        @DisplayName("Should send reminder for subscription expiring in 7 days")
        void shouldSendReminderForExpiringSubscription() {
            // Given
            LocalDate expiryDate = today.plusDays(7);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            BillingAddress billingAddress = createBillingAddress(100L);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(paymentEmailService).sendSubscriptionExpiryReminder(
                    eq("billing100@example.com"),
                    eq("Test Company 100"),
                    eq("PRO"),
                    eq(expiryDate),
                    eq(7L),
                    eq(new BigDecimal("9999.00")),
                    eq(true)
            );
        }

        @Test
        @DisplayName("Should not send reminder if no email available")
        void shouldNotSendReminderIfNoEmail() {
            // Given
            LocalDate expiryDate = today.plusDays(7);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.empty());

            // When
            scheduler.processSubscriptions();

            // Then
            verify(paymentEmailService, never()).sendSubscriptionExpiryReminder(
                    any(), any(), any(), any(), anyLong(), any(), anyBoolean());
        }

        @Test
        @DisplayName("Should send reminders for multiple subscriptions")
        void shouldSendRemindersForMultipleSubscriptions() {
            // Given
            LocalDate expiryDate = today.plusDays(7);
            Subscription sub1 = createActiveSubscription(1L, 100L, expiryDate);
            Subscription sub2 = createActiveSubscription(2L, 200L, expiryDate);
            
            BillingAddress address1 = createBillingAddress(100L);
            BillingAddress address2 = createBillingAddress(200L);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of(sub1, sub2));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(address1));
            when(billingAddressRepository.findByCompanyId(200L))
                    .thenReturn(Optional.of(address2));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(paymentEmailService, times(2)).sendSubscriptionExpiryReminder(
                    any(), any(), any(), any(), anyLong(), any(), anyBoolean());
        }
    }

    // ==================== Invoice Generation Tests ====================

    @Nested
    @DisplayName("Renewal Invoice Generation")
    class InvoiceGenerationTests {

        @Test
        @DisplayName("Should generate renewal invoice for auto-renew subscription")
        void shouldGenerateRenewalInvoice() {
            // Given
            Subscription subscription = createActiveSubscription(1L, 100L, today);
            subscription.setAutoRenew(true);
            
            BillingAddress billingAddress = createBillingAddress(100L);
            Invoice invoice = createInvoice(1L, 100L, InvoiceStatus.PENDING);
            
            InvoiceResponse invoiceResponse = InvoiceResponse.builder()
                    .id(1L)
                    .invoiceNumber("INV-2024-1")
                    .build();

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of(subscription));
            when(invoiceRepository.findByCompanyIdAndInvoiceDateAfter(eq(100L), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(billingService.createInvoice(eq(100L), any(BigDecimal.class)))
                    .thenReturn(invoiceResponse);
            when(invoiceRepository.findById(1L))
                    .thenReturn(Optional.of(invoice));
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(billingService).createInvoice(100L, new BigDecimal("9999.00"));
            verify(paymentEmailService).sendRenewalInvoiceNotification(
                    eq("billing100@example.com"),
                    eq("Test Company 100"),
                    eq("INV-2024-1"),
                    eq(invoice.getTotalAmount()),
                    eq(invoice.getDueDate())
            );
        }

        @Test
        @DisplayName("Should skip invoice generation if pending invoice exists")
        void shouldSkipIfPendingInvoiceExists() {
            // Given
            Subscription subscription = createActiveSubscription(1L, 100L, today);
            Invoice existingInvoice = createInvoice(99L, 100L, InvoiceStatus.PENDING);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of(subscription));
            when(invoiceRepository.findByCompanyIdAndInvoiceDateAfter(eq(100L), any(LocalDate.class)))
                    .thenReturn(List.of(existingInvoice));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(billingService, never()).createInvoice(any(), any());
        }

        @Test
        @DisplayName("Should skip invoice generation for non-auto-renew subscription")
        void shouldSkipNonAutoRenewSubscription() {
            // Given
            Subscription subscription = createActiveSubscription(1L, 100L, today);
            subscription.setAutoRenew(false);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of(subscription));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(billingService, never()).createInvoice(any(), any());
        }
    }

    // ==================== Grace Period Tests ====================

    @Nested
    @DisplayName("Grace Period Handling")
    class GracePeriodTests {

        @Test
        @DisplayName("Should send grace period warning for expired subscription within grace period")
        void shouldSendGracePeriodWarning() {
            // Given
            LocalDate expiryDate = today.minusDays(1); // Expired yesterday
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            BillingAddress billingAddress = createBillingAddress(100L);

            // Return empty for other queries
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of(subscription), List.of()); // First call for grace, second for expiry
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(paymentEmailService).sendGracePeriodWarning(
                    eq("billing100@example.com"),
                    eq("Test Company 100"),
                    eq(expiryDate),
                    eq(2), // 3 day grace - 1 day expired = 2 days remaining
                    eq(new BigDecimal("9999.00"))
            );
        }
    }

    // ==================== Suspension Tests ====================

    @Nested
    @DisplayName("Subscription Suspension")
    class SuspensionTests {

        @Test
        @DisplayName("Should suspend subscription after grace period with unpaid invoice")
        void shouldSuspendAfterGracePeriod() {
            // Given
            LocalDate expiryDate = today.minusDays(4); // Expired 4 days ago (past 3-day grace)
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            Invoice unpaidInvoice = createInvoice(1L, 100L, InvoiceStatus.PENDING);
            BillingAddress billingAddress = createBillingAddress(100L);

            LocalDate gracePeriodEnd = today.minusDays(3);
            
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), eq(today.plusDays(8))))
                    .thenReturn(List.of()); // No reminders
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), eq(today)))
                    .thenReturn(List.of()); // No grace period
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), eq(gracePeriodEnd)))
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(invoiceRepository.findByCompanyIdAndStatus(100L, InvoiceStatus.PENDING))
                    .thenReturn(List.of(unpaidInvoice));
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenReturn(subscription);

            // When
            scheduler.processSubscriptions();

            // Then
            verify(subscriptionRepository).save(argThat(sub -> 
                    sub.getStatus() == SubscriptionStatus.INACTIVE));
            verify(paymentEmailService).sendSubscriptionSuspensionNotice(
                    eq("billing100@example.com"),
                    eq("Test Company 100"),
                    eq("PRO")
            );
        }

        @Test
        @DisplayName("Should renew subscription if invoice was paid")
        void shouldRenewIfPaid() {
            // Given
            LocalDate expiryDate = today.minusDays(4);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            subscription.setBillingCycle("MONTHLY");
            BillingAddress billingAddress = createBillingAddress(100L);

            LocalDate gracePeriodEnd = today.minusDays(3);
            
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(invoiceRepository.findByCompanyIdAndStatus(100L, InvoiceStatus.PENDING))
                    .thenReturn(List.of()); // No unpaid invoices
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenReturn(subscription);

            // When
            scheduler.processSubscriptions();

            // Then
            verify(subscriptionRepository).save(argThat(sub -> 
                    sub.getStatus() == SubscriptionStatus.ACTIVE));
            verify(paymentEmailService, never()).sendSubscriptionSuspensionNotice(any(), any(), any());
        }
    }

    // ==================== Manual Trigger Tests ====================

    @Nested
    @DisplayName("Manual Trigger Operations")
    class ManualTriggerTests {

        @Test
        @DisplayName("Should send manual reminder for specific subscription")
        void shouldSendManualReminder() {
            // Given
            Subscription subscription = createActiveSubscription(1L, 100L, today.plusDays(3));
            BillingAddress billingAddress = createBillingAddress(100L);

            when(subscriptionRepository.findById(1L))
                    .thenReturn(Optional.of(subscription));
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(billingAddress));

            // When
            scheduler.sendManualReminder(1L);

            // Then
            verify(paymentEmailService).sendSubscriptionExpiryReminder(
                    eq("billing100@example.com"),
                    eq("Test Company 100"),
                    any(),
                    any(),
                    anyLong(),
                    any(),
                    anyBoolean()
            );
        }

        @Test
        @DisplayName("Should throw exception for non-existent subscription")
        void shouldThrowForNonExistentSubscription() {
            // Given
            when(subscriptionRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> scheduler.sendManualReminder(999L));
        }
    }

    // ==================== Status Tests ====================

    @Nested
    @DisplayName("Scheduler Status")
    class StatusTests {

        @Test
        @DisplayName("Should return scheduler status")
        void shouldReturnStatus() {
            // When
            SubscriptionRenewalScheduler.SchedulerStatus status = scheduler.getStatus();

            // Then
            assertThat(status.isEnabled()).isTrue();
            assertThat(status.getReminderDaysBefore()).isEqualTo(7);
            assertThat(status.getGracePeriodDays()).isEqualTo(3);
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should continue processing after email failure")
        void shouldContinueAfterEmailFailure() {
            // Given
            LocalDate expiryDate = today.plusDays(7);
            Subscription sub1 = createActiveSubscription(1L, 100L, expiryDate);
            Subscription sub2 = createActiveSubscription(2L, 200L, expiryDate);
            
            BillingAddress address1 = createBillingAddress(100L);
            BillingAddress address2 = createBillingAddress(200L);

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of(sub1, sub2));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(billingAddressRepository.findByCompanyId(100L))
                    .thenReturn(Optional.of(address1));
            when(billingAddressRepository.findByCompanyId(200L))
                    .thenReturn(Optional.of(address2));

            // First email call throws exception
            doThrow(new RuntimeException("Email service unavailable"))
                    .doNothing()
                    .when(paymentEmailService).sendSubscriptionExpiryReminder(
                            any(), any(), any(), any(), anyLong(), any(), anyBoolean());

            // When
            scheduler.processSubscriptions();

            // Then - should still attempt to send second email
            verify(paymentEmailService, times(2)).sendSubscriptionExpiryReminder(
                    any(), any(), any(), any(), anyLong(), any(), anyBoolean());
        }

        @Test
        @DisplayName("Should handle invoice generation failure gracefully")
        void shouldHandleInvoiceGenerationFailure() {
            // Given
            Subscription subscription = createActiveSubscription(1L, 100L, today);
            
            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of(subscription));
            when(invoiceRepository.findByCompanyIdAndInvoiceDateAfter(eq(100L), any(LocalDate.class)))
                    .thenReturn(List.of());
            when(billingService.createInvoice(any(), any()))
                    .thenThrow(new RuntimeException("Billing service error"));

            // When - should not throw
            scheduler.processSubscriptions();

            // Then - verify billing was attempted
            verify(billingService).createInvoice(eq(100L), any(BigDecimal.class));
        }
    }

    // ==================== Billing Cycle Tests ====================

    @Nested
    @DisplayName("Billing Cycle Calculations")
    class BillingCycleTests {

        @Test
        @DisplayName("Should calculate monthly renewal correctly")
        void shouldCalculateMonthlyRenewal() {
            // Given
            LocalDate expiryDate = today.minusDays(4);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            subscription.setBillingCycle("MONTHLY");

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(invoiceRepository.findByCompanyIdAndStatus(eq(100L), any()))
                    .thenReturn(List.of());
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(subscriptionRepository).save(argThat(sub -> 
                    sub.getEndDate().equals(expiryDate.plusMonths(1))));
        }

        @Test
        @DisplayName("Should calculate quarterly renewal correctly")
        void shouldCalculateQuarterlyRenewal() {
            // Given
            LocalDate expiryDate = today.minusDays(4);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            subscription.setBillingCycle("QUARTERLY");

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(invoiceRepository.findByCompanyIdAndStatus(eq(100L), any()))
                    .thenReturn(List.of());
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(subscriptionRepository).save(argThat(sub -> 
                    sub.getEndDate().equals(expiryDate.plusMonths(3))));
        }

        @Test
        @DisplayName("Should calculate yearly renewal correctly")
        void shouldCalculateYearlyRenewal() {
            // Given
            LocalDate expiryDate = today.minusDays(4);
            Subscription subscription = createActiveSubscription(1L, 100L, expiryDate);
            subscription.setBillingCycle("YEARLY");

            when(subscriptionRepository.findByStatusAndEndDateBefore(
                    eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(subscription));
            when(subscriptionRepository.findSubscriptionsToRenew(today))
                    .thenReturn(List.of());
            when(invoiceRepository.findByCompanyIdAndStatus(eq(100L), any()))
                    .thenReturn(List.of());
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            scheduler.processSubscriptions();

            // Then
            verify(subscriptionRepository).save(argThat(sub -> 
                    sub.getEndDate().equals(expiryDate.plusYears(1))));
        }
    }
}
