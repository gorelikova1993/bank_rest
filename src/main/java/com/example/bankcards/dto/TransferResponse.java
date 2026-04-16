package com.example.bankcards.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private String status;
    
}
