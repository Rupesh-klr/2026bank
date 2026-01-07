package com.bank.india.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, UUID> {

    /**
     * Fetch all transaction logs for a specific wallet ordered by most recent first.
     * This allows you to show a "Statement" to the user.
     */
    List<TransactionHistory> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    /**
     * Find transactions by type (e.g., all 'DEBIT' operations) for a specific wallet.
     */
    List<TransactionHistory> findByWalletIdAndTransactionType(UUID walletId, String transactionType);
}