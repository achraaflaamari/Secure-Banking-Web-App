package com.kma.backend.controller;

import com.kma.backend.dto.CompteDTO;
import com.kma.backend.dto.ProfileDTO;
import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.dto.TransactionRequest;
import com.kma.backend.service.CompteService;
import com.kma.backend.service.ProfileService;
import com.kma.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private CompteService compteService;

    @Autowired
    private TransactionService transactionService;

    // Profile endpoints
    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            ProfileDTO profile = profileService.getProfileByUsername(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateProfile(Authentication authentication, @RequestBody ProfileDTO profileDTO) {
        try {
            String username = authentication.getName();
            ProfileDTO updatedProfile = profileService.updateProfile(username, profileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Account endpoints
    @GetMapping("/account")
    public ResponseEntity<CompteDTO> getAccount(Authentication authentication) {
        try {
            String username = authentication.getName();
            CompteDTO compte = compteService.getCompteByUsername(username);
            return ResponseEntity.ok(compte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Transaction endpoints
    @PostMapping("/transaction")
    public ResponseEntity<TransactionDTO> createTransaction(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            String senderUsername = authentication.getName();
            TransactionDTO transaction = transactionService.createTransaction(senderUsername, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserTransactionHistory(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/transactions/sent")
    public ResponseEntity<List<TransactionDTO>> getSentTransactions(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserSentTransactions(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/transactions/received")
    public ResponseEntity<List<TransactionDTO>> getReceivedTransactions(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserReceivedTransactions(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        try {
            TransactionDTO transaction = transactionService.getTransactionById(id);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
