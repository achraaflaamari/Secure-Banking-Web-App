package com.kma.backend.service;

import com.kma.backend.dto.TransactionDTO;
import com.kma.backend.exception.NotFoundException;
import com.kma.backend.model.Compte;
import com.kma.backend.model.Transaction;
import com.kma.backend.model.TransactionStatus;
import com.kma.backend.repository.CompteRepository;
import com.kma.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminTransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CompteRepository compteRepository;

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAllOrderByCreatedAtDesc();
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TransactionDTO> getAllTransactionsPage(Pageable pageable) {
        Page<Transaction> page = transactionRepository.findAllOrderByCreatedAtDesc(pageable);
        List<TransactionDTO> items = page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageImpl<>(items, pageable, page.getTotalElements());
    }

    public TransactionDTO getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return convertToDTO(transaction);
    }

    public List<TransactionDTO> getTransactionsByStatus(String status) {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(status).stream()
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
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        TransactionStatus target;
        try {
            target = TransactionStatus.valueOf(newStatus.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status");
        }

        TransactionStatus current;
        try {
            current = TransactionStatus.valueOf(transaction.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid current status");
        }

        // Allowed transitions: PENDING -> COMPLETED, COMPLETED -> REVERSED
        boolean allowed =
                (current == TransactionStatus.PENDING && target == TransactionStatus.COMPLETED) ||
                (current == TransactionStatus.COMPLETED && target == TransactionStatus.REVERSED);
        if (!allowed) {
            throw new IllegalArgumentException("Invalid status transition");
        }

        transaction.setStatus(target.name());
        Transaction updatedTransaction = transactionRepository.save(transaction);

        return convertToDTO(updatedTransaction);
    }

    @Transactional
    public void reverseTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (!TransactionStatus.COMPLETED.name().equalsIgnoreCase(transaction.getStatus())) {
            throw new IllegalArgumentException("Only completed transactions can be reversed");
        }

        Compte senderCompte = compteRepository.findByUserId(transaction.getSender().getId())
                .orElseThrow(() -> new NotFoundException("Sender account not found"));
        Compte receiverCompte = compteRepository.findByUserId(transaction.getReceiver().getId())
                .orElseThrow(() -> new NotFoundException("Receiver account not found"));

        if (Boolean.FALSE.equals(senderCompte.getIsActive()) || Boolean.FALSE.equals(receiverCompte.getIsActive())) {
            throw new IllegalArgumentException("Cannot reverse transaction for inactive account");
        }

        BigDecimal amount = transaction.getAmount();
        if (receiverCompte.getSolde().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Receiver balance is insufficient to reverse");
        }

        // revert balance changes
        receiverCompte.setSolde(receiverCompte.getSolde().subtract(amount));
        senderCompte.setSolde(senderCompte.getSolde().add(amount));
        compteRepository.save(receiverCompte);
        compteRepository.save(senderCompte);

        transaction.setStatus(TransactionStatus.REVERSED.name());
        transactionRepository.save(transaction);
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
