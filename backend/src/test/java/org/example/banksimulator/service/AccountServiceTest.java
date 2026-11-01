package org.example.banksimulator.service;

import org.example.banksimulator.component.AccountNumberGenerator;
import org.example.banksimulator.entity.Account;
import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.AccountStatus;
import org.example.banksimulator.repository.AccountRepository;
import org.example.banksimulator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.example.banksimulator.enums.UserStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountActiveUserReturnAccount() {
        Long id = 1L;
        User user = new User();
        user.setStatus(ACTIVE);
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(accountNumberGenerator.generate()).thenReturn("KZ8888811123131");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Account account = new Account();
        assertNotNull(account);
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("KZT", account.getCurrency());
        assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
        assertEquals(1L, account.getOwner().getId());
        assertEquals("KZ8888811123131", account.getAccountNumber());
        verify(userRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    @Test
    void createAccountBlockedUserThrowsException(){
        Long id = 1L;
        User user = new User();
        user.setStatus(BLOCKED);
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class,() -> accountService.createAccount(id));
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountNumberGenerator, never()).generate();
    }
    @Test
    void createAccountUserNotFoundThrowsException(){
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> accountService.createAccount(id));
    }

    @Test
    void createAccountClosedUserThrowsException(){
        Long id = 1L;
        User user = new User();
        user.setStatus(CLOSED);
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class,() -> accountService.createAccount(id));
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountNumberGenerator, never()).generate();
    }
}