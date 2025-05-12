package com.midou.ben.bankingapp.dto;

import java.math.BigDecimal;

public record TransactionRequest (
         BigDecimal amount
) {
}
