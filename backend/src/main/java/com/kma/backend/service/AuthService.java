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
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;

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

    @Autowired
    private EmailOtpService emailOtpService;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateRip20Digits() {
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private String generateUniqueRip() {
        for (int i = 0; i < 10; i++) {
            String rip = generateRip20Digits();
            if (!profileRepository.existsByRip(rip)) {
                return rip;
            }
        }
        throw new IllegalStateException("Failed to generate unique RIP");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = null;
        String password = request.getPassword() == null ? null : request.getPassword();
        String fullname = request.getFullname() == null ? null : request.getFullname().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        LocalDate birthday = request.getBirthday();

        // Email is the authenticated identifier
        username = email;

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return new AuthResponse(null, null, null, "Username already exists");
        }

        // Check if email already exists
        if (profileRepository.existsByEmail(email)) {
            return new AuthResponse(null, null, null, "Email already exists");
        }

        if (birthday == null) {
            return new AuthResponse(null, null, null, "Birthday is required");
        }
        if (Period.between(birthday, LocalDate.now()).getYears() < 18) {
            return new AuthResponse(null, null, null, "You must be at least 18 years old");
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
        profile.setRip(generateUniqueRip());
        profile.setPhone(phone);
        profile.setBirthday(birthday);
        // Keep inactive until email OTP verification
        profile.setIsActive(false);

        profileRepository.save(profile);

        // Create compte (account)
        Compte compte = new Compte();
        compte.setUser(savedUser);
        compte.setSolde(BigDecimal.valueOf(100));
        // Keep inactive until email OTP verification
        compte.setIsActive(false);

        compteRepository.save(compte);

        emailOtpService.createAndSendOtp(email, EmailOtpService.PURPOSE_SIGNUP);
        return new AuthResponse(null, savedUser.getUsername(), savedUser.getRole(), "OTP_SENT");
    }

    @Transactional
    public AuthResponse verifySignupOtp(String email, String code) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        User user = userRepository.findByUsername(normalizedEmail).orElse(null);
        if (user == null) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        boolean ok = emailOtpService.verifyOtp(normalizedEmail, EmailOtpService.PURPOSE_SIGNUP, code);
        if (!ok) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        Compte compte = compteRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null || compte == null) {
            return new AuthResponse(null, null, null, "Account not ready");
        }

        profile.setIsActive(true);
        compte.setIsActive(true);
        profileRepository.save(profile);
        compteRepository.save(compte);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        User user = userRepository.findByUsername(email)
                .orElse(null);

        if (user == null) {
            return new AuthResponse(null, null, null, "Email invalide");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, null, "Mot de passe invalide");
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            return new AuthResponse(null, null, null, "Account is permanently deactivated");
        }

        if ("ADMIN".equals(user.getRole())) {
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful");
        }

        if (!"ADMIN".equals(user.getRole())) {
            Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
            Compte compte = compteRepository.findByUserId(user.getId()).orElse(null);
            if (profile == null || compte == null || Boolean.FALSE.equals(profile.getIsActive()) || Boolean.FALSE.equals(compte.getIsActive())) {
                return new AuthResponse(null, null, null, "Account is deactivated");
            }
        }

        emailOtpService.createAndSendOtp(email, EmailOtpService.PURPOSE_LOGIN_2FA);
        return new AuthResponse(null, user.getUsername(), user.getRole(), "OTP_REQUIRED");
    }

    public AuthResponse verifyLoginOtp(String email, String code) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        User user = userRepository.findByUsername(normalizedEmail).orElse(null);
        if (user == null) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        boolean ok = emailOtpService.verifyOtp(normalizedEmail, EmailOtpService.PURPOSE_LOGIN_2FA, code);
        if (!ok) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful");
    }

    public AuthResponse forgotPassword(String email) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail != null && userRepository.existsByUsername(normalizedEmail)) {
            emailOtpService.createAndSendOtp(normalizedEmail, EmailOtpService.PURPOSE_RESET_PASSWORD);
        }
        return new AuthResponse(null, null, null, "If the email exists, an OTP has been sent");
    }

    @Transactional
    public AuthResponse resetPassword(String email, String code, String newPassword) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        User user = userRepository.findByUsername(normalizedEmail).orElse(null);
        if (user == null) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        boolean ok = emailOtpService.verifyOtp(normalizedEmail, EmailOtpService.PURPOSE_RESET_PASSWORD, code);
        if (!ok) {
            return new AuthResponse(null, null, null, "Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new AuthResponse(null, null, null, "Password updated");
    }

    public AuthResponse logout() {
        return new AuthResponse(null, null, null, "Logout successful");
    }
}
