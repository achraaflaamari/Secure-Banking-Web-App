package com.kma.backend.repository;

import com.kma.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    Page<User> findByRoleNot(String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role <> :role")
    java.util.List<User> findAllByRoleNot(@Param("role") String role);

    @Query("SELECT u FROM User u WHERE u.role <> :role AND LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchByUsernameExcludingRole(@Param("query") String query, @Param("role") String role, Pageable pageable);
}
