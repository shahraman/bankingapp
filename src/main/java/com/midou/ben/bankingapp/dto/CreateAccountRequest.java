package com.midou.ben.bankingapp.dto;

import java.math.BigDecimal;

public record CreateAccountRequest (
         String ownerName,
         String accountNumber,
         BigDecimal initialBalance
){
}