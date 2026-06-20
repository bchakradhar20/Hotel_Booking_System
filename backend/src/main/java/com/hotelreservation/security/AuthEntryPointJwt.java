package com.hotelreservation.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelreservation.dto.ErrorResponse;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom AuthenticationEntryPoint triggered when an unauthenticated user
 * tries to access a protected resource.
 * Returns a structured JSON 401 Unauthorized response instead of the default redirect.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructor injection for Jackson ObjectMapper.
     *
     * @param objectMapper used to serialize the ErrorResponse to JSON
     */
    public AuthEntryPointJwt(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Called when authentication is required but not provided or invalid.
     * Writes a 401 JSON response directly to the HTTP response output stream.
     *
     * @param request       the HTTP request that required authentication
     * @param response      the HTTP response to write the error to
     * @param authException the exception that caused the authentication failure
     * @throws IOException if writing to the response stream fails
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        logger.error("Unauthorized access attempt: {}", authException.getMessage());

        // Build a structured error response
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Full authentication is required to access this resource",
                request.getRequestURI(),
                LocalDateTime.now(),
                null
        );

        // Set the response content type and status code
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Write the JSON error body to the response
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
