//package com.bank.india.service;
package com.bank.india.api;
//
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository repository;

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
    public Wallet credit(UUID id, BigDecimal amount) {
        Wallet wallet = getWallet(id);
        wallet.setBalance(wallet.getBalance().add(amount));
        return repository.save(wallet);
    }

    @Transactional
    public Wallet debit(UUID id, BigDecimal amount) {
        Wallet wallet = getWallet(id);
//        BigDecimal ba = wallet.getBalance();
//        if (ba.compareTo(new BigDecimal("100.00")) < 0) {
//            throw new BusinessException("Insufficient funds. your balance is less than 100.");
//        }
        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        // Validation Rule: Min ₹100 must remain
        if (newBalance.compareTo(new BigDecimal("100.00")) < 0) {
            throw new BusinessException("Insufficient funds. Minimum wallet balance of ₹100 required.");
        }

        wallet.setBalance(newBalance);
        return repository.save(wallet);
    }
}