package com.example.controller.response;

import lombok.Data;

@Data
public class BuyerOrderLineResponse {
    private String id;
    private String productName;
    private int quantity;
    private double price;
}
