package com.hotelreservation.auth.security;

import com.hotelreservation.auth.entity.User;
import com.hotelreservation.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation for the Auth Service.
 *
 * <p>This is the bridge between the application's {@link User} database entity
 * and Spring Security's authentication pipeline. Spring Security calls
 * {@link #loadUserByUsername(String)} automatically during the login flow when
 * {@code AuthenticationManager.authenticate()} is invoked in
 * {@link com.hotelreservation.auth.service.AuthService#authenticateUser}.
 *
 * <p>Authentication flow:
 * <ol>
 *   <li>Client submits username + password via {@code POST /api/auth/signin}.</li>
 *   <li>{@link com.hotelreservation.auth.service.AuthService} calls
 *       {@code authenticationManager.authenticate(...)}.</li>
 *   <li>Spring Security's {@code DaoAuthenticationProvider} calls
 *       {@link #loadUserByUsername(String)} here to fetch the stored user.</li>
 *   <li>The provider verifies the submitted password against the stored BCrypt hash.</li>
 *   <li>On success, the returned {@link UserDetailsImpl} becomes the authenticated principal.</li>
 * </ol>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs the service with the user repository.
     *
     * @param userRepository JPA repository used to look up users by username during login
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user from the database by username for Spring Security authentication.
     *
     * <p>This method is called internally by Spring Security's
     * {@code DaoAuthenticationProvider} during the login flow.
     * The user's roles are fetched eagerly (defined on the {@link User} entity)
     * so they are available when building the authorities list.
     *
     * <p>The {@code @Transactional} annotation ensures the roles collection
     * (lazily initialized by default in some configurations) is fully loaded
     * within this database transaction.
     *
     * @param username the username submitted by the client during login
     * @return a fully populated {@link UserDetailsImpl} for Spring Security use
     * @throws UsernameNotFoundException if no user with the given username exists
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look up the user by username — throw standard Spring Security exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert the User entity to a Spring Security principal object
        return UserDetailsImpl.build(user);
    }
}
