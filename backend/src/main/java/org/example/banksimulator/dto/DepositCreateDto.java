package org.example.banksimulator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositCreateDto(
        @NotNull Long accountId,
        @NotNull @Positive BigDecimal depositAmount)
{}
