package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.DocumentStatus;
import com.evfleet.fleet.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private Long id;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    private DocumentStatus documentStatus;

    @NotBlank(message = "Entity type is required (VEHICLE or DRIVER)")
    private String entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "File name is required")
    private String fileName;

    private String filePath;
    private Long fileSize;
    private String mimeType;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    private String issuingAuthority;
    private String issuingLocation;

    private Boolean isVerified;
    private String verifiedBy;
    private java.time.LocalDateTime verifiedAt;

    private String[] tags;
    private String notes;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Computed fields
    private Long daysUntilExpiry;
    private Boolean isExpired;
    private Boolean requiresAction;
}
