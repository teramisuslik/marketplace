package com.example.serviceforpay.service;

import com.example.serviceforpay.DTO.BuyProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PayListener {

    private final PayService payService;

    @Transactional
    @KafkaListener(
        topics = "buy-product",
        groupId = "buy",
        properties = {"spring.json.value.default.type=com.example.serviceforpay.DTO.BuyProductDTO"}
    )
    public void handleMassage(BuyProductDTO buyProductDTO){
        payService.printMassage(buyProductDTO);
    }
}
