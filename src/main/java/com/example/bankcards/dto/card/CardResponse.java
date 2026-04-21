package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardResponse {
    
    private Long id;
    
    private String maskedCardNumber;
    
    private BigDecimal balance;
    
    private LocalDate expirationDate;
    
    private CardStatus status;
    
}
