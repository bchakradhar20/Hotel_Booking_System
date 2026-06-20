package com.hotelreservation.service;

import com.hotelreservation.dto.*;
import com.hotelreservation.entity.*;
import com.hotelreservation.exception.APIException;
import com.hotelreservation.repository.*;
import com.hotelreservation.security.*;
import com.hotelreservation.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl covering signup and signin scenarios.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignupRequestDTO signupRequest;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequestDTO("testuser", "test@email.com", "password123", "1234567890", null);
        loginRequest = new LoginRequestDTO("testuser", "password123");
    }

    /**
     * Tests successful user registration with unique username and email.
     * Expects success response and user to be saved once.
     */
    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(new Role(1L, "ROLE_USER")));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ApiResponse response = authService.registerUser(signupRequest);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Tests that signup throws APIException when the username is already taken.
     */
    @Test
    void registerUser_UsernameAlreadyTaken_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Username is already taken");
    }

    /**
     * Tests that signup throws APIException when the email is already registered.
     */
    @Test
    void registerUser_EmailAlreadyInUse_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Email is already in use");
    }

    /**
     * Tests successful authentication returns a valid UserInfoResponse with a JWT token.
     */
    @Test
    void authenticateUser_Success() {
        Role role = new Role(1L, "ROLE_USER");
        User user = new User(1L, "testuser", "test@email.com", "encoded", "123", Set.of(role));
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(authentication)).thenReturn("mock.jwt.token");

        UserInfoResponse response = authService.authenticateUser(loginRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRoles()).contains("ROLE_USER");
    }

    /**
     * Tests that authentication throws BadCredentialsException for invalid credentials.
     */
    @Test
    void authenticateUser_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
