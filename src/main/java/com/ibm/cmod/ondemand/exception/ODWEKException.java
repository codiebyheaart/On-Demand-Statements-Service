package com.ibm.cmod.ondemand.exception;

/**
 * Exception thrown during ODWEK/CMOD operations
 */
public class ODWEKException extends RuntimeException {

    public ODWEKException(String message) {
        super(message);
    }

    public ODWEKException(String message, Throwable cause) {
        super(message, cause);
    }
}
