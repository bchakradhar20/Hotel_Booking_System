package com.hotelreservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for user login request.
 * Contains credentials needed for authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /** Username submitted by the client for authentication */
    @NotBlank(message = "Username is required")
    private String username;

    /** Raw password submitted by the client (will be matched against BCrypt hash) */
    @NotBlank(message = "Password is required")
    private String password;
}
