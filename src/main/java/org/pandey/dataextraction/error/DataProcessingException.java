package org.pandey.dataextraction.error;

/**
 * Custom exception for errors occurring during the execution and saving of data.
 */
public class DataProcessingException extends Exception {
    public DataProcessingException(String message) {
        super(message);
    }

    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
