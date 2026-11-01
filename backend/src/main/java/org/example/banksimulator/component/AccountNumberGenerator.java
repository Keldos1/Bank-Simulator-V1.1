package org.example.banksimulator.component;

import org.example.banksimulator.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private static final String PREFIX = "KZ";
    private static final int NUMBER_LENGTH = 18;

    private final AccountRepository accountRepository;
    private final SecureRandom random = new SecureRandom();

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String generate() {
        String accountNumber;
        do {
            StringBuilder number = new StringBuilder(PREFIX);
            for (int i = 0; i < NUMBER_LENGTH; i++) {
                number.append(random.nextInt(10));
            }
            accountNumber = number.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
