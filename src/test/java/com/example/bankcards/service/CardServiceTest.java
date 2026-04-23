package com.example.bankcards.service;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardTransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private CardEncryptionService cardEncryptionService;
    @Mock
    private HashService hashService;
    @Mock
    private CardNumberGeneratorService cardNumberGeneratorService;
    
    @InjectMocks
    private CardService cardService;
    
    private User user;
    private Card card;
    
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setLogin("anna");
        user.setEmail("anna@test.com");
        user.setEnabled(true);
        user.setRole(Role.ROLE_USER);
        
        card = new Card();
        card.setId(10L);
        card.setOwner(user);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setHolderName("ANNA PETROVA");
        card.setLastFourDigits("1234");
    }
    
    @Test
    void createCard_shouldCreateCardSuccessfully() {
        CardCreateRequest request = new CardCreateRequest();
        request.setOwnerId(1L);
        request.setHolderName("ANNA PETROVA");
        request.setBalance(BigDecimal.valueOf(1000));
        
        Card mappedCard = new Card();
        mappedCard.setHolderName("ANNA PETROVA");
        mappedCard.setBalance(BigDecimal.valueOf(1000));
        
        Card savedCard = new Card();
        savedCard.setId(10L);
        savedCard.setOwner(user);
        savedCard.setHolderName("ANNA PETROVA");
        savedCard.setBalance(BigDecimal.valueOf(1000));
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setExpirationDate(LocalDate.now().plusYears(5));
        savedCard.setLastFourDigits("1234");
        savedCard.setCardHash("hash");
        savedCard.setEncryptedCardNumber("encrypted");
        
        CardResponse response = new CardResponse();
        response.setId(10L);
        response.setMaskedCardNumber("**** **** **** 1234");
        response.setBalance(BigDecimal.valueOf(1000));
        response.setStatus(CardStatus.ACTIVE);
        response.setExpirationDate(savedCard.getExpirationDate());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(request)).thenReturn(mappedCard);
        when(cardNumberGeneratorService.generate()).thenReturn("1111222233331234");
        when(hashService.sha256("1111222233331234")).thenReturn("hash");
        when(cardRepository.existsByCardHash("hash")).thenReturn(false);
        when(cardEncryptionService.encrypt("1111222233331234")).thenReturn("encrypted");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(cardMapper.toResponse(savedCard)).thenReturn(response);
        
        CardResponse result = cardService.createCard(request);
        
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("**** **** **** 1234", result.getMaskedCardNumber());
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        
        verify(userRepository).findById(1L);
        verify(cardNumberGeneratorService).generate();
        verify(hashService).sha256("1111222233331234");
        verify(cardEncryptionService).encrypt("1111222233331234");
        verify(cardRepository).save(any(Card.class));
    }
    
    @Test
    void blockCard_shouldSetBlockRequestedStatus() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anna", null)
        );
        
        when(userRepository.findByLogin("anna")).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(card));
        
        CardResponse response = new CardResponse();
        response.setId(10L);
        response.setStatus(CardStatus.BLOCK_REQUESTED);
        
        when(cardMapper.toResponse(card)).thenReturn(response);
        
        CardResponse result = cardService.blockCard(10L);
        
        assertEquals(CardStatus.BLOCK_REQUESTED, card.getStatus());
        assertEquals(CardStatus.BLOCK_REQUESTED, result.getStatus());
    }
    
    @Test
    void transferBetweenOwnCards_shouldTransferMoneySuccessfully() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anna", null)
        );
        
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        
        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        
        CardTransferRequest request = new CardTransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200));
        
        when(userRepository.findByLogin("anna")).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(toCard));
        
        cardService.transferBetweenOwnCards(request);
        
        assertEquals(BigDecimal.valueOf(800), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());
    }
    
    @Test
    void transferBetweenOwnCards_shouldThrowWhenInsufficientFunds() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anna", null)
        );
        
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(100));
        
        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        
        CardTransferRequest request = new CardTransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200));
        
        when(userRepository.findByLogin("anna")).thenReturn(Optional.of(user));
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(2L, 1L)).thenReturn(Optional.of(toCard));
        
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cardService.transferBetweenOwnCards(request)
        );
        
        assertEquals("Insufficient funds", ex.getMessage());
    }
}
