package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.DocumentDTO;
import com.evfleet.fleet.model.Document;
import com.evfleet.fleet.model.DocumentStatus;
import com.evfleet.fleet.model.DocumentType;
import com.evfleet.fleet.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private static final String UPLOAD_DIR = "/app/documents/"; // Docker volume path

    @Transactional
    public DocumentDTO createDocument(DocumentDTO dto, MultipartFile file) throws IOException {
        // Save file
        String filePath = saveFile(file, dto.getEntityType(), dto.getEntityId());

        Document document = Document.builder()
                .documentNumber(dto.getDocumentNumber())
                .documentType(dto.getDocumentType())
                .documentStatus(DocumentStatus.ACTIVE)
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .issueDate(dto.getIssueDate())
                .expiryDate(dto.getExpiryDate())
                .issuingAuthority(dto.getIssuingAuthority())
                .issuingLocation(dto.getIssuingLocation())
                .isVerified(false)
                .tags(dto.getTags())
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy())
                .build();

        Document saved = documentRepository.save(document);
        log.info("Document created: {} for {} ID: {}", saved.getDocumentType(), saved.getEntityType(), saved.getEntityId());

        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByEntity(String entityType, Long entityId) {
        List<Document> documents = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return documents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return convertToDTO(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getExpiredDocuments() {
        List<Document> documents = documentRepository.findExpiredDocuments();
        return documents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getExpiringDocuments(int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<Document> documents = documentRepository.findExpiringDocuments(endDate);
        return documents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsRequiringAction() {
        List<Document> documents = documentRepository.findDocumentsRequiringAction();
        return documents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public DocumentDTO verifyDocument(Long id, String verifiedBy) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        document.setIsVerified(true);
        document.setVerifiedBy(verifiedBy);
        document.setVerifiedAt(LocalDateTime.now());

        Document updated = documentRepository.save(document);
        log.info("Document verified: {} by {}", id, verifiedBy);

        return convertToDTO(updated);
    }

    @Transactional
    public DocumentDTO updateDocument(Long id, DocumentDTO dto) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        document.setDocumentNumber(dto.getDocumentNumber());
        document.setTitle(dto.getTitle());
        document.setDescription(dto.getDescription());
        document.setIssueDate(dto.getIssueDate());
        document.setExpiryDate(dto.getExpiryDate());
        document.setIssuingAuthority(dto.getIssuingAuthority());
        document.setIssuingLocation(dto.getIssuingLocation());
        document.setTags(dto.getTags());
        document.setNotes(dto.getNotes());
        document.setUpdatedBy(dto.getUpdatedBy());

        Document updated = documentRepository.save(document);
        log.info("Document updated: {}", id);

        return convertToDTO(updated);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        // Delete file
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.error("Error deleting file: {}", document.getFilePath(), e);
        }

        documentRepository.delete(document);
        log.info("Document deleted: {}", id);
    }

    private String saveFile(MultipartFile file, String entityType, Long entityId) throws IOException {
        String uploadPath = UPLOAD_DIR + entityType.toLowerCase() + "/" + entityId + "/";
        Path uploadDir = Paths.get(uploadPath);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private DocumentDTO convertToDTO(Document document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .documentNumber(document.getDocumentNumber())
                .documentType(document.getDocumentType())
                .documentStatus(document.getDocumentStatus())
                .entityType(document.getEntityType())
                .entityId(document.getEntityId())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .mimeType(document.getMimeType())
                .issueDate(document.getIssueDate())
                .expiryDate(document.getExpiryDate())
                .issuingAuthority(document.getIssuingAuthority())
                .issuingLocation(document.getIssuingLocation())
                .isVerified(document.getIsVerified())
                .verifiedBy(document.getVerifiedBy())
                .verifiedAt(document.getVerifiedAt())
                .tags(document.getTags())
                .notes(document.getNotes())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .createdBy(document.getCreatedBy())
                .updatedBy(document.getUpdatedBy())
                .daysUntilExpiry(document.getDaysUntilExpiry())
                .isExpired(document.isExpired())
                .requiresAction(document.getDocumentStatus().requiresAction())
                .build();
    }
}
