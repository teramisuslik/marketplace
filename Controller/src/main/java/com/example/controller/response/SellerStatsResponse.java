package com.example.controller.response;

import lombok.Data;

@Data
public class SellerStatsResponse {
    private double revenueToday;
    private int ordersCountToday;
    private double avgCheckToday;
}
