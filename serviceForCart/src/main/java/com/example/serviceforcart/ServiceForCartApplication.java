package com.example.serviceforcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceForCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceForCartApplication.class, args);
    }

}
