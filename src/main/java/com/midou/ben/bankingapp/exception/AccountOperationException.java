package com.midou.ben.bankingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Maps exception to 400 Bad Request
public class AccountOperationException extends RuntimeException {
    public AccountOperationException(String message) {
        super(message);
    }
}