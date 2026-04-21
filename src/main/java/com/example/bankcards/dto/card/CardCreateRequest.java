package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardCreateRequest {
    
    @NotNull
    private Long ownerId;
    
    @NotBlank
    private String holderName;
    
    @PositiveOrZero
    private BigDecimal balance;
}
