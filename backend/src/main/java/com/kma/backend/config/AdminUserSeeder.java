package com.kma.backend.config;

import com.kma.backend.model.Compte;
import com.kma.backend.model.Profile;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.ProfileRepository;
import com.kma.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CompteRepository compteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeeder(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            CompteRepository compteRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.compteRepository = compteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin@kma.local");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin");
        String adminFullname = System.getenv().getOrDefault("ADMIN_FULLNAME", "Administrator");
        String adminPhone = System.getenv().getOrDefault("ADMIN_PHONE", "0000000000");

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");
        User savedAdmin = userRepository.save(admin);

        Profile profile = new Profile();
        profile.setUser(savedAdmin);
        profile.setFullname(adminFullname);
        profile.setEmail(adminUsername);
        profile.setPhone(adminPhone);
        profile.setIsActive(true);
        profileRepository.save(profile);

    }
}
