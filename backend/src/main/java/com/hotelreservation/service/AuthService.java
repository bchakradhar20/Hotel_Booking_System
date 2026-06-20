package com.hotelreservation.service;

import com.hotelreservation.dto.*;

/**
 * Service interface defining authentication operations.
 * Implementations handle user signup, signin, and JWT generation.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     * Validates uniqueness of username and email, encodes the password,
     * and assigns the default ROLE_USER.
     *
     * @param signupRequest the signup form data from the client
     * @return ApiResponse with a success or failure message
     */
    ApiResponse registerUser(SignupRequestDTO signupRequest);

    /**
     * Authenticates a user and generates a JWT token upon success.
     *
     * @param loginRequest login credentials (username and password)
     * @return UserInfoResponse containing the JWT token and user details
     */
    UserInfoResponse authenticateUser(LoginRequestDTO loginRequest);
    /**
     * Registers a new admin user. Requires the caller to already be authenticated as ADMIN.
     * Assigns both ROLE_USER and ROLE_ADMIN to the new account.
     *
     * @param signupRequest the registration details for the new admin
     * @return ApiResponse with success confirmation message
     */
    ApiResponse registerAdmin(SignupRequestDTO signupRequest);
}
