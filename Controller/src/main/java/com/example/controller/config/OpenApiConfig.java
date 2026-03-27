package com.example.controller.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI controllerGatewayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketplace API Gateway")
                        .description(
                                "Внешнее API для клиентов. Для защищённых методов передайте JWT в заголовке Authorization.")
                        .version("1.0"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearer-jwt",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "JWT из поля token в ответе POST /login. В Authorize вводите только сам токен (без Bearer). Поле Authorization в форме запроса скрыто — иначе пустое значение перебивало бы токен из Authorize и давало 403.")));
    }
}
