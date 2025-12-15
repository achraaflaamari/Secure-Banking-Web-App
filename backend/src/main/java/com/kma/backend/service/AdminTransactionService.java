package com.kma.backend.service;

import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.model.Transaction;
import com.kma.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllOrderByCreatedAtDesc();
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return convertToDTO(transaction);
    }

    public List<TransactionDTO> getTransactionsByStatus(String status) {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase(status))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionDTO updateTransactionStatus(Long transactionId, String newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(newStatus);
        Transaction updatedTransaction = transactionRepository.save(transaction);

        return convertToDTO(updatedTransaction);
    }

    @Transactional
    public void reverseTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if ("COMPLETED".equalsIgnoreCase(transaction.getStatus())) {
            transaction.setStatus("REVERSED");
            transactionRepository.save(transaction);
            // Note: In a real application, you would also reverse the balance changes
        } else {
            throw new RuntimeException("Only completed transactions can be reversed");
        }
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
