package com.ibm.cmod.ondemand.controller;

import com.ibm.cmod.ondemand.dto.IngestionReportResponse;
import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import com.ibm.cmod.ondemand.service.IngestionService;
import com.ibm.cmod.ondemand.service.StatementService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Statement operations
 */
@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private static final Logger logger = LoggerFactory.getLogger(StatementController.class);

    private final StatementService statementService;
    private final IngestionService ingestionService;

    public StatementController(StatementService statementService, IngestionService ingestionService) {
        this.statementService = statementService;
        this.ingestionService = ingestionService;
    }

    /**
     * CREATE - Create a new statement
     * POST /api/statements
     */
    @PostMapping
    public ResponseEntity<StatementResponse> createStatement(@Valid @RequestBody StatementRequest request) {
        logger.info("API: Create statement request for customer: {}", request.getCustomerId());
        StatementResponse response = statementService.createStatement(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * READ - Get statement by ID
     * GET /api/statements/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StatementResponse> getStatement(@PathVariable String id) {
        logger.info("API: Get statement request for ID: {}", id);
        StatementResponse response = statementService.getStatement(id);
        return ResponseEntity.ok(response);
    }

    /**
     * READ - Get statements by customer ID
     * GET /api/statements?customerId={customerId}
     */
    @GetMapping
    public ResponseEntity<List<StatementResponse>> getStatements(
            @RequestParam(required = false) String customerId) {
        
        if (customerId != null) {
            logger.info("API: Get statements for customer: {}", customerId);
            List<StatementResponse> responses = statementService.getStatementsByCustomer(customerId);
            return ResponseEntity.ok(responses);
        } else {
            logger.info("API: Get all statements");
            List<StatementResponse> responses = statementService.getAllStatements();
            return ResponseEntity.ok(responses);
        }
    }

    /**
     * UPDATE - Update statement
     * PUT /api/statements/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StatementResponse> updateStatement(
            @PathVariable String id,
            @Valid @RequestBody StatementRequest request) {
        
        logger.info("API: Update statement request for ID: {}", id);
        StatementResponse response = statementService.updateStatement(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE - Delete statement (soft delete)
     * DELETE /api/statements/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatement(@PathVariable String id) {
        logger.info("API: Delete statement request for ID: {}", id);
        statementService.deleteStatement(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DOWNLOAD - Download statement file
     * GET /api/statements/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadStatement(@PathVariable String id) {
        logger.info("API: Download statement request for ID: {}", id);
        Resource file = statementService.downloadStatement(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * BATCH - Trigger manual ingestion
     * POST /api/statements/ingest?date={date}
     */
    @PostMapping("/ingest")
    public ResponseEntity<IngestionReportResponse> ingestStatements(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now().minusDays(1);
        logger.info("API: Manual ingestion request for date: {}", targetDate);

        IngestionReportResponse report = ingestionService.ingestStatements(targetDate);
        return ResponseEntity.ok(report);
    }

    /**
     * HEALTH - Health check endpoint
     * GET /api/statements/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("On-Demand Statements Service is running");
    }
}

package com.ibm.cmod.ondemand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for batch ingestion report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionReportResponse {

    private int totalProcessed;
    private int successCount;
    private int failureCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private long processingTimeMs;

    @Builder.Default
    private List<String> successfulStatements = new ArrayList<>();

    @Builder.Default
    private List<FailureDetail> failures = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailureDetail {
        private String customerId;
        private String reason;
    }

    public void addSuccess(String statementId) {
        successfulStatements.add(statementId);
        successCount++;
        totalProcessed++;
    }

    public void addFailure(String customerId, String reason) {
        failures.add(new FailureDetail(customerId, reason));
        failureCount++;
        totalProcessed++;
    }

    //  builder and setter methods
    public int getTotalProcessed() {
        return totalProcessed;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public static IngestionReportResponseBuilder builder() {
        return new IngestionReportResponseBuilder();
    }

    public static class IngestionReportResponseBuilder {
        private LocalDateTime startTime;

        public IngestionReportResponseBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public IngestionReportResponse build() {
            IngestionReportResponse response = new IngestionReportResponse();
            response.startTime = this.startTime;
            return response;
        }
    }
}

package com.ibm.cmod.ondemand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import java.time.LocalDate;

/**
 * Request DTO for creating/updating statements
 */
public class StatementRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Statement date is required")
    @Past(message = "Statement date must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statementDate;

    private String documentType;

    public StatementRequest() {
    }

    public StatementRequest(String customerId, LocalDate statementDate, String documentType) {
        this.customerId = customerId;
        this.statementDate = statementDate;
        this.documentType = documentType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public static StatementRequestBuilder builder() {
        return new StatementRequestBuilder();
    }

    public static class StatementRequestBuilder {
        private String customerId;
        private LocalDate statementDate;
        private String documentType;

        public StatementRequestBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public StatementRequestBuilder statementDate(LocalDate statementDate) {
            this.statementDate = statementDate;
            return this;
        }

        public StatementRequestBuilder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public StatementRequest build() {
            return new StatementRequest(customerId, statementDate, documentType);
        }
    }
}

package com.ibm.cmod.ondemand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for statement operations
 */
public class StatementResponse {

    private String id;
    private String customerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statementDate;

    private String documentPath;
    private String documentType;
    private String status;
    private Long fileSizeBytes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String downloadUrl;

    public StatementResponse() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getStatus() {
        return status;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    // Builder
    public static StatementResponseBuilder builder() {
        return new StatementResponseBuilder();
    }

    public static class StatementResponseBuilder {
        private final StatementResponse response = new StatementResponse();

        public StatementResponseBuilder id(String id) {
            response.id = id;
            return this;
        }

        public StatementResponseBuilder customerId(String customerId) {
            response.customerId = customerId;
            return this;
        }

        public StatementResponseBuilder statementDate(LocalDate statementDate) {
            response.statementDate = statementDate;
            return this;
        }

        public StatementResponseBuilder documentPath(String documentPath) {
            response.documentPath = documentPath;
            return this;
        }

        public StatementResponseBuilder documentType(String documentType) {
            response.documentType = documentType;
            return this;
        }

        public StatementResponseBuilder status(String status) {
            response.status = status;
            return this;
        }

        public StatementResponseBuilder fileSizeBytes(Long fileSizeBytes) {
            response.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public StatementResponseBuilder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public StatementResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public StatementResponseBuilder downloadUrl(String downloadUrl) {
            response.downloadUrl = downloadUrl;
            return this;
        }

        public StatementResponse build() {
            return response;
        }
    }
}

package com.ibm.cmod.ondemand.entity;

import javax.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Statement Entity representing On-Demand Statement metadata
 */
@Entity
@Table(name = "statements")
@EntityListeners(AuditingEntityListener.class)
public class Statement {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatementStatus status;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Integer version;

    public enum StatementStatus {
        PENDING, AVAILABLE, ARCHIVED, DELETED
    }

    public Statement() {
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (status == null) {
            status = StatementStatus.PENDING;
        }
        if (documentType == null) {
            documentType = "MONTHLY_STATEMENT";
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public LocalDate getStatementDate() { return statementDate; }
    public void setStatementDate(LocalDate statementDate) { this.statementDate = statementDate; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public StatementStatus getStatus() { return status; }
    public void setStatus(StatementStatus status) { this.status = status; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    // Builder
    public static StatementBuilder builder() {
        return new StatementBuilder();
    }

    public static class StatementBuilder {
        private final Statement statement = new Statement();

        public StatementBuilder id(String id) {
            statement.id = id;
            return this;
        }

        public StatementBuilder customerId(String customerId) {
            statement.customerId = customerId;
            return this;
        }

        public StatementBuilder statementDate(LocalDate statementDate) {
            statement.statementDate = statementDate;
            return this;
        }

        public StatementBuilder documentPath(String documentPath) {
            statement.documentPath = documentPath;
            return this;
        }

        public StatementBuilder documentType(String documentType) {
            statement.documentType = documentType;
            return this;
        }

        public StatementBuilder status(StatementStatus status) {
            statement.status = status;
            return this;
        }

        public StatementBuilder fileSizeBytes(Long fileSizeBytes) {
            statement.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public Statement build() {
            return statement;
        }
    }
}

package com.ibm.cmod.ondemand.exception;

import java.time.LocalDateTime;

/**
 * Standard error response structure
 */
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

package com.ibm.cmod.ondemand.exception;

/**
 * Exception thrown during file storage operations
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.ibm.cmod.ondemand.exception;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StatementNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStatementNotFound(
            StatementNotFoundException ex, HttpServletRequest request) {
        logger.error("Statement not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ODWEKException.class)
    public ResponseEntity<ErrorResponse> handleODWEKException(
            ODWEKException ex, HttpServletRequest request) {
        logger.error("ODWEK operation failed: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ODWEK Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex, HttpServletRequest request) {
        logger.error("File storage operation failed: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File Storage Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.ibm.cmod.ondemand.exception;

/**
 * Exception thrown during ODWEK/CMOD operations
 */
public class ODWEKException extends RuntimeException {

    public ODWEKException(String message) {
        super(message);
    }

    public ODWEKException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.ibm.cmod.ondemand.exception;

/**
 * Exception thrown when a statement is not found
 */
public class StatementNotFoundException extends RuntimeException {

    public StatementNotFoundException(String statementId) {
        super("Statement not found with ID: " + statementId);
    }

    public StatementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.ibm.cmod.ondemand.repository;

import com.ibm.cmod.ondemand.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Statement entity operations
 */
@Repository
public interface StatementRepository extends JpaRepository<Statement, String> {

    /**
     * Find all statements for a specific customer
     */
    List<Statement> findByCustomerId(String customerId);

    /**
     * Find statements by statement date
     */
    List<Statement> findByStatementDate(LocalDate statementDate);

    /**
     * Find statements by status
     */
    List<Statement> findByStatus(Statement.StatementStatus status);

    /**
     * Find statements by customer and status
     */
    List<Statement> findByCustomerIdAndStatus(String customerId, Statement.StatementStatus status);

    /**
     * Find statements by date range
     */
    List<Statement> findByStatementDateBetween(LocalDate startDate, LocalDate endDate);
}

package com.ibm.cmod.ondemand.scheduler;

import com.ibm.cmod.ondemand.dto.IngestionReportResponse;
import com.ibm.cmod.ondemand.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduled task for daily AFP ingestion
 */
@Component
@ConditionalOnProperty(name = "app.batch.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class DailyIngestionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyIngestionScheduler.class);

    private final IngestionService ingestionService;

    public DailyIngestionScheduler(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Scheduled daily ingestion task
     * Runs daily at 2 AM by default (configurable via app.batch.ingestion.cron)
     */
    @Scheduled(cron = "${app.batch.ingestion.cron:0 0 2 * * ?}")
    public void runDailyIngestion() {
        logger.info("===== Starting Daily AFP Ingestion Batch Job =====");

        try {
            // Ingest statements for previous day
            LocalDate targetDate = LocalDate.now().minusDays(1);
            IngestionReportResponse report = ingestionService.ingestStatements(targetDate);

            logger.info("Daily ingestion completed successfully");
            logger.info("Report: {} total, {} successful, {} failed, {}ms processing time",
                    report.getTotalProcessed(),
                    report.getSuccessCount(),
                    report.getFailureCount(),
                    report.getProcessingTimeMs());

            if (report.getFailureCount() > 0) {
                logger.warn("Ingestion had {} failures. Review logs for details.", report.getFailureCount());
            }

        } catch (Exception e) {
            logger.error("Daily ingestion batch job failed", e);
        }

        logger.info("===== Daily AFP Ingestion Batch Job Completed =====");
    }
}

package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.exception.FileStorageException;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service for managing file system storage
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.storage.location:./storage/afp-files}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
            logger.info("Initialized file storage at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to initialize file storage", e);
            throw new FileStorageException("Failed to initialize file storage", e);
        }
    }

    /**
     * Store file in file system
     */
    public String storeFile(String filename, byte[] fileData) {
        try {
            if (filename.contains("..")) {
                throw new FileStorageException("Invalid filename: " + filename);
            }

            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Create parent directories if they don't exist
            Files.createDirectories(destinationFile.getParent());

            // Write file
            Files.write(destinationFile, fileData);

            logger.info("Stored file: {} ({} bytes)", filename, fileData.length);
            return destinationFile.toString();

        } catch (IOException e) {
            logger.error("Failed to store file: {}", filename, e);
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    /**
     * Load file from file system
     */
    public Resource loadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                logger.debug("Loaded file: {}", filename);
                return resource;
            } else {
                logger.error("File not found or not readable: {}", filename);
                throw new FileStorageException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for file: {}", filename, e);
            throw new FileStorageException("Malformed URL for file: " + filename, e);
        }
    }

    /**
     * Delete file from file system
     */
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            logger.info("Deleted file: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filename, e);
            throw new FileStorageException("Failed to delete file: " + filename, e);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            return Files.size(file);
        } catch (IOException e) {
            logger.error("Failed to get file size: {}", filename, e);
            return 0;
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        return Files.exists(file);
    }
}

package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.dto.IngestionReportResponse;
import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for handling batch AFP ingestion
 */
@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);

    private final StatementService statementService;
    private final Random random = new Random();

    public IngestionService(StatementService statementService) {
        this.statementService = statementService;
    }

    /**
     * Ingest statements for a specific date
     * Simulates reading from CMOD staging area and processing
     */
    public IngestionReportResponse ingestStatements(LocalDate targetDate) {
        logger.info("Starting batch ingestion for date: {}", targetDate);
        LocalDateTime startTime = LocalDateTime.now();

        IngestionReportResponse report = IngestionReportResponse.builder()
                .startTime(startTime)
                .build();

        try {
            // Simulate fetching statement metadata from CMOD staging
            List<StatementMetadata> stagingStatements = fetchFromStaging(targetDate);
            logger.info("Found {} statements in staging for date: {}", stagingStatements.size(), targetDate);

            // Process each statement
            for (StatementMetadata metadata : stagingStatements) {
                try {
                    // Create statement request
                    StatementRequest request = StatementRequest.builder()
                            .customerId(metadata.customerId)
                            .statementDate(metadata.statementDate)
                            .documentType(metadata.documentType)
                            .build();

                    // Create statement (triggers ODWEK call and file storage)
                    StatementResponse response = statementService.createStatement(request);
                    report.addSuccess(response.getId());
                    
                    logger.debug("Successfully ingested statement for customer: {}", metadata.customerId);

                } catch (Exception e) {
                    logger.error("Failed to ingest statement for customer: {}", metadata.customerId, e);
                    report.addFailure(metadata.customerId, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Batch ingestion failed", e);
        }

        LocalDateTime endTime = LocalDateTime.now();
        report.setEndTime(endTime);
        report.setProcessingTimeMs(
                java.time.Duration.between(startTime, endTime).toMillis()
        );

        logger.info("Batch ingestion completed: {} successful, {} failed, {} total",
                report.getSuccessCount(), report.getFailureCount(), report.getTotalProcessed());

        return report;
    }

    /**
     * Simulate fetching statement metadata from CMOD staging area
     * In production, this would read from actual CMOD staging directory
     */
    private List<StatementMetadata> fetchFromStaging(LocalDate targetDate) {
        List<StatementMetadata> statements = new ArrayList<>();

        // Generate 5-10 random statements for demo
        int count = 5 + random.nextInt(6); // 5 to 10 statements

        for (int i = 0; i < count; i++) {
            String customerId = String.format("CUST-%05d", 10000 + random.nextInt(90000));
            statements.add(new StatementMetadata(
                    customerId,
                    targetDate,
                    "MONTHLY_STATEMENT"
            ));
        }

        return statements;
    }

    /**
     * Internal class to represent statement metadata from staging
     */
    private static class StatementMetadata {
        String customerId;
        LocalDate statementDate;
        String documentType;

        StatementMetadata(String customerId, LocalDate statementDate, String documentType) {
            this.customerId = customerId;
            this.statementDate = statementDate;
            this.documentType = documentType;
        }
    }
}

package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import com.ibm.cmod.ondemand.entity.Statement;
import com.ibm.cmod.ondemand.exception.StatementNotFoundException;
import com.ibm.cmod.ondemand.repository.StatementRepository;
import com.ibm.cmod.ondemand.service.odwek.ODWEKClient;
import com.ibm.cmod.ondemand.util.AFPFileGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing statement operations
 */
@Service
@Transactional
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    private final StatementRepository statementRepository;
    private final ODWEKClient odwekClient;
    private final FileStorageService fileStorageService;
    private final AFPFileGenerator afpFileGenerator;

    public StatementService(StatementRepository statementRepository,
                            ODWEKClient odwekClient,
                            FileStorageService fileStorageService,
                            AFPFileGenerator afpFileGenerator) {
        this.statementRepository = statementRepository;
        this.odwekClient = odwekClient;
        this.fileStorageService = fileStorageService;
        this.afpFileGenerator = afpFileGenerator;
    }

    /**
     * Create a new statement
     */
    public StatementResponse createStatement(StatementRequest request) {
        logger.info("Creating statement for customer: {}, date: {}", 
                request.getCustomerId(), request.getStatementDate());

        // Generate AFP document via mock ODWEK
        byte[] afpData = afpFileGenerator.generateStatementDocument(
                request.getCustomerId(),
                request.getStatementDate(),
                request.getDocumentType() != null ? request.getDocumentType() : "MONTHLY_STATEMENT"
        );

        // Store in file system via ODWEK
        String statementId = UUID.randomUUID().toString();
        String filename = String.format("%s_%s.pdf", request.getCustomerId(), statementId.substring(0, 8));
        String filePath = fileStorageService.storeFile(filename, afpData);

        // Simulate ODWEK storage and get CMOD path
        String cmodPath = odwekClient.storeAFPDocument(
                request.getCustomerId(),
                request.getStatementDate(),
                request.getDocumentType() != null ? request.getDocumentType() : "MONTHLY_STATEMENT",
                afpData
        );

        // Save statement metadata
        Statement statement = Statement.builder()
                .id(statementId)
                .customerId(request.getCustomerId())
                .statementDate(request.getStatementDate())
                .documentPath(filename)
                .documentType(request.getDocumentType() != null ? request.getDocumentType() : "MONTHLY_STATEMENT")
                .status(Statement.StatementStatus.AVAILABLE)
                .fileSizeBytes(fileStorageService.getFileSize(filename))
                .build();

        statement = statementRepository.save(statement);
        logger.info("Created statement with ID: {}", statement.getId());

        return mapToResponse(statement);
    }

    /**
     * Get statement by ID
     */
    @Transactional(readOnly = true)
    public StatementResponse getStatement(String id) {
        logger.debug("Fetching statement with ID: {}", id);
        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException(id));

        return mapToResponse(statement);
    }

    /**
     * Get statements by customer ID
     */
    @Transactional(readOnly = true)
    public List<StatementResponse> getStatementsByCustomer(String customerId) {
        logger.debug("Fetching statements for customer: {}", customerId);
        List<Statement> statements = statementRepository.findByCustomerId(customerId);

        return statements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all statements
     */
    @Transactional(readOnly = true)
    public List<StatementResponse> getAllStatements() {
        logger.debug("Fetching all statements");
        return statementRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update statement
     */
    public StatementResponse updateStatement(String id, StatementRequest request) {
        logger.info("Updating statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException(id));

        // Update metadata
        statement.setCustomerId(request.getCustomerId());
        statement.setStatementDate(request.getStatementDate());

        if (request.getDocumentType() != null) {
            statement.setDocumentType(request.getDocumentType());
        }

        statement = statementRepository.save(statement);
        logger.info("Updated statement with ID: {}", id);

        return mapToResponse(statement);
    }

    /**
     * Delete statement (soft delete)
     */
    public void deleteStatement(String id) {
        logger.info("Deleting statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException(id));

        // Soft delete
        statement.setStatus(Statement.StatementStatus.DELETED);
        statementRepository.save(statement);

        // Optionally delete from ODWEK
        if (statement.getDocumentPath() != null) {
            try {
                fileStorageService.deleteFile(statement.getDocumentPath());
            } catch (Exception e) {
                logger.warn("Failed to delete file for statement: {}", id, e);
            }
        }

        logger.info("Deleted statement with ID: {}", id);
    }

    /**
     * Download statement file
     */
    @Transactional(readOnly = true)
    public Resource downloadStatement(String id) {
        logger.info("Downloading statement with ID: {}", id);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new StatementNotFoundException(id));

        if (statement.getDocumentPath() == null) {
            throw new StatementNotFoundException("Document not available for statement: " + id);
        }

        return fileStorageService.loadFile(statement.getDocumentPath());
    }

    /**
     * Map entity to response DTO
     */
    private StatementResponse mapToResponse(Statement statement) {
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
}

package com.ibm.cmod.ondemand.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility to generate mock AFP files (using PDF for demo purposes)
 * In production, this would interface with actual AFP generators
 */
@Component
public class AFPFileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AFPFileGenerator.class);

    /**
     * Generate a mock statement document (PDF) for demo purposes
     */
    public byte[] generateStatementDocument(String customerId, LocalDate statementDate, String documentType) {
        logger.info("Generating AFP/PDF document for customer: {}, date: {}", customerId, statementDate);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("ON-DEMAND STATEMENT");
                contentStream.endText();

                // Customer Info
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Customer ID: " + customerId);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText("Statement Date: " + statementDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 660);
                contentStream.showText("Document Type: " + documentType);
                contentStream.endText();

                // Mock Statement Content
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 620);
                contentStream.showText("Account Summary");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(50, 590);
                contentStream.showText("Opening Balance:     $1,250.00");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(50, 570);
                contentStream.showText("Deposits:            $  500.00");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(50, 550);
                contentStream.showText("Withdrawals:         $  300.00");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(50, 530);
                contentStream.showText("Closing Balance:     $1,450.00");
                contentStream.endText();

                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("This is a simulated AFP document generated via ODWEK for demo purposes.");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.newLineAtOffset(50, 35);
                contentStream.showText("Generated by: IBM CMOD On-Demand Statements Service");
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            logger.info("Successfully generated AFP/PDF document");
            return baos.toByteArray();

        } catch (IOException e) {
            logger.error("Failed to generate AFP/PDF document", e);
            throw new RuntimeException("Failed to generate statement document", e);
        }
    }
}

