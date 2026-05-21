package com.example.marketplace.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderResponse {
    private String id;
    private String number;
    private String date;
    private double amount;
    /** placed | in_transit | delivered — для UI покупателя */
    private String status;
    private String sellerName;
    private List<BuyerOrderLineResponse> items;
}
