package org.example.banksimulator.dto;

import org.example.banksimulator.enums.TransactionStatus;
import org.example.banksimulator.enums.TransactionType;

import java.math.BigDecimal;

public record DepositDto(
        TransactionType type,
        BigDecimal amount,
        TransactionStatus status,
        Long accountId,
        String accountNumber
) {}