package com.hotelreservation.user.service;

import com.hotelreservation.user.dto.UserDTO;
import com.hotelreservation.user.entity.Role;
import com.hotelreservation.user.entity.User;
import com.hotelreservation.user.exception.APIException;
import com.hotelreservation.user.exception.ResourceNotFoundException;
import com.hotelreservation.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}.
 * All dependencies are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(1L, "ROLE_USER"));

        user = new User(1L, "john", "john@test.com", "hashed", "0501234567", roles);
        userDTO = new UserDTO(1L, "john", "john@test.com", "0501234567");
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void getProfile_success_returnsUserDTO() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getProfile("john");

        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getProfile_throwsResourceNotFound_whenUserMissing() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("unknown");
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void updateProfile_success_updatesEmail() {
        UserDTO updateRequest = new UserDTO(null, null, "newemail@test.com", null);
        User updatedUser = new User(1L, "john", "newemail@test.com", "hashed", "0501234567", new HashSet<>());
        UserDTO updatedDTO = new UserDTO(1L, "john", "newemail@test.com", "0501234567");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@test.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDTO.class)).thenReturn(updatedDTO);

        UserDTO result = userService.updateProfile("john", updateRequest);

        assertThat(result.getEmail()).isEqualTo("newemail@test.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_success_updatesPhoneNumber() {
        UserDTO updateRequest = new UserDTO(null, null, null, "0599999999");
        User updatedUser = new User(1L, "john", "john@test.com", "hashed", "0599999999", new HashSet<>());
        UserDTO updatedDTO = new UserDTO(1L, "john", "john@test.com", "0599999999");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDTO.class)).thenReturn(updatedDTO);

        UserDTO result = userService.updateProfile("john", updateRequest);

        assertThat(result.getPhoneNumber()).isEqualTo("0599999999");
    }

    @Test
    void updateProfile_success_doesNotChangeEmailWhenSameEmailSubmitted() {
        // Submitting the same email should NOT trigger a uniqueness check
        UserDTO updateRequest = new UserDTO(null, null, "john@test.com", null);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateProfile("john", updateRequest);

        // existsByEmail must never be called when the email has not changed
        verify(userRepository, never()).existsByEmail(any());
        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void updateProfile_success_doesNothingWhenBothFieldsNull() {
        UserDTO updateRequest = new UserDTO(null, null, null, null);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateProfile("john", updateRequest);

        assertThat(result.getEmail()).isEqualTo("john@test.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_throwsBadRequest_whenNewEmailAlreadyTaken() {
        UserDTO updateRequest = new UserDTO(null, null, "taken@test.com", null);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("john", updateRequest))
                .isInstanceOf(APIException.class)
                .hasMessage("Email is already in use by another account")
                .extracting(e -> ((APIException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_throwsResourceNotFound_whenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("ghost", userDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsAllUsers() {
        User user2 = new User(2L, "jane", "jane@test.com", "hashed", "0509999999", new HashSet<>());
        UserDTO dto2 = new UserDTO(2L, "jane", "jane@test.com", "0509999999");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        when(modelMapper.map(eq(user), eq(UserDTO.class))).thenReturn(userDTO);
        when(modelMapper.map(eq(user2), eq(UserDTO.class))).thenReturn(dto2);

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getUsername).contains("john", "jane");
    }

    @Test
    void getAllUsers_returnsEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllUsers_returnsSingleUser() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@test.com");
    }
}
