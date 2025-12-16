package com.kma.backend.repository;

import com.kma.backend.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);
    boolean existsByEmail(String email);
    Optional<Profile> findByEmail(String email);

    Optional<Profile> findByRip(String rip);
    boolean existsByRip(String rip);
}
