package com.example.controller.response;

import java.util.List;
import lombok.Data;

@Data
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
