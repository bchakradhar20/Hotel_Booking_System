package com.hotelreservation.service;

import com.hotelreservation.dto.UserDTO;

import java.util.List;

/**
 * Service interface defining user profile management operations.
 */
public interface UserService {

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param username the authenticated user's username
     * @return the user's profile as a DTO
     */
    UserDTO getProfile(String username);

    /**
     * Updates the authenticated user's mutable profile fields (email, phoneNumber).
     * Username, password, and roles cannot be changed through this method.
     *
     * @param username the authenticated user's username
     * @param userDTO  the updated profile data
     * @return the updated profile as a DTO
     */
    UserDTO updateProfile(String username, UserDTO userDTO);

    /**
     * Retrieves all users in the system (admin only).
     *
     * @return list of all users as DTOs
     */
    List<UserDTO> getAllUsers();
}
