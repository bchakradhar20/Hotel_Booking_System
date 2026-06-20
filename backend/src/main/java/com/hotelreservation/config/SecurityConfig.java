package com.hotelreservation.config;

import com.hotelreservation.security.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

/**
 * Central Spring Security configuration for the Hotel Reservation System.
 * Configures JWT-based stateless authentication, CORS, CSRF, and endpoint authorization rules.
 * Enables method-level security for @PreAuthorize annotations.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor injection for security dependencies.
     *
     * @param userDetailsService       loads user details from the database
     * @param authEntryPointJwt        handles unauthorized access attempts
     * @param jwtAuthenticationFilter  processes JWT tokens on every request
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          AuthEntryPointJwt authEntryPointJwt,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authEntryPointJwt = authEntryPointJwt;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures BCrypt password encoder for hashing user passwords.
     *
     * @return BCryptPasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the DAO authentication provider.
     * Links the UserDetailsService and PasswordEncoder for credential verification.
     *
     * @return configured DaoAuthenticationProvider bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean required for manual authentication in AuthService.
     *
     * @param authConfig Spring's authentication configuration
     * @return the AuthenticationManager bean
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain configuration.
     * Authorization rules:
     * - Public: /api/auth/** endpoints, Swagger UI, and GET /api/rooms
     * - USER role: reservations and profile endpoints
     * - ADMIN role: room management and all reservations management
     * - All other requests require authentication.
     * Session management is STATELESS because JWT is used instead of server-side sessions.
     * CSRF is disabled because the application uses token-based authentication (not cookies).
     *
     * @param http the HttpSecurity builder
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT-based REST APIs
                .csrf(csrf -> csrf.disable())

                // Configure CORS to allow cross-origin requests from the React frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Set custom 401 entry point for unauthenticated requests
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPointJwt))

                // Use STATELESS session — no server-side session is created or used
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define endpoint-level authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Authentication endpoints are publicly accessible
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger UI and API docs are publicly accessible
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        // Anyone can browse available rooms
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        // All other endpoints require a valid authenticated session
                        .anyRequest().authenticated()
                );

        // Register the JWT filter before Spring's default UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) to allow the React frontend
     * running on localhost:3000 to make requests to this backend.
     *
     * @return CORS configuration source bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow requests from the React development server
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        // Allow standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow all headers including Authorization
        config.setAllowedHeaders(List.of("*"));
        // Allow cookies and credentials in cross-origin requests
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
