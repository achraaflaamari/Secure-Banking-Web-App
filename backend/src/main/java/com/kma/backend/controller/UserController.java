package com.kma.backend.controller;

import com.kma.backend.dto.CompteDTO;
import com.kma.backend.dto.AuthResponse;
import com.kma.backend.dto.ErrorResponse;
import com.kma.backend.dto.PasswordChangeRequest;
import com.kma.backend.dto.PasswordChangeVerifyRequest;
import com.kma.backend.dto.ProfileDTO;
import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.dto.TransactionInitiateResponse;
import com.kma.backend.dto.TransactionRequest;
import com.kma.backend.dto.TransactionVerifyRequest;
import com.kma.backend.service.CompteService;
import com.kma.backend.service.ProfileService;
import com.kma.backend.service.TransactionService;
import com.kma.backend.service.UserPasswordService;
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

    @Autowired
    private UserPasswordService userPasswordService;

    // Profile endpoints
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            ProfileDTO profile = profileService.getProfileByUsername(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody ProfileDTO profileDTO) {
        try {
            String username = authentication.getName();
            ProfileDTO updatedProfile = profileService.updateProfile(username, profileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Password change endpoints (OTP)
    @PostMapping("/password/change/request")
    public ResponseEntity<?> requestPasswordChange(Authentication authentication, @Valid @RequestBody PasswordChangeRequest request) {
        try {
            String username = authentication.getName();
            userPasswordService.requestPasswordChangeOtp(username, request.getCurrentPassword());
            return ResponseEntity.ok(new AuthResponse(null, username, null, "OTP_SENT"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/password/change/verify")
    public ResponseEntity<?> verifyPasswordChange(Authentication authentication, @Valid @RequestBody PasswordChangeVerifyRequest request) {
        try {
            String username = authentication.getName();
            userPasswordService.verifyPasswordChangeOtpAndUpdate(username, request.getCode(), request.getNewPassword());
            return ResponseEntity.ok(new AuthResponse(null, username, null, "Password updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Account endpoints
    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Authentication authentication) {
        try {
            String username = authentication.getName();
            CompteDTO compte = compteService.getCompteByUsername(username);
            return ResponseEntity.ok(compte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Transaction endpoints
    @PostMapping("/transaction")
    public ResponseEntity<?> createTransaction(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            String senderUsername = authentication.getName();
            TransactionDTO transaction = transactionService.createTransaction(senderUsername, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/transaction/initiate")
    public ResponseEntity<?> initiateTransaction(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            String senderUsername = authentication.getName();
            TransactionInitiateResponse response = transactionService.initiateTransaction(senderUsername, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/transaction/verify")
    public ResponseEntity<?> verifyTransaction(Authentication authentication, @Valid @RequestBody TransactionVerifyRequest request) {
        try {
            String senderUsername = authentication.getName();
            TransactionDTO transaction = transactionService.verifyTransaction(senderUsername, request.getTransactionId(), request.getCode());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserTransactionHistory(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/transactions/sent")
    public ResponseEntity<?> getSentTransactions(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserSentTransactions(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/transactions/received")
    public ResponseEntity<?> getReceivedTransactions(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<TransactionDTO> transactions = transactionService.getUserReceivedTransactions(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable Long id) {
        try {
            TransactionDTO transaction = transactionService.getTransactionById(id);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }
}
