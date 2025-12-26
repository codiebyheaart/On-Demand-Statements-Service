package com.ibm.cmod.ondemand.repository;

import com.ibm.cmod.ondemand.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Statement entity
 */
@Repository
public interface StatementRepository extends JpaRepository<Statement, String> {

    /**
     * Find statements by customer ID
     */
    List<Statement> findByCustomerId(String customerId);

    /**
     * Find statements by customer ID and statement date
     */
    List<Statement> findByCustomerIdAndStatementDate(String customerId, LocalDate statementDate);

    /**
     * Find statements by status
     */
    List<Statement> findByStatus(Statement.StatementStatus status);
}
