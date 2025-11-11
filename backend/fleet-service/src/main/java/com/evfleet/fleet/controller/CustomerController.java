package com.evfleet.fleet.controller;

import com.evfleet.fleet.model.Customer;
import com.evfleet.fleet.model.CustomerFeedback;
import com.evfleet.fleet.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers and their feedback")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        try {
            Customer created = customerService.createCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating customer", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        try {
            Customer customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{customerCode}")
    @Operation(summary = "Get customer by customer code")
    public ResponseEntity<Customer> getCustomerByCode(@PathVariable String customerCode) {
        try {
            Customer customer = customerService.getCustomerByCode(customerCode);
            return ResponseEntity.ok(customer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active customers")
    public ResponseEntity<List<Customer>> getActiveCustomers() {
        List<Customer> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers by name")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String name) {
        List<Customer> customers = customerService.searchCustomersByName(name);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get customers by city")
    public ResponseEntity<List<Customer>> getCustomersByCity(@PathVariable String city) {
        List<Customer> customers = customerService.getCustomersByCity(city);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated customers")
    public ResponseEntity<List<Customer>> getTopRatedCustomers(@RequestParam(defaultValue = "4.0") BigDecimal minRating) {
        List<Customer> customers = customerService.getTopRatedCustomers(minRating);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/over-credit-limit")
    @Operation(summary = "Get customers over credit limit")
    public ResponseEntity<List<Customer>> getCustomersOverCreditLimit() {
        List<Customer> customers = customerService.getCustomersOverCreditLimit();
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            Customer updated = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/balance")
    @Operation(summary = "Update customer outstanding balance")
    public ResponseEntity<Customer> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        try {
            Customer updated = customerService.updateOutstandingBalance(id, amount);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/delivery")
    @Operation(summary = "Record a delivery for customer")
    public ResponseEntity<Customer> recordDelivery(
            @PathVariable Long id,
            @RequestParam boolean success) {
        try {
            Customer updated = customerService.recordDelivery(id, success);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Feedback endpoints
    @PostMapping("/feedback")
    @Operation(summary = "Add customer feedback")
    public ResponseEntity<CustomerFeedback> addFeedback(@RequestBody CustomerFeedback feedback) {
        try {
            CustomerFeedback created = customerService.addFeedback(feedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error adding feedback", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/feedback")
    @Operation(summary = "Get feedback for a customer")
    public ResponseEntity<List<CustomerFeedback>> getCustomerFeedback(@PathVariable Long id) {
        List<CustomerFeedback> feedback = customerService.getCustomerFeedback(id);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/feedback/unaddressed")
    @Operation(summary = "Get unaddressed feedback")
    public ResponseEntity<List<CustomerFeedback>> getUnaddressedFeedback() {
        List<CustomerFeedback> feedback = customerService.getUnaddressedFeedback();
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/feedback/negative")
    @Operation(summary = "Get negative feedback")
    public ResponseEntity<List<CustomerFeedback>> getNegativeFeedback() {
        List<CustomerFeedback> feedback = customerService.getNegativeFeedback();
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/feedback/{id}/respond")
    @Operation(summary = "Respond to customer feedback")
    public ResponseEntity<CustomerFeedback> respondToFeedback(
            @PathVariable Long id,
            @RequestParam String responseText,
            @RequestParam String respondedBy) {
        try {
            CustomerFeedback updated = customerService.respondToFeedback(id, responseText, respondedBy);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
