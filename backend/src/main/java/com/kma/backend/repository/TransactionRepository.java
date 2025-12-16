package com.kma.backend.repository;

import com.kma.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :userId OR t.receiver.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.receiver.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByReceiverId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    List<Transaction> findAllOrderByCreatedAtDesc();

    @Query("SELECT t FROM Transaction t ORDER BY t.createdAt DESC")
    Page<Transaction> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.status) = LOWER(:status) ORDER BY t.createdAt DESC")
    List<Transaction> findByStatusOrderByCreatedAtDesc(@Param("status") String status);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.status) = LOWER(:status) ORDER BY t.createdAt DESC")
    Page<Transaction> findByStatusOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);
}
