package com.hotelreservation.auth.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility component for JWT token generation, parsing, and validation.
 *
 * <p>Tokens are signed using HMAC SHA-256 with a Base64-encoded secret loaded
 * from {@code application.yml}. Each token embeds:
 * <ul>
 *   <li>{@code sub} — the authenticated username (standard JWT subject claim)</li>
 *   <li>{@code userId} — the user's database primary key (custom claim)</li>
 *   <li>{@code roles} — list of role names, e.g. {@code ["ROLE_USER", "ROLE_ADMIN"]} (custom claim)</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 *
 * <p>The custom {@code userId} and {@code roles} claims allow the API Gateway
 * to extract user context and forward it as request headers to downstream services,
 * avoiding repeated database lookups on every request.
 */
@Component
public class JwtUtil {

    /** Base64-encoded HMAC secret key, injected from {@code app.jwt.secret}. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token validity in milliseconds, injected from {@code app.jwt.expiration}. */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a signed JWT token for a successfully authenticated user.
     *
     * <p>Embeds the username as the subject, and includes {@code userId} and
     * {@code roles} as custom claims so the API Gateway can forward user context
     * to downstream microservices without additional database calls.
     *
     * @param authentication the authenticated principal from Spring Security
     * @return a compact, URL-safe signed JWT string
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Extract role name strings from the principal's granted authorities
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                // Standard subject claim — used by all services to identify the caller
                .setSubject(userPrincipal.getUsername())
                // Custom claim: userId forwarded by the API Gateway as X-User-Id header
                .claim("userId", userPrincipal.getUserId())
                // Custom claim: roles forwarded by the API Gateway as X-Roles header
                .claim("roles", roles)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationMs)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a validated JWT token string.
     *
     * @param token the compact JWT string to parse
     * @return the username stored in the token's {@code sub} claim
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token for structural integrity, valid signature, and non-expiry.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid; {@code false} for any failure
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Any JWT exception (malformed, expired, wrong signature) results in rejection
            return false;
        }
    }

    /**
     * Derives the HMAC SHA signing key from the configured Base64-encoded secret.
     *
     * @return the {@link Key} used to sign and verify JWT tokens
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
