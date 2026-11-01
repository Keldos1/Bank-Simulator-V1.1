package org.example.banksimulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferCreateDto(
        @NotNull Long sourceAccountId,
        @NotBlank String destinationAccountNumber,
        @NotNull @Positive BigDecimal transferAmount)
{}
