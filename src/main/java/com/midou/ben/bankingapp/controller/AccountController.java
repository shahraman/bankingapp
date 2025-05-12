package com.midou.ben.bankingapp.controller;

import com.midou.ben.bankingapp.dto.AccountResponse;
import com.midou.ben.bankingapp.dto.CreateAccountRequest;
import com.midou.ben.bankingapp.dto.TransactionRequest;
import com.midou.ben.bankingapp.exception.AccountNotFoundException;
import com.midou.ben.bankingapp.model.Account;
import com.midou.ben.bankingapp.service.AccountService;
import com.midou.ben.bankingapp.utils.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts") // Base path for account operations
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // --- API Endpoints ---

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        log.info("Received request to create account: {}", request);
        // Basic validation (more robust validation can be added)
        if (request.ownerName() == null || request.ownerName().isEmpty() ||
                request.accountNumber() == null || request.accountNumber().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Or throw a specific validation exception
        }
        Account createdAccount = accountService.createAccount(
                request.ownerName(),
                request.accountNumber(),
                request.initialBalance()
        );
        return new ResponseEntity<>(AccountMapper.mapToAccountResponse(createdAccount), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        log.info("Received request to get account: {}", accountNumber);
        Optional<Account> accountOpt = accountService.getAccountByAccountNumber(accountNumber);

        // Using functional style for Optional handling
        return accountOpt
                .map(account -> ResponseEntity.ok(AccountMapper.mapToAccountResponse(account))) // If found, map to Response DTO and return 200 OK
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber)); // If not found, throw exception (handled globally or by @ResponseStatus)
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber, @RequestBody TransactionRequest request) {
        log.info("Received request to deposit {} into account {}", request.amount(), accountNumber);
        if (request.amount() == null) {
            return ResponseEntity.badRequest().build(); // Or throw validation exception
        }
        Account updatedAccount = accountService.deposit(accountNumber, request.amount());
        return ResponseEntity.ok(AccountMapper.mapToAccountResponse(updatedAccount));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber, @RequestBody TransactionRequest request) {
        log.info("Received request to withdraw {} from account {}", request.amount(), accountNumber);
        if (request.amount() == null) {
            return ResponseEntity.badRequest().build(); // Or throw validation exception
        }
        Account updatedAccount = accountService.withdraw(accountNumber, request.amount());
        return ResponseEntity.ok(AccountMapper.mapToAccountResponse(updatedAccount));
    }


}