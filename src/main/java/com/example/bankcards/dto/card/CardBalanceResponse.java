package com.example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
public class CardBalanceResponse {
    private Long id;
    private BigDecimal balance;
}
