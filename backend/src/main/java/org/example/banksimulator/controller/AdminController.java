package org.example.banksimulator.controller;

import org.example.banksimulator.dto.AccountDetailsDto;
import org.example.banksimulator.dto.AccountDto;
import org.example.banksimulator.dto.UserProfileDto;
import org.example.banksimulator.service.AccountService;
import org.example.banksimulator.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminController {
    private final AccountService accountService;
    private final UserService userService;

    public AdminController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @PatchMapping("/accounts/{accountId}/block")
    public AccountDto block(@PathVariable Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @PatchMapping("/accounts/{accountId}/unblock")
    public AccountDto unblock(@PathVariable Long accountId) {
        return accountService.unblockAccount(accountId);
    }

    @GetMapping("/users")
    public Page<UserProfileDto> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountDetailsDto> getUserAccounts(
            @PathVariable Long userId) {
        return accountService.getAccountsByUserId(userId);
    }

    @PatchMapping("/users/{userId}/block")
    public UserProfileDto blockUser(
            @PathVariable Long userId) {
        return userService.blockUser(userId);
    }

    @PatchMapping("/users/{userId}/unblock")
    public UserProfileDto unblockUser(
            @PathVariable Long userId) {
        return userService.unblockUser(userId);
    }

}
