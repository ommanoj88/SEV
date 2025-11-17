package com.evfleet.billing.service;

import com.evfleet.billing.dto.*;
import com.evfleet.billing.model.Invoice;
import com.evfleet.billing.model.Payment;
import com.evfleet.billing.model.PricingPlan;
import com.evfleet.billing.model.Subscription;
import com.evfleet.billing.repository.*;
import com.evfleet.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Billing Service
 * Handles all billing-related business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final BillingAddressRepository billingAddressRepository;

    // ========== SUBSCRIPTION METHODS ==========

    public SubscriptionResponse getSubscription(Long companyId) {
        log.info("GET /api/v1/billing/subscription - Fetching subscription for company: {}", companyId);
        Subscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "companyId", companyId));
        return SubscriptionResponse.fromEntity(subscription);
    }

    public SubscriptionResponse updateSubscription(Long companyId, SubscriptionRequest request) {
        log.info("POST /api/v1/billing/subscription/update - Updating subscription for company: {} to tier: {}, cycle: {}",
                companyId, request.getTier(), request.getCycle());

        Subscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "companyId", companyId));

        // Get pricing plan
        PricingPlan.BillingCycle cycle = PricingPlan.BillingCycle.valueOf(request.getCycle().toUpperCase());
        PricingPlan plan = pricingPlanRepository.findByName(request.getTier().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("PricingPlan", "name", request.getTier()));

        // Calculate new amount
        BigDecimal newAmount = plan.calculatePrice(request.getVehicleCount());

        // Update subscription
        subscription.setPlanType(request.getTier().toUpperCase());
        subscription.setBillingCycle(cycle.name());
        subscription.setVehicleCount(request.getVehicleCount());
        subscription.setAmount(newAmount);

        Subscription updated = subscriptionRepository.save(subscription);
        log.info("Subscription updated successfully for company: {}", companyId);
        return SubscriptionResponse.fromEntity(updated);
    }

    public void cancelSubscription(Long companyId) {
        log.info("POST /api/v1/billing/subscription/cancel - Cancelling subscription for company: {}", companyId);
        Subscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "companyId", companyId));

        subscription.cancel();
        subscriptionRepository.save(subscription);
        log.info("Subscription cancelled for company: {}", companyId);
    }

    // ========== INVOICE METHODS ==========

    public List<InvoiceResponse> getInvoices(Long companyId) {
        log.info("GET /api/v1/billing/invoices - Fetching invoices for company: {}", companyId);
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        return invoices.stream()
                .map(InvoiceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{} - Fetching invoice", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        return InvoiceResponse.fromEntity(invoice);
    }

    public InvoiceResponse createInvoice(Long companyId, BigDecimal amount) {
        log.info("POST /api/v1/billing/invoices - Creating invoice for company: {}, amount: {}", companyId, amount);

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .companyId(companyId)
                .invoiceNumber(invoiceNumber)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .subtotal(amount)
                .taxAmount(amount.multiply(BigDecimal.valueOf(0.18))) // 18% GST
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(amount.multiply(BigDecimal.valueOf(1.18)))
                .status(Invoice.InvoiceStatus.PENDING)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {}", invoiceNumber);
        return InvoiceResponse.fromEntity(saved);
    }

    // ========== PAYMENT METHODS ==========

    public List<PaymentResponse> getPaymentHistory(Long companyId) {
        log.info("GET /api/v1/billing/payments - Fetching payment history for company: {}", companyId);
        List<Payment> payments = paymentRepository.findByCompanyId(companyId);
        return payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("GET /api/v1/billing/payments/{} - Fetching payment", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    public PaymentResponse processPayment(Long invoiceId, PaymentRequest request) {
        log.info("POST /api/v1/billing/invoices/{}/pay - Processing payment", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        // Create payment record
        Payment payment = Payment.builder()
                .invoiceId(invoiceId)
                .amount(request.getAmount())
                .paymentMethod(Payment.PaymentMethodType.valueOf(request.getPaymentMethod().toUpperCase()))
                .transactionId(request.getTransactionReference() != null ?
                        request.getTransactionReference() : UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .status(Payment.PaymentStatus.PENDING)
                .remarks(request.getRemarks())
                .build();

        // Process payment (simplified - would integrate with payment gateway in production)
        try {
            payment.markAsCompleted(payment.getTransactionId());
            invoice.markAsPaid(LocalDate.now(), request.getAmount());
            invoiceRepository.save(invoice);
            log.info("Payment processed successfully for invoice: {}", invoiceId);
        } catch (Exception e) {
            payment.markAsFailed("Payment processing failed: " + e.getMessage());
            log.error("Payment processing failed for invoice: {}", invoiceId, e);
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(saved);
    }

    public List<PaymentResponse> getInvoicePaymentHistory(Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}/payment-history - Fetching payment history", invoiceId);
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        return payments.stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== PRICING METHODS ==========

    public List<PricingPlanResponse> getPricingPlans() {
        log.info("GET /api/v1/billing/pricing-plans - Fetching all pricing plans");
        List<PricingPlan> plans = pricingPlanRepository.findByIsActiveTrue();
        return plans.stream()
                .map(PricingPlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== BILLING ADDRESS METHODS ==========

    public BillingAddressResponse getBillingAddress(Long companyId) {
        log.info("GET /api/v1/billing/address - Fetching billing address for company: {}", companyId);
        return billingAddressRepository.findByCompanyId(companyId)
                .map(BillingAddressResponse::fromEntity)
                .orElse(null);
    }

    public BillingAddressResponse updateBillingAddress(Long companyId, BillingAddressRequest request) {
        log.info("PUT /api/v1/billing/address - Updating billing address for company: {}", companyId);

        var address = billingAddressRepository.findByCompanyId(companyId)
                .orElse(com.evfleet.billing.model.BillingAddress.builder()
                        .companyId(companyId)
                        .build());

        address.setCompanyName(request.getCompanyName());
        address.setGstNumber(request.getGstNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setContactPerson(request.getContactPerson());
        address.setContactEmail(request.getContactEmail());
        address.setContactPhone(request.getContactPhone());

        var saved = billingAddressRepository.save(address);
        log.info("Billing address updated for company: {}", companyId);
        return BillingAddressResponse.fromEntity(saved);
    }

    // ========== HELPER METHODS ==========

    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", invoiceRepository.count() + 1);
    }
}
