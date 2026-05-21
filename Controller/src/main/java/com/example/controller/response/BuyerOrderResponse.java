package com.example.controller.response;

import java.util.List;
import lombok.Data;

@Data
public class BuyerOrderResponse {
    private String id;
    private String number;
    private String date;
    private double amount;
    private String status;
    private String sellerName;
    private List<BuyerOrderLineResponse> items;
}
