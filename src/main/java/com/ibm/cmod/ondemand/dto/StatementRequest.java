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
