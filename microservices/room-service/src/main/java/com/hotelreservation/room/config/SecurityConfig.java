package com.hotelreservation.room.config;

import com.hotelreservation.room.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the Room Service.
 *
 * <p>Enforces the following access policy:
 * <ul>
 *   <li>GET {@code /api/rooms/**} — public; anyone can browse available rooms.</li>
 *   <li>POST/PUT/DELETE {@code /api/rooms/**} — requires a valid JWT with {@code ROLE_ADMIN}
 *       (enforced via {@code @PreAuthorize} on the controller methods).</li>
 *   <li>Swagger UI endpoints — publicly accessible for API documentation.</li>
 * </ul>
 *
 * <p><strong>CSRF note:</strong> CSRF protection is intentionally disabled.
 * This service is a stateless REST API secured with JWT tokens sent via the
 * {@code Authorization} header — not browser cookies. CSRF attacks require the
 * browser to automatically attach credentials, which does not apply to header-based JWTs.
 * See: <a href="https://docs.spring.io/spring-security/reference/features/exploits/csrf.html">Spring Security CSRF</a>
 *
 * <p>Method-level security ({@code @PreAuthorize}) is enabled to allow
 * fine-grained role checks directly on controller methods.
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
     * @param jwtAuthFilter filter that validates JWT tokens on every incoming request
     * @param corsConfigurationSource provides CORS configuration for cross-origin requests
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Defines the HTTP security filter chain for the Room Service.
     *
     * <p>Configuration highlights:
     * <ul>
     *   <li>CSRF disabled — stateless JWT API; see class Javadoc for rationale.</li>
     *   <li>STATELESS session — no server-side sessions are created or maintained.</li>
     *   <li>Public GET access to room endpoints to allow unauthenticated browsing.</li>
     *   <li>{@link JwtAuthFilter} is registered before Spring's default
     *       {@link UsernamePasswordAuthenticationFilter} so the JWT is validated first.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder
     * @return the fully configured {@link org.springframework.security.web.SecurityFilterChain}
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with the configured CorsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF disabled: stateless JWT API — no session/cookie credentials involved
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Room browsing is a public feature — no token required for reads
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        // Swagger UI and OpenAPI docs are publicly accessible
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // All other requests (POST, PUT, DELETE) require a valid JWT
                        .anyRequest().authenticated());

        // Register the JWT filter before Spring's username/password filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
