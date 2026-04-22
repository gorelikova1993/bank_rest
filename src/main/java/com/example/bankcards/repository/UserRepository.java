package com.example.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByLogin(String login);
    
    boolean existsByEmail(String email);
    
}
