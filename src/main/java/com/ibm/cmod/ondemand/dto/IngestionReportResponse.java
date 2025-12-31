package com.ibm.cmod.ondemand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for batch ingestion report
 */
public class IngestionReportResponse {

    private int totalProcessed;
    private int successCount;
    private int failureCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private long processingTimeMs;

    private List<String> successfulStatements = new ArrayList<>();
    private List<FailureDetail> failures = new ArrayList<>();

    public IngestionReportResponse() {
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

    // Getters and Setters
    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public List<String> getSuccessfulStatements() { return successfulStatements; }
    public void setSuccessfulStatements(List<String> successfulStatements) { this.successfulStatements = successfulStatements; }

    public List<FailureDetail> getFailures() { return failures; }
    public void setFailures(List<FailureDetail> failures) { this.failures = failures; }

    // Builder
    public static IngestionReportResponseBuilder builder() {
        return new IngestionReportResponseBuilder();
    }

    public static class IngestionReportResponseBuilder {
        private final IngestionReportResponse response = new IngestionReportResponse();

        public IngestionReportResponseBuilder startTime(LocalDateTime startTime) {
            response.startTime = startTime;
            return this;
        }

        public IngestionReportResponse build() {
            return response;
        }
    }

    // Failure Detail class
    public static class FailureDetail {
        private String customerId;
        private String reason;

        public FailureDetail() {
        }

        public FailureDetail(String customerId, String reason) {
            this.customerId = customerId;
            this.reason = reason;
        }

        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
