package com.example.controller.DTO;

import lombok.Data;

@Data
public class AttachYookassaPaymentRequest {
    private Long paymentId;
    private String yookassaPaymentId;
}
