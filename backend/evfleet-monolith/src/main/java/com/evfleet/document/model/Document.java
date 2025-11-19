package com.evfleet.document.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Document Entity
 * 
 * Represents documents such as RC, Insurance, Licenses, Fitness Certificates
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_type", columnList = "document_type"),
    @Index(name = "idx_document_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_document_status", columnList = "status"),
    @Index(name = "idx_document_expiry", columnList = "expiry_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verification_date")
    private LocalDate verificationDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum DocumentType {
        RC,                     // Registration Certificate
        INSURANCE,              // Insurance Certificate
        PERMIT,                 // Commercial Permit
        FITNESS_CERTIFICATE,    // Fitness Certificate
        POLLUTION_CERTIFICATE,  // Pollution Under Control Certificate
        DRIVER_LICENSE,         // Driver's License
        AADHAR_CARD,           // Aadhar Card
        PAN_CARD,              // PAN Card
        OTHER                  // Other documents
    }

    public enum EntityType {
        VEHICLE,
        DRIVER
    }

    public enum DocumentStatus {
        PENDING,      // Uploaded but not verified
        VERIFIED,     // Verified and approved
        REJECTED,     // Rejected due to issues
        EXPIRED       // Document has expired
    }

    /**
     * Check if the document is expired
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if the document is expiring soon (within 30 days)
     */
    public boolean isExpiringSoon() {
        if (expiryDate == null) {
            return false;
        }
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return expiryDate.isAfter(LocalDate.now()) && expiryDate.isBefore(thirtyDaysFromNow);
    }
}
