package com.evfleet.document.controller;

import com.evfleet.document.dto.DocumentRequest;
import com.evfleet.document.dto.DocumentResponse;
import com.evfleet.document.model.Document;
import com.evfleet.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Document Controller
 * 
 * Handles document management operations
 * Provides API endpoints for the frontend DocumentManagementPage
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documents", description = "Document Management API")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a new document
     * POST /api/documents
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "notes", required = false) String notes) {
        
        log.info("POST /api/documents - Uploading {} for {} ID: {}", documentType, entityType, entityId);

        // Build document entity
        Document document = Document.builder()
            .documentType(Document.DocumentType.valueOf(documentType))
            .entityType(Document.EntityType.valueOf(entityType))
            .entityId(entityId)
            .documentNumber(documentNumber)
            .issueDate(issueDate != null ? LocalDate.parse(issueDate) : null)
            .expiryDate(expiryDate != null ? LocalDate.parse(expiryDate) : null)
            .notes(notes)
            .build();

        Document saved = documentService.uploadDocument(document, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(saved));
    }

    /**
     * Get document by ID
     * GET /api/documents/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        log.info("GET /api/documents/{}", id);
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * Download document file
     * GET /api/documents/{id}/download
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Download document file")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        log.info("GET /api/documents/{}/download", id);
        
        Document document = documentService.getDocumentById(id);
        Resource resource = documentService.downloadDocument(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getMimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .body(resource);
    }

    /**
     * Verify a document
     * PUT /api/documents/{id}/verify
     */
    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify a document")
    public ResponseEntity<DocumentResponse> verifyDocument(
            @PathVariable Long id,
            @RequestParam(value = "verifiedBy", required = false, defaultValue = "1") Long verifiedBy) {
        log.info("PUT /api/documents/{}/verify", id);
        Document document = documentService.verifyDocument(id, verifiedBy);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * Reject a document
     * PUT /api/documents/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a document")
    public ResponseEntity<DocumentResponse> rejectDocument(
            @PathVariable Long id,
            @RequestParam(value = "notes", required = false) String notes) {
        log.info("PUT /api/documents/{}/reject", id);
        Document document = documentService.rejectDocument(id, notes);
        return ResponseEntity.ok(DocumentResponse.from(document));
    }

    /**
     * Delete a document
     * DELETE /api/documents/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        log.info("DELETE /api/documents/{}", id);
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get documents by entity (vehicle or driver)
     * GET /api/documents?entityType=VEHICLE&entityId=1
     */
    @GetMapping
    @Operation(summary = "Get documents by entity")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        log.info("GET /api/documents?entityType={}&entityId={}", entityType, entityId);
        
        List<DocumentResponse> documents = documentService
            .getDocumentsByEntity(Document.EntityType.valueOf(entityType), entityId)
            .stream()
            .map(DocumentResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents that require action
     * GET /api/documents/action-required
     */
    @GetMapping("/action-required")
    @Operation(summary = "Get documents that require action")
    public ResponseEntity<List<DocumentResponse>> getActionRequiredDocuments() {
        log.info("GET /api/documents/action-required");
        
        List<DocumentResponse> documents = documentService.getActionRequiredDocuments()
            .stream()
            .map(DocumentResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by status
     * GET /api/documents/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByStatus(@PathVariable String status) {
        log.info("GET /api/documents/status/{}", status);
        
        List<DocumentResponse> documents = documentService
            .getDocumentsByStatus(Document.DocumentStatus.valueOf(status))
            .stream()
            .map(DocumentResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(documents);
    }
}
