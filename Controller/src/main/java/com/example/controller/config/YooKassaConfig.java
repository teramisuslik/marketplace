package com.example.controller.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(YooKassaProperties.class)
public class YooKassaConfig {

    @Bean
    public RestClient yooKassaRestClient(YooKassaProperties properties) {
        return RestClient.builder().baseUrl(properties.getApiBaseUrl()).build();
    }
}
