package com.kma.backend.service;

import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.dto.TransactionInitiateResponse;
import com.kma.backend.dto.TransactionRequest;
import com.kma.backend.model.Compte;
import com.kma.backend.model.Profile;
import com.kma.backend.model.Transaction;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.ProfileRepository;
import com.kma.backend.repository.TransactionRepository;
import com.kma.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EmailOtpService emailOtpService;

    @Transactional
    public TransactionInitiateResponse initiateTransaction(String senderEmail, TransactionRequest request) {
        User sender = userRepository.findByUsername(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Profile senderProfile = profileRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender profile not found"));
        if (Boolean.FALSE.equals(senderProfile.getIsActive())) {
            throw new IllegalArgumentException("Sender is not active");
        }

        String receiverRip = request.getReceiverRip() == null ? null : request.getReceiverRip().trim();
        Profile receiverProfileLookup = profileRepository.findByRip(receiverRip)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        User receiver = receiverProfileLookup.getUser();
        if (receiver.getId() != null && receiver.getId().equals(sender.getId())) {
            throw new IllegalArgumentException("Cannot transfer to نفسك (self-transfer is not allowed)");
        }

        if (Boolean.FALSE.equals(receiverProfileLookup.getIsActive())) {
            throw new IllegalArgumentException("Receiver is not active");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        Compte senderCompte = compteRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        if (Boolean.FALSE.equals(senderCompte.getIsActive())) {
            throw new IllegalArgumentException("Sender account is not active");
        }
        if (senderCompte.getSolde().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Compte receiverCompte = compteRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));
        if (Boolean.FALSE.equals(receiverCompte.getIsActive())) {
            throw new IllegalArgumentException("Receiver account is not active");
        }

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("PENDING");
        Transaction saved = transactionRepository.save(transaction);

        String purpose = EmailOtpService.PURPOSE_PREFIX_TRANSACTION + saved.getId();
        emailOtpService.createAndSendOtp(senderEmail, purpose);

        return new TransactionInitiateResponse(saved.getId(), "OTP_REQUIRED");
    }

    @Transactional
    public TransactionDTO verifyTransaction(String senderEmail, Long transactionId, String code) {
        User sender = userRepository.findByUsername(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getSender() == null || transaction.getSender().getId() == null
                || !transaction.getSender().getId().equals(sender.getId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        if (!"PENDING".equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalArgumentException("Transaction is not pending");
        }

        String purpose = EmailOtpService.PURPOSE_PREFIX_TRANSACTION + transactionId;
        boolean ok = emailOtpService.verifyOtp(senderEmail, purpose, code);
        if (!ok) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        Compte senderCompte = compteRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        Compte receiverCompte = compteRepository.findByUserId(transaction.getReceiver().getId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (senderCompte.getSolde().compareTo(transaction.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        senderCompte.setSolde(senderCompte.getSolde().subtract(transaction.getAmount()));
        receiverCompte.setSolde(receiverCompte.getSolde().add(transaction.getAmount()));
        compteRepository.save(senderCompte);
        compteRepository.save(receiverCompte);

        transaction.setStatus("COMPLETED");
        Transaction updated = transactionRepository.save(transaction);
        return convertToDTO(updated);
    }

    @Transactional
    public TransactionDTO createTransaction(String senderEmail, TransactionRequest request) {
        // Validate sender
        User sender = userRepository.findByUsername(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Profile senderProfile = profileRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender profile not found"));
        if (Boolean.FALSE.equals(senderProfile.getIsActive())) {
            throw new IllegalArgumentException("Sender is not active");
        }

        // Validate receiver
        String receiverRip = request.getReceiverRip() == null ? null : request.getReceiverRip().trim();
        Profile receiverProfileLookup = profileRepository.findByRip(receiverRip)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        User receiver = receiverProfileLookup.getUser();
        if (receiver.getId() != null && receiver.getId().equals(sender.getId())) {
            throw new IllegalArgumentException("Cannot transfer to نفسك (self-transfer is not allowed)");
        }

        Profile receiverProfile = receiverProfileLookup;
        if (Boolean.FALSE.equals(receiverProfile.getIsActive())) {
            throw new IllegalArgumentException("Receiver is not active");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        // Check sender's balance
        Compte senderCompte = compteRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        if (Boolean.FALSE.equals(senderCompte.getIsActive())) {
            throw new IllegalArgumentException("Sender account is not active");
        }

        if (senderCompte.getSolde().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Check receiver account exists
        Compte receiverCompte = compteRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (Boolean.FALSE.equals(receiverCompte.getIsActive())) {
            throw new IllegalArgumentException("Receiver account is not active");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setStatus("COMPLETED");

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Update balances
        senderCompte.setSolde(senderCompte.getSolde().subtract(request.getAmount()));
        receiverCompte.setSolde(receiverCompte.getSolde().add(request.getAmount()));

        compteRepository.save(senderCompte);
        compteRepository.save(receiverCompte);

        return convertToDTO(savedTransaction);
    }

    public List<TransactionDTO> getUserTransactionHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getUserSentTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findBySenderId(user.getId());
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getUserReceivedTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByReceiverId(user.getId());
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return convertToDTO(transaction);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSenderId(transaction.getSender().getId());
        dto.setSenderUsername(transaction.getSender().getUsername());
        dto.setReceiverId(transaction.getReceiver().getId());
        dto.setReceiverUsername(transaction.getReceiver().getUsername());
        dto.setAmount(transaction.getAmount());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}
