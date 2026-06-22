package com.rms.config;

import com.rms.entity.User;
import com.rms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures a default admin/admin123 account always exists and works.
 *
 * On every startup:
 *  - If no "admin" user exists, creates one with password "admin123".
 *  - If "admin" exists but its stored hash does NOT match "admin123"
 *    (e.g. the placeholder hash that used to be in schema.sql), it is
 *    reset to a correctly encoded "admin123" so login stops returning 401.
 *
 * Remove or disable this once you've set your own admin credentials in production.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByName("admin").ifPresentOrElse(existing -> {
            if (!passwordEncoder.matches("admin123", existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(existing);
                System.out.println("[DataSeeder] Existing 'admin' user had an invalid password hash - reset to 'admin123'.");
            }
        }, () -> {
            User admin = User.builder()
                    .name("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .type("admin")
                    .build();
            userRepository.save(admin);
            System.out.println("[DataSeeder] Created default user 'admin' with password 'admin123'.");
        });
    }
}