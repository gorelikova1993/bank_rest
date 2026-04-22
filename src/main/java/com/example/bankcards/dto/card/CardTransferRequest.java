package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CardTransferRequest {
    
    @NotNull
    @Positive
    private Long fromCardId;
    
    @NotNull
    @Positive
    private Long toCardId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
}
