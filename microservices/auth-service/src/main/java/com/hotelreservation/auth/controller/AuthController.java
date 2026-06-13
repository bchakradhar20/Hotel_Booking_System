package com.hotelreservation.auth.controller;

import com.hotelreservation.auth.dto.*;
import com.hotelreservation.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user (ROLE_USER)")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(req));
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register an admin user (requires adminSecret)")
    public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody SignupRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(req));
    }

    @PostMapping("/signin")
    @Operation(summary = "Login and get a JWT token")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequestDTO req) {
        return ResponseEntity.ok(authService.authenticateUser(req));
    }
}
