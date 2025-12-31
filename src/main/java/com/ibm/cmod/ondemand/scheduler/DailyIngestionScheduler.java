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
