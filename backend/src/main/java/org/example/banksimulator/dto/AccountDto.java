package org.example.banksimulator.dto;

import org.example.banksimulator.enums.AccountStatus;

public record AccountDto(
        Long id,
        String accountNumber)
{}
