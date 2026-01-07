//package com.bank.india.service;
package com.bank.india.api;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j // For logging alerts
public class WalletService {

    private final WalletRepository repository;
    private final TransactionHistoryRepository historyRepository;

    // Injecting values from properties
    @Value("${wallet.transaction.min-value}")
    private BigDecimal minTransactionValue;

    @Value("${wallet.transaction.max-value}")
    private BigDecimal maxTransactionValue;

    @Value("${wallet.rules.min-required-balance}")
    private BigDecimal minRequiredBalance;

    @Value("${wallet.alerts.threshold}")
    private BigDecimal alertThreshold;

    public Wallet createWallet(String userId, String currency) {
        if (repository.existsByUserId(userId)) {
            throw new BusinessException("User already has a wallet.");
        }
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency != null ? currency : "INR")
                .balance(BigDecimal.ZERO)
                .build();
        return repository.save(wallet);
    }

    public Wallet getWallet(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    @Transactional
    public Wallet credit(UUID id, BigDecimal amount, String transactionReference) {
        validateTransactionAmount(id, amount, transactionReference);
        Wallet wallet = getWallet(id);

        BigDecimal currentBal = wallet.getBalance();

// Calculate NEW state
        BigDecimal newBalance = currentBal.add(amount);
//        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);

        // Save Wallet
        Wallet savedWallet = repository.save(wallet);

        // 1. Log to DB
        logTransaction(id, "CREDIT", amount, newBalance, currentBal, "Wallet credited successfully",transactionReference );

        // 2. Alert if > 1M
        checkHighValueAlert(id, newBalance);

        return savedWallet;
    }

    @Transactional
    public Wallet debit(UUID id, BigDecimal amount ,String transactionReference ) {
        validateTransactionAmount(id, amount, transactionReference);
        Wallet wallet = getWallet(id);

        BigDecimal currentBal = wallet.getBalance();
        BigDecimal newBalance = currentBal.subtract(amount);

        // Validation Rule: Min ₹100 must remain
        if (newBalance.compareTo(new BigDecimal("100.00")) < 0) {

            logTransaction(id, "DEBIT", amount, newBalance, currentBal,"Insufficient funds. Minimum wallet balance of ₹100 required.", transactionReference);
            throw new BusinessException("Insufficient funds. Minimum wallet balance of ₹100 required.");
        }

        wallet.setBalance(newBalance);
        logTransaction(id, "DEBIT", amount, newBalance, currentBal,"Wallet debited successfully", transactionReference);
        return repository.save(wallet);
    }

    // Common validation for transaction limits
    private void validateTransactionAmount(UUID id, BigDecimal amount, String transactionReference ) {
        String ref = (transactionReference != null) ? transactionReference : "INTERNAL-" + UUID.randomUUID();
        if (amount.compareTo(minTransactionValue) < 0) {
            logTransaction(id, "check", amount, new BigDecimal(0), new BigDecimal(0), "Not processed!!!(MIN)",ref);
            throw new BusinessException("Transaction failed. Minimum allowed value is ₹" + minTransactionValue);
        }
        if (amount.compareTo(maxTransactionValue) > 0) {
            logTransaction(id, "check", amount, new BigDecimal(0), new BigDecimal(0), "Not processed!!!(MAX)", ref);
            throw new BusinessException("Transaction failed. Maximum allowed value is ₹" + maxTransactionValue);
        }
    }
    private void logTransaction(UUID walletId, String type, BigDecimal amount, BigDecimal balanceAfter,BigDecimal balanceBefore, String note, String transactionReference) {
        TransactionHistory history = TransactionHistory.builder()
                .walletId(walletId)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .balanceBefore(balanceBefore)
                .transactionReference(transactionReference)
                .message(note)
                .build();
        historyRepository.save(history);
    }

    private void checkHighValueAlert(UUID walletId, BigDecimal balance) {
        if (balance.compareTo(alertThreshold) >= 0) {
            // In a real app, you'd call a Notification Service or email here
            log.warn("ALERT: Wallet {} has crossed the threshold! Current Balance: {}", walletId, balance);
        }
    }
}