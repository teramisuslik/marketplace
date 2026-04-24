package com.example.marketplace.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderLineResponse {
    private String id;
    private String title;
    private int qty;
}
