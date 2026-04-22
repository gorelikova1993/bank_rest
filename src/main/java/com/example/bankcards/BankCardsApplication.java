package com.example.bankcards;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Cards API",
                version = "1.0",
                description = "API для управления пользователями и банковскими картами"
        )
)
@SpringBootApplication
@EnableJpaAuditing
public class BankCardsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankCardsApplication.class, args);
    }

}
