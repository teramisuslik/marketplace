package com.example.marketplace.DTO;

import lombok.Data;

@Data
public class CheckoutLineEnriched {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double lineTotalRub;
}
