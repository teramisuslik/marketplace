package com.example.controller.service;

import com.example.controller.DTO.AttachYookassaPaymentRequest;
import com.example.controller.DTO.CheckoutPaymentResponse;
import com.example.controller.DTO.PaymentStatusResponse;
import com.example.controller.DTO.RecordCheckoutResponse;
import com.example.controller.client.UserClient;
import com.example.controller.client.YooKassaClient;
import com.example.controller.client.YooKassaClient.YooKassaCreateResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayPaymentService {

    private final UserClient userClient;
    private final YooKassaClient yooKassaClient;
    private final ObjectMapper objectMapper;

    public CheckoutPaymentResponse startOnlinePayment(String token, RecordCheckoutResponse checkout) {
        Long paymentId = userClient.createPendingPayment(token, checkout);
        YooKassaCreateResult created =
                yooKassaClient.createRedirectPayment(checkout.getTotalRub(), paymentId, "Оплата заказа маркетплейса");
        AttachYookassaPaymentRequest attach = new AttachYookassaPaymentRequest();
        attach.setPaymentId(paymentId);
        attach.setYookassaPaymentId(created.yookassaPaymentId());
        userClient.attachYookassa(attach);

        CheckoutPaymentResponse response = new CheckoutPaymentResponse();
        response.setPaymentId(paymentId);
        response.setConfirmationUrl(created.confirmationUrl());
        return response;
    }

    public void handleWebhook(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText("");
            JsonNode obj = root.path("object");
            String yookassaId = obj.path("id").asText(null);
            if (yookassaId == null) {
                return;
            }
            if ("payment.succeeded".equals(event)) {
                userClient.webhookPaymentSucceeded(yookassaId);
            } else if ("payment.canceled".equals(event)) {
                userClient.webhookPaymentCanceled(yookassaId);
            }
        } catch (Exception e) {
            log.warn("Webhook parse error: {}", e.getMessage());
        }
    }

    public PaymentStatusResponse getStatus(String token, Long paymentId) {
        return syncStatusFromYooKassa(token, paymentId);
    }

    public PaymentStatusResponse syncStatusFromYooKassa(String token, Long paymentId) {
        PaymentStatusResponse status = userClient.getPaymentStatus(token, paymentId);
        if (!"pending".equals(status.getStatus()) || status.getYookassaPaymentId() == null) {
            return status;
        }
        try {
            String ykStatus = yooKassaClient.fetchPaymentStatus(status.getYookassaPaymentId());
            if ("succeeded".equals(ykStatus) || "waiting_for_capture".equals(ykStatus)) {
                userClient.webhookPaymentSucceeded(status.getYookassaPaymentId());
                return userClient.getPaymentStatus(token, paymentId);
            }
            if ("canceled".equals(ykStatus)) {
                userClient.webhookPaymentCanceled(status.getYookassaPaymentId());
                return userClient.getPaymentStatus(token, paymentId);
            }
        } catch (Exception e) {
            log.warn("YooKassa sync for payment {} failed: {}", paymentId, e.getMessage());
        }
        return status;
    }
}
