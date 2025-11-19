package com.evfleet.document.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.document.model.Document;
import com.evfleet.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Document Service
 * 
 * Handles document management operations
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload a new document
     */
    public Document uploadDocument(Document document, MultipartFile file) {
        log.info("Uploading document: {} for {} ID: {}", 
            document.getDocumentType(), document.getEntityType(), document.getEntityId());

        // Store file
        String storedFileName = fileStorageService.storeFile(file);
        
        // Set file metadata
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(storedFileName);
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setStatus(Document.DocumentStatus.PENDING);

        // Auto-mark as expired if expiry date is in the past
        if (document.getExpiryDate() != null && document.getExpiryDate().isBefore(LocalDate.now())) {
            document.setStatus(Document.DocumentStatus.EXPIRED);
        }

        Document saved = documentRepository.save(document);
        log.info("Document uploaded successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Download a document
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long id) {
        log.info("Downloading document: {}", id);
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        return fileStorageService.loadFileAsResource(document.getFilePath());
    }

    /**
     * Verify a document
     */
    public Document verifyDocument(Long id, Long verifiedBy) {
        log.info("Verifying document: {} by user: {}", id, verifiedBy);
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        document.setStatus(Document.DocumentStatus.VERIFIED);
        document.setVerifiedBy(verifiedBy);
        document.setVerificationDate(LocalDate.now());
        
        Document saved = documentRepository.save(document);
        log.info("Document verified successfully: {}", id);
        return saved;
    }

    /**
     * Reject a document
     */
    public Document rejectDocument(Long id, String notes) {
        log.info("Rejecting document: {}", id);
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        document.setStatus(Document.DocumentStatus.REJECTED);
        if (notes != null) {
            document.setNotes(notes);
        }
        
        Document saved = documentRepository.save(document);
        log.info("Document rejected: {}", id);
        return saved;
    }

    /**
     * Delete a document
     */
    public void deleteDocument(Long id) {
        log.info("Deleting document: {}", id);
        
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        
        // Delete file from storage
        if (document.getFilePath() != null) {
            fileStorageService.deleteFile(document.getFilePath());
        }
        
        // Delete from database
        documentRepository.delete(document);
        log.info("Document deleted successfully: {}", id);
    }

    /**
     * Get all documents for an entity (vehicle or driver)
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByEntity(Document.EntityType entityType, Long entityId) {
        log.info("Fetching documents for {} ID: {}", entityType, entityId);
        return documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    /**
     * Get documents that require action (pending, expiring soon, expired)
     */
    @Transactional(readOnly = true)
    public List<Document> getActionRequiredDocuments() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return documentRepository.findActionRequiredDocuments(today, thirtyDaysFromNow);
    }

    /**
     * Get documents by status
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByStatus(Document.DocumentStatus status) {
        return documentRepository.findByStatus(status);
    }
}
