package com.midou.ben.bankingapp.service;

import com.midou.ben.bankingapp.model.Account;
import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService {

    Account createAccount(String ownerName, String accountNumber, BigDecimal initialBalance);

    Optional<Account> getAccountByAccountNumber(String accountNumber);

    Account deposit(String accountNumber, BigDecimal amount);

    Account withdraw(String accountNumber, BigDecimal amount);
}
