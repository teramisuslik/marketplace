package com.example.serviceforpay.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @GetMapping("/api/test")
    public String apiEndpoint() {
        return "public api";
    }

    @GetMapping("/other")
    public String otherEndpoint() {
        return "secured";
    }
}
