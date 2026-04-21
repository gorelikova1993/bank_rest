package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    
    public CardResponse toResponse(Card card) {
        CardResponse dto = new CardResponse();
        dto.setId(card.getId());
        dto.setBalance(card.getBalance());
        dto.setStatus(card.getStatus());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setMaskedCardNumber("**** **** **** " + card.getLastFourDigits());
        return dto;
    }
    
    public Card toEntity(CardCreateRequest createRequest) {
        Card card = new Card();
        card.setHolderName(createRequest.getHolderName());
        card.setBalance(createRequest.getBalance());
        return card;
    }
}
