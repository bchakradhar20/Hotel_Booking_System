package com.hotelreservation.exception;

import com.hotelreservation.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralized global exception handler for all REST API controllers.
 * Converts exceptions into consistent, structured JSON error responses.
 */
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    /**
     * Handles resource not found exceptions (404).
     *
     * @param ex      the ResourceNotFoundException thrown
     * @param request the current HTTP request
     * @return structured 404 error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Handles business logic API exceptions with custom HTTP status codes.
     *
     * @param ex      the APIException thrown
     * @param request the current HTTP request
     * @return structured error response with the exception's HTTP status
     */
    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> handleApiException(APIException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(ex.getStatus(), ex.getStatus().getReasonPhrase(),
                ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /**
     * Handles Bean Validation errors from @Valid annotated request bodies.
     * Collects all field-level constraint violations into a map.
     *
     * @param ex      the MethodArgumentNotValidException thrown
     * @param request the current HTTP request
     * @return structured 400 error response with field-level validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Collect all field validation errors into a map: fieldName -> errorMessage
        Map<String, String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, duplicate) -> existing
                ));
        return buildError(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Input validation failed", request.getRequestURI(), validationErrors);
    }

    /**
     * Handles Spring Security authentication failures (401).
     *
     * @param ex      the AuthenticationException thrown
     * @param request the current HTTP request
     * @return structured 401 error response
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Handles Spring Security authorization failures (403).
     *
     * @param ex      the AccessDeniedException thrown
     * @param request the current HTTP request
     * @return structured 403 error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource", request.getRequestURI(), null);
    }

    /**
     * Handles all unhandled exceptions as a fallback (500).
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return structured 500 error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request.getRequestURI(), null);
    }

    /**
     * Utility method to construct a consistent ErrorResponse object.
     *
     * @param status           HTTP status code
     * @param error            short error type description
     * @param message          detailed error message
     * @param path             the request URI that caused the error
     * @param validationErrors optional map of field-level validation errors
     * @return populated ErrorResponse DTO
     */
    private ErrorResponse buildError(HttpStatus status, String error, String message,
                                     String path, Map<String, String> validationErrors) {
        return new ErrorResponse(
                status.value(),
                error,
                message,
                path,
                LocalDateTime.now(),
                validationErrors
        );
    }
}
