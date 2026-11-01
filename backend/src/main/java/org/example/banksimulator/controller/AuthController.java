package org.example.banksimulator.controller;

import jakarta.validation.Valid;
import org.example.banksimulator.dto.UserDto;
import org.example.banksimulator.dto.UserCreateDto;
import org.example.banksimulator.dto.UserProfileDto;
import org.example.banksimulator.security.CustomUserDetails;
import org.example.banksimulator.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserDto registration(@Valid @RequestBody UserCreateDto dto){
        return userService.userRegistration(
                dto.phoneNumber(),dto.password(), dto.firstName(), dto.lastName(), dto.birthDate());
    }

    @GetMapping("/me")
    public UserProfileDto profile(@AuthenticationPrincipal CustomUserDetails user){
        return userService.getMyProfile(user.getId());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
                "token", token.getToken(),
                "headerName", token.getHeaderName()
        );
    }
}
