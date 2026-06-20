package com.hotelreservation.auth.service;

import com.hotelreservation.auth.dto.ApiResponse;
import com.hotelreservation.auth.dto.LoginRequestDTO;
import com.hotelreservation.auth.dto.SignupRequestDTO;
import com.hotelreservation.auth.dto.UserInfoResponse;
import com.hotelreservation.auth.entity.Role;
import com.hotelreservation.auth.entity.User;
import com.hotelreservation.auth.exception.APIException;
import com.hotelreservation.auth.repository.RoleRepository;
import com.hotelreservation.auth.repository.UserRepository;
import com.hotelreservation.auth.security.JwtUtil;
import com.hotelreservation.auth.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for all user authentication and registration business logic.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Registering new standard users with {@code ROLE_USER}.</li>
 *   <li>Registering admin users (secret-gated) with {@code ROLE_USER} + {@code ROLE_ADMIN}.</li>
 *   <li>Authenticating users via Spring Security and issuing signed JWT tokens.</li>
 * </ul>
 *
 * <p>Follows the <em>Single Responsibility Principle</em>: this class handles only
 * authentication concerns. Password hashing, JWT generation, and role management
 * are delegated to dedicated collaborators injected via constructor.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Admin registration secret loaded from {@code application.properties}.
     * Anyone who submits this secret in the signup request receives {@code ROLE_ADMIN}.
     * Must be changed to a strong, unique value before deploying to production.
     */
    @Value("${app.admin.secret}")
    private String adminSecret;

    /**
     * Constructs the service with all required authentication dependencies.
     *
     * @param userRepository        repository for persisting and querying users
     * @param roleRepository        repository for role lookups and creation
     * @param passwordEncoder       BCrypt encoder for hashing raw passwords
     * @param authenticationManager Spring Security manager for credential verification
     * @param jwtUtil               utility for generating signed JWT tokens
     */
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user account with the default {@code ROLE_USER} role.
     *
     * <p>Validates that neither the username nor email is already in use before
     * persisting the new user. The raw password is BCrypt-encoded before storage.
     *
     * @param request the signup form data submitted by the client
     * @return {@link ApiResponse} confirming successful registration
     * @throws APIException with {@code 400 BAD_REQUEST} if username or email is already taken
     */
    @Transactional
    public ApiResponse registerUser(SignupRequestDTO request) {
        log.info("Registering new user: {}", request.getUsername());
        validateUniqueCredentials(request.getUsername(), request.getEmail());

        User user = buildUserEntity(request);
        Role userRole = roleRepository.findByRoleName(ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, ROLE_USER)));

        user.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());
        return new ApiResponse("User registered successfully", true);
    }

    /**
     * Registers a new admin user from the public admin registration endpoint.
     *
     * <p>Access is gated by the {@code adminSecret} field in the request body.
     * Only callers who know the configured secret can self-register as admins.
     * The created account receives both {@code ROLE_USER} and {@code ROLE_ADMIN}.
     *
     * @param request registration details including the admin secret key
     * @return {@link ApiResponse} confirming successful admin registration
     * @throws APIException with {@code 403 FORBIDDEN} if the admin secret is invalid
     * @throws APIException with {@code 400 BAD_REQUEST} if username or email is already taken
     */
    @Transactional
    public ApiResponse registerAdmin(SignupRequestDTO request) {
        log.info("Registering new admin: {}", request.getUsername());
        if (request.getAdminSecret() == null || !request.getAdminSecret().equals(adminSecret)) {
            log.warn("Admin registration failed — invalid secret for username: {}", request.getUsername());
            throw new APIException(HttpStatus.FORBIDDEN, "Invalid admin secret key");
        }

        validateUniqueCredentials(request.getUsername(), request.getEmail());
        User user = buildUserEntity(request);

        Role userRole = roleRepository.findByRoleName(ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, ROLE_USER)));
        Role adminRole = roleRepository.findByRoleName(ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, ROLE_ADMIN)));

        user.setRoles(new HashSet<>(Set.of(userRole, adminRole)));
        userRepository.save(user);
        log.info("Admin registered successfully: {}", request.getUsername());
        return new ApiResponse("Admin registered successfully", true);
    }

    /**
     * Authenticates a user using Spring Security and returns a signed JWT token.
     *
     * <p>Authentication flow:
     * <ol>
     *   <li>{@link AuthenticationManager} verifies the username and BCrypt password.</li>
     *   <li>On success, the {@link UserDetailsImpl} principal is extracted from the result.</li>
     *   <li>A signed JWT (with userId and roles claims) is generated via {@link JwtUtil}.</li>
     *   <li>The token and user metadata are returned in a {@link UserInfoResponse}.</li>
     * </ol>
     *
     * @param request the login credentials (username and raw password)
     * @return {@link UserInfoResponse} containing the JWT token and user identity info
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public UserInfoResponse authenticateUser(LoginRequestDTO request) {
        log.info("Authentication attempt for user: {}", request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwtToken = jwtUtil.generateToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info("User authenticated successfully: {}", request.getUsername());
        return new UserInfoResponse(jwtToken, userDetails.getUserId(),
                userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    /**
     * Validates that neither the given username nor email already exists in the database.
     *
     * <p>Extracted as a private method to apply the DRY principle — both
     * {@link #registerUser} and {@link #registerAdmin} share this validation.
     *
     * @param username the desired username to check
     * @param email    the desired email address to check
     * @throws APIException with {@code 400 BAD_REQUEST} if either is already taken
     */
    private void validateUniqueCredentials(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration rejected — username already taken: {}", username);
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected — email already in use: {}", email);
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }
    }

    /**
     * Constructs a new {@link User} entity from the signup request data.
     *
     * <p>The raw password is BCrypt-encoded here. Roles are not assigned
     * in this method — they must be set by the calling registration method.
     *
     * @param request the signup form data
     * @return a new {@link User} entity with encoded password, without roles or ID
     */
    private User buildUserEntity(SignupRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Encode the raw password with BCrypt before persisting — never store plaintext
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        return user;
    }
}
