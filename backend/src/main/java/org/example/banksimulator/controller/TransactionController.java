package org.example.banksimulator.controller;

import jakarta.validation.Valid;
import org.example.banksimulator.dto.*;
import org.example.banksimulator.security.CustomUserDetails;
import org.example.banksimulator.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public DepositDto deposit(@Valid @RequestBody DepositCreateDto dto) {
        return transactionService.deposit(
                dto.accountId(), dto.depositAmount());
    }

    @PostMapping("/withdraw")
    public WithdrawDto withdraw(@Valid @RequestBody WithdrawCreateDto dto, @AuthenticationPrincipal CustomUserDetails user) {
        return transactionService.withdraw(
                dto.accountId(), dto.withdrawAmount(), user.getId());
    }

    @PostMapping("/transfer")
    public TransferDto transfer(@Valid @RequestBody TransferCreateDto dto, @AuthenticationPrincipal CustomUserDetails user){
        return transactionService.transfer(
                dto.sourceAccountId(), dto.destinationAccountNumber(), dto.transferAmount(), user.getId());
    }
    @GetMapping("/{accountId}/history")
    public ResponseEntity<Page<TransactionHistoryDto>> history(@PathVariable Long accountId, @AuthenticationPrincipal CustomUserDetails user, Pageable pageable){
        Page<TransactionHistoryDto> history = transactionService.getHistory(accountId, user.getId(), pageable);
        return ResponseEntity.ok(history);
    }

}
