package com.evfleet.document.repository;

import com.evfleet.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Document Repository
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByEntityTypeAndEntityId(Document.EntityType entityType, Long entityId);
    
    List<Document> findByStatus(Document.DocumentStatus status);
    
    List<Document> findByExpiryDateBefore(LocalDate date);
    
    List<Document> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT d FROM Document d WHERE d.status = 'PENDING' OR " +
           "(d.expiryDate IS NOT NULL AND d.expiryDate < :thirtyDaysFromNow AND d.expiryDate >= :today) OR " +
           "(d.expiryDate IS NOT NULL AND d.expiryDate < :today)")
    List<Document> findActionRequiredDocuments(@Param("today") LocalDate today, 
                                               @Param("thirtyDaysFromNow") LocalDate thirtyDaysFromNow);
}
