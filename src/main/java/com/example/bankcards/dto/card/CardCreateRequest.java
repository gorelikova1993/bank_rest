package com.example.bankcards.dto.card;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardCreateRequest {
    
    private Long ownerId;
    
    private String cardNumber;
    
    private LocalDate expirationDate;
    
    private BigDecimal balance;
}
