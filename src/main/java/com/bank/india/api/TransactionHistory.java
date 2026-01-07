package com.bank.india.api;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionHistory {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private UUID walletId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal balanceBefore;
    private String message;
    private String transactionReference;
    @CreationTimestamp
    private LocalDateTime createdAt;

}