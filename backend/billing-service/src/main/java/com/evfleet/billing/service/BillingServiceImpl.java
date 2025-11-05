package com.evfleet.billing.service;

import com.evfleet.billing.dto.BillingAddressDto;
import com.evfleet.billing.dto.PaymentProcessRequest;
import com.evfleet.billing.entity.*;
import com.evfleet.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PricingPlanRepository pricingPlanRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> getSubscription(String companyId) {
        return subscriptionRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public Subscription updateSubscription(String companyId, String tier, String cycle) {
        Optional<Subscription> existingOpt = subscriptionRepository.findByCompanyId(companyId);
        
        Subscription subscription;
        if (existingOpt.isPresent()) {
            subscription = existingOpt.get();
            subscription.setPlanType(tier);
            subscription.setBillingCycle(cycle);
        } else {
            subscription = new Subscription();
            subscription.setId(UUID.randomUUID().toString());
            subscription.setCompanyId(companyId);
            subscription.setPlanType(tier);
            subscription.setBillingCycle(cycle);
            subscription.setStartDate(LocalDate.now());
            subscription.setVehicleCount(0);
            subscription.setAmount(java.math.BigDecimal.ZERO);
        }
        
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void cancelSubscription(String companyId) {
        Optional<Subscription> subscription = subscriptionRepository.findByCompanyId(companyId);
        subscription.ifPresent(sub -> {
            sub.setStatus("CANCELLED");
            sub.setAutoRenew(false);
            sub.setEndDate(LocalDate.now());
            subscriptionRepository.save(sub);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices(String companyId) {
        return invoiceRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoiceById(String id) {
        return invoiceRepository.findById(id);
    }

    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getId() == null || invoice.getId().isEmpty()) {
            invoice.setId(UUID.randomUUID().toString());
        }
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        }
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadInvoice(String id) {
        // Placeholder for PDF generation
        // In a real implementation, this would generate a PDF invoice
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        if (invoice.isPresent()) {
            String content = String.format("Invoice %s\nAmount: %s\nStatus: %s",
                    invoice.get().getInvoiceNumber(),
                    invoice.get().getTotalAmount(),
                    invoice.get().getStatus());
            return content.getBytes();
        }
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentHistory(String companyId) {
        // Get all invoices for the company, then get payments for those invoices
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        return invoices.stream()
                .flatMap(invoice -> paymentRepository.findByInvoiceId(invoice.getId()).stream())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }

    @Override
    @Transactional
    public Payment processPayment(PaymentProcessRequest request) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setInvoiceId(request.getInvoiceId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod("CARD");
        payment.setTransactionId("TXN-" + System.currentTimeMillis());
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());
        
        // Mock gateway response
        Map<String, Object> gatewayResponse = new HashMap<>();
        gatewayResponse.put("gateway", "razorpay");
        gatewayResponse.put("payment_id", "pay_" + UUID.randomUUID().toString());
        gatewayResponse.put("status", "captured");
        payment.setGatewayResponse(gatewayResponse);
        
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingPlan> getPricingPlans() {
        return pricingPlanRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public BillingAddressDto getBillingAddress(String companyId) {
        // Placeholder - In real implementation, this would fetch from a billing_addresses table
        BillingAddressDto address = new BillingAddressDto();
        address.setFleetId(companyId);
        address.setCompanyName("Sample Company");
        address.setAddressLine1("123 Main St");
        address.setCity("Mumbai");
        address.setState("Maharashtra");
        address.setZipCode("400001");
        address.setCountry("India");
        return address;
    }

    @Override
    @Transactional
    public BillingAddressDto updateBillingAddress(String companyId, BillingAddressDto address) {
        // Placeholder - In real implementation, this would update the billing_addresses table
        address.setFleetId(companyId);
        return address;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getPaymentMethods(String companyId) {
        return paymentMethodRepository.findByCompanyIdAndIsActiveTrue(companyId);
    }

    @Override
    @Transactional
    public PaymentMethod addPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod.getId() == null || paymentMethod.getId().isEmpty()) {
            paymentMethod.setId(UUID.randomUUID().toString());
        }
        return paymentMethodRepository.save(paymentMethod);
    }

    @Override
    @Transactional
    public void setDefaultPaymentMethod(String companyId, String paymentMethodId) {
        // First, unset all defaults for this company
        List<PaymentMethod> methods = paymentMethodRepository.findByCompanyId(companyId);
        methods.forEach(method -> {
            method.setIsDefault(false);
            paymentMethodRepository.save(method);
        });
        
        // Set the new default
        Optional<PaymentMethod> method = paymentMethodRepository.findById(paymentMethodId);
        method.ifPresent(pm -> {
            pm.setIsDefault(true);
            paymentMethodRepository.save(pm);
        });
    }

    @Override
    @Transactional
    public void deletePaymentMethod(String id) {
        paymentMethodRepository.deleteById(id);
    }
}
