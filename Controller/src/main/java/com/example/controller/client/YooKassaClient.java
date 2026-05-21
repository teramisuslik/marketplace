package com.example.controller.client;

import com.example.controller.config.YooKassaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class YooKassaClient {

    private final RestClient yooKassaRestClient;
    private final YooKassaProperties properties;
    private final ObjectMapper objectMapper;

    public YooKassaCreateResult createRedirectPayment(double amountRub, Long internalPaymentId, String description) {
        if (properties.getShopId() == null
                || properties.getShopId().isBlank()
                || properties.getSecretKey() == null
                || properties.getSecretKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "ЮKassa не настроена (shop-id / secret-key)");
        }
        String amount =
                BigDecimal.valueOf(amountRub).setScale(2, RoundingMode.HALF_UP).toPlainString();
        String returnUrl = properties.getReturnUrl();
        if (!returnUrl.contains("paymentId=")) {
            returnUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "paymentId=" + internalPaymentId;
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("description", description != null ? description : "Заказ маркетплейса");
        ObjectNode amountNode = body.putObject("amount");
        amountNode.put("value", amount);
        amountNode.put("currency", "RUB");
        ObjectNode confirmation = body.putObject("confirmation");
        confirmation.put("type", "redirect");
        confirmation.put("return_url", returnUrl);
        ObjectNode metadata = body.putObject("metadata");
        metadata.put("internal_payment_id", String.valueOf(internalPaymentId));

        String auth = Base64.getEncoder()
                .encodeToString(
                        (properties.getShopId() + ":" + properties.getSecretKey()).getBytes(StandardCharsets.UTF_8));

        try {
            String responseBody = yooKassaRestClient
                    .post()
                    .uri("/payments")
                    .header("Authorization", "Basic " + auth)
                    .header("Idempotence-Key", UUID.randomUUID().toString())
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String yookassaId = root.path("id").asText(null);
            String confirmationUrl =
                    root.path("confirmation").path("confirmation_url").asText(null);
            if (yookassaId == null || confirmationUrl == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Некорректный ответ ЮKassa");
            }
            return new YooKassaCreateResult(yookassaId, confirmationUrl);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ошибка ЮKassa: " + e.getMessage());
        }
    }

    /** Статус платежа в ЮKassa: pending, waiting_for_capture, succeeded, canceled */
    public String fetchPaymentStatus(String yookassaPaymentId) {
        if (properties.getShopId() == null
                || properties.getShopId().isBlank()
                || properties.getSecretKey() == null
                || properties.getSecretKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "ЮKassa не настроена (shop-id / secret-key)");
        }
        String auth = Base64.getEncoder()
                .encodeToString(
                        (properties.getShopId() + ":" + properties.getSecretKey()).getBytes(StandardCharsets.UTF_8));
        try {
            String responseBody = yooKassaRestClient
                    .get()
                    .uri("/payments/{id}", yookassaPaymentId)
                    .header("Authorization", "Basic " + auth)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("status").asText("pending");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Ошибка запроса статуса ЮKassa: " + e.getMessage());
        }
    }

    public record YooKassaCreateResult(String yookassaPaymentId, String confirmationUrl) {}
}
