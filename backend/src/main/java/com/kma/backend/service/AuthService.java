package com.kma.backend.service;

import com.kma.backend.dto.AuthResponse;
import com.kma.backend.dto.LoginRequest;
import com.kma.backend.dto.RegisterRequest;
import com.kma.backend.model.Compte;
import com.kma.backend.model.Profile;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.ProfileRepository;
import com.kma.backend.repository.UserRepository;
import com.kma.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String password = request.getPassword() == null ? null : request.getPassword();
        String fullname = request.getFullname() == null ? null : request.getFullname().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return new AuthResponse(null, null, null, "Username already exists");
        }

        // Check if email already exists
        if (profileRepository.existsByEmail(email)) {
            return new AuthResponse(null, null, null, "Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        // Create profile
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setFullname(fullname);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setIsActive(true);

        profileRepository.save(profile);

        // Create compte (account)
        Compte compte = new Compte();
        compte.setUser(savedUser);
        compte.setSolde(BigDecimal.valueOf(100));
        compte.setIsActive(true);

        compteRepository.save(compte);

        // Generate token
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole());

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole(), "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, null, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful");
    }

    public AuthResponse logout() {
        return new AuthResponse(null, null, null, "Logout successful");
    }
}
