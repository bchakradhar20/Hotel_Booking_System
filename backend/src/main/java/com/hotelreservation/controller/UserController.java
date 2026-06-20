package com.hotelreservation.controller;

import com.hotelreservation.dto.*;
import com.hotelreservation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user profile and admin user management.
 * Users can view and update their own profile.
 * Admins can view all users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor injection for UserService.
     *
     * @param userService service handling user profile logic
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param userDetails the currently authenticated user
     * @return 200 OK with the user's profile data
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    /**
     * Updates the authenticated user's email and/or phone number.
     * Username, password, and roles cannot be modified through this endpoint.
     *
     * @param userDTO     the updated profile fields
     * @param userDetails the currently authenticated user
     * @return 200 OK with the updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UserDTO userDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), userDTO));
    }

    /**
     * Retrieves all registered users in the system. Requires ADMIN role.
     *
     * @return 200 OK with list of all user profiles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
