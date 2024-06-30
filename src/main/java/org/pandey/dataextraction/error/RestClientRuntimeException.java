package org.pandey.dataextraction.error;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class RestClientRuntimeException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public RestClientRuntimeException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RestClientRuntimeException(String message, Throwable cause, HttpStatusCode statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}


