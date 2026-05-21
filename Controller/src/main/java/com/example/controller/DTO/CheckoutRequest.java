package com.example.controller.DTO;

import java.util.List;
import lombok.Data;

@Data
public class CheckoutRequest {
    private String paymentTiming;

    private List<CheckoutLineItem> lines;
}
