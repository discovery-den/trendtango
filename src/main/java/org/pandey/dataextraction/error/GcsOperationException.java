package org.pandey.dataextraction.error;

/**
 * Custom exception for errors occurring during the execution and saving of data.
 */
public class GcsOperationException extends Exception {
    public GcsOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
