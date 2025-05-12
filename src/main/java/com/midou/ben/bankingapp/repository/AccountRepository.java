package com.midou.ben.bankingapp.repository;

import com.midou.ben.bankingapp.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> { // Entity type and Primary Key type

    // Custom query method to find an account by its account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Check if an account number already exists
    boolean existsByAccountNumber(String accountNumber);
}
