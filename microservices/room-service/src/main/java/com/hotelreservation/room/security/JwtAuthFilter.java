package com.hotelreservation.room.security;

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
 * JWT authentication filter executed once per HTTP request.
 *
 * <p>Extracts and validates the Bearer token from the Authorization header.
 * On success, populates the Spring Security context with the authenticated principal
 * and their granted authorities derived from the X-Roles header forwarded by the API Gateway.
 *
 * <p>If authentication fails for any reason, the filter logs the error at WARN level
 * and continues the filter chain unauthenticated. Downstream security rules then
 * decide whether to reject the request.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    /** Header name for the Bearer JWT token. */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Header name forwarded by the API Gateway containing comma-separated role names. */
    private static final String ROLES_HEADER = "X-Roles";

    /** Bearer token prefix to strip before parsing the JWT. */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Constructs the filter with the required JWT utility.
     *
     * @param jwtUtil utility for JWT validation and claim extraction
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Core filter logic invoked once per request.
     *
     * <p>Authentication flow:
     * <ol>
     *   <li>Extract the Bearer token from the Authorization header.</li>
     *   <li>Validate the token signature and expiry via {@link JwtUtil#validateToken(String)}.</li>
     *   <li>Extract the username from the token subject claim.</li>
     *   <li>Parse granted authorities from the X-Roles header (comma-separated role names).</li>
     *   <li>Store a {@link UsernamePasswordAuthenticationToken} in the {@link SecurityContextHolder}.</li>
     * </ol>
     *
     * @param request     the incoming HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the remaining filter chain to execute after this filter
     * @throws ServletException if a servlet-level error occurs
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Only process requests that carry a Bearer token
            if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
                // Strip the "Bearer " prefix to obtain the raw JWT string
                String token = authHeader.substring(BEARER_PREFIX.length());

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);

                    // Parse the X-Roles header forwarded by the API Gateway.
                    // Each role name is a comma-separated entry (e.g. "ROLE_ADMIN,ROLE_USER").
                    String rolesHeader = request.getHeader(ROLES_HEADER);
                    List<SimpleGrantedAuthority> authorities = buildAuthorities(rolesHeader);

                    // Build a fully authenticated token — credentials are null because
                    // the JWT itself is the proof of identity; no password is needed here.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Log the failure at WARN level to preserve stack-trace visibility
            // without blocking the filter chain — unauthenticated requests are
            // handled by the downstream security rules.
            log.warn("JWT authentication failed for request [{}]: {}",
                    request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Converts a comma-separated roles string into a list of {@link SimpleGrantedAuthority}.
     *
     * @param rolesHeader the raw X-Roles header value, may be {@code null} or empty
     * @return list of granted authorities; empty list if the header is absent
     */
    private List<SimpleGrantedAuthority> buildAuthorities(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return Collections.emptyList();
        }
        // Split on comma, trim whitespace, and wrap each role in a GrantedAuthority
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
