package com.hotelreservation.user.controller;

import com.hotelreservation.user.dto.UserDTO;
import com.hotelreservation.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for user profile and admin user management.
 * 
 * <p>The authenticated user's identity is supplied via the {@code X-Username} header
 * injected by the API Gateway after JWT validation. This avoids re-parsing the JWT
 * in this service and keeps authentication concerns at the gateway boundary.
 *
 * <p>Access policy:
 * <ul>
 *   <li>Profile read/update — requires {@code ROLE_USER} or {@code ROLE_ADMIN}.</li>
 *   <li>Get all users — requires {@code ROLE_ADMIN}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and admin user management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the controller with the required user service.
     *
     * @param userService service handling all user profile business logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * <p>The {@code X-Username} header is injected by the API Gateway — clients
     * do not send this header directly.
     *
     * @param username the authenticated user's username, forwarded by the API Gateway
     * @return {@code 200 OK} with the user's profile data
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    /**
     * Updates the authenticated user's mutable profile fields (email, phone number).
     *
     * <p>Username, password, and roles cannot be changed through this endpoint.
     * The {@code X-Username} header identifies which user's profile to update.
     *
     * @param userDTO  the updated profile fields from the request body
     * @param username the authenticated user's username, forwarded by the API Gateway
     * @return {@code 200 OK} with the updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UserDTO userDTO,
            @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(userService.updateProfile(username, userDTO));
    }

    /**
     * Retrieves all registered users in the system. Requires {@code ROLE_ADMIN}.
     *
     * @return {@code 200 OK} with a list of all user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
