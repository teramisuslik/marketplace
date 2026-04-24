package com.example.marketplace.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderResponse {
    private String id;
    private String date;
    private String buyerName;
    private String address;
    private String comment;
    private String status;
    private List<SellerOrderLineResponse> items;
    private double totalRub;
}
