package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.DocumentDTO;
import com.evfleet.fleet.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Management", description = "APIs for managing vehicle and driver documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document", description = "Upload a new document for a vehicle or driver")
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestPart("document") DocumentDTO documentDTO,
            @RequestPart("file") MultipartFile file) {
        try {
            DocumentDTO created = documentService.createDocument(documentDTO, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IOException e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get documents by entity", description = "Get all documents for a vehicle or driver")
    public ResponseEntity<List<DocumentDTO>> getDocumentsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<DocumentDTO> documents = documentService.getDocumentsByEntity(entityType, entityId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired documents")
    public ResponseEntity<List<DocumentDTO>> getExpiredDocuments() {
        List<DocumentDTO> documents = documentService.getExpiredDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring documents", description = "Get documents expiring within specified days")
    public ResponseEntity<List<DocumentDTO>> getExpiringDocuments(
            @RequestParam(defaultValue = "30") int days) {
        List<DocumentDTO> documents = documentService.getExpiringDocuments(days);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/action-required")
    @Operation(summary = "Get documents requiring action")
    public ResponseEntity<List<DocumentDTO>> getDocumentsRequiringAction() {
        List<DocumentDTO> documents = documentService.getDocumentsRequiringAction();
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document")
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentDTO documentDTO) {
        try {
            DocumentDTO updated = documentService.updateDocument(id, documentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify a document")
    public ResponseEntity<DocumentDTO> verifyDocument(
            @PathVariable Long id,
            @RequestParam String verifiedBy) {
        try {
            DocumentDTO verified = documentService.verifyDocument(id, verifiedBy);
            return ResponseEntity.ok(verified);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
