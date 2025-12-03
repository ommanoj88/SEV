package com.evfleet.billing.scheduler;

import com.evfleet.billing.dto.InvoiceResponse;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.Subscription;
import com.evfleet.billing.model.Subscription.SubscriptionStatus;
import com.evfleet.billing.model.BillingAddress;
import com.evfleet.billing.repository.BillingAddressRepository;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.repository.SubscriptionRepository;
import com.evfleet.billing.service.BillingService;
import com.evfleet.billing.service.PaymentEmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subscription Renewal Scheduler
 * 
 * Handles automated subscription management:
 * - Sends renewal reminder emails 7 days before expiry
 * - Auto-generates renewal invoices on expiry date
 * - Enforces 3-day grace period after expiry
 * - Suspends service after grace period ends
 * - Handles expired subscriptions cleanup
 * 
 * Schedule: Daily at 6:00 AM IST
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class SubscriptionRenewalScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingAddressRepository billingAddressRepository;
    private final BillingService billingService;
    private final PaymentEmailService paymentEmailService;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    @Autowired
    public SubscriptionRenewalScheduler(
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository,
            BillingAddressRepository billingAddressRepository,
            BillingService billingService,
            @Autowired(required = false) PaymentEmailService paymentEmailService,
            MeterRegistry meterRegistry,
            @Autowired(required = false) Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.billingAddressRepository = billingAddressRepository;
        this.billingService = billingService;
        this.paymentEmailService = paymentEmailService;
        this.meterRegistry = meterRegistry;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    // Configuration properties
    @Value("${subscription.reminder.days-before:7}")
    private int reminderDaysBefore;

    @Value("${subscription.grace-period.days:3}")
    private int gracePeriodDays;

    @Value("${subscription.enabled:true}")
    private boolean schedulerEnabled;

    // Metrics
    private Counter remindersCounter;
    private Counter renewalsCounter;
    private Counter suspensionsCounter;
    private Counter invoicesGeneratedCounter;

    @PostConstruct
    public void initMetrics() {
        remindersCounter = Counter.builder("subscription.reminders.sent")
                .description("Number of renewal reminders sent")
                .register(meterRegistry);
        renewalsCounter = Counter.builder("subscription.renewals.processed")
                .description("Number of subscriptions renewed")
                .register(meterRegistry);
        suspensionsCounter = Counter.builder("subscription.suspensions")
                .description("Number of subscriptions suspended")
                .register(meterRegistry);
        invoicesGeneratedCounter = Counter.builder("subscription.invoices.generated")
                .description("Number of renewal invoices generated")
                .register(meterRegistry);
    }

    /**
     * Main scheduled task - runs daily at 6:00 AM
     */
    @Scheduled(cron = "${subscription.scheduler.cron:0 0 6 * * ?}")
    @Transactional
    public void processSubscriptions() {
        if (!schedulerEnabled) {
            log.info("Subscription scheduler is disabled");
            return;
        }

        LocalDate today = LocalDate.now(clock);
        log.info("Starting subscription renewal check for date: {}", today);

        try {
            // Step 1: Send reminders for subscriptions expiring soon
            sendExpiryReminders(today);

            // Step 2: Generate renewal invoices for expiring subscriptions
            generateRenewalInvoices(today);

            // Step 3: Handle grace period expirations
            handleGracePeriodExpirations(today);

            // Step 4: Handle final expirations (after grace period)
            handleFinalExpirations(today);

            log.info("Subscription renewal check completed");
        } catch (Exception e) {
            log.error("Error during subscription renewal processing: {}", e.getMessage(), e);
        }
    }

    /**
     * Send reminder emails for subscriptions expiring in reminderDaysBefore days
     */
    private void sendExpiryReminders(LocalDate today) {
        LocalDate reminderDate = today.plusDays(reminderDaysBefore);
        
        log.info("Checking for subscriptions expiring on: {}", reminderDate);
        
        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, reminderDate.plusDays(1))
                .stream()
                .filter(s -> s.getEndDate() != null)
                .filter(s -> s.getEndDate().isEqual(reminderDate) || 
                            (s.getEndDate().isAfter(today) && s.getEndDate().isBefore(reminderDate.plusDays(1))))
                .toList();

        AtomicInteger remindersSent = new AtomicInteger(0);

        for (Subscription subscription : expiringSubscriptions) {
            try {
                sendReminderEmail(subscription);
                remindersSent.incrementAndGet();
                remindersCounter.increment();
            } catch (Exception e) {
                log.error("Failed to send reminder for subscription {}: {}", 
                        subscription.getId(), e.getMessage());
            }
        }

        log.info("Sent {} expiry reminders", remindersSent.get());
    }

    /**
     * Generate renewal invoices for subscriptions that expire today
     */
    private void generateRenewalInvoices(LocalDate today) {
        log.info("Checking for subscriptions expiring today for invoice generation");

        List<Subscription> expiringToday = subscriptionRepository
                .findSubscriptionsToRenew(today)
                .stream()
                .filter(s -> s.getAutoRenew() != null && s.getAutoRenew())
                .toList();

        AtomicInteger invoicesGenerated = new AtomicInteger(0);

        for (Subscription subscription : expiringToday) {
            try {
                // Check if a pending renewal invoice already exists
                if (hasExistingPendingInvoice(subscription)) {
                    log.debug("Pending invoice already exists for subscription {}", subscription.getId());
                    continue;
                }

                // Generate renewal invoice
                Invoice invoice = generateRenewalInvoice(subscription);
                invoicesGenerated.incrementAndGet();
                invoicesGeneratedCounter.increment();

                // Send invoice notification
                sendInvoiceNotification(subscription, invoice);

                log.info("Generated renewal invoice {} for subscription {}", 
                        invoice.getInvoiceNumber(), subscription.getId());

            } catch (Exception e) {
                log.error("Failed to generate invoice for subscription {}: {}", 
                        subscription.getId(), e.getMessage());
            }
        }

        log.info("Generated {} renewal invoices", invoicesGenerated.get());
    }

    /**
     * Handle subscriptions in grace period (expired but within grace window)
     */
    private void handleGracePeriodExpirations(LocalDate today) {
        LocalDate gracePeriodEnd = today.minusDays(gracePeriodDays);
        
        log.info("Checking for subscriptions with expired grace period (ended before: {})", gracePeriodEnd);

        // Find subscriptions that expired but haven't been marked as such yet
        List<Subscription> expiredWithinGrace = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, today)
                .stream()
                .filter(s -> s.getEndDate() != null)
                .filter(s -> s.getEndDate().isAfter(gracePeriodEnd))
                .toList();

        for (Subscription subscription : expiredWithinGrace) {
            try {
                // Send grace period warning
                sendGracePeriodWarning(subscription, today);
            } catch (Exception e) {
                log.error("Failed to send grace period warning for subscription {}: {}", 
                        subscription.getId(), e.getMessage());
            }
        }
    }

    /**
     * Handle final expirations - suspend service after grace period
     */
    private void handleFinalExpirations(LocalDate today) {
        LocalDate gracePeriodEnd = today.minusDays(gracePeriodDays);
        
        log.info("Checking for subscriptions past grace period (ended before: {})", gracePeriodEnd);

        List<Subscription> pastGracePeriod = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, gracePeriodEnd);

        AtomicInteger suspensionCount = new AtomicInteger(0);

        for (Subscription subscription : pastGracePeriod) {
            try {
                // Check if there's any unpaid invoice
                boolean hasUnpaidInvoice = hasUnpaidInvoice(subscription);
                
                if (hasUnpaidInvoice) {
                    // Suspend the subscription
                    suspendSubscription(subscription);
                    suspensionCount.incrementAndGet();
                    suspensionsCounter.increment();

                    // Send suspension notification
                    sendSuspensionNotification(subscription);

                    log.info("Suspended subscription {} due to non-payment", subscription.getId());
                } else {
                    // Invoice was paid, renew the subscription
                    renewSubscription(subscription);
                    renewalsCounter.increment();
                    
                    log.info("Renewed subscription {} after payment confirmation", subscription.getId());
                }
            } catch (Exception e) {
                log.error("Failed to process expired subscription {}: {}", 
                        subscription.getId(), e.getMessage());
            }
        }

        log.info("Suspended {} subscriptions, renewed others with paid invoices", suspensionCount.get());
    }

    // ==================== Helper Methods ====================

    private void sendReminderEmail(Subscription subscription) {
        if (paymentEmailService == null) {
            log.debug("PaymentEmailService not available, skipping reminder email");
            return;
        }
        String email = getCompanyEmail(subscription.getCompanyId());
        if (email == null) {
            log.warn("No email found for company {}", subscription.getCompanyId());
            return;
        }

        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(clock), subscription.getEndDate());
        
        paymentEmailService.sendSubscriptionExpiryReminder(
                email,
                getCompanyName(subscription.getCompanyId()),
                subscription.getPlanType(),
                subscription.getEndDate(),
                daysUntilExpiry,
                subscription.getAmount(),
                subscription.getAutoRenew() != null && subscription.getAutoRenew()
        );
    }

    private void sendGracePeriodWarning(Subscription subscription, LocalDate today) {
        if (paymentEmailService == null) {
            log.debug("PaymentEmailService not available, skipping grace period warning");
            return;
        }
        String email = getCompanyEmail(subscription.getCompanyId());
        if (email == null) return;

        long daysInGrace = ChronoUnit.DAYS.between(subscription.getEndDate(), today);
        int daysRemaining = gracePeriodDays - (int) daysInGrace;

        paymentEmailService.sendGracePeriodWarning(
                email,
                getCompanyName(subscription.getCompanyId()),
                subscription.getEndDate(),
                daysRemaining,
                subscription.getAmount()
        );
    }

    private void sendSuspensionNotification(Subscription subscription) {
        if (paymentEmailService == null) {
            log.debug("PaymentEmailService not available, skipping suspension notification");
            return;
        }
        String email = getCompanyEmail(subscription.getCompanyId());
        if (email == null) return;

        paymentEmailService.sendSubscriptionSuspensionNotice(
                email,
                getCompanyName(subscription.getCompanyId()),
                subscription.getPlanType()
        );
    }

    private void sendInvoiceNotification(Subscription subscription, Invoice invoice) {
        if (paymentEmailService == null) {
            log.debug("PaymentEmailService not available, skipping invoice notification");
            return;
        }
        String email = getCompanyEmail(subscription.getCompanyId());
        if (email == null) return;

        paymentEmailService.sendRenewalInvoiceNotification(
                email,
                getCompanyName(subscription.getCompanyId()),
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount(),
                invoice.getDueDate()
        );
    }

    private Invoice generateRenewalInvoice(Subscription subscription) {
        // Calculate renewal amount
        BigDecimal amount = subscription.getAmount();

        // Create invoice via billing service
        InvoiceResponse response = billingService.createInvoice(
                subscription.getCompanyId(), 
                amount
        );

        // Fetch and return the actual invoice
        return invoiceRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("Failed to find generated invoice"));
    }

    private boolean hasExistingPendingInvoice(Subscription subscription) {
        // Check for pending invoices within the last billing cycle
        LocalDate startOfCycle = subscription.getEndDate() != null 
                ? subscription.getEndDate().minusDays(30) 
                : LocalDate.now(clock).minusDays(30);
        
        List<Invoice> recentInvoices = invoiceRepository.findByCompanyIdAndInvoiceDateAfter(
                subscription.getCompanyId(), 
                startOfCycle
        );
        
        return recentInvoices.stream()
                .anyMatch(inv -> inv.getStatus() == Invoice.InvoiceStatus.PENDING);
    }

    private boolean hasUnpaidInvoice(Subscription subscription) {
        List<Invoice> pendingInvoices = invoiceRepository.findByCompanyIdAndStatus(
                subscription.getCompanyId(),
                Invoice.InvoiceStatus.PENDING
        );
        
        return !pendingInvoices.isEmpty();
    }

    private void suspendSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.INACTIVE);
        subscriptionRepository.save(subscription);
    }

    private void renewSubscription(Subscription subscription) {
        // Calculate new end date based on billing cycle
        LocalDate newEndDate = calculateNewEndDate(subscription);
        subscription.renew(newEndDate);
        subscriptionRepository.save(subscription);
    }

    private LocalDate calculateNewEndDate(Subscription subscription) {
        LocalDate baseDate = subscription.getEndDate() != null 
                ? subscription.getEndDate() 
                : LocalDate.now(clock);
                
        return switch (subscription.getBillingCycle()) {
            case "MONTHLY" -> baseDate.plusMonths(1);
            case "QUARTERLY" -> baseDate.plusMonths(3);
            case "YEARLY" -> baseDate.plusYears(1);
            default -> baseDate.plusMonths(1);
        };
    }

    private String getCompanyEmail(Long companyId) {
        return billingAddressRepository.findByCompanyId(companyId)
                .map(BillingAddress::getContactEmail)
                .orElse(null);
    }

    private String getCompanyName(Long companyId) {
        return billingAddressRepository.findByCompanyId(companyId)
                .map(BillingAddress::getCompanyName)
                .orElse("Valued Customer");
    }

    // ==================== Manual Trigger Methods ====================

    /**
     * Manually trigger reminder emails for a specific subscription
     */
    @Transactional
    public void sendManualReminder(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        
        sendReminderEmail(subscription);
        log.info("Manual reminder sent for subscription: {}", subscriptionId);
    }

    /**
     * Get current scheduler status
     */
    public SchedulerStatus getStatus() {
        return SchedulerStatus.builder()
                .enabled(schedulerEnabled)
                .reminderDaysBefore(reminderDaysBefore)
                .gracePeriodDays(gracePeriodDays)
                .lastRunDate(null) // Would need to track this
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class SchedulerStatus {
        private boolean enabled;
        private int reminderDaysBefore;
        private int gracePeriodDays;
        private LocalDate lastRunDate;
    }
}
