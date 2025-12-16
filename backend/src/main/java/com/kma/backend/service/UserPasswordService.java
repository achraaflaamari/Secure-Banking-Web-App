package com.kma.backend.service;

import com.kma.backend.exception.NotFoundException;
import com.kma.backend.model.User;
import com.kma.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailOtpService emailOtpService;

    public void requestPasswordChangeOtp(String username, String currentPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel invalide");
        }

        emailOtpService.createAndSendOtp(username, EmailOtpService.PURPOSE_CHANGE_PASSWORD);
    }

    @Transactional
    public void verifyPasswordChangeOtpAndUpdate(String username, String code, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean ok = emailOtpService.verifyOtp(username, EmailOtpService.PURPOSE_CHANGE_PASSWORD, code);
        if (!ok) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
