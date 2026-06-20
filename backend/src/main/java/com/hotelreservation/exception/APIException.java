package com.hotelreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * General-purpose application exception for business logic violations.
 * Supports custom HTTP status codes for flexible error signaling.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class APIException extends RuntimeException {

    /** HTTP status code associated with this exception */
    private final HttpStatus status;

    /**
     * Constructs an APIException with a specific HTTP status and message.
     *
     * @param status  HTTP status code to return to the client
     * @param message descriptive error message explaining the violation
     */
    public APIException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status associated with this exception.
     *
     * @return the HTTP status code
     */
    public HttpStatus getStatus() {
        return status;
    }
}
