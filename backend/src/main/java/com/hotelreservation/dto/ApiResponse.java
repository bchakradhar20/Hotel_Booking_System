package com.hotelreservation.dto;

import lombok.*;

/**
 * Generic API response wrapper for success messages.
 * Provides a consistent response structure across all endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    /** Human-readable message describing the result of the operation */
    private String message;

    /** Indicates whether the operation was successful */
    private boolean success;
}
