package com.hotelreservation.auth.security;

import com.hotelreservation.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security principal object wrapping an authenticated {@link User} entity.
 *
 * <p>This class bridges the application's {@link User} domain model with Spring Security's
 * {@link UserDetails} contract. It is constructed by
 * {@link UserDetailsServiceImpl#loadUserByUsername(String)} during the authentication process
 * and then used by {@link JwtUtil#generateToken(org.springframework.security.core.Authentication)}
 * to embed user identity into the JWT token.
 *
 * <p>Fields carried by this principal:
 * <ul>
 *   <li>{@code userId} — the user's database primary key, embedded in the JWT as a custom claim</li>
 *   <li>{@code username} — the login name, used as the JWT subject ({@code sub} claim)</li>
 *   <li>{@code email} — included in the login response for UI display</li>
 *   <li>{@code password} — the BCrypt hash used by Spring Security to verify the submitted password</li>
 *   <li>{@code authorities} — the user's roles as {@link SimpleGrantedAuthority} objects</li>
 * </ul>
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a fully populated principal with all required identity fields.
     *
     * @param userId      the user's database ID
     * @param username    the user's login name
     * @param email       the user's email address
     * @param password    the user's BCrypt-encoded password hash
     * @param authorities the user's granted authorities (roles)
     */
    public UserDetailsImpl(Long userId, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method that builds a {@link UserDetailsImpl} from a {@link User} entity.
     *
     * <p>Called by {@link UserDetailsServiceImpl} after loading the user from the database.
     * Each role in the user's role set is converted to a {@link SimpleGrantedAuthority}
     * using the role's name (e.g. {@code "ROLE_ADMIN"}).
     *
     * @param user the user entity loaded from the database
     * @return a fully constructed {@link UserDetailsImpl} ready for use by Spring Security
     */
    public static UserDetailsImpl build(User user) {
        // Convert each Role entity into a Spring Security GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    // ── Spring Security account status checks ──────────────────────────────────
    // All return true because this application does not implement account locking,
    // expiry, or disabling features. Override these to add account management logic.

    /** @return {@code true} — account expiry is not implemented */
    @Override public boolean isAccountNonExpired()     { return true; }

    /** @return {@code true} — account locking is not implemented */
    @Override public boolean isAccountNonLocked()      { return true; }

    /** @return {@code true} — credential expiry is not implemented */
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** @return {@code true} — account disabling is not implemented */
    @Override public boolean isEnabled()               { return true; }
}
