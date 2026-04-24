package com.example.controller.DTO;

import java.util.List;
import lombok.Data;

@Data
public class RecordCheckoutRequest {
    private Long buyerUserId;
    private String buyerDisplayName;
    private String paymentTiming;
    private List<SellerCheckoutGroup> sellerGroups;
}
