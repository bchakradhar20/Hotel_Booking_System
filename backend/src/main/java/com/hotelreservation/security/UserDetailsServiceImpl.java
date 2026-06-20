package com.hotelreservation.security;

import com.hotelreservation.entity.User;
import com.hotelreservation.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation used by Spring Security
 * to load user-specific data during the authentication process.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor injection for UserRepository dependency.
     *
     * @param userRepository repository used to fetch user data from the database
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for Spring Security authentication.
     * Fetches the User entity from the database and wraps it in UserDetailsImpl.
     * The @Transactional annotation ensures the roles collection (EAGER) is loaded
     * within an active database session.
     *
     * @param username the username provided during login
     * @return populated UserDetails object for authentication
     * @throws UsernameNotFoundException if no user with the given username exists
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database; throw if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Build and return the Spring Security UserDetails wrapper
        return UserDetailsImpl.build(user);
    }
}
