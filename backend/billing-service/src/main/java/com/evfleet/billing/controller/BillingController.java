package com.evfleet.billing.controller;

import com.evfleet.billing.dto.BillingAddressDto;
import com.evfleet.billing.dto.PaymentProcessRequest;
import com.evfleet.billing.dto.PaymentRequestDTO;
import com.evfleet.billing.dto.InvoiceDTO;
import com.evfleet.billing.dto.SubscriptionUpdateRequest;
import com.evfleet.billing.entity.*;
import com.evfleet.billing.service.BillingService;
import com.evfleet.billing.service.InvoiceGenerationService;
import com.evfleet.billing.service.PaymentProcessingService;
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
    private final InvoiceGenerationService invoiceGenerationService;
    private final PaymentProcessingService paymentProcessingService;
    
    // TODO: Extract company ID from authentication context in production
    // For demo purposes, using a default company ID
    // In production, this should be extracted from JWT token or authenticated user session
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

    // ========== PR 18: INVOICE GENERATION & PAYMENT PROCESSING ==========

    /**
     * Finalize invoice and prepare for payment.
     * Moves invoice from DRAFT to FINALIZED status.
     */
    @PostMapping("/invoices/{invoiceId}/finalize")
    @Operation(summary = "Finalize invoice", description = "Finalizes an invoice for payment processing")
    public ResponseEntity<String> finalizeInvoice(@PathVariable String invoiceId) {
        invoiceGenerationService.finalizeInvoice(invoiceId);
        return ResponseEntity.ok("Invoice finalized successfully");
    }

    /**
     * Get invoice details with full breakdown by tier.
     */
    @GetMapping("/invoices/{invoiceId}/details")
    @Operation(summary = "Get invoice details", description = "Retrieves complete invoice details including tier breakdown")
    public ResponseEntity<InvoiceDTO> getInvoiceDetails(@PathVariable String invoiceId) {
        InvoiceDTO invoiceDTO = invoiceGenerationService.getInvoiceDetails(invoiceId);
        return ResponseEntity.ok(invoiceDTO);
    }

    /**
     * Process payment for an invoice using PaymentRequestDTO.
     * Supports multiple payment methods: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, UPI
     */
    @PostMapping("/invoices/{invoiceId}/pay")
    @Operation(summary = "Process payment", description = "Processes payment for an invoice")
    public ResponseEntity<Payment> processPayment(
            @PathVariable String invoiceId,
            @Valid @RequestBody PaymentRequestDTO request) {
        Payment payment = paymentProcessingService.processPayment(
                invoiceId,
                request.getAmount(),
                request.getPaymentMethod()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * Retry failed payment for an invoice.
     */
    @PostMapping("/invoices/{invoiceId}/retry-payment")
    @Operation(summary = "Retry payment", description = "Retries payment for a failed invoice")
    public ResponseEntity<Payment> retryPayment(
            @PathVariable String invoiceId,
            @RequestParam String paymentMethod) {
        Payment payment = paymentProcessingService.retryPayment(invoiceId, paymentMethod);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payment history for an invoice.
     */
    @GetMapping("/invoices/{invoiceId}/payment-history")
    @Operation(summary = "Get payment history", description = "Retrieves all payment transactions for an invoice")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable String invoiceId) {
        List<Payment> payments = paymentProcessingService.getPaymentHistory(invoiceId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Verify payment status for an invoice.
     */
    @GetMapping("/invoices/{invoiceId}/payment-status")
    @Operation(summary = "Verify payment status", description = "Checks if payment was successfully processed")
    public ResponseEntity<Boolean> verifyPaymentStatus(@PathVariable String invoiceId) {
        boolean isPaid = paymentProcessingService.verifyPaymentStatus(invoiceId);
        return ResponseEntity.ok(isPaid);
    }

    /**
     * Handle overdue invoices and apply late fees.
     */
    @PostMapping("/invoices/{invoiceId}/handle-overdue")
    @Operation(summary = "Handle overdue invoice", description = "Applies late fees to overdue invoices (>30 days)")
    public ResponseEntity<String> handleOverdueInvoice(@PathVariable String invoiceId) {
        paymentProcessingService.handleOverdueInvoice(invoiceId);
        return ResponseEntity.ok("Overdue invoice processed");
    }
}
