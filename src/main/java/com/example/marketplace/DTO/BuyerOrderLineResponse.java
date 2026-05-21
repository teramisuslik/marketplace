package com.example.marketplace.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderLineResponse {
    private String id;
    private String productName;
    private int quantity;
    private double price;
}
