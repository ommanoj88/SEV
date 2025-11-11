package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Document;
import com.evfleet.fleet.model.DocumentStatus;
import com.evfleet.fleet.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find by entity
    List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);

    // Find by document type
    List<Document> findByDocumentType(DocumentType documentType);

    // Find by status
    List<Document> findByDocumentStatus(DocumentStatus status);

    // Find expired documents
    @Query("SELECT d FROM Document d WHERE d.expiryDate < CURRENT_DATE")
    List<Document> findExpiredDocuments();

    // Find expiring soon documents
    @Query("SELECT d FROM Document d WHERE d.expiryDate BETWEEN CURRENT_DATE AND :endDate")
    List<Document> findExpiringDocuments(@Param("endDate") LocalDate endDate);

    // Find documents requiring action
    @Query("SELECT d FROM Document d WHERE d.documentStatus IN ('EXPIRED', 'EXPIRING_SOON', 'PENDING_RENEWAL')")
    List<Document> findDocumentsRequiringAction();

    // Find by entity and type
    List<Document> findByEntityTypeAndEntityIdAndDocumentType(
        String entityType, Long entityId, DocumentType documentType);

    // Check if document exists
    boolean existsByEntityTypeAndEntityIdAndDocumentType(
        String entityType, Long entityId, DocumentType documentType);

    // Find unverified documents
    List<Document> findByIsVerified(Boolean isVerified);

    // Find by created date range
    List<Document> findByCreatedAtBetween(
        java.time.LocalDateTime startDate, 
        java.time.LocalDateTime endDate
    );
}
