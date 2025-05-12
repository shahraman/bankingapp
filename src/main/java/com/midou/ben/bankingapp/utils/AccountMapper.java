package com.midou.ben.bankingapp.utils;

import com.midou.ben.bankingapp.dto.AccountResponse;
import com.midou.ben.bankingapp.model.Account;

public  class AccountMapper {

    // --- Helper Method for Mapping ---
    public static AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

}
