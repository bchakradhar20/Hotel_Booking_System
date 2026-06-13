package com.hotelreservation.auth.service;

import com.hotelreservation.auth.dto.*;
import com.hotelreservation.auth.entity.*;
import com.hotelreservation.auth.exception.APIException;
import com.hotelreservation.auth.repository.*;
import com.hotelreservation.auth.security.*;
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

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.admin.secret}")
    private String adminSecret;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ApiResponse registerUser(SignupRequestDTO req) {
        validateUnique(req.getUsername(), req.getEmail());
        User user = buildUser(req);
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));
        user.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(user);
        return new ApiResponse("User registered successfully", true);
    }

    @Transactional
    public ApiResponse registerAdmin(SignupRequestDTO req) {
        if (req.getAdminSecret() == null || !req.getAdminSecret().equals(adminSecret))
            throw new APIException(HttpStatus.FORBIDDEN, "Invalid admin secret key");
        validateUnique(req.getUsername(), req.getEmail());
        User user = buildUser(req);
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
        user.setRoles(new HashSet<>(Set.of(userRole, adminRole)));
        userRepository.save(user);
        return new ApiResponse("Admin registered successfully", true);
    }

    public UserInfoResponse authenticateUser(LoginRequestDTO req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return new UserInfoResponse(jwt, userDetails.getUserId(),
                userDetails.getUsername(), userDetails.getEmail(), roles);
    }

    private void validateUnique(String username, String email) {
        if (userRepository.existsByUsername(username))
            throw new APIException(HttpStatus.BAD_REQUEST, "Username is already taken");
        if (userRepository.existsByEmail(email))
            throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use");
    }

    private User buildUser(SignupRequestDTO req) {
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhoneNumber(req.getPhoneNumber());
        return user;
    }
}
