package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    
    @NotBlank
    private String login;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    private String password;
    
    private String firstName;
    private String lastName;
    private String middleName;
    
    
}
