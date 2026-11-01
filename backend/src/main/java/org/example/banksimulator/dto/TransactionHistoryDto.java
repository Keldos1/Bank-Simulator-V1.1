package org.example.banksimulator.dto;

import org.example.banksimulator.enums.TransactionStatus;
import org.example.banksimulator.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionHistoryDto(
        Long transactionId,
        TransactionType type,
        BigDecimal amount,
        String direction,
        TransactionStatus status,
        Instant createdAt)
{}
