package org.example.banksimulator.dto;

import org.example.banksimulator.enums.Role;

public record UserProfileDto(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        Role role) {
}
