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
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole("ADMIN");
        User savedAdmin = userRepository.save(admin);

        Profile profile = new Profile();
        profile.setUser(savedAdmin);
        profile.setFullname("Administrator");
        profile.setEmail("admin@kma.local");
        profile.setPhone("0000000000");
        profile.setIsActive(true);
        profileRepository.save(profile);
    }
}
