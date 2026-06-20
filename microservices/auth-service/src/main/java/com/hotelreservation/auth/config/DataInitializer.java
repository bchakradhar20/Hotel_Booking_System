package com.hotelreservation.auth.config;

import com.hotelreservation.auth.entity.Role;
import com.hotelreservation.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Application startup component that seeds the roles table with default roles.
 *
 * <p>Implements {@link CommandLineRunner} so Spring Boot executes the {@link #run} method
 * automatically after the application context is fully loaded and all beans are wired.
 *
 * <p>Ensures that {@code ROLE_USER} and {@code ROLE_ADMIN} always exist in the database
 * before any registration request is processed. Without these records, user registration
 * would fail because {@link com.hotelreservation.auth.service.AuthService} looks up roles
 * by name and would throw an exception if they are missing.
 *
 * <p>The check before each insert (isEmpty guard) makes this idempotent — restarting
 * the application never duplicates the roles, which is safe for both development and production.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    /**
     * Constructs the initializer with the role repository.
     *
     * @param roleRepository JPA repository used to check existence and save roles
     */
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Seeds the roles table with the two required system roles on application startup.
     *
     * <p>Each role is only inserted if it does not already exist, making this
     * method safe to run on every restart (idempotent operation).
     *
     * <p>Roles seeded:
     * <ul>
     *   <li>{@code ROLE_USER} — assigned to every registered user by default</li>
     *   <li>{@code ROLE_ADMIN} — assigned additionally during admin registration</li>
     * </ul>
     *
     * @param args command-line arguments passed by Spring Boot (not used)
     */
    @Override
    public void run(String... args) {
        // Seed ROLE_USER — every registered user receives this role
        if (roleRepository.findByRoleName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
        }

        // Seed ROLE_ADMIN — assigned to admin accounts during admin registration
        if (roleRepository.findByRoleName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
        }
    }
}
