package com.hotelreservation.service.impl;

import com.hotelreservation.dto.UserDTO;
import com.hotelreservation.entity.User;
import com.hotelreservation.exception.*;
import com.hotelreservation.mapper.UserMapper;
import com.hotelreservation.repository.UserRepository;
import com.hotelreservation.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService providing user profile management logic.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Constructor injection for user repository and mapper.
     *
     * @param userRepository repository for user database operations
     * @param userMapper     mapper for User entity / DTO conversions
     */
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param username the authenticated user's username
     * @return the user's profile as a DTO
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    public UserDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toDTO(user);
    }

    /**
     * Updates only the mutable profile fields: email and phoneNumber.
     * Username, password, and roles are intentionally protected from updates here.
     * Email uniqueness is validated if the email is being changed.
     *
     * @param username the authenticated user's username
     * @param userDTO  the updated profile data
     * @return the updated user profile as a DTO
     * @throws APIException if the new email is already used by another account
     */
    @Override
    @Transactional
    public UserDTO updateProfile(String username, UserDTO userDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // If the email is being changed, ensure it is not already taken by another user
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use by another account");
            }
            user.setEmail(userDTO.getEmail());
        }

        // Update phone number if provided
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Retrieves all registered users in the system (admin only).
     *
     * @return list of all users as DTOs
     */
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}
