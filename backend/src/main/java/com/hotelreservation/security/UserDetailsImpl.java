package com.hotelreservation.security;

import com.hotelreservation.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * Wraps the User entity and provides authentication context to the security framework.
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long userId;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs UserDetailsImpl from individual fields.
     *
     * @param userId      the user's unique identifier
     * @param username    the user's login name
     * @param email       the user's email
     * @param password    the BCrypt-encoded password
     * @param authorities the user's granted authorities (roles)
     */
    public UserDetailsImpl(Long userId, String username, String email,
                           String password, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method to build UserDetailsImpl from a User entity.
     * Maps roles from the User entity to Spring Security GrantedAuthority objects.
     *
     * @param user the User entity loaded from the database
     * @return populated UserDetailsImpl instance
     */
    public static UserDetailsImpl build(User user) {
        // Convert each Role entity to a SimpleGrantedAuthority for Spring Security
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

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
