package com.ibm.cmod.ondemand.service.odwek;

import com.ibm.cmod.ondemand.exception.ODWEKException;
import com.ibm.cmod.ondemand.util.AFPFileGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

/**
 * Mock implementation of ODWEK client for testing
 */
@Service
public class MockODWEKClientImpl implements ODWEKClient {

    private static final Logger logger = LoggerFactory.getLogger(MockODWEKClientImpl.class);
    private static final Random random = new Random();

    @Value("${app.odwek.mock.min-delay-ms:100}")
    private int minDelay;

    @Value("${app.odwek.mock.max-delay-ms:500}")
    private int maxDelay;

    private final AFPFileGenerator afpGenerator;

    public MockODWEKClientImpl(AFPFileGenerator afpGenerator) {
        this.afpGenerator = afpGenerator;
    }

    @Override
    public byte[] fetchDocument(String customerId, LocalDate statementDate) {
        logger.info("Mock ODWEK: Fetching document for customer: {}, date: {}", customerId, statementDate);

        // Simulate network delay
        simulateDelay();

        try {
            // Generate mock AFP/PDF document
            byte[] document = afpGenerator.generateStatementPDF(customerId, statementDate);
            logger.info("Mock ODWEK: Successfully fetched document ({} bytes)", document.length);
            return document;

        } catch (Exception e) {
            logger.error("Mock ODWEK: Failed to fetch document", e);
            throw new ODWEKException("Failed to fetch document from CMOD", e);
        }
    }

    @Override
    public void storeDocument(String documentId, byte[] document) {
        logger.info("Mock ODWEK: Storing document: {} ({} bytes)", documentId, document.length);
        simulateDelay();
        logger.info("Mock ODWEK: Document stored successfully");
    }

    @Override
    public void deleteDocument(String documentId) {
        logger.info("Mock ODWEK: Deleting document: {}", documentId);
        simulateDelay();
        logger.info("Mock ODWEK: Document deleted successfully");
    }

    @Override
    public boolean isAvailable(String documentId) {
        logger.debug("Mock ODWEK: Checking availability of document: {}", documentId);
        simulateDelay();
        // Always return true for mock
        return true;
    }

    /**
     * Simulate network delay to CMOD server
     */
    private void simulateDelay() {
        try {
            int delay = minDelay + random.nextInt(maxDelay - minDelay);
            Thread.sleep(delay);
            logger.debug("Mock ODWEK: Simulated {}ms network delay", delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Mock ODWEK: Delay interrupted", e);
        }
    }
}
