package com.example.serviceforcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceForCastApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceForCastApplication.class, args);
    }

}
