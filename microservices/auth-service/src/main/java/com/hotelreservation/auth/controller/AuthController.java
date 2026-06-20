package com.hotelreservation.auth.controller;

import com.hotelreservation.auth.dto.ApiResponse;
import com.hotelreservation.auth.dto.LoginRequestDTO;
import com.hotelreservation.auth.dto.SignupRequestDTO;
import com.hotelreservation.auth.dto.UserInfoResponse;
import com.hotelreservation.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication operations.
 *
 * <p>All endpoints in this controller are publicly accessible — no JWT is required.
 * Business logic is fully delegated to {@link AuthService} to keep this controller thin.
 *
 * <p>Exposes three operations:
 * <ul>
 *   <li>Standard user registration — assigns {@code ROLE_USER}.</li>
 *   <li>Admin registration — assigns {@code ROLE_USER + ROLE_ADMIN}, gated by a secret key.</li>
 *   <li>Login — returns a signed JWT token on successful authentication.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs the controller with the required authentication service.
     *
     * @param authService service handling registration and authentication business logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account with the default {@code ROLE_USER} role.
     *
     * @param request the validated registration form data
     * @return {@code 201 Created} with a success message, or {@code 400} if username/email is taken
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user (ROLE_USER)")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
    }

    /**
     * Registers a new admin user from the public admin registration form.
     *
     * <p>The request body must include the correct {@code adminSecret} value.
     * No JWT is required; access is gated by knowledge of the secret.
     *
     * @param request the validated registration form data including the admin secret
     * @return {@code 201 Created} with a success message, or {@code 403} for invalid secret
     */
    @PostMapping("/register-admin")
    @Operation(summary = "Register an admin user (requires adminSecret in request body)")
    public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody SignupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(request));
    }

    /**
     * Authenticates a user and returns a signed JWT token on success.
     *
     * <p>The returned token contains {@code userId} and {@code roles} claims used
     * by the API Gateway to forward user context headers to downstream services.
     *
     * @param request the validated login credentials (username and password)
     * @return {@code 200 OK} with the JWT token and user info, or {@code 401} for bad credentials
     */
    @PostMapping("/signin")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.authenticateUser(request));
    }
}
