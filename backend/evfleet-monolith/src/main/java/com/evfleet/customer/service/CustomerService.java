package com.evfleet.customer.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.customer.dto.CustomerRequest;
import com.evfleet.customer.dto.FeedbackRequest;
import com.evfleet.customer.model.Customer;
import com.evfleet.customer.model.CustomerFeedback;
import com.evfleet.customer.repository.CustomerFeedbackRepository;
import com.evfleet.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Customer Management operations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerFeedbackRepository feedbackRepository;

    /**
     * Get all customers for a company
     */
    public List<Customer> getCustomersByCompany(Long companyId) {
        log.info("Getting customers for company: {}", companyId);
        return customerRepository.findByCompanyId(companyId);
    }

    /**
     * Get customer by ID
     */
    public Customer getCustomerById(Long id) {
        log.info("Getting customer by ID: {}", id);
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    /**
     * Create a new customer
     */
    @Transactional
    public Customer createCustomer(CustomerRequest request) {
        log.info("Creating new customer: {}", request.getName());

        Customer customer = Customer.builder()
            .companyId(request.getCompanyId())
            .customerType(request.getCustomerType())
            .name(request.getName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .gstin(request.getGstin())
            .pan(request.getPan())
            .businessName(request.getBusinessName())
            .dateOfBirth(request.getDateOfBirth())
            .status(request.getStatus() != null ? request.getStatus() : Customer.CustomerStatus.ACTIVE)
            .notes(request.getNotes())
            .build();

        return customerRepository.save(customer);
    }

    /**
     * Update an existing customer
     */
    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer: {}", id);

        Customer customer = getCustomerById(id);
        customer.setCustomerType(request.getCustomerType());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customer.setCountry(request.getCountry());
        customer.setGstin(request.getGstin());
        customer.setPan(request.getPan());
        customer.setBusinessName(request.getBusinessName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setStatus(request.getStatus());
        customer.setNotes(request.getNotes());

        return customerRepository.save(customer);
    }

    /**
     * Delete a customer
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer: {}", id);
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    /**
     * Submit customer feedback
     */
    @Transactional
    public CustomerFeedback submitFeedback(FeedbackRequest request) {
        log.info("Submitting feedback for customer: {}", request.getCustomerId());

        // Verify customer exists
        getCustomerById(request.getCustomerId());

        CustomerFeedback feedback = CustomerFeedback.builder()
            .customerId(request.getCustomerId())
            .tripId(request.getTripId())
            .driverId(request.getDriverId())
            .vehicleId(request.getVehicleId())
            .rating(request.getRating())
            .comment(request.getComment())
            .feedbackType(CustomerFeedback.FeedbackType.GENERAL)
            .build();

        return feedbackRepository.save(feedback);
    }

    /**
     * Get feedback for a customer
     */
    public List<CustomerFeedback> getFeedbackByCustomer(Long customerId) {
        log.info("Getting feedback for customer: {}", customerId);
        return feedbackRepository.findByCustomerId(customerId);
    }

    /**
     * Get all feedback
     */
    public List<CustomerFeedback> getAllFeedback() {
        log.info("Getting all feedback");
        return feedbackRepository.findAll();
    }
}
