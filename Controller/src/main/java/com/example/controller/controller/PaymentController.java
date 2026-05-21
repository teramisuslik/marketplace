package com.example.controller.controller;

import com.example.controller.DTO.PaymentStatusResponse;
import com.example.controller.service.GatewayPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final GatewayPaymentService gatewayPaymentService;

    @PostMapping("/payments/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String body) {
        gatewayPaymentService.handleWebhook(body);
        return ResponseEntity.ok().build();
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/payments/{id}/status")
    public PaymentStatusResponse status(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token, @PathVariable("id") Long id) {
        return gatewayPaymentService.getStatus(token, id);
    }

    @Operation(
            security = @SecurityRequirement(name = "bearer-jwt"),
            summary = "Синхронизировать статус с ЮKassa (для локальной разработки без webhook)")
    @PostMapping("/payments/{id}/sync")
    public PaymentStatusResponse sync(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token, @PathVariable("id") Long id) {
        return gatewayPaymentService.syncStatusFromYooKassa(token, id);
    }
}
