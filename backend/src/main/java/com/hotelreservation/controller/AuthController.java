package com.hotelreservation.controller;

import com.hotelreservation.dto.*;
import com.hotelreservation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication operations.
 * Handles public user registration, admin self-registration (secret-gated), and login.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor injection for AuthService.
     *
     * @param authService service handling registration and authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account with the default ROLE_USER role.
     *
     * @param signupRequest the registration form data
     * @return 201 Created with success message
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequestDTO signupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(signupRequest));
    }

    /**
     * Registers a new admin user from the public admin registration form.
     * Access is gated by the adminSecret field — no JWT required.
     * The service layer validates the secret against app.admin.secret in application.properties.
     *
     * @param signupRequest registration details including adminSecret
     * @return 201 Created with success message
     */
    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody SignupRequestDTO signupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(signupRequest));
    }

    /**
     * Authenticates a user and returns a JWT token upon successful login.
     *
     * @param loginRequest the login credentials
     * @return 200 OK with JWT token and user info
     */
    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }
}
