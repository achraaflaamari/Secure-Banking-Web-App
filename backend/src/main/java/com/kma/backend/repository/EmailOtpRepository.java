package com.kma.backend.repository;

import com.kma.backend.model.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String email,
            String purpose,
            LocalDateTime now
    );
}
