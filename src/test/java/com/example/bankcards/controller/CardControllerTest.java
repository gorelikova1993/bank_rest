package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardBalanceResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardTransferRequest;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {
    
    @Mock
    private CardService cardService;
    
    @InjectMocks
    private CardController cardController;
    
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    
    @Test
    void getBalance_shouldReturnOk() throws Exception {
        CardBalanceResponse response = new CardBalanceResponse(4L, BigDecimal.valueOf(1000));
        
        when(cardService.getMyCardBalance(4L)).thenReturn(response);
        
        mockMvc.perform(get("/api/cards/my/4/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.balance").value(1000));
        
        verify(cardService).getMyCardBalance(4L);
    }
    
    @Test
    void transfer_shouldReturnNoContent() throws Exception {
        CardTransferRequest request = new CardTransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        
        doNothing().when(cardService).transferBetweenOwnCards(any(CardTransferRequest.class));
        
        mockMvc.perform(post("/api/cards/my/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(cardService).transferBetweenOwnCards(any(CardTransferRequest.class));
    }
    
    @Test
    void createCard_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        CardCreateRequest request = new CardCreateRequest();
        request.setHolderName("");
        request.setBalance(BigDecimal.valueOf(-100));
        
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
