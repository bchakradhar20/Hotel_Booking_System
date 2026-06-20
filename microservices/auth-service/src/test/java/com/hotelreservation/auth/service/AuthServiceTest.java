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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}.
 * All dependencies are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private SignupRequestDTO signupRequest;

    @BeforeEach
    void setUp() {
        // Inject the @Value field that Spring would normally bind from application.yml
        ReflectionTestUtils.setField(authService, "adminSecret", "secret123");
        signupRequest = new SignupRequestDTO("john", "john@test.com", "pass123", "0501234567", null);
    }

    // ── registerUser ──────────────────────────────────────────────────────────

    @Test
    void registerUser_success_returnsSuccessResponse() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ApiResponse response = authService.registerUser(signupRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_createsRoleWhenNotFoundInDb() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        // ROLE_USER does not exist yet — service must create it
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(new Role(1L, "ROLE_USER"));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ApiResponse response = authService.registerUser(signupRequest);

        assertThat(response.isSuccess()).isTrue();
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void registerUser_throwsBadRequest_whenUsernameTaken() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Username is already taken")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_throwsBadRequest_whenEmailTaken() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Email is already in use")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }

    // ── registerAdmin ─────────────────────────────────────────────────────────

    @Test
    void registerAdmin_success_returnssAdminRegisteredMessage() {
        signupRequest.setAdminSecret("secret123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(new Role(2L, "ROLE_ADMIN")));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ApiResponse response = authService.registerAdmin(signupRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Admin registered successfully");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_throwsForbidden_whenSecretIsWrong() {
        signupRequest.setAdminSecret("wrongSecret");

        assertThatThrownBy(() -> authService.registerAdmin(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Invalid admin secret key")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAdmin_throwsForbidden_whenSecretIsNull() {
        signupRequest.setAdminSecret(null);

        assertThatThrownBy(() -> authService.registerAdmin(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Invalid admin secret key")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void registerAdmin_throwsBadRequest_whenUsernameTakenAfterSecretValidation() {
        signupRequest.setAdminSecret("secret123");
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerAdmin(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Username is already taken");
    }

    @Test
    void registerAdmin_createsRolesWhenNotFoundInDb() {
        signupRequest.setAdminSecret("secret123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ApiResponse response = authService.registerAdmin(signupRequest);

        assertThat(response.isSuccess()).isTrue();
    }

    // ── authenticateUser ──────────────────────────────────────────────────────

    @Test
    void authenticateUser_success_returnsTokenAndUserInfo() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("john", "pass123");
        UserDetailsImpl principal = new UserDetailsImpl(
                1L, "john", "john@test.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtil.generateToken(auth)).thenReturn("jwt-token");

        UserInfoResponse response = authService.authenticateUser(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getEmail()).isEqualTo("john@test.com");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void authenticateUser_success_returnsAllRolesForAdmin() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "pass123");
        UserDetailsImpl principal = new UserDetailsImpl(
                2L, "admin", "admin@test.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")));

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(auth)).thenReturn("admin-token");

        UserInfoResponse response = authService.authenticateUser(loginRequest);

        assertThat(response.getRoles()).hasSize(2);
        assertThat(response.getRoles()).contains("ROLE_USER", "ROLE_ADMIN");
        assertThat(response.getToken()).isEqualTo("admin-token");
    }
}
