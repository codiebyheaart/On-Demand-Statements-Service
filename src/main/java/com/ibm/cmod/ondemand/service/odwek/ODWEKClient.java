package com.ibm.cmod.ondemand.service.odwek;

import java.time.LocalDate;

/**
 * Interface for ODWEK client operations
 */
public interface ODWEKClient {

    /**
     * Fetch document from CMOD
     */
    byte[] fetchDocument(String customerId, LocalDate statementDate);

    /**
     * Store document in CMOD
     */
    void storeDocument(String documentId, byte[] document);

    /**
     * Delete document from CMOD
     */
    void deleteDocument(String documentId);

    /**
     * Check if document is available in CMOD
     */
    boolean isAvailable(String documentId);
}
