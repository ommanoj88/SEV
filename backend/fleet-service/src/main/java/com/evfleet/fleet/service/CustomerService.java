package com.evfleet.fleet.service;

import com.evfleet.fleet.model.Customer;
import com.evfleet.fleet.model.CustomerFeedback;
import com.evfleet.fleet.repository.CustomerFeedbackRepository;
import com.evfleet.fleet.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerFeedbackRepository feedbackRepository;

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Validate customer code uniqueness
        if (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new RuntimeException("Customer code already exists: " + customer.getCustomerCode());
        }

        // Set default values
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }
        if (customer.getOutstandingBalance() == null) {
            customer.setOutstandingBalance(BigDecimal.ZERO);
        }
        if (customer.getTotalDeliveries() == null) {
            customer.setTotalDeliveries(0);
        }
        if (customer.getSuccessfulDeliveries() == null) {
            customer.setSuccessfulDeliveries(0);
        }
        if (customer.getFailedDeliveries() == null) {
            customer.setFailedDeliveries(0);
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: {} - {}", saved.getCustomerCode(), saved.getCustomerName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new RuntimeException("Customer not found with code: " + customerCode));
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByIsActive(true);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.searchByName(name);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByCity(String city) {
        return customerRepository.findByCity(city);
    }

    @Transactional(readOnly = true)
    public List<Customer> getTopRatedCustomers(BigDecimal minRating) {
        return customerRepository.findTopRatedCustomers(minRating);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersOverCreditLimit() {
        return customerRepository.findCustomersOverCreditLimit();
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomerById(id);

        // Update fields
        if (customer.getCustomerName() != null) {
            existing.setCustomerName(customer.getCustomerName());
        }
        if (customer.getCustomerType() != null) {
            existing.setCustomerType(customer.getCustomerType());
        }
        if (customer.getPrimaryContactName() != null) {
            existing.setPrimaryContactName(customer.getPrimaryContactName());
        }
        if (customer.getPrimaryPhone() != null) {
            existing.setPrimaryPhone(customer.getPrimaryPhone());
        }
        if (customer.getSecondaryPhone() != null) {
            existing.setSecondaryPhone(customer.getSecondaryPhone());
        }
        if (customer.getEmail() != null) {
            existing.setEmail(customer.getEmail());
        }
        if (customer.getAddressLine1() != null) {
            existing.setAddressLine1(customer.getAddressLine1());
        }
        if (customer.getAddressLine2() != null) {
            existing.setAddressLine2(customer.getAddressLine2());
        }
        if (customer.getCity() != null) {
            existing.setCity(customer.getCity());
        }
        if (customer.getState() != null) {
            existing.setState(customer.getState());
        }
        if (customer.getPostalCode() != null) {
            existing.setPostalCode(customer.getPostalCode());
        }
        if (customer.getLatitude() != null) {
            existing.setLatitude(customer.getLatitude());
        }
        if (customer.getLongitude() != null) {
            existing.setLongitude(customer.getLongitude());
        }
        if (customer.getCreditLimit() != null) {
            existing.setCreditLimit(customer.getCreditLimit());
        }
        if (customer.getIsActive() != null) {
            existing.setIsActive(customer.getIsActive());
        }
        if (customer.getSpecialInstructions() != null) {
            existing.setSpecialInstructions(customer.getSpecialInstructions());
        }

        Customer updated = customerRepository.save(existing);
        log.info("Customer updated: {}", updated.getCustomerCode());
        return updated;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
        log.info("Customer deleted: {}", id);
    }

    @Transactional
    public Customer updateOutstandingBalance(Long customerId, BigDecimal amount) {
        Customer customer = getCustomerById(customerId);
        BigDecimal newBalance = customer.getOutstandingBalance().add(amount);
        customer.setOutstandingBalance(newBalance);
        
        Customer updated = customerRepository.save(customer);
        log.info("Customer {} balance updated: {}", customer.getCustomerCode(), newBalance);
        return updated;
    }

    @Transactional
    public Customer recordDelivery(Long customerId, boolean success) {
        Customer customer = getCustomerById(customerId);
        customer.setTotalDeliveries(customer.getTotalDeliveries() + 1);
        
        if (success) {
            customer.setSuccessfulDeliveries(customer.getSuccessfulDeliveries() + 1);
        } else {
            customer.setFailedDeliveries(customer.getFailedDeliveries() + 1);
        }

        Customer updated = customerRepository.save(customer);
        log.info("Delivery recorded for customer {}: success={}", customer.getCustomerCode(), success);
        return updated;
    }

    @Transactional
    public CustomerFeedback addFeedback(CustomerFeedback feedback) {
        CustomerFeedback saved = feedbackRepository.save(feedback);
        
        // Update customer's average rating
        updateCustomerRating(feedback.getCustomerId());
        
        log.info("Feedback added for customer {}: rating={}", feedback.getCustomerId(), feedback.getRating());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CustomerFeedback> getCustomerFeedback(Long customerId) {
        return feedbackRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<CustomerFeedback> getUnaddressedFeedback() {
        return feedbackRepository.findRecentUnaddressedFeedback();
    }

    @Transactional(readOnly = true)
    public List<CustomerFeedback> getNegativeFeedback() {
        return feedbackRepository.findNegativeFeedback();
    }

    @Transactional
    public CustomerFeedback respondToFeedback(Long feedbackId, String responseText, String respondedBy) {
        CustomerFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + feedbackId));

        feedback.setResponseText(responseText);
        feedback.setRespondedBy(respondedBy);
        feedback.setRespondedAt(LocalDateTime.now());
        feedback.setIsAddressed(true);

        CustomerFeedback updated = feedbackRepository.save(feedback);
        log.info("Feedback {} addressed by {}", feedbackId, respondedBy);
        return updated;
    }

    @Transactional
    public void updateCustomerRating(Long customerId) {
        Double averageRating = feedbackRepository.calculateAverageRatingForCustomer(customerId);
        if (averageRating != null) {
            Customer customer = getCustomerById(customerId);
            customer.setServiceRating(BigDecimal.valueOf(averageRating));
            customerRepository.save(customer);
            log.info("Customer {} rating updated: {}", customer.getCustomerCode(), averageRating);
        }
    }
}
