package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    List<Card> findAllByOwnerId(Long ownerId);
    
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);
    
    Page<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);
    
    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);
    
    Optional<Card> findByCardHash(String cardHash);
    
    boolean existsByCardHash(String cardHash);
    
    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);
    
}
