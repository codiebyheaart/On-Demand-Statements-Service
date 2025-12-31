package com.ibm.cmod.ondemand.service;

import com.ibm.cmod.ondemand.dto.IngestionReportResponse;
import com.ibm.cmod.ondemand.dto.StatementRequest;
import com.ibm.cmod.ondemand.dto.StatementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service for batch AFP ingestion
 */
@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    private static final Random random = new Random();

    private final StatementService statementService;

    public IngestionService(StatementService statementService) {
        this.statementService = statementService;
    }

    /**
     * Ingest statements for a specific date
     * Simulates batch processing of AFP files from staging area
     */
    public IngestionReportResponse ingestStatements(LocalDate targetDate) {
        logger.info("Starting AFP ingestion for date: {}", targetDate);

        IngestionReportResponse report = IngestionReportResponse.builder()
                .startTime(LocalDateTime.now())
                .build();

        try {
            // Simulate finding 5-10 statements in staging area
            int count = 5 + random.nextInt(6); // 5 to 10
            logger.info("Found {} statements to ingest", count);

            for (int i = 0; i < count; i++) {
                try {
                    // Generate customer ID
                    String customerId = "CUST-" + (10000 + random.nextInt(90000));

                    // Create statement request
                    StatementRequest request = StatementRequest.builder()
                            .customerId(customerId)
                            .statementDate(targetDate)
                            .documentType("MONTHLY_STATEMENT")
                            .build();

                    // Create via StatementService
                    StatementResponse response = statementService.createStatement(request);
                    report.addSuccess(response.getId());

                    logger.debug("Ingested statement: {}", response.getId());

                } catch (Exception e) {
                    logger.error("Failed to ingest statement {}/{}", i + 1, count, e);
                    report.addFailure("CUST-UNKNOWN", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Ingestion failed critically", e);
        } finally {
            report.setEndTime(LocalDateTime.now());
            long duration = java.time.Duration.between(report.getStartTime(), report.getEndTime()).toMillis();
            report.setProcessingTimeMs(duration);
        }

        logger.info("Ingestion completed: {} total, {} success, {} failed",
                report.getTotalProcessed(), report.getSuccessCount(), report.getFailureCount());

        return report;
    }
}
