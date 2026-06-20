package com.hotelreservation.service;

import com.hotelreservation.dto.UserDTO;
import com.hotelreservation.entity.User;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.UserMapper;
import com.hotelreservation.repository.UserRepository;
import com.hotelreservation.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl covering profile retrieval, update, and admin operations.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User(1L, "john", "john@email.com", "encoded", "123456", Set.of());
        userDTO = new UserDTO(1L, "john", "john@email.com", "123456");
    }

    /**
     * Tests that getProfile returns the correct user DTO for a valid username.
     */
    @Test
    void getProfile_ExistingUser_ReturnsProfile() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getProfile("john");

        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getEmail()).isEqualTo("john@email.com");
    }

    /**
     * Tests that getProfile throws ResourceNotFoundException for a non-existent user.
     */
    @Test
    void getProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Tests successful profile update with a new phone number.
     */
    @Test
    void updateProfile_ValidUpdate_ReturnsUpdatedProfile() {
        UserDTO updateRequest = new UserDTO(null, null, "john@email.com", "999999999");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.updateProfile("john", updateRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    /**
     * Tests that updateProfile throws APIException when the new email is already used.
     */
    @Test
    void updateProfile_EmailAlreadyUsed_ThrowsException() {
        UserDTO updateRequest = new UserDTO(null, null, "existing@email.com", null);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("john", updateRequest))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Email is already in use");
    }

    /**
     * Tests that getAllUsers returns all users in the system.
     */
    @Test
    void getAllUsers_ReturnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("john");
    }
}
