package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import com.ibm.cmod.ondemand.entity.Statement;
import com.ibm.cmod.ondemand.exception.StatementNotFoundException;
import com.ibm.cmod.ondemand.repository.StatementRepository;
import com.ibm.cmod.ondemand.service.odwek.ODWEKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Statement business logic
 */
@Service
@Transactional
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    private final StatementRepository statementRepository;
    private final ODWEKClient odwekClient;
    private final FileStorageService fileStorageService;

    public StatementService(StatementRepository statementRepository,
                           ODWEKClient odwekClient,
                           FileStorageService fileStorageService) {
        this.statementRepository = statementRepository;
        this.odwekClient = odwekClient;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create a new statement
     */
    public StatementResponse createStatement(StatementRequest request) {
        logger.info("Creating statement for customer: {}", request.getCustomerId());

        // Build statement entity
        Statement statement = Statement.builder()
                .customerId(request.getCustomerId())
                .statementDate(request.getStatementDate())
                .documentType(request.getDocumentType() != null ? request.getDocumentType() : "MONTHLY_STATEMENT")
                .status(Statement.StatementStatus.PENDING)
                .build();

        // Fetch AFP from ODWEK (simulated)
        byte[] afpData = odwekClient.fetchDocument(statement.getCustomerId(), statement.getStatementDate());

        // Store file
        String filename = generateFilename(statement);
        String filePath = fileStorageService.storeFile(filename, afpData);
        statement.setDocumentPath(filePath);
        statement.setFileSizeBytes(fileStorageService.getFileSize(filename));
        statement.setStatus(Statement.StatementStatus.AVAILABLE);

        // Save to database
        Statement saved = statementRepository.save(statement);
        logger.info("Statement created successfully with ID: {}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Get statement by ID
     */
    @Transactional(readOnly = true)
    public StatementResponse getStatement(String id) {
        logger.info("Fetching statement with ID: {}", id);
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException("Statement not found with ID: " + id));
        return toResponse(statement);
    }

    /**
     * Get statements by customer ID
     */
    @Transactional(readOnly = true)
    public List<StatementResponse> getStatementsByCustomer(String customerId) {
        logger.info("Fetching statements for customer: {}", customerId);
        List<Statement> statements = statementRepository.findByCustomerId(customerId);
        return statements.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all statements
     */
    @Transactional(readOnly = true)
    public List<StatementResponse> getAllStatements() {
        logger.info("Fetching all statements");
        List<Statement> statements = statementRepository.findAll();
        return statements.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update statement
     */
    public StatementResponse updateStatement(String id, StatementRequest request) {
        logger.info("Updating statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException("Statement not found with ID: " + id));

        // Update fields
        statement.setCustomerId(request.getCustomerId());
        statement.setStatementDate(request.getStatementDate());
        if (request.getDocumentType() != null) {
            statement.setDocumentType(request.getDocumentType());
        }

        Statement updated = statementRepository.save(statement);
        logger.info("Statement updated successfully: {}", id);

        return toResponse(updated);
    }

    /**
     * Delete statement (soft delete)
     */
    public void deleteStatement(String id) {
        logger.info("Deleting statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException("Statement not found with ID: " + id));

        // Soft delete
        statement.setStatus(Statement.StatementStatus.DELETED);
        statementRepository.save(statement);

        logger.info("Statement soft-deleted successfully: {}", id);
    }

    /**
     * Download statement file
     */
    @Transactional(readOnly = true)
    public Resource downloadStatement(String id) {
        logger.info("Downloading statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException("Statement not found with ID: " + id));

        String filename = extractFilename(statement.getDocumentPath());
        return fileStorageService.loadFile(filename);
    }

    /**
     * Convert entity to response DTO
     */
    private StatementResponse toResponse(Statement statement) {
        return StatementResponse.builder()
                .id(statement.getId())
                .customerId(statement.getCustomerId())
                .statementDate(statement.getStatementDate())
                .documentPath(statement.getDocumentPath())
                .documentType(statement.getDocumentType())
                .status(statement.getStatus().name())
                .fileSizeBytes(statement.getFileSizeBytes())
                .createdAt(statement.getCreatedAt())
                .updatedAt(statement.getUpdatedAt())
                .downloadUrl("/api/statements/" + statement.getId() + "/download")
                .build();
    }

    /**
     * Generate filename for AFP file
     */
    private String generateFilename(Statement statement) {
        return String.format("%s_%s.pdf",
                statement.getCustomerId(),
                statement.getId());
    }

    /**
     * Extract filename from full path
     */
    private String extractFilename(String path) {
        if (path == null) return null;
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSeparator >= 0 ? path.substring(lastSeparator + 1) : path;
    }
}
