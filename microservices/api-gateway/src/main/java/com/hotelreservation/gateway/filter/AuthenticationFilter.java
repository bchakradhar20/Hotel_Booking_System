package com.hotelreservation.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

/**
 * Gateway filter that validates JWT tokens and forwards user context headers
 * (X-Username, X-User-Id, X-Roles) to downstream microservices.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public AuthenticationFilter() { super(Config.class); }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSigningKey()).build()
                        .parseClaimsJws(authHeader.substring(7)).getBody();

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                String rolesHeader = roles != null ? String.join(",", roles) : "";
                Object userIdObj = claims.get("userId");
                String userId = userIdObj != null ? userIdObj.toString() : "";

                var mutated = exchange.getRequest().mutate()
                        .header("X-Username", claims.getSubject())
                        .header("X-User-Id", userId)
                        .header("X-Roles", rolesHeader)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public static class Config {}
}
