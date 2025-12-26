-- On-Demand Statements Schema
CREATE TABLE IF NOT EXISTS statements (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    statement_date DATE NOT NULL,
    document_path VARCHAR(500),
    document_type VARCHAR(50) DEFAULT 'MONTHLY_STATEMENT',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    file_size_bytes BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_customer_id ON statements(customer_id);
CREATE INDEX idx_statement_date ON statements(statement_date);
CREATE INDEX idx_status ON statements(status);
CREATE INDEX idx_created_at ON statements(created_at);

-- Audit table for tracking changes
CREATE TABLE IF NOT EXISTS statement_audit (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    statement_id VARCHAR(36) NOT NULL,
    action VARCHAR(20) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(1000)
);

CREATE INDEX idx_audit_statement_id ON statement_audit(statement_id);
