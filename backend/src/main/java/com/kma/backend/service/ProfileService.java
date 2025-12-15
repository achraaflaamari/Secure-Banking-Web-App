package com.kma.backend.service;

import com.kma.backend.dto.ProfileDTO;
import com.kma.backend.model.Profile;
import com.kma.backend.model.User;
import com.kma.backend.repository.ProfileRepository;
import com.kma.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    public ProfileDTO getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return convertToDTO(profile);
    }

    public ProfileDTO getProfileById(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return convertToDTO(profile);
    }

    @Transactional
    public ProfileDTO updateProfile(String username, ProfileDTO profileDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profileDTO.getFullname() != null) {
            profile.setFullname(profileDTO.getFullname());
        }
        if (profileDTO.getPhone() != null) {
            profile.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getBirthday() != null) {
            profile.setBirthday(profileDTO.getBirthday());
        }
        if (profileDTO.getIsActive() != null) {
            profile.setIsActive(profileDTO.getIsActive());
        }

        Profile updatedProfile = profileRepository.save(profile);
        return convertToDTO(updatedProfile);
    }

    private ProfileDTO convertToDTO(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setFullname(profile.getFullname());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setBirthday(profile.getBirthday());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        dto.setIsActive(profile.getIsActive());
        return dto;
    }
}
