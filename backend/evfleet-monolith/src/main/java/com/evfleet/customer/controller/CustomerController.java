package com.evfleet.customer.controller;

import com.evfleet.common.dto.ApiResponse;
import com.evfleet.customer.dto.CustomerRequest;
import com.evfleet.customer.dto.CustomerResponse;
import com.evfleet.customer.dto.FeedbackRequest;
import com.evfleet.customer.dto.FeedbackResponse;
import com.evfleet.customer.model.Customer;
import com.evfleet.customer.model.CustomerFeedback;
import com.evfleet.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Management Controller
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "Customer & Feedback Management API")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Get all customers
     */
    @GetMapping
    @Operation(summary = "Get all customers for a company")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false) Long companyId) {
        log.info("GET /api/customers - companyId: {}", companyId);

        if (companyId == null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Company ID is required", List.of()));
        }

        List<CustomerResponse> customers = customerService.getCustomersByCompany(companyId)
            .stream()
            .map(CustomerResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Customers retrieved successfully", customers));
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{}", id);

        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Customer retrieved successfully", CustomerResponse.from(customer)));
    }

    /**
     * Create a new customer
     */
    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        log.info("POST /api/customers - Creating customer: {}", request.getName());

        Customer customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Customer created successfully", CustomerResponse.from(customer)));
    }

    /**
     * Update a customer
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        log.info("PUT /api/customers/{}", id);

        Customer customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(
            "Customer updated successfully", CustomerResponse.from(customer)));
    }

    /**
     * Delete a customer
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{}", id);

        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    /**
     * Submit customer feedback
     */
    @PostMapping("/feedback")
    @Operation(summary = "Submit customer feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request) {
        log.info("POST /api/customers/feedback - Customer: {}", request.getCustomerId());

        CustomerFeedback feedback = customerService.submitFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Feedback submitted successfully", FeedbackResponse.from(feedback)));
    }

    /**
     * Get feedback for a customer
     */
    @GetMapping("/{id}/feedback")
    @Operation(summary = "Get feedback for a customer")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getCustomerFeedback(@PathVariable Long id) {
        log.info("GET /api/customers/{}/feedback", id);

        List<FeedbackResponse> feedback = customerService.getFeedbackByCustomer(id)
            .stream()
            .map(FeedbackResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
            "Feedback retrieved successfully", feedback));
    }
}
