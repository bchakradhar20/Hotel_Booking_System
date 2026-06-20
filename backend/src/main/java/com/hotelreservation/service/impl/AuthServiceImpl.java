package com.hotelreservation.service.impl;

import com.hotelreservation.dto.*;
import com.hotelreservation.entity.*;
import com.hotelreservation.exception.APIException;
import com.hotelreservation.repository.*;
import com.hotelreservation.security.*;
import com.hotelreservation.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService providing user registration and authentication logic.
 * Handles signup validation, BCrypt encoding, role assignment, and JWT generation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Admin secret key loaded from application.properties.
     * Required when self-registering as ADMIN from the public registration page.
     */
    @Value("${app.admin.secret}")
    private String adminSecret;

    /**
     * Constructor injection for all required authentication dependencies.
     *
     * @param userRepository        repository for user persistence
     * @param roleRepository        repository for role lookups
     * @param passwordEncoder       BCrypt encoder for password hashing
     * @param authenticationManager Spring Security manager for credential verification
     * @param jwtUtil               utility for JWT token generation
     */
    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user with ROLE_USER.
     * Validates username and email uniqueness before persisting.
     *
     * @param signupRequest the signup form data from the client
     * @return ApiResponse with success confirmation message
     */
    @Override
    @Transactional
    public ApiResponse registerUser(SignupRequestDTO signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        // BCrypt encode — never store plaintext passwords
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhoneNumber(signupRequest.getPhoneNumber());

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        user.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(user);

        return new ApiResponse("User registered successfully", true);
    }

    /**
     * Registers a new admin user from the public registration page.
     * Validates the admin secret key before assigning ROLE_ADMIN + ROLE_USER.
     *
     * Security flow:
     * 1. Validate the submitted adminSecret against the configured app.admin.secret.
     * 2. If the secret is wrong, reject the request with 403 FORBIDDEN.
     * 3. If correct, create the user with both ROLE_USER and ROLE_ADMIN.
     *
     * This endpoint is publicly accessible but gated by a secret key,
     * so only authorised personnel who know the secret can create admin accounts.
     *
     * @param signupRequest registration details including the adminSecret field
     * @return ApiResponse with success confirmation message
     */
    @Override
    @Transactional
    public ApiResponse registerAdmin(SignupRequestDTO signupRequest) {
        // Step 1: Validate the admin secret key submitted by the user
        // Reject immediately if the secret is null, blank, or does not match
        if (signupRequest.getAdminSecret() == null
                || !signupRequest.getAdminSecret().equals(adminSecret)) {
            throw new APIException(HttpStatus.FORBIDDEN, "Invalid admin secret key");
        }

        // Step 2: Standard uniqueness validations
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        // Step 3: Build the admin user entity
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhoneNumber(signupRequest.getPhoneNumber());

        // Step 4: Assign both ROLE_USER and ROLE_ADMIN
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));

        user.setRoles(new HashSet<>(Set.of(userRole, adminRole)));
        userRepository.save(user);

        return new ApiResponse("Admin registered successfully", true);
    }

    /**
     * Authenticates a user using Spring Security's AuthenticationManager.
     * On successful authentication, generates a JWT token and returns user details.
     *
     * Authentication flow:
     * 1. AuthenticationManager verifies username and BCrypt-encoded password.
     * 2. On success, extract UserDetailsImpl from the Authentication object.
     * 3. Generate a signed JWT token using JwtUtil.
     * 4. Return the token along with user identity and roles.
     *
     * @param loginRequest login credentials (username and password)
     * @return UserInfoResponse containing the JWT token and user metadata
     */
    @Override
    public UserInfoResponse authenticateUser(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new UserInfoResponse(jwt, userDetails.getUserId(),
                userDetails.getUsername(), userDetails.getEmail(), roles);
    }
}
