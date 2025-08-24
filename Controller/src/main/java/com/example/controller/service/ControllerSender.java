package com.example.controller.service;

import com.example.controller.DTO.BuyProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerSender {

    @Value("${topic.send}")
    private String topicName;

    private final KafkaTemplate<String, BuyProductDTO> kafkaTemplate;

    public void send(BuyProductDTO buyProductDTO) {
        kafkaTemplate.send(topicName, buyProductDTO.getUserId().toString(), buyProductDTO);
    }
}
