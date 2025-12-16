package com.kma.backend.service;

import com.kma.backend.dto.UserDTO;
import com.kma.backend.model.Profile;
import com.kma.backend.model.Compte;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.ProfileRepository;
import com.kma.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CompteRepository compteRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllByRoleNot("ADMIN");
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    public List<UserDTO> searchUsers(String searchTerm) {
        List<User> users = userRepository.findAllByRoleNot("ADMIN");
        return users.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<UserDTO> getUsersPage(Pageable pageable) {
        Page<User> page = userRepository.findByRoleNot("ADMIN", pageable);
        List<UserDTO> items = page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    public Page<UserDTO> searchUsersPage(String query, Pageable pageable) {
        Page<User> page = userRepository.searchByUsernameExcludingRole(query, "ADMIN", pageable);
        List<UserDTO> items = page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("User is permanently deactivated");
        }

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        profile.setIsActive(true);
        profileRepository.save(profile);
        compte.setIsActive(true);
        compteRepository.save(compte);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        profile.setIsActive(false);
        profileRepository.save(profile);
        compte.setIsActive(false);
        compteRepository.save(compte);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Soft delete - deactivate the user
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        profile.setIsActive(false);
        profileRepository.save(profile);

        compte.setIsActive(false);
        compteRepository.save(compte);

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        if (profile != null) {
            dto.setFullname(profile.getFullname());
            dto.setEmail(profile.getEmail());
            dto.setPhone(profile.getPhone());
            dto.setIsActive(profile.getIsActive());
        }
        return dto;
    }
}
