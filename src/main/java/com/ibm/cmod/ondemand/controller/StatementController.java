package com.ibm.cmod.ondemand.controller;

import com.ibm.cmod.ondemand.dto.IngestionReportResponse;
import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import com.ibm.cmod.ondemand.service.IngestionService;
import com.ibm.cmod.ondemand.service.StatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
