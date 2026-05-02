package com.lpu.config;

import com.lpu.entity.Role;
import com.lpu.entity.User;
import com.lpu.repository.RoleRepository;
import com.lpu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("DATA SEEDER: Checking for roles and default users...");

        // 1. Seed Roles
        Role userRole = seedRole("ROLE_USER");
        Role adminRole = seedRole("ROLE_ADMIN");

        // 2. Seed Default Admin
        seedUser("admin@finflow.com", "admin123", adminRole);

        // 3. Seed Default User
        seedUser("user@finflow.com", "user123", userRole);

        log.info("DATA SEEDER: Initialization complete.");
    }

    private Role seedRole(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> {
            log.info("DATA SEEDER: Creating role {}", roleName);
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }

    private void seedUser(String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            log.info("DATA SEEDER: Creating default user {}", email);
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Set.of(role));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
