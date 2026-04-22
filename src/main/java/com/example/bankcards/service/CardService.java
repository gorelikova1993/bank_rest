package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardBalanceResponse;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardStatusUpdateRequest;
import com.example.bankcards.dto.card.CardTransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardGenerationException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InvalidCardStatusException;
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
    
    public Page<CardResponse> getAllCards(CardStatus status, Pageable pageable) {
        Page<Card> allCards;
        if (status == null) {
             allCards = cardRepository.findAll(pageable);
        } else {
            allCards = cardRepository.findAllByStatus(status, pageable);
        }

        return allCards.map(cardMapper::toResponse);
    }
    
    public CardResponse getCardById(Long id) {
        return cardRepository.findById(id).map(cardMapper::toResponse)
                .orElseThrow(() ->  new CardNotFoundException("Card is not found"));
    }
    
    public Page<CardResponse> getMyCards(CardStatus status, Pageable pageable) {
        User user = getCurrentUser();
        Page<Card> cards;
        if (status == null ) {
            cards = cardRepository.findAllByOwnerId(user.getId(), pageable);
        } else {
            cards = cardRepository.findAllByOwnerIdAndStatus(user.getId(), status, pageable);
        }
        
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
    
    public CardBalanceResponse getMyCardBalance(Long id) {
        User user = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        
        return new CardBalanceResponse(card.getId(), card.getBalance());
    }
    
    @Transactional
    public CardResponse blockCard(Long id) {
        User user = getCurrentUser();
        Card card = cardRepository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (card.getStatus() == CardStatus.BLOCK_REQUESTED || card.getStatus() == CardStatus.BLOCKED) {
            throw new InvalidCardStatusException("Card is already blocked");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalStateException("Expired card cannot be blocked");
        }
        card.setStatus(CardStatus.BLOCK_REQUESTED);
        return cardMapper.toResponse(card);
    }
    
    @Transactional
    public void transferBetweenOwnCards(CardTransferRequest request) {
        User user = getCurrentUser();
        
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new IllegalArgumentException("Source and destination cards must be different");
        }
        
        Card fromCard = cardRepository.findByIdAndOwnerId(request.getFromCardId(), user.getId())
                .orElseThrow(() ->  new CardNotFoundException("Source card not found"));
        
        Card toCard = cardRepository.findByIdAndOwnerId(request.getToCardId(), user.getId())
                .orElseThrow(() ->  new CardNotFoundException("Destination card not found"));
        
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw  new IllegalStateException("Only active cards can participate in transfers");
        }
        
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));
        
    }
    
}
