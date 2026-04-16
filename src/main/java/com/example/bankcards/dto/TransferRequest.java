package com.example.bankcards.dto;

import java.math.BigDecimal;

public class TransferRequest {
    
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
