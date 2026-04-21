package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    
    @NotBlank(message = "Логин не должен быть пустым")
    private String login;
    
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
    
}
