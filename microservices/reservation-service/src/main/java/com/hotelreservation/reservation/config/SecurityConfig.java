package com.hotelreservation.reservation.config;

import com.hotelreservation.reservation.security.JwtAuthFilter;
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
 * Spring Security configuration for the Reservation Service.
 *
 * <p>All reservation endpoints require a valid JWT — there are no public reservation operations.
 * Role-based access control (e.g. ADMIN-only endpoints) is enforced at the method level
 * via {@code @PreAuthorize} annotations on controller methods.
 *
 * <p><strong>CSRF note:</strong> CSRF protection is intentionally disabled.
 * This is a stateless REST microservice secured with JWT tokens passed via the
 * {@code Authorization: Bearer} header. CSRF attacks exploit browser-managed credentials
 * (cookies/sessions), which are not used here. Disabling CSRF is the correct and standard
 * practice for stateless JWT REST APIs.
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
     * @param jwtAuthFilter filter responsible for JWT validation on every request
     * @param corsConfigurationSource provides CORS configuration for cross-origin requests
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Defines the HTTP security filter chain for the Reservation Service.
     *
     * <p>Policy:
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; see class Javadoc for rationale.</li>
     *   <li>STATELESS session management — no HTTP sessions are used.</li>
     *   <li>Swagger UI paths are publicly accessible for documentation purposes.</li>
     *   <li>Every other endpoint requires a valid JWT token.</li>
     *   <li>{@link JwtAuthFilter} executes before Spring's default authentication filter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link org.springframework.security.web.SecurityFilterChain}
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the configured CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled: stateless JWT API — Authorization header, not cookies
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
