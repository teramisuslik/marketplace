package com.example.controller.DTO;

import lombok.Data;

@Data
public class CheckoutLineItem {
    private Long productId;
    private Integer quantity;
}
