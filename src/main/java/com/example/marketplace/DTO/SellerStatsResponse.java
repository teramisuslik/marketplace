package com.example.marketplace.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerStatsResponse {
    private double revenueToday;
    private int ordersCountToday;
    private double avgCheckToday;
}
