package com.example.controller.response;

import lombok.Data;

@Data
public class Response {
    private String token;

    public Response(String token) {
        this.token = token;
    }
}
