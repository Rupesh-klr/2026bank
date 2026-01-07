package com.bank.india.api;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService service;

    @PostMapping
    public Wallet create(@RequestBody Map<String, String> request) {
        return service.createWallet(request.get("userId"), request.get("currency"));
    }
//    @GetMapping("/")
//    public get(@PathVariable UUID walletId) {
//        return new ResponseEntity<>(Map.of("message","weneed" ), HttpStatus.NOT_FOUND);
//    }

    @GetMapping("/{walletId}")
    public Wallet get(@PathVariable UUID walletId) {
        return service.getWallet(walletId);
    }

    @PostMapping("/{walletId}/credit")
    public Map<String, Object> credit(@PathVariable UUID walletId, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String transactionReference = request.get("reference").toString();
        Wallet wallet = service.credit(walletId, amount, transactionReference);
        return Map.of("walletId", wallet.getId(), "newBalance", wallet.getBalance(), "message", "Wallet credited successfully");
    }

    @PostMapping("/{walletId}/debit")
    public Map<String, Object> debit(@PathVariable UUID walletId, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        String transactionReference = request.get("reference").toString();
        Wallet wallet = service.debit(walletId, amount, transactionReference);
        return Map.of("walletId", wallet.getId(), "newBalance", wallet.getBalance(), "message", "Wallet debited successfully");
    }


}