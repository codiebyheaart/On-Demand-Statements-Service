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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

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
