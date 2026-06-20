package com.hotelreservation.auth.exception;

import com.hotelreservation.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> handleApiException(APIException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(build(ex.getStatus(), ex.getStatus().getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", "Input validation failed", req.getRequestURI(), errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", req.getRequestURI(), null);
    }

    private ErrorResponse build(HttpStatus status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(status.value(), error, message, path, LocalDateTime.now(java.time.ZoneId.systemDefault()), validationErrors);
    }
}
