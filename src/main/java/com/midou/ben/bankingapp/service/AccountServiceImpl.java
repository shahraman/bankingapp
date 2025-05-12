package com.midou.ben.bankingapp.service;

import com.midou.ben.bankingapp.exception.AccountNotFoundException;
import com.midou.ben.bankingapp.exception.InsufficientFundsException;
import com.midou.ben.bankingapp.exception.AccountOperationException;
import com.midou.ben.bankingapp.model.Account;
import com.midou.ben.bankingapp.repository.AccountRepository;
import lombok.RequiredArgsConstructor; // Lombok for constructor injection
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional; // Import Spring's Transactional

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository; // Inject repository

    @Override
    @Transactional
    public Account createAccount(String ownerName, String accountNumber, BigDecimal initialBalance) {
        log.info("Attempting to create account for owner: {}, number: {}", ownerName, accountNumber);
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountOperationException("Account number '" + accountNumber + "' already exists.");
        }
        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountOperationException("Initial balance cannot be negative.");
        }

        Account account = new Account(accountNumber, ownerName, initialBalance);
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully with ID: {} and Number: {}", savedAccount.getId(), savedAccount.getAccountNumber());
        return savedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        log.debug("Fetching account by number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    // Default Transactional: Propagation.REQUIRED, Isolation.DEFAULT
    // Ensures that if any exception occurs, the DB changes are rolled back.
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Account deposit(String accountNumber, BigDecimal amount) {
        log.info("Attempting to deposit {} into account {}", amount, accountNumber);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountOperationException("Deposit amount must be positive.");
        }

        // Find the account - use orElseThrow for cleaner exception handling
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));
        // Synchronize on the specific account object instance
        synchronized (account) {
            // Perform deposit
            account.setBalance(account.getBalance().add(amount));
            Account updatedAccount = accountRepository.save(account); // Save updates
            log.info("Deposit successful for account {}. New balance: {}", accountNumber, updatedAccount.getBalance());
            return updatedAccount;
        }
    }

    @Override
    // Using stricter isolation level might be necessary depending on concurrency requirements,
    // but READ_COMMITTED is often sufficient.
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Account withdraw(String accountNumber, BigDecimal amount) {
        log.info("Attempting to withdraw {} from account {}", amount, accountNumber);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountOperationException("Withdrawal amount must be positive.");
        }

        // Find the account
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        // Check for sufficient funds
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for withdrawal attempt on account {}. Required: {}, Available: {}",
                    accountNumber, amount, account.getBalance());
            throw new InsufficientFundsException("Insufficient funds in account " + accountNumber);
        }

        // Synchronize on the specific account object instance
        synchronized (account) {
            // Perform withdrawal
            account.setBalance(account.getBalance().subtract(amount));
            Account updatedAccount = accountRepository.save(account); // Save updates
            log.info("Withdrawal successful for account {}. New balance: {}", accountNumber, updatedAccount.getBalance());
            return updatedAccount;
        }
    }
}