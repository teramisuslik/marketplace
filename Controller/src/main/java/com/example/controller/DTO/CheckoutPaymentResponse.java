package com.example.controller.DTO;

import lombok.Data;

@Data
public class CheckoutPaymentResponse {
    private String message;
    private String confirmationUrl;
    private Long paymentId;
}
