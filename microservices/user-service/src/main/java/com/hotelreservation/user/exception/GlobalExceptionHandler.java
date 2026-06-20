package com.hotelreservation.user.exception;

import com.hotelreservation.user.dto.ApiResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleNotFound(ResourceNotFoundException ex) {
        return new ApiResponse(ex.getMessage(), false);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ApiResponse> handleApiException(APIException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiResponse(ex.getMessage(), false));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)// if request header is missing
    public ApiResponse handleMissingHeader(MissingRequestHeaderException ex) {
        return new ApiResponse("Access must be through API Gateway", false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)// validation rules failed according to @Valid annotation
    public ApiResponse handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ApiResponse(msg, false);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// built-in exception handler for any unhandled exceptions
    public ApiResponse handleGeneric(Exception ex) {
        return new ApiResponse("An unexpected error occurred: " + ex.getMessage(), false);
    }
}
