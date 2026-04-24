package com.example.controller.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class AssistantConfig {

    @Bean
    public RestClient ollamaRestClient(AssistantProperties assistantProperties) {
        return RestClient.builder()
                .baseUrl(assistantProperties.getOllamaBaseUrl())
                .build();
    }
}
