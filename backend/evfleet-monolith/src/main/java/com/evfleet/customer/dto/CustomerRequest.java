package com.evfleet.customer.dto;

import com.evfleet.customer.model.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a customer
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Customer type is required")
    private Customer.CustomerType customerType;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String address;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    // Business fields
    private String gstin;

    private String pan;

    private String businessName;

    // Individual fields
    private String dateOfBirth;

    private Customer.CustomerStatus status;

    private String notes;
}
