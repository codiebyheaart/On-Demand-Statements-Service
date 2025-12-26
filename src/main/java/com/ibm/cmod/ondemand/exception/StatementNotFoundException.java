package com.ibm.cmod.ondemand.exception;

/**
 * Exception thrown when a statement is not found
 */
public class StatementNotFoundException extends RuntimeException {

    public StatementNotFoundException(String message) {
        super(message);
    }

    public StatementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
