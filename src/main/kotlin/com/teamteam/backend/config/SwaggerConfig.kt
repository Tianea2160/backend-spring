package com.teamteam.backend.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Teamteam Swagger Documentation",
        description = "this is teamteam spring backend documentation",
        version="0.0.1",
        contact = Contact(
            name = "tianea",
            email = "rhlehfndvkd7557@gmail.com",
        )
    )
)
@Configuration
class SwaggerConfig