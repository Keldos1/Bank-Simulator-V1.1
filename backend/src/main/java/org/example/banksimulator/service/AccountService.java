package org.example.banksimulator.service;

import org.example.banksimulator.component.AccountNumberGenerator;
import org.example.banksimulator.dto.AccountDetailsDto;
import org.example.banksimulator.dto.AccountDto;
import org.example.banksimulator.entity.Account;
import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.AccountStatus;
import org.example.banksimulator.enums.Role;
import org.example.banksimulator.enums.UserStatus;
import org.example.banksimulator.exceptions.*;
import org.example.banksimulator.repository.AccountRepository;
import org.example.banksimulator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Transactional
    public AccountDto createAccount(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "Cannot open account: user is blocked or closed"
            );
        }
        Account account = Account.builder()
                .accountNumber(accountNumberGenerator.generate())
                .balance(BigDecimal.ZERO)
                .currency("KZT")
                .accountStatus(AccountStatus.ACTIVE)
                .owner(user)
                .build();
        Account newAccount = accountRepository.save(account);
        return new AccountDto(
                newAccount.getId(),
                newAccount.getAccountNumber());
    }

    @Transactional(readOnly = true)
    public List<AccountDetailsDto> getMyAccounts(Long ownerId) {
        List<Account> accounts = accountRepository.findByOwner_Id(ownerId);
        return accounts.stream().map(
                account -> new AccountDetailsDto(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getAccountStatus()
                )
        ).toList();
    }

    @Transactional
    public AccountDto closeAccount(Long accountId, Long ownerId) {
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found"));
        if (!account.getOwner().getId().equals(ownerId)) {
            throw new NotOwnerException("You are not the owner of this account");
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Your account not active");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("The account needs to be reset to zero");
        }
        account.setAccountStatus(AccountStatus.CLOSED);
        return new AccountDto(
                account.getId(),
                account.getAccountNumber()
        );
    }

    @Transactional
    public AccountDto blockAccount(Long accountId){
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found"));
        if(account.getOwner().getRole() != Role.CLIENT && account.getOwner().getRole() != Role.EMPLOYEE){
            throw new AdminsException("Insufficient rights");
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Your account not active");
        }
        account.setAccountStatus(AccountStatus.BLOCKED);
        return new AccountDto(
                account.getId(),
                account.getAccountNumber()
        );
    }

    @Transactional
    public AccountDto unblockAccount(Long accountId){
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found")
        );
        if(account.getOwner().getRole() != Role.CLIENT && account.getOwner().getRole() != Role.EMPLOYEE){
            throw new AdminsException("Insufficient rights");
        }
        if (account.getAccountStatus() != AccountStatus.BLOCKED) {
            throw new AccountNotActiveException("Your account not BLOCKED");
        }
        account.setAccountStatus(AccountStatus.ACTIVE);
        return new AccountDto(
                account.getId(),
                account.getAccountNumber()
        );
    }

    @Transactional(readOnly = true)
    public List<AccountDetailsDto> getAccountsByUserId(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }

        return accountRepository.findByOwner_Id(userId)
                .stream()
                .map(account -> new AccountDetailsDto(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getAccountStatus()
                ))
                .toList();
    }
}
