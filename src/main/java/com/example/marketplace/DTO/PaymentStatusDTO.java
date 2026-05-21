package com.example.marketplace.DTO;

import lombok.Data;

@Data
public class PaymentStatusDTO {
    private Long paymentId;
    private String status;
    private String yookassaPaymentId;
}
