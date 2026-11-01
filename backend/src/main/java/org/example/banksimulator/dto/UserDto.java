package org.example.banksimulator.dto;


import org.example.banksimulator.enums.UserStatus;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        UserStatus status)
{}
