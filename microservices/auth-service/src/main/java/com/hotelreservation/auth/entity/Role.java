package com.hotelreservation.auth.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a user role in the system.
 *
 * <p>Mapped to the {@code roles} table in {@code hotel_auth_db}.
 * Roles are seeded by {@link com.hotelreservation.auth.config.DataInitializer}
 * on application startup, so they always exist before any user registration.
 *
 * <p>Supported role values:
 * <ul>
 *   <li>{@code ROLE_USER} — assigned to every registered user by default</li>
 *   <li>{@code ROLE_ADMIN} — assigned additionally during admin registration</li>
 * </ul>
 *
 * <p>The {@code ROLE_} prefix is the Spring Security convention for role names
 * used with {@code hasRole()} and {@code hasAnyRole()} in {@code @PreAuthorize} expressions.
 */
@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role {

    /** Auto-generated primary key. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    /** Unique role name (e.g. "ROLE_USER", "ROLE_ADMIN"). Max 50 characters. */
    @Column(nullable = false, unique = true, length = 50)
    private String roleName;
}
