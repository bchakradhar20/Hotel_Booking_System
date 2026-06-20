package com.hotelreservation.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for new user registration request.
 * Validated before processing to ensure data integrity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {

    /** Desired username — must be unique in the system */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** User's email address — must be unique and valid format */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Raw password — will be encoded with BCrypt before storage */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Optional phone number for the user profile */
    private String phoneNumber;

    /**
     * Optional admin secret key — if provided and correct, ROLE_ADMIN is assigned.
     * Only used by the admin registration endpoint, ignored on public signup.
     */
    private String adminSecret;
}
