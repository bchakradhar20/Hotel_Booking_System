package com.hotelreservation.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a registered user in the system.
 *
 * <p>Mapped to the {@code users} table in {@code hotel_auth_db}.
 * Both username and email are enforced as unique at both the database level
 * (via {@code @UniqueConstraint}) and the application level (checked in
 * {@link com.hotelreservation.auth.service.AuthService#validateUniqueCredentials}).
 *
 * <p>The {@code roles} collection uses a many-to-many relationship with the
 * {@code roles} table through the {@code user_roles} join table. Roles are
 * fetched eagerly ({@code FetchType.EAGER}) because every authentication and
 * authorization decision requires the user's roles immediately.
 *
 * <p>The password field always stores a BCrypt-encoded hash — never a plaintext password.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    /** Auto-generated primary key. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /** Unique login identifier. Max 50 characters. */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Unique email address. Max 100 characters. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** BCrypt-encoded password hash. Never stored as plaintext. */
    @Column(nullable = false)
    private String password;

    /** Optional contact phone number. Max 20 characters. */
    @Column(length = 20)
    private String phoneNumber;

    /**
     * Set of roles assigned to this user (e.g. ROLE_USER, ROLE_ADMIN).
     *
     * <p>Uses EAGER loading so roles are always available when Spring Security
     * builds the authentication principal. The join table {@code user_roles}
     * links users to roles by their respective primary keys.
     *
     * <p>{@code CascadeType.MERGE} and {@code CascadeType.PERSIST} allow roles
     * to be saved/merged together with the user in a single transaction.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
