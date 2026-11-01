package org.example.banksimulator.dto;

public record ErrorResponse(
        int status,
        String massage
) {}
