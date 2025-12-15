package com.kma.backend.service;

import com.kma.backend.dto.CompteDTO;
import com.kma.backend.model.Compte;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CompteService {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private UserRepository userRepository;

    public CompteDTO getCompteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Compte compte = compteRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return convertToDTO(compte);
    }

    public CompteDTO getCompteById(Long userId) {
        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return convertToDTO(compte);
    }

    @Transactional
    public CompteDTO updateSolde(Long userId, BigDecimal amount) {
        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        compte.setSolde(compte.getSolde().add(amount));
        Compte updatedCompte = compteRepository.save(compte);

        return convertToDTO(updatedCompte);
    }

    @Transactional
    public void activateAccount(Long userId) {
        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        compte.setIsActive(true);
        compteRepository.save(compte);
    }

    @Transactional
    public void deactivateAccount(Long userId) {
        Compte compte = compteRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        compte.setIsActive(false);
        compteRepository.save(compte);
    }

    private CompteDTO convertToDTO(Compte compte) {
        CompteDTO dto = new CompteDTO();
        dto.setId(compte.getId());
        dto.setUserId(compte.getUser().getId());
        dto.setSolde(compte.getSolde());
        dto.setIsActive(compte.getIsActive());
        return dto;
    }
}
