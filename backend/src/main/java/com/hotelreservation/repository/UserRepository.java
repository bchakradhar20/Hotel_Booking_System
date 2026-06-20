package com.hotelreservation.repository;

import com.hotelreservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity database operations.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     * Used by UserDetailsService during authentication and login.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, or empty if not
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user with the given username already exists.
     * Used during signup to enforce username uniqueness.
     *
     * @param username the username to check
     * @return true if the username is already taken; false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email already exists.
     * Used during signup to enforce email uniqueness.
     *
     * @param email the email to check
     * @return true if the email is already registered; false otherwise
     */
    boolean existsByEmail(String email);
}
