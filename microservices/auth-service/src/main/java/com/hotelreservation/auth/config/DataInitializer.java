package com.hotelreservation.auth.config;

import com.hotelreservation.auth.entity.Role;
import com.hotelreservation.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.findByRoleName("ROLE_USER").isEmpty())
            roleRepository.save(new Role(null, "ROLE_USER"));
        if (roleRepository.findByRoleName("ROLE_ADMIN").isEmpty())
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
    }
}
