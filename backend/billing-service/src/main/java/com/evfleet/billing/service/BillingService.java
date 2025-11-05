package com.evfleet.billing.service;

import com.evfleet.billing.dto.BillingAddressDto;
import com.evfleet.billing.dto.PaymentProcessRequest;
import com.evfleet.billing.entity.*;

import java.util.List;
import java.util.Optional;

public interface BillingService {
    
    // Subscription management
    Optional<Subscription> getSubscription(String companyId);
    Subscription updateSubscription(String companyId, String tier, String cycle);
    void cancelSubscription(String companyId);
    
    // Invoice management
    List<Invoice> getAllInvoices(String companyId);
    Optional<Invoice> getInvoiceById(String id);
    Invoice createInvoice(Invoice invoice);
    byte[] downloadInvoice(String id);
    
    // Payment management
    List<Payment> getPaymentHistory(String companyId);
    Optional<Payment> getPaymentById(String id);
    Payment processPayment(PaymentProcessRequest request);
    
    // Pricing plans
    List<PricingPlan> getPricingPlans();
    
    // Billing address
    BillingAddressDto getBillingAddress(String companyId);
    BillingAddressDto updateBillingAddress(String companyId, BillingAddressDto address);
    
    // Payment methods
    List<PaymentMethod> getPaymentMethods(String companyId);
    PaymentMethod addPaymentMethod(PaymentMethod paymentMethod);
    void setDefaultPaymentMethod(String companyId, String paymentMethodId);
    void deletePaymentMethod(String id);
}
