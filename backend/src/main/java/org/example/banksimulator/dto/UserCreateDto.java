package org.example.banksimulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserCreateDto(
        @NotNull @NotBlank String phoneNumber,
        @NotNull @NotBlank @Size(min = 8) String password,
        @NotNull @NotBlank String firstName,
        @NotNull @NotBlank String lastName,
        @NotNull LocalDate birthDate)
{}
