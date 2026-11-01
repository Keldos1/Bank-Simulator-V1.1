package org.example.banksimulator.service;

import org.example.banksimulator.dto.DepositDto;
import org.example.banksimulator.dto.WithdrawDto;
import org.example.banksimulator.entity.Account;
import org.example.banksimulator.entity.Transaction;
import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.AccountStatus;
import org.example.banksimulator.enums.TransactionStatus;
import org.example.banksimulator.enums.TransactionType;
import org.example.banksimulator.enums.UserStatus;
import org.example.banksimulator.exceptions.InsufficientFundsException;
import org.example.banksimulator.exceptions.InvalidTransactionAmountException;
import org.example.banksimulator.repository.AccountRepository;
import org.example.banksimulator.repository.TransactionRepository;
import org.example.banksimulator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void depositUserActiveAccountActiveAmountNotZeroReturnTransaction(){
        BigDecimal amount = BigDecimal.TEN;
        Long userId = 2L;
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Long accountID = 1L;
        Account account = new Account();
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setId(accountID);
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findByIdForUpdate(accountID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));


        DepositDto dto = transactionService.deposit(accountID, amount);
        assertNotNull(dto);
        assertEquals(TransactionType.DEPOSIT, dto.type());
        assertEquals(TransactionStatus.COMPLETED, dto.status());
        assertEquals(new BigDecimal("100.00").add(amount), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void depositCheckForInvalidTransAmountException(){
        Long accountId = 1L;
        Long userId = 2L;
        BigDecimal amount = new BigDecimal("-1000.3");
        BigDecimal initialBalance = new BigDecimal("10000.4");

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Account account = new Account();
        account.setId(accountId);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(initialBalance);
        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidTransactionAmountException.class,
                () -> transactionService.deposit(accountId, amount));
        assertEquals(initialBalance, account.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void checkForSuccessfulWithdraw(){
        BigDecimal amount = new BigDecimal("30.0");
        Long userId = 2L;
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Long accountID = 1L;
        Account account = new Account();
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setId(accountID);
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findByIdForUpdate(accountID)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));


        WithdrawDto dto = transactionService.withdraw(accountID, amount, userId);
        assertNotNull(dto);
        assertEquals(TransactionType.WITHDRAWAL, dto.type());
        assertEquals(TransactionStatus.COMPLETED, dto.status());
        assertEquals(new BigDecimal("70.00"), account.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void checkWithdrawForInsufficientFoundsException(){
        Long accountId = 1L;
        Long userId = 2L;
        BigDecimal amount = new BigDecimal("150.0");
        BigDecimal initialBalance = new BigDecimal("100.0");

        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Account account = new Account();
        account.setId(accountId);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(initialBalance);
        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(accountId, amount, userId));
        assertEquals(initialBalance, account.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
