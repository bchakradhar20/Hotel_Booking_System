package com.hotelreservation.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that runs once per HTTP request.
 * Extracts and validates the Bearer token from the Authorization header,
 * then sets the authenticated user into the Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructor injection for JWT utility and UserDetailsService.
     *
     * @param jwtUtil            utility class for JWT operations
     * @param userDetailsService service used to load user details from the database
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Core filter method invoked for every HTTP request.
     * JWT authentication flow:
     * 1. Extract the Bearer token from the Authorization header.
     * 2. Validate the token using JwtUtil.
     * 3. Load the corresponding UserDetails from the database.
     * 4. Set the authenticated principal into the SecurityContext.
     * If any step fails, the filter chain continues without authentication,
     * and the security framework will reject the request if the endpoint requires auth.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract the JWT token from the Authorization header
            String jwt = parseJwt(request);

            // Step 2: Validate the token; if invalid or missing, skip authentication
            if (jwt != null && jwtUtil.validateToken(jwt)) {

                // Step 3: Extract the username from the token's subject claim
                String username = jwtUtil.getUsernameFromToken(jwt);

                // Step 4: Load user details from the database using the extracted username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Build the authentication token with the user's authorities
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Step 6: Attach request-specific details (e.g., IP address, session ID)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 7: Store the authentication in the SecurityContext for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue the filter chain regardless of authentication outcome
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token string from the Authorization header.
     * Expects the header to be in the format: "Bearer <token>"
     *
     * @param request the HTTP request to extract the token from
     * @return the token string without the "Bearer " prefix, or null if not present
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Check that the header is present and starts with "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}
