package com.example.controller.DTO;

import java.util.List;
import lombok.Data;

@Data
public class CheckoutRequest {
    /** {@code now} или {@code on_delivery} */
    private String paymentTiming;

    private List<CheckoutLineItem> lines;
}
