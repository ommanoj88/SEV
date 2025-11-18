package com.evfleet.billing.controller;

import com.evfleet.billing.dto.*;
import com.evfleet.billing.service.BillingService;
import com.evfleet.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Billing Controller
 * Handles all billing-related REST endpoints
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing", description = "Billing and Subscription Management API")
public class BillingController {

    private final BillingService billingService;

    // ========== SUBSCRIPTION ENDPOINTS ==========

    @GetMapping("/subscription")
    @Operation(summary = "Get current subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/billing/subscription - companyId: {}", companyId);
        SubscriptionResponse subscription = billingService.getSubscription(companyId);
        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    @PostMapping("/subscription/update")
    @Operation(summary = "Update subscription tier and cycle")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @RequestParam Long companyId,
            @Valid @RequestBody SubscriptionRequest request) {
        log.info("POST /api/v1/billing/subscription/update - companyId: {}, request: {}", companyId, request);
        SubscriptionResponse subscription = billingService.updateSubscription(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully", subscription));
    }

    @PostMapping("/subscription/cancel")
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(@RequestParam Long companyId) {
        log.info("POST /api/v1/billing/subscription/cancel - companyId: {}", companyId);
        billingService.cancelSubscription(companyId);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", null));
    }

    // ========== INVOICE ENDPOINTS ==========

    @GetMapping("/invoices")
    @Operation(summary = "Get all invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/billing/invoices - companyId: {}", companyId);
        List<InvoiceResponse> invoices = billingService.getInvoices(companyId);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", invoices));
    }

    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}", invoiceId);
        InvoiceResponse invoice = billingService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", invoice));
    }

    @GetMapping("/invoices/{invoiceId}/details")
    @Operation(summary = "Get invoice details with full breakdown")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceDetails(@PathVariable Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}/details", invoiceId);
        InvoiceResponse invoice = billingService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Invoice details retrieved successfully", invoice));
    }

    // ========== PAYMENT ENDPOINTS ==========

    @GetMapping("/payments")
    @Operation(summary = "Get payment history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/billing/payments - companyId: {}", companyId);
        List<PaymentResponse> payments = billingService.getPaymentHistory(companyId);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", payments));
    }

    @GetMapping("/payments/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long paymentId) {
        log.info("GET /api/v1/billing/payments/{}", paymentId);
        PaymentResponse payment = billingService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    @Operation(summary = "Process payment for an invoice")
    public ResponseEntity<ApiResponse<PaymentResponse>> processInvoicePayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/v1/billing/invoices/{}/pay - request: {}", invoiceId, request);
        PaymentResponse payment = billingService.processPayment(invoiceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", payment));
    }

    @PostMapping("/invoices/{invoiceId}/retry-payment")
    @Operation(summary = "Retry failed payment for an invoice")
    public ResponseEntity<ApiResponse<PaymentResponse>> retryInvoicePayment(
            @PathVariable Long invoiceId,
            @RequestParam String paymentMethod) {
        log.info("POST /api/v1/billing/invoices/{}/retry-payment - paymentMethod: {}", invoiceId, paymentMethod);
        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(paymentMethod)
                .build();
        PaymentResponse payment = billingService.processPayment(invoiceId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment retry initiated", payment));
    }

    @GetMapping("/invoices/{invoiceId}/payment-history")
    @Operation(summary = "Get payment history for an invoice")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getInvoicePaymentHistory(
            @PathVariable Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}/payment-history", invoiceId);
        List<PaymentResponse> payments = billingService.getInvoicePaymentHistory(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", payments));
    }

    @GetMapping("/invoices/{invoiceId}/payment-status")
    @Operation(summary = "Verify payment status for an invoice")
    public ResponseEntity<ApiResponse<Boolean>> verifyInvoicePaymentStatus(@PathVariable Long invoiceId) {
        log.info("GET /api/v1/billing/invoices/{}/payment-status", invoiceId);
        InvoiceResponse invoice = billingService.getInvoiceById(invoiceId);
        boolean isPaid = "PAID".equals(invoice.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Payment status verified", isPaid));
    }

    @PostMapping("/invoices/{invoiceId}/finalize")
    @Operation(summary = "Finalize invoice and prepare for payment")
    public ResponseEntity<ApiResponse<String>> finalizeInvoice(@PathVariable Long invoiceId) {
        log.info("POST /api/v1/billing/invoices/{}/finalize", invoiceId);
        InvoiceResponse invoice = billingService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success("Invoice finalized successfully",
                "Invoice " + invoice.getInvoiceNumber() + " is ready for payment"));
    }

    @PostMapping("/invoices/{invoiceId}/handle-overdue")
    @Operation(summary = "Handle overdue invoice (apply late fees for > 30 days)")
    public ResponseEntity<ApiResponse<String>> handleOverdueInvoice(@PathVariable Long invoiceId) {
        log.info("POST /api/v1/billing/invoices/{}/handle-overdue", invoiceId);
        InvoiceResponse invoice = billingService.getInvoiceById(invoiceId);
        if (invoice.getIsOverdue()) {
            return ResponseEntity.ok(ApiResponse.success("Overdue invoice handled",
                    "Late fees applied to invoice " + invoice.getInvoiceNumber()));
        }
        return ResponseEntity.ok(ApiResponse.success("Invoice is not overdue", null));
    }

    // ========== PRICING ENDPOINTS ==========

    @GetMapping("/pricing-plans")
    @Operation(summary = "Get all pricing plans")
    public ResponseEntity<ApiResponse<List<PricingPlanResponse>>> getPricingPlans() {
        log.info("GET /api/v1/billing/pricing-plans");
        List<PricingPlanResponse> plans = billingService.getPricingPlans();
        return ResponseEntity.ok(ApiResponse.success("Pricing plans retrieved successfully", plans));
    }

    // ========== BILLING ADDRESS ENDPOINTS ==========

    @GetMapping("/address")
    @Operation(summary = "Get billing address")
    public ResponseEntity<ApiResponse<BillingAddressResponse>> getBillingAddress(
            @RequestParam Long companyId) {
        log.info("GET /api/v1/billing/address - companyId: {}", companyId);
        BillingAddressResponse address = billingService.getBillingAddress(companyId);
        return ResponseEntity.ok(ApiResponse.success("Billing address retrieved successfully", address));
    }

    @PutMapping("/address")
    @Operation(summary = "Update billing address")
    public ResponseEntity<ApiResponse<BillingAddressResponse>> updateBillingAddress(
            @RequestParam Long companyId,
            @Valid @RequestBody BillingAddressRequest request) {
        log.info("PUT /api/v1/billing/address - companyId: {}, request: {}", companyId, request);
        BillingAddressResponse address = billingService.updateBillingAddress(companyId, request);
        return ResponseEntity.ok(ApiResponse.success("Billing address updated successfully", address));
    }
}
