package com.example.controller.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yookassa")
public class YooKassaProperties {
    private String shopId = "";
    private String secretKey = "";
    private String returnUrl = "http://localhost:5174/payment/return";
    private String apiBaseUrl = "https://api.yookassa.ru/v3";
}
