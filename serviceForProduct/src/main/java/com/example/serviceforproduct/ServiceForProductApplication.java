package com.example.serviceforproduct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceForProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceForProductApplication.class, args);
    }

}
