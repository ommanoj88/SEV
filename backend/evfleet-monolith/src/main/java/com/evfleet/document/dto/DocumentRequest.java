package com.evfleet.document.dto;

import com.evfleet.document.model.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Document Request DTO
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    
    private String documentType;
    private String entityType;
    private Long entityId;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String notes;

    public Document toEntity() {
        return Document.builder()
            .documentType(Document.DocumentType.valueOf(documentType))
            .entityType(Document.EntityType.valueOf(entityType))
            .entityId(entityId)
            .documentNumber(documentNumber)
            .issueDate(issueDate)
            .expiryDate(expiryDate)
            .status(Document.DocumentStatus.PENDING)
            .notes(notes)
            .build();
    }
}
