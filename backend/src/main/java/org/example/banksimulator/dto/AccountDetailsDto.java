package org.example.banksimulator.dto;

import org.example.banksimulator.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountDetailsDto(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        AccountStatus accountStatus)
{}
