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
