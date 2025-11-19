package com.evfleet.document.dto;

import com.evfleet.document.model.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Document Response DTO
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    
    private Long id;
    private String documentType;
    private String entityType;
    private Long entityId;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String status;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Long verifiedBy;
    private LocalDate verificationDate;
    private String notes;
    private boolean expired;
    private boolean expiringSoon;

    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
            .id(document.getId())
            .documentType(document.getDocumentType().name())
            .entityType(document.getEntityType().name())
            .entityId(document.getEntityId())
            .documentNumber(document.getDocumentNumber())
            .issueDate(document.getIssueDate())
            .expiryDate(document.getExpiryDate())
            .status(document.getStatus().name())
            .fileName(document.getFileName())
            .fileSize(document.getFileSize())
            .mimeType(document.getMimeType())
            .verifiedBy(document.getVerifiedBy())
            .verificationDate(document.getVerificationDate())
            .notes(document.getNotes())
            .expired(document.isExpired())
            .expiringSoon(document.isExpiringSoon())
            .build();
    }
}
