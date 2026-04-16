package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
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
    
    private String status;
    
    private LocalDate expirationDate;
    
    public CardResponse toResponse(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setBalance(card.getBalance());
        dto.setStatus(card.getStatus().name());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setMaskedCardNumber("**** **** **** " + card.getLastFourDigits());
        return dto;
    }
}
