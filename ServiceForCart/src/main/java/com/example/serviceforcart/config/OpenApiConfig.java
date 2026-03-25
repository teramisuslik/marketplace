package com.example.serviceforcart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service API")
                        .description("Корзина: добавление товаров и просмотр по JWT.")
                        .version("1.0"));
    }
}
