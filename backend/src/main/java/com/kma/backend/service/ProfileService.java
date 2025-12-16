package com.kma.backend.service;

import com.kma.backend.dto.ProfileDTO;
import com.kma.backend.exception.NotFoundException;
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

    public ProfileDTO getProfileByUsername(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        return convertToDTO(profile);
    }

    public ProfileDTO getProfileById(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        return convertToDTO(profile);
    }

    @Transactional
    public ProfileDTO updateProfile(String email, ProfileDTO profileDTO) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        if (profileDTO.getFullname() != null) {
            profile.setFullname(profileDTO.getFullname().trim());
        }
        if (profileDTO.getPhone() != null) {
            profile.setPhone(profileDTO.getPhone().trim());
        }
        if (profileDTO.getBirthday() != null) {
            profile.setBirthday(profileDTO.getBirthday());
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
        dto.setRip(profile.getRip());
        dto.setPhone(profile.getPhone());
        dto.setBirthday(profile.getBirthday());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        dto.setIsActive(profile.getIsActive());
        return dto;
    }
}
