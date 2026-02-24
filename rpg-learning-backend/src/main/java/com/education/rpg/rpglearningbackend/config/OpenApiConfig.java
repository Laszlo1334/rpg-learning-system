package com.education.rpg.rpglearningbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RPG Learning System API",
                version = "1.0",
                description = "Документація бекенду для освітньої RPG-гри. Авторизація працює автоматично через Google OAuth2."
        )
)
public class OpenApiConfig {
}