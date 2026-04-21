package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardStatusUpdateRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class CardController {
    private final CardService cardService;
    
    @GetMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }
    
    @GetMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }
    
    @GetMapping("/cards/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponse>> getMyCards(@PageableDefault(size = 10)Pageable pageable){
        return ResponseEntity.ok(cardService.getMyCards(pageable));
    }
    
    @GetMapping("/cards/my/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponse> getMyCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getMyCardById(id));
    }
    
    @PatchMapping("/admin/cards/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> updateStatus(@PathVariable Long id, @RequestBody @Valid CardStatusUpdateRequest request){
        return ResponseEntity.ok(cardService.updateCardStatus(id, request));
    }
    
    
    @PostMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> create(@RequestBody @Valid CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }
}
