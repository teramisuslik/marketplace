package com.example.marketplace.DTO;

import java.util.List;
import lombok.Data;

@Data
public class RecordCheckoutRequest {
    private Long buyerUserId;
    private String buyerDisplayName;
    /** {@code now} — оплата онлайн; {@code on_delivery} — при получении */
    private String paymentTiming;

    private List<SellerCheckoutGroup> sellerGroups;
}
