package com.evfleet.customer.dto;

import com.evfleet.customer.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for customer
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private Long companyId;
    private Customer.CustomerType customerType;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String gstin;
    private String pan;
    private String businessName;
    private String dateOfBirth;
    private Customer.CustomerStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static CustomerResponse from(Customer customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .companyId(customer.getCompanyId())
            .customerType(customer.getCustomerType())
            .name(customer.getName())
            .email(customer.getEmail())
            .phone(customer.getPhone())
            .address(customer.getAddress())
            .city(customer.getCity())
            .state(customer.getState())
            .postalCode(customer.getPostalCode())
            .country(customer.getCountry())
            .gstin(customer.getGstin())
            .pan(customer.getPan())
            .businessName(customer.getBusinessName())
            .dateOfBirth(customer.getDateOfBirth())
            .status(customer.getStatus())
            .notes(customer.getNotes())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}
