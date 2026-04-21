package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardBlockRequest;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardStatusUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardGenerationException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardService {
    
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardEncryptionService cardEncryptionService;
    private final HashService hashService;
    private final CardNumberGeneratorService cardNumberGeneratorService;
    private static final int MAX_ATTEMPTS = 5;
    
    @Transactional
    public CardResponse createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Card card = cardMapper.toEntity(request);
        
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        
        String rawCardNumber = null;
        String hash = null;
        
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            
            rawCardNumber = cardNumberGeneratorService.generate();
            hash = hashService.sha256(rawCardNumber);
            
            if(!cardRepository.existsByCardHash(hash)) {
                break;
            }
            
            if (i == MAX_ATTEMPTS - 1) {
                throw new CardGenerationException("Не удалось сгенерировать уникальный номер карты");
            }
        }
        
        String lastFour = rawCardNumber.substring(rawCardNumber.length() - 4);
        String encrypted = cardEncryptionService.encrypt(rawCardNumber);
        
        card.setCardHash(hash);
        
        card.setEncryptedCardNumber(encrypted);
        card.setLastFourDigits(lastFour);
        
        LocalDate expirationDate = LocalDate.now().plusYears(5);
        card.setExpirationDate(expirationDate);
        
        Card savedCard = cardRepository.save(card);
        return cardMapper.toResponse(savedCard);
    }
    
    public Page<CardResponse> getCardsByOwnerId(Long ownerId, Pageable pageable) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Page<Card> cards = cardRepository.findAllByOwnerId(user.getId(), pageable);
        
        return cards.map(cardMapper::toResponse);
    }
    
    public Page<CardResponse> getAllCards(Pageable pageable) {
        Page<Card> allCards = cardRepository.findAll(pageable);
        return allCards.map(cardMapper::toResponse);
    }
    
    public CardResponse getCardById(Long id) {
        return cardRepository.findById(id).map(cardMapper::toResponse)
                .orElseThrow(() ->  new CardNotFoundException("Card is not found"));
    }
    
    public Page<CardResponse> getMyCards(Pageable pageable) {
        User user = getCurrentUser();
        
        Page<Card> cards = cardRepository.findAllByOwnerId(user.getId(), pageable);
        
        return cards.map(cardMapper::toResponse);
    }
    
    public CardResponse getMyCardById(Long id) {
        User user = getCurrentUser();
        
        Card card = cardRepository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        
        return cardMapper.toResponse(card);
    }
    
    @Transactional
    public CardResponse updateCardStatus(Long id, CardStatusUpdateRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setStatus(request.getStatus());
        return cardMapper.toResponse(card);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
