package com.hotelreservation.user.config;

import com.hotelreservation.user.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the User Service.
 *
 * <p>All user-profile endpoints require a valid JWT. The username of the authenticated
 * caller is forwarded via the {@code X-Username} header by the API Gateway, so this
 * service does not need to re-query the JWT claims directly from the token body.
 *
 * <p>Role-based access (e.g. ADMIN-only {@code GET /api/users}) is enforced at
 * the method level via {@code @PreAuthorize} annotations.
 *
 * <p><strong>CSRF note:</strong> CSRF protection is intentionally disabled.
 * This microservice is stateless and uses JWT tokens in the {@code Authorization} header.
 * CSRF attacks are only relevant when the browser automatically sends credentials (e.g. via
 * session cookies). Since no cookies or sessions are used here, disabling CSRF is both
 * correct and standard practice for JWT-secured REST APIs.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Constructs the security configuration with the JWT authentication filter and CORS config.
     *
     * @param jwtAuthFilter filter that validates JWT tokens and populates the security context
     * @param corsConfigurationSource provides CORS configuration for cross-origin requests
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Defines the HTTP security filter chain for the User Service.
     *
     * <p>Configuration:
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; see class Javadoc for full rationale.</li>
     *   <li>STATELESS session — no server-side sessions are created.</li>
     *   <li>Swagger UI is publicly accessible for API documentation.</li>
     *   <li>All remaining endpoints require a valid authenticated JWT.</li>
     *   <li>{@link JwtAuthFilter} runs before Spring's default authentication filter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link org.springframework.security.web.SecurityFilterChain}
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the configured CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled: stateless JWT API — no session or cookie credentials used
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
