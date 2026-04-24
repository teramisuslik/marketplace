package com.example.controller.DTO;

import java.util.List;
import lombok.Data;

@Data
public class SellerCheckoutGroup {
    private Long sellerUserId;
    private List<CheckoutLineEnriched> lines;
}
