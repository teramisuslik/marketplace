package com.example.marketplace.jwt;

import lombok.Data;

@Data
public class Response {
    private String token;

    public Response(String token) {
        this.token = token;
    }
}
