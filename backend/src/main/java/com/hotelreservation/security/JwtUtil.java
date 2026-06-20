package com.hotelreservation.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for JWT token generation, parsing, and validation.
 * Uses HMAC SHA-256 algorithm with a configurable secret key and expiration.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /** Secret key used for signing JWT tokens — loaded from application.properties */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token validity duration in milliseconds — loaded from application.properties */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generates a JWT token for an authenticated user.
     * The token contains the username as the subject and includes issued-at
     * and expiration timestamps signed with HMAC SHA-256.
     *
     * @param authentication the successfully authenticated principal from Spring Security
     * @return signed JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                // Set the subject to the authenticated username
                .setSubject(userPrincipal.getUsername())
                // Record when the token was issued
                .setIssuedAt(new Date())
                // Set the expiration time based on configured duration
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // Sign the token using HMAC SHA-256 with the secret key
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token string.
     *
     * @param token the JWT token to parse
     * @return the username embedded in the token's subject claim
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
     * Validates a JWT token for correctness and expiry.
     * Logs specific error messages for different failure scenarios
     * to aid in debugging and security auditing.
     *
     * @param token the JWT token string to validate
     * @return true if the token is valid; false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Attempt to parse and verify the token signature and expiry
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            // Token structure is invalid (e.g., missing parts)
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // Token has passed its expiration timestamp
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // Token uses an algorithm or format not supported
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token string is null, empty, or only whitespace
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Derives the signing key from the configured Base64-encoded secret string.
     * Uses HMAC SHA key generation as required for HS256.
     *
     * @return the HMAC SHA signing key
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
