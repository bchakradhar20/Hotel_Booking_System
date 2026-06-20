package com.hotelreservation.reservation.security;

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
 * JWT authentication filter executed once per HTTP request in the Reservation Service.
 *
 * <p>Validates the Bearer token from the Authorization header and, on success,
 * populates the Spring Security context. Role-based authorities are sourced from
 * the {@code X-Roles} header injected by the API Gateway after token validation.
 *
 * <p>Authentication failures are logged at WARN level and do not abort the filter chain;
 * downstream security rules enforce access control for protected endpoints.
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
     * @param jwtUtil utility for JWT validation and claim extraction
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Validates the JWT token and sets the authenticated principal in the security context.
     *
     * <p>Steps:
     * <ol>
     *   <li>Detect and strip the Bearer prefix from the Authorization header.</li>
     *   <li>Validate the token via {@link JwtUtil#validateToken(String)}.</li>
     *   <li>Extract the username (subject claim) from the validated token.</li>
     *   <li>Build granted authorities from the comma-separated {@code X-Roles} header.</li>
     *   <li>Register the authentication in {@link SecurityContextHolder}.</li>
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
                String token = authHeader.substring(BEARER_PREFIX.length());

                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);

                    // Roles are forwarded by the API Gateway as a comma-separated header,
                    // avoiding re-parsing the JWT claims in every downstream service.
                    List<SimpleGrantedAuthority> authorities =
                            buildAuthorities(request.getHeader(ROLES_HEADER));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Log at WARN to preserve diagnostic information without surfacing
            // internal details to the client. The filter chain continues and
            // the endpoint security policy rejects unauthorized access.
            log.warn("JWT authentication failed for [{}]: {}",
                    request.getRequestURI(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parses a comma-separated roles string into Spring Security {@link SimpleGrantedAuthority} objects.
     *
     * @param rolesHeader the raw {@code X-Roles} header value; may be {@code null}
     * @return a non-null list of granted authorities (empty if the header is absent)
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
