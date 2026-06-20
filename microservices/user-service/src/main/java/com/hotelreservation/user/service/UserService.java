package com.hotelreservation.user.service;

import com.hotelreservation.user.dto.UserDTO;
import com.hotelreservation.user.entity.User;
import com.hotelreservation.user.exception.APIException;
import com.hotelreservation.user.exception.ResourceNotFoundException;
import com.hotelreservation.user.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for user profile management business logic.
 *
 * <p>Provides operations for viewing and updating user profiles, and listing
 * all users for admin purposes. This service shares the same database
 * ({@code hotel_auth_db}) as the Auth Service to read the user records
 * created during registration, while remaining a separate deployable unit.
 *
 * <p>Security constraints:
 * <ul>
 *   <li>Users may only view and update their own profile.</li>
 *   <li>Only admins can retrieve the full list of users.</li>
 *   <li>Username, password, and roles are intentionally excluded from the updatable fields.</li>
 * </ul>
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /**
     * Constructs the service with the required repository and mapper.
     *
     * @param userRepository JPA repository for user data access
     * @param modelMapper    ModelMapper for converting {@link User} entities to {@link UserDTO}
     */
    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves the profile of the authenticated user.
     *
     * <p>The API Gateway may forward either the username or email in the X-Username header.
     * This method attempts to find the user by username first, then by email as fallback.
     *
     * @param identifier the username or email of the authenticated caller (forwarded by the API Gateway)
     * @return the user's profile as a {@link UserDTO}
     * @throws ResourceNotFoundException if no user with the given username or email exists
     */
    public UserDTO getProfile(String identifier) {
        log.info("Fetching profile for identifier: {}", identifier);
        User user = findUserByIdentifier(identifier);
        return toDTO(user);
    }

    /**
     * Updates the mutable profile fields of the authenticated user.
     *
     * <p>The API Gateway may forward either the username or email in the X-Username header.
     * This method attempts to find the user by username first, then by email as fallback.
     *
     * <p>Only {@code phoneNumber} and {@code email} may be changed through this endpoint.
     * Username, password, and roles are intentionally protected from modification here,
     * following the principle of least privilege.
     *
     * <p>If the email is being changed, it is validated against all existing accounts
     * to prevent duplicate email addresses.
     *
     * @param identifier the username or email of the authenticated caller
     * @param userDTO    the updated profile data from the request body
     * @return the updated user profile as a {@link UserDTO}
     * @throws ResourceNotFoundException if the user is not found
     * @throws APIException with {@code 400 BAD_REQUEST} if the new email is already in use
     */
    @Transactional
    public UserDTO updateProfile(String identifier, UserDTO userDTO) {
        log.info("Updating profile for identifier: {}", identifier);
        User user = findUserByIdentifier(identifier);

        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank() && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                log.warn("Profile update rejected — email already in use: {}", userDTO.getEmail());
                throw new APIException(HttpStatus.BAD_REQUEST, "Email is already in use by another account");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        log.info("Profile updated for identifier: {}", identifier);
        return toDTO(userRepository.save(user));
    }

    /**
     * Retrieves all registered users in the system. Intended for admin access only.
     *
     * @return list of all user profiles as {@link UserDTO}; empty list if none exist
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Finds a user by username or email (fallback).
     *
     * <p>Tries username first, then email if not found by username.
     *
     * @param identifier the username or email
     * @return the {@link User} entity
     * @throws ResourceNotFoundException if not found by either username or email
     */
    private User findUserByIdentifier(String identifier) {
        log.debug("Looking up user by identifier: {}", identifier);
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> {
                    log.warn("User not found for identifier: {}", identifier);
                    return new ResourceNotFoundException("User", "username or email", identifier);
                });
    }

    /**
     * Converts a {@link User} entity to a {@link UserDTO} using ModelMapper.
     *
     * <p>Password is excluded from the DTO by design — the DTO
     * only exposes safe, non-sensitive profile fields.
     *
     * @param user the entity to convert
     * @return the mapped {@link UserDTO}
     */
    private UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
}
