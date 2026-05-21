package com.example.marketplace.service;

import com.example.marketplace.DTO.AttachYookassaPaymentRequest;
import com.example.marketplace.DTO.PaymentStatusDTO;
import com.example.marketplace.entity.Payment;
import com.example.marketplace.entity.PaymentStatus;
import com.example.marketplace.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShopOrderService shopOrderService;
    private final UserService userService;

    @Transactional
    public Payment createPending(Long buyerUserId, List<Long> orderIds, double amountRub) {
        if (orderIds == null || orderIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет заказов для оплаты");
        }
        Payment payment = Payment.builder()
                .buyerUserId(buyerUserId)
                .amountRub(BigDecimal.valueOf(amountRub).setScale(2, RoundingMode.HALF_UP))
                .status(PaymentStatus.pending)
                .createdAt(Instant.now())
                .shopOrderIds(orderIds)
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public void attachYookassaId(AttachYookassaPaymentRequest body) {
        Payment payment = paymentRepository
                .findById(body.getPaymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Платёж не найден"));
        payment.setYookassaPaymentId(body.getYookassaPaymentId());
        paymentRepository.save(payment);
    }

    @Transactional
    public void markSucceededByYookassaId(String yookassaPaymentId) {
        Payment payment = paymentRepository
                .findByYookassaPaymentId(yookassaPaymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Платёж не найден"));
        if (payment.getStatus() == PaymentStatus.succeeded) {
            return;
        }
        payment.setStatus(PaymentStatus.succeeded);
        paymentRepository.save(payment);
        shopOrderService.markOrdersPaid(payment.getShopOrderIds());
    }

    @Transactional
    public void markCanceledByYookassaId(String yookassaPaymentId) {
        paymentRepository.findByYookassaPaymentId(yookassaPaymentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.canceled);
            paymentRepository.save(payment);
        });
    }

    public PaymentStatusDTO getStatus(String authorization, Long paymentId) {
        Long buyerId = userService.getUserid(authorization);
        Payment payment = paymentRepository
                .findByIdAndBuyerUserId(paymentId, buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Платёж не найден"));
        PaymentStatusDTO dto = new PaymentStatusDTO();
        dto.setPaymentId(payment.getId());
        dto.setStatus(payment.getStatus().name());
        dto.setYookassaPaymentId(payment.getYookassaPaymentId());
        return dto;
    }
}
