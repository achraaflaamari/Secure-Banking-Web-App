package com.kma.backend.service;

import com.kma.backend.model.EmailOtp;
import com.kma.backend.repository.EmailOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailOtpService {

    public static final String PURPOSE_SIGNUP = "SIGNUP";
    public static final String PURPOSE_LOGIN_2FA = "LOGIN_2FA";
    public static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    public static final String PURPOSE_CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String PURPOSE_PREFIX_TRANSACTION = "TRANSACTION_";

    private static final int OTP_LENGTH = 6;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtpCode() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;
        int code = min + secureRandom.nextInt(max - min + 1);
        return String.valueOf(code);
    }

    @Transactional
    public void createAndSendOtp(String email, String purpose) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        String code = generateOtpCode();

        EmailOtp otp = new EmailOtp();
        otp.setEmail(normalizedEmail);
        otp.setPurpose(purpose);
        otp.setCodeHash(passwordEncoder.encode(code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(otp);

        String subject = "Your KMA verification code";
        String body = "Your verification code is: " + code + "\n\nThis code expires in 10 minutes.";
        emailService.sendOtpEmail(normalizedEmail, subject, body);
    }

    @Transactional
    public boolean verifyOtp(String email, String purpose, String code) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        EmailOtp otp = emailOtpRepository
                .findTopByEmailAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                        normalizedEmail,
                        purpose,
                        LocalDateTime.now()
                )
                .orElse(null);

        if (otp == null) {
            return false;
        }
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            return false;
        }

        otp.setUsedAt(LocalDateTime.now());
        emailOtpRepository.save(otp);
        return true;
    }
}
