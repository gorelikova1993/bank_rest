package com.example.bankcards.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CardNumberGeneratorService {
    
    private static final int CARD_LENGTH = 16;
    private static final String BIN = "4000000";
    
    private final SecureRandom random = new SecureRandom();
    
    public String generate() {
        StringBuilder cardNumber = new StringBuilder(BIN);
        
        while (cardNumber.length() < CARD_LENGTH) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }
}
