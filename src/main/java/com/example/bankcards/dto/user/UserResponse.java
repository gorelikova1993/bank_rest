package com.example.bankcards.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String login;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String middleName;
    private boolean enabled;
}
