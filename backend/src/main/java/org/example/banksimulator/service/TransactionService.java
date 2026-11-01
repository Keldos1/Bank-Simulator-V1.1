package org.example.banksimulator.service;

import org.example.banksimulator.dto.DepositDto;
import org.example.banksimulator.dto.TransactionHistoryDto;
import org.example.banksimulator.dto.TransferDto;
import org.example.banksimulator.dto.WithdrawDto;
import org.example.banksimulator.enums.TransactionStatus;
import org.example.banksimulator.enums.TransactionType;
import org.example.banksimulator.enums.UserStatus;
import org.example.banksimulator.exceptions.*;
import org.example.banksimulator.entity.Account;
import org.example.banksimulator.entity.Transaction;
import org.example.banksimulator.entity.User;
import org.example.banksimulator.enums.AccountStatus;
import org.example.banksimulator.repository.AccountRepository;
import org.example.banksimulator.repository.TransactionRepository;
import org.example.banksimulator.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DepositDto deposit(Long accountID, BigDecimal depositAmount) {
        Account account = accountRepository.findByIdForUpdate(accountID).orElseThrow(
                () -> new AccountNotFoundException("Account Not Found"));

        User user = account.getOwner();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "Cannot deposit: user is blocked or closed");
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "You are account is not ACTIVE");
        }
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(
                    "Deposit amount must be greater than zero");
        }
        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .amount(depositAmount)
                .destinationAccount(account)
                .build();
        account.setBalance(account.getBalance().add(depositAmount));
        Transaction saveTransaction = transactionRepository.save(transaction);
        return new DepositDto(saveTransaction.getType(),
                saveTransaction.getAmount(),
                saveTransaction.getStatus(),
                accountID,
                saveTransaction.getDestinationAccount().getAccountNumber());
    }


    @Transactional
    public WithdrawDto withdraw(Long accountId, BigDecimal withdrawAmount, Long ownerId) {
        User user = userRepository.findById(ownerId).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "Cannot withdraw: user is blocked or closed");
        }
        Account account = accountRepository.findByIdForUpdate(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account Not Found"));
        if (!account.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not the owner of this account");
        }
        if (withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(
                    "Withdraw amount must be greater than zero");
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "You are account is not ACTIVE");
        }
        if (account.getBalance().compareTo(withdrawAmount) < 0) {
            throw new InsufficientFundsException(
                    "You are withdrawing more than you are allowed to"
            );
        }
        Transaction transaction = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .amount(withdrawAmount)
                .sourceAccount(account)
                .initiatedBy(user)
                .build();
        account.setBalance(account.getBalance().subtract(withdrawAmount));
        Transaction saveTransaction = transactionRepository.save(transaction);
        return new WithdrawDto(
                saveTransaction.getType(),
                saveTransaction.getAmount(),
                saveTransaction.getStatus(),
                saveTransaction.getSourceAccount().getId(),
                saveTransaction.getSourceAccount().getAccountNumber());
    }

    @Transactional
    public TransferDto transfer(Long sourceAccountId, String destinationAccountNumber, BigDecimal transferAmount, Long initiatorId) {
        Long firstId;
        Long secondId;
        Account transferSourceAccount;
        Account transferDestinationAccount;

        if (destinationAccountNumber == null ||
                destinationAccountNumber.isBlank()) {
            throw new IllegalStateException(
                    "Введите номер счёта получателя"
            );
        }
        Account destination = accountRepository
                .findByAccountNumber(destinationAccountNumber.trim())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Счёт получателя не найден. Проверьте номер счёта."
                ));
        Long destinationAccountId = destination.getId();

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IncorrectlyTransferException("Transfers to the same account are not available");
        }
        if (sourceAccountId > destinationAccountId) {
            firstId = destinationAccountId;
            secondId = sourceAccountId;
        } else {
            firstId = sourceAccountId;
            secondId = destinationAccountId;
        }
        Account firstAccount = accountRepository.findByIdForUpdate(firstId).orElseThrow(
                () -> new AccountNotFoundException("Account not found: " + firstId));
        Account secondAccount = accountRepository.findByIdForUpdate(secondId).orElseThrow(
                () -> new AccountNotFoundException("Account not found: " + secondId));

        User user = userRepository.findById(initiatorId).orElseThrow(
                () -> new UserNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "Cannot transfer: user is blocked or closed");
        }
        if (firstAccount.getId().equals(sourceAccountId)) {
            transferSourceAccount = firstAccount;
            transferDestinationAccount = secondAccount;
        } else {
            transferSourceAccount = secondAccount;
            transferDestinationAccount = firstAccount;
        }
        if (!transferSourceAccount.getOwner().getId().equals(initiatorId)) {
            throw new AccessDeniedException(
                    "You are not the owner of this account");
        }
        if (transferDestinationAccount.getOwner().getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "Cannot transfer: Destination Account owner is blocked or closed");
        }
        if (transferAmount == null ||
                transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(
                    "Transfer amount must be greater than zero"
            );
        }

        if (transferSourceAccount.getAccountStatus() != AccountStatus.ACTIVE || transferDestinationAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "This accounts aren't ACTIVE");
        }
        if (!"KZT".equals(transferSourceAccount.getCurrency())
                || !"KZT".equals(transferDestinationAccount.getCurrency())) {
            throw new IncorrectCurrencyException("Incorrectly specified currency");
        }
        if (transferSourceAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new InsufficientFundsException(
                    "You are transferring more than you are allowed to");
        }
        Transaction transaction = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .amount(transferAmount)
                .sourceAccount(transferSourceAccount)
                .destinationAccount(transferDestinationAccount)
                .initiatedBy(user)
                .build();
        transferSourceAccount.setBalance(transferSourceAccount.getBalance().subtract(transferAmount));
        transferDestinationAccount.setBalance(transferDestinationAccount.getBalance().add(transferAmount));
        Transaction saveTransaction = transactionRepository.save(transaction);
        return new TransferDto(
                saveTransaction.getType(),
                saveTransaction.getAmount(),
                saveTransaction.getStatus(),
                saveTransaction.getSourceAccount().getId(),
                saveTransaction.getSourceAccount().getAccountNumber(),
                saveTransaction.getDestinationAccount().getId(),
                saveTransaction.getDestinationAccount().getAccountNumber());
    }

    @Transactional(readOnly = true)
    public Page<TransactionHistoryDto> getHistory(Long accountId, Long currentUserId, Pageable pageable) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found"));
        if (!account.getOwner().getId().equals(currentUserId)) {
            throw new NotOwnerException("You are not the owner of this account");
        }
        Page<Transaction> historyPage = transactionRepository.findHistoryByAccountId(accountId, pageable);
        return historyPage.map(transaction -> {
                    String direction;
                    if (transaction.getType() == TransactionType.DEPOSIT) {
                        direction = "INCOMING";
                    } else if (transaction.getType() == TransactionType.WITHDRAWAL) {
                        direction = "OUTGOING";
                    } else if (transaction.getDestinationAccount() != null
                            && transaction.getDestinationAccount().getId().equals(accountId)) {
                        direction = "INCOMING";
                    } else {
                        direction = "OUTGOING";
                    }
                    return new TransactionHistoryDto(
                            transaction.getId(),
                            transaction.getType(),
                            transaction.getAmount(),
                            direction,
                            transaction.getStatus(),
                            transaction.getCreatedAt()
                    );
                }
        );
    }
}
