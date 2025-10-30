package com.kyedev.spring_security_jwt.config;

import com.kyedev.spring_security_jwt.entity.Role;
import com.kyedev.spring_security_jwt.entity.User;
import com.kyedev.spring_security_jwt.repository.RoleRepository;
import com.kyedev.spring_security_jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeDefaultUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role roleUser = Role.builder()
                    .name("ROLE_USER")
                    .build();

            Role roleModerator = Role.builder()
                    .name("ROLE_MODERATOR")
                    .description("Moderator role")
                    .build();

            Role roleAdmin = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator role")
                    .build();

            roleRepository.save(roleUser);
            roleRepository.save(roleModerator);
            roleRepository.save(roleAdmin);
        }
        log.info("Roles initialized successfully");
    }

    private void initializeDefaultUsers() {
        if (userRepository.count() == 0) {
            // Create Admin User
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .roles(adminRoles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(admin);

            // Create Moderator User
            Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR")
                    .orElseThrow(() -> new RuntimeException("Moderator role not found"));

            Set<Role> moderatorRoles = new HashSet<>();
            moderatorRoles.add(moderatorRole);

            User moderator = User.builder()
                    .username("moderator")
                    .email("moderator@example.com")
                    .password(passwordEncoder.encode("moderator123"))
                    .firstName("Moderator")
                    .lastName("User")
                    .roles(moderatorRoles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(moderator);

            // Create Regular User
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("User role not found"));

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);

            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .firstName("Regular")
                    .lastName("User")
                    .roles(userRoles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(user);

            log.info("Default users initialized successfully");
            log.info("Admin: admin/admin123");
            log.info("Moderator: moderator/moderator123");
            log.info("User: user/user123");
        }
    }
}
