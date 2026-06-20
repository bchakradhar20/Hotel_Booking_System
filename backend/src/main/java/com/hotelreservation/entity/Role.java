package com.hotelreservation.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a user role in the system.
 * Roles are used for role-based access control (RBAC).
 * Expected values: ROLE_ADMIN, ROLE_USER
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /** Primary key for the role entity */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    /**
     * Unique role name (e.g., ROLE_ADMIN, ROLE_USER).
     * Must not be null and must be unique across the table.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String roleName;
}
