package com.hotelreservation.repository;

import com.hotelreservation.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity database operations.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name.
     * Used during user registration to assign the default ROLE_USER.
     *
     * @param roleName the role name to search for (e.g., "ROLE_USER", "ROLE_ADMIN")
     * @return an Optional containing the Role if found, or empty if not
     */
    Optional<Role> findByRoleName(String roleName);
}
