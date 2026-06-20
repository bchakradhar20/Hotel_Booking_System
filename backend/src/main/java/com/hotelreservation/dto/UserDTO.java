package com.hotelreservation.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for transferring user data between layers.
 * Used for profile viewing and update operations.
 * Password and role fields are intentionally excluded for security.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** User's unique identifier (read-only in update context) */
    private Long userId;

    /** User's unique username (read-only — cannot be changed after registration) */
    private String username;

    /** User's email address — updatable by the user */
    @Email(message = "Invalid email format")
    private String email;

    /** User's phone number — updatable by the user */
    private String phoneNumber;
}
