package com.hotelreservation.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter executed once per HTTP request in the User Service.
 *
 * <p>Validates the Bearer token from the Authorization header and populates the
 * Spring Security context with the authenticated principal and role-based authorities.
 * Roles are sourced from the {@code X-Roles} header injected upstream by the API Gateway,
 * eliminating the need to re-query the database in this service.
 *
 * <p>Failures are logged at WARN level; the filter chain continues regardless to allow
 * Spring Security endpoint rules to enforce access control uniformly.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ROLES_HEADER = "X-Roles";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Constructs the filter with the required JWT utility.
     *
     * @param jwtUtil utility for JWT validation and username extraction
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Performs JWT validation and security context population for each incoming request.
     *
     * <p>Processing steps:
     * <ol>
     *   <li>Check for a Bearer token in the Authorization header.</li>
     *   <li>Validate the token's signature and expiry.</li>
     *   <li>Extract the username from the JWT subject claim.</li>
     *   <li>Map the comma-separated {@code X-Roles} header to granted authorities.</li>
     *   <li>Register the authentication in the {@link SecurityContextHolder}.</li>
     * </ol>
     *
     * @param request     the incoming HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
                // Remove the "Bearer " prefix to obtain the raw JWT
                String token = authHeader.substring(BEARER_PREFIX.length());

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);

                    // Build authorities from the gateway-forwarded X-Roles header.
                    // This avoids a database round-trip since the gateway has already
                    // validated the token and extracted the roles from JWT claims.
                    List<SimpleGrantedAuthority> authorities =
                            buildAuthorities(request.getHeader(ROLES_HEADER));

                    // null credentials: JWT is the authentication proof, no password needed
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // WARN level preserves the diagnostic message without exposing
            // internals to the caller. Access control is enforced by security rules.
            log.warn("JWT authentication failed for request [{}]: {}",
                    request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Converts a comma-separated roles string into a list of {@link SimpleGrantedAuthority}.
     *
     * @param rolesHeader the {@code X-Roles} header value; may be {@code null} or blank
     * @return non-null list of granted authorities; empty if header is absent or blank
     */
    private List<SimpleGrantedAuthority> buildAuthorities(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return Collections.emptyList();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
