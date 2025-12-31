package com.ibm.cmod.ondemand.util;

import com.ibm.cmod.ondemand.exception.FileStorageException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility to generate mock AFP files (using PDF format for demo)
 */
@Component
public class AFPFileGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AFPFileGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Generate a statement PDF (simulating AFP file)
     */
    public byte[] generateStatementPDF(String customerId, LocalDate statementDate) {
        logger.debug("Generating AFP/PDF for customer: {}, date: {}", customerId, statementDate);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("BANK STATEMENT");
                contentStream.endText();

                // Customer Info
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Customer ID: " + customerId);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText("Statement Date: " + statementDate.format(DATE_FORMATTER));
                contentStream.endText();

                // Statement Details
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("Account Number: XXXX-XXXX-" + customerId.substring(Math.max(0, customerId.length() - 4)));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 630);
                contentStream.showText("Opening Balance: $1,234.56");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 610);
                contentStream.showText("Closing Balance: $2,345.67");
                contentStream.endText();

                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("This is a simulated AFP document generated for demonstration purposes.");
                contentStream.endText();
            }

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            byte[] pdfBytes = baos.toByteArray();



            logger.debug("Generated PDF: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (IOException e) {
            logger.error("Failed to generate AFP/PDF", e);
            throw new FileStorageException("Failed to generate statement PDF", e);
        }
    }
}
