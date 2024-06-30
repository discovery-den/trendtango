package org.pandey.dataextraction.error;

/**
 * Custom exception for JSON serialization errors.
 */
public class JsonSerializationException extends Exception {
    public JsonSerializationException(String message) {
        super(message);
    }

    public JsonSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
