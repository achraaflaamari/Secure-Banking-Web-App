package com.kma.backend.service;

import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.dto.TransactionRequest;
import com.kma.backend.model.Compte;
import com.kma.backend.model.Transaction;
import com.kma.backend.model.User;
import com.kma.backend.repository.CompteRepository;
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

    @Transactional
    public TransactionDTO createTransaction(String senderUsername, TransactionRequest request) {
        // Validate sender
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Validate receiver
        String receiverUsername = request.getReceiverUsername() == null ? null : request.getReceiverUsername().trim();
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        // Check sender's balance
        Compte senderCompte = compteRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        if (senderCompte.getSolde().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Check receiver account exists
        Compte receiverCompte = compteRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

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
