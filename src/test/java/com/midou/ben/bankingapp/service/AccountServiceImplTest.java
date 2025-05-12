package com.midou.ben.bankingapp.service;

import com.midou.ben.bankingapp.exception.AccountNotFoundException;
import com.midou.ben.bankingapp.exception.AccountOperationException;
import com.midou.ben.bankingapp.exception.InsufficientFundsException;
import com.midou.ben.bankingapp.model.Account;
import com.midou.ben.bankingapp.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class AccountServiceImplTest {

    @Mock 
    private AccountRepository accountRepository;

    @InjectMocks 
    private AccountServiceImpl accountService;

    private Account testAccount;
    private String testAccountNumber;
    private String testOwnerName;

    @BeforeEach
    void setUp() {
        // Common setup for tests
        testAccountNumber = "ACC001";
        testOwnerName = "Midou Ben";
        // Create a sample account for reuse in tests
        testAccount = new Account(testAccountNumber, testOwnerName, new BigDecimal("1000.00"));
        testAccount.setId(1L); // Simulate a persisted account
        testAccount.setCreatedAt(LocalDateTime.now().minusDays(1));
        testAccount.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    // --- createAccount Tests ---

    @Test
    void createAccount_whenValidDetails_shouldCreateAndReturnAccount() {
        // Arrange
        String newAccountNumber = "ACCNEW002";
        String owner = "Veuve Noire";
        BigDecimal initialBalance = new BigDecimal("500.00");

        when(accountRepository.existsByAccountNumber(newAccountNumber)).thenReturn(false);
        // When save is called, return the account passed to it
        // Here, we use an ArgumentCaptor to capture the account being saved
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        when(accountRepository.save(accountCaptor.capture())).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.setId(2L); // Simulate ID generation
            savedAccount.setCreatedAt(LocalDateTime.now()); // Simulate @PrePersist
            savedAccount.setUpdatedAt(LocalDateTime.now()); // Simulate @PrePersist
            return savedAccount;
        });

        // Act
        Account createdAccount = accountService.createAccount(owner, newAccountNumber, initialBalance);

        // Assert
        assertNotNull(createdAccount);
        assertEquals(owner, createdAccount.getOwnerName());
        assertEquals(newAccountNumber, createdAccount.getAccountNumber());
        assertEquals(0, initialBalance.compareTo(createdAccount.getBalance())); // Compare BigDecimals
        assertNotNull(createdAccount.getId());
        assertNotNull(createdAccount.getCreatedAt());
        assertNotNull(createdAccount.getUpdatedAt());

        verify(accountRepository, times(1)).existsByAccountNumber(newAccountNumber);
        verify(accountRepository, times(1)).save(any(Account.class));

        Account capturedAccount = accountCaptor.getValue();
        assertEquals(owner, capturedAccount.getOwnerName());
        assertEquals(newAccountNumber, capturedAccount.getAccountNumber());
    }

    @Test
    void createAccount_whenAccountNumberExists_shouldThrowAccountOperationException() {
        // Arrange
        when(accountRepository.existsByAccountNumber(testAccountNumber)).thenReturn(true);

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.createAccount(testOwnerName, testAccountNumber, new BigDecimal("100"));
        });
        assertEquals("Account number '" + testAccountNumber + "' already exists.", exception.getMessage());
        verify(accountRepository, times(1)).existsByAccountNumber(testAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_whenInitialBalanceIsNegative_shouldThrowAccountOperationException() {
        // Arrange
        BigDecimal negativeBalance = new BigDecimal("-100.00");
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false); // Assume new account number

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.createAccount("Test User", "NEWACC", negativeBalance);
        });
        assertEquals("Initial balance cannot be negative.", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- getAccountByAccountNumber Tests ---

    @Test
    void getAccountByAccountNumber_whenAccountExists_shouldReturnAccount() {
        // Arrange
        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));

        // Act
        Optional<Account> foundAccountOpt = accountService.getAccountByAccountNumber(testAccountNumber);

        // Assert
        assertTrue(foundAccountOpt.isPresent());
        assertEquals(testAccount, foundAccountOpt.get());
        verify(accountRepository, times(1)).findByAccountNumber(testAccountNumber);
    }

    @Test
    void getAccountByAccountNumber_whenAccountDoesNotExist_shouldReturnEmptyOptional() {
        // Arrange
        String nonExistentAccountNumber = "NONEXISTENT";
        when(accountRepository.findByAccountNumber(nonExistentAccountNumber)).thenReturn(Optional.empty());

        // Act
        Optional<Account> foundAccountOpt = accountService.getAccountByAccountNumber(nonExistentAccountNumber);

        // Assert
        assertFalse(foundAccountOpt.isPresent());
        verify(accountRepository, times(1)).findByAccountNumber(nonExistentAccountNumber);
    }

    // --- deposit Tests ---

    @Test
    void deposit_whenAccountExistsAndAmountPositive_shouldIncreaseBalance() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = testAccount.getBalance().add(depositAmount);

        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));
        // Simulate the save operation updating the balance
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        Account updatedAccount = accountService.deposit(testAccountNumber, depositAmount);

        // Assert
        assertNotNull(updatedAccount);
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()));
        verify(accountRepository, times(1)).findByAccountNumber(testAccountNumber);
        verify(accountRepository, times(1)).save(testAccount); // Check if the correct account instance was saved
    }

    @Test
    void deposit_whenAccountNotFound_shouldThrowAccountNotFoundException() {
        // Arrange
        String nonExistentAccountNumber = "ACC999";
        BigDecimal depositAmount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(nonExistentAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.deposit(nonExistentAccountNumber, depositAmount);
        });
        verify(accountRepository, times(1)).findByAccountNumber(nonExistentAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deposit_whenAmountIsZero_shouldThrowAccountOperationException() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.deposit(testAccountNumber, zeroAmount);
        });
        assertEquals("Deposit amount must be positive.", exception.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void deposit_whenAmountIsNegative_shouldThrowAccountOperationException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-50.00");

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.deposit(testAccountNumber, negativeAmount);
        });
        assertEquals("Deposit amount must be positive.", exception.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // --- withdraw Tests ---

    @Test
    void withdraw_whenAccountExistsAndSufficientFundsAndAmountPositive_shouldDecreaseBalance() {
        // Arrange
        BigDecimal withdrawalAmount = new BigDecimal("300.00");
        BigDecimal expectedBalance = testAccount.getBalance().subtract(withdrawalAmount);

        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = accountService.withdraw(testAccountNumber, withdrawalAmount);

        // Assert
        assertNotNull(updatedAccount);
        assertEquals(0, expectedBalance.compareTo(updatedAccount.getBalance()));
        verify(accountRepository, times(1)).findByAccountNumber(testAccountNumber);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void withdraw_whenAccountNotFound_shouldThrowAccountNotFoundException() {
        // Arrange
        String nonExistentAccountNumber = "ACC999";
        BigDecimal withdrawalAmount = new BigDecimal("100.00");
        when(accountRepository.findByAccountNumber(nonExistentAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.withdraw(nonExistentAccountNumber, withdrawalAmount);
        });
        verify(accountRepository, times(1)).findByAccountNumber(nonExistentAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdraw_whenInsufficientFunds_shouldThrowInsufficientFundsException() {
        // Arrange
        BigDecimal withdrawalAmount = new BigDecimal("2000.00"); // More than current balance
        when(accountRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountService.withdraw(testAccountNumber, withdrawalAmount);
        });
        verify(accountRepository, times(1)).findByAccountNumber(testAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdraw_whenAmountIsZero_shouldThrowAccountOperationException() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.withdraw(testAccountNumber, zeroAmount);
        });
        assertEquals("Withdrawal amount must be positive.", exception.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void withdraw_whenAmountIsNegative_shouldThrowAccountOperationException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-50.00");

        // Act & Assert
        AccountOperationException exception = assertThrows(AccountOperationException.class, () -> {
            accountService.withdraw(testAccountNumber, negativeAmount);
        });
        assertEquals("Withdrawal amount must be positive.", exception.getMessage());
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }
}