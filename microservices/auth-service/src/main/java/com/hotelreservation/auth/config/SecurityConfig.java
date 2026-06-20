package com.hotelreservation.auth.config;

import com.hotelreservation.auth.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the Auth Service.
 *
 * <p>This service handles only authentication operations (signup, login).
 * All endpoints are publicly accessible because:
 * <ul>
 *   <li>Signup and login are unauthenticated entry points by design.</li>
 *   <li>JWT validation for protected resources is handled at the API Gateway level.</li>
 * </ul>
 *
 * <p><strong>CSRF note:</strong> CSRF protection is intentionally disabled.
 * This service uses stateless JWT-based authentication rather than cookie/session-based auth.
 * CSRF tokens are only necessary when the browser automatically sends credentials (e.g. cookies).
 * Since JWTs are sent via the Authorization header, CSRF attacks are not applicable here.
 * See: <a href="https://docs.spring.io/spring-security/reference/features/exploits/csrf.html">Spring Security CSRF</a>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Constructs the security configuration with the custom UserDetailsService and CORS config.
     *
     * @param userDetailsService loads user data from the database during authentication
     * @param corsConfigurationSource provides CORS configuration for cross-origin requests
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Provides a BCrypt password encoder bean used for hashing and verifying passwords.
     *
     * <p>BCrypt is the recommended algorithm for password hashing: it is adaptive,
     * incorporates a salt automatically, and is resistant to brute-force attacks.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the DAO authentication provider that ties together the
     * {@link UserDetailsServiceImpl} and the {@link PasswordEncoder}.
     *
     * <p>Spring Security uses this provider to load the user by username
     * and verify the submitted raw password against the stored BCrypt hash.
     *
     * @return a configured {@link DaoAuthenticationProvider}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean.
     *
     * <p>The {@code AuthService} injects this bean to programmatically authenticate
     * login requests via {@code AuthenticationManager.authenticate(...)}.
     *
     * @param config Spring's authentication configuration
     * @return the application's {@link AuthenticationManager}
     * @throws Exception if the manager cannot be built
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the HTTP security filter chain for the Auth Service.
     *
     * <p>Policy decisions:
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; no session/cookie credentials are used.</li>
     *   <li>Session management is STATELESS — no server-side session is created.</li>
     *   <li>All requests are permitted — signup/login must be publicly accessible.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link org.springframework.security.web.SecurityFilterChain}
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the configured CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled: JWT over Authorization header is not vulnerable to CSRF attacks
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }
}
