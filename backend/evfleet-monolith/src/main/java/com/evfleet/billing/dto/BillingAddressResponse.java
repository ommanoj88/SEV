package com.evfleet.billing.dto;

import com.evfleet.billing.model.BillingAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Billing Address Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingAddressResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String gstNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BillingAddressResponse fromEntity(BillingAddress address) {
        return BillingAddressResponse.builder()
                .id(address.getId())
                .companyId(address.getCompanyId())
                .companyName(address.getCompanyName())
                .gstNumber(address.getGstNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .contactPerson(address.getContactPerson())
                .contactEmail(address.getContactEmail())
                .contactPhone(address.getContactPhone())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
