package com.example.controller.DTO;

import lombok.Data;

@Data
public class PaymentStatusResponse {
    private Long paymentId;
    private String status;
    private String yookassaPaymentId;
}
