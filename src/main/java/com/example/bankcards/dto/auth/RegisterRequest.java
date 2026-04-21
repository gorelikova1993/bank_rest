package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    
    @NotBlank(message = "Логин не должен быть пустым")
    private String login;
    
    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email не должен быть пустым")
    private String email;
    
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
    
    private String firstName;
    private String lastName;
    private String middleName;
    
    
}
