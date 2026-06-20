package com.hotelreservation.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for structured error responses returned by the global exception handler.
 * Provides consistent error format across all API error scenarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code of the error */
    private int status;

    /** Short description of the error type */
    private String error;

    /** Detailed error message for the client */
    private String message;

    /** API path that triggered the error */
    private String path;

    /** Timestamp when the error occurred */
    private LocalDateTime timestamp;

    /** Field-specific validation errors (field name -> error message) */
    private Map<String, String> validationErrors;
}
