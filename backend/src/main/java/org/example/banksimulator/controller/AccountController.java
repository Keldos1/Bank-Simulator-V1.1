package org.example.banksimulator.controller;

import org.example.banksimulator.dto.AccountDetailsDto;
import org.example.banksimulator.dto.AccountDto;
import org.example.banksimulator.security.CustomUserDetails;
import org.example.banksimulator.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public AccountDto create(@AuthenticationPrincipal CustomUserDetails user){
        return accountService.createAccount(user.getId());
    }

    @GetMapping("/my")
    public List<AccountDetailsDto> getAccounts(@AuthenticationPrincipal CustomUserDetails user){
        return accountService.getMyAccounts(user.getId());
    }

    @PatchMapping("/{accountId}/close")
    public AccountDto close(@PathVariable Long accountId, @AuthenticationPrincipal CustomUserDetails user){
        return accountService.closeAccount(accountId, user.getId());
    }
}
