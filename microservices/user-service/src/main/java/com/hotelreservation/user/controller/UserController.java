package com.hotelreservation.user.controller;

import com.hotelreservation.user.dto.UserDTO;
import com.hotelreservation.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user profile", description = "Gets the profile of the currently authenticated user")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UserDTO userDTO,
            @RequestHeader("X-Username") String username) {
        return ResponseEntity.ok(userService.updateProfile(username, userDTO));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
