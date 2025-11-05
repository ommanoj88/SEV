package com.evfleet.billing.controller;

import com.evfleet.billing.dto.BillingAddressDto;
import com.evfleet.billing.dto.PaymentProcessRequest;
import com.evfleet.billing.dto.SubscriptionUpdateRequest;
import com.evfleet.billing.entity.*;
import com.evfleet.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Billing and subscription management endpoints")
public class BillingController {

    private final BillingService billingService;
    
    // For demo purposes, using a default company ID
    // In production, this would be extracted from authentication context
    private static final String DEFAULT_COMPANY_ID = "COMP001";

    // ========== SUBSCRIPTION MANAGEMENT ==========

    @GetMapping("/subscription")
    @Operation(summary = "Get current subscription", description = "Retrieves the current subscription details")
    public ResponseEntity<Subscription> getSubscription() {
        Optional<Subscription> subscription = billingService.getSubscription(DEFAULT_COMPANY_ID);
        return subscription.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subscription/update")
    @Operation(summary = "Update subscription", description = "Updates the subscription tier and billing cycle")
    public ResponseEntity<Subscription> updateSubscription(@Valid @RequestBody SubscriptionUpdateRequest request) {
        Subscription updated = billingService.updateSubscription(DEFAULT_COMPANY_ID, request.getTier(), request.getCycle());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/subscription/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancels the current subscription")
    public ResponseEntity<Void> cancelSubscription() {
        billingService.cancelSubscription(DEFAULT_COMPANY_ID);
        return ResponseEntity.noContent().build();
    }

    // ========== INVOICE MANAGEMENT ==========

    @GetMapping("/invoices")
    @Operation(summary = "Get all invoices", description = "Retrieves all invoices for the company")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = billingService.getAllInvoices(DEFAULT_COMPANY_ID);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get invoice by ID", description = "Retrieves a specific invoice by ID")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable String id) {
        Optional<Invoice> invoice = billingService.getInvoiceById(id);
        return invoice.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/invoices")
    @Operation(summary = "Create invoice", description = "Creates a new invoice")
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody Invoice invoice) {
        invoice.setCompanyId(DEFAULT_COMPANY_ID);
        Invoice created = billingService.createInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/invoices/{id}/download")
    @Operation(summary = "Download invoice PDF", description = "Downloads invoice as PDF")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String id) {
        byte[] pdfBytes = billingService.downloadInvoice(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // ========== PAYMENT MANAGEMENT ==========

    @GetMapping("/payments")
    @Operation(summary = "Get payment history", description = "Retrieves all payment transactions")
    public ResponseEntity<List<Payment>> getPaymentHistory() {
        List<Payment> payments = billingService.getPaymentHistory(DEFAULT_COMPANY_ID);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a specific payment by ID")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        Optional<Payment> payment = billingService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/payments/process")
    @Operation(summary = "Process payment", description = "Processes a payment for an invoice")
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody PaymentProcessRequest request) {
        Payment payment = billingService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // ========== PRICING PLANS ==========

    @GetMapping("/pricing-plans")
    @Operation(summary = "Get pricing plans", description = "Retrieves all available pricing plans")
    public ResponseEntity<List<PricingPlan>> getPricingPlans() {
        List<PricingPlan> plans = billingService.getPricingPlans();
        return ResponseEntity.ok(plans);
    }

    // ========== BILLING ADDRESS ==========

    @GetMapping("/address")
    @Operation(summary = "Get billing address", description = "Retrieves the billing address")
    public ResponseEntity<BillingAddressDto> getBillingAddress() {
        BillingAddressDto address = billingService.getBillingAddress(DEFAULT_COMPANY_ID);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/address")
    @Operation(summary = "Update billing address", description = "Updates the billing address")
    public ResponseEntity<BillingAddressDto> updateBillingAddress(@Valid @RequestBody BillingAddressDto address) {
        BillingAddressDto updated = billingService.updateBillingAddress(DEFAULT_COMPANY_ID, address);
        return ResponseEntity.ok(updated);
    }

    // ========== PAYMENT METHODS ==========

    @GetMapping("/payment-methods")
    @Operation(summary = "Get payment methods", description = "Retrieves all payment methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods() {
        List<PaymentMethod> methods = billingService.getPaymentMethods(DEFAULT_COMPANY_ID);
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/payment-methods")
    @Operation(summary = "Add payment method", description = "Adds a new payment method")
    public ResponseEntity<PaymentMethod> addPaymentMethod(@Valid @RequestBody PaymentMethod paymentMethod) {
        paymentMethod.setCompanyId(DEFAULT_COMPANY_ID);
        PaymentMethod created = billingService.addPaymentMethod(paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/payment-methods/{id}/set-default")
    @Operation(summary = "Set default payment method", description = "Sets a payment method as default")
    public ResponseEntity<Void> setDefaultPaymentMethod(@PathVariable String id) {
        billingService.setDefaultPaymentMethod(DEFAULT_COMPANY_ID, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/payment-methods/{id}")
    @Operation(summary = "Delete payment method", description = "Deletes a payment method")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable String id) {
        billingService.deletePaymentMethod(id);
        return ResponseEntity.noContent().build();
    }
}
