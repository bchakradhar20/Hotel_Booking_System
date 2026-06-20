package com.hotelreservation.dto;

import lombok.*;

import java.util.List;

/**
 * DTO returned to the client after successful authentication.
 * Contains the JWT token and basic user identity information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** Generated JWT token used for subsequent authenticated requests */
    private String token;

    /** Authenticated user's unique identifier */
    private Long userId;

    /** Authenticated user's username */
    private String username;

    /** Authenticated user's email */
    private String email;

    /** List of role names assigned to the authenticated user (e.g., ROLE_ADMIN) */
    private List<String> roles;
}
