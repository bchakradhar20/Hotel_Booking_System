package com.hotelreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 Not Found response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a ResourceNotFoundException with a descriptive message.
     *
     * @param resourceName name of the resource that was not found (e.g., "Room")
     * @param fieldName    field used to look up the resource (e.g., "roomId")
     * @param fieldValue   value that was searched for (e.g., 42)
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
