package com.example.controller.service;

import com.example.controller.DTO.BuyProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ControllerSenderTest {

    @Mock
    private KafkaTemplate<String, BuyProductDTO> kafkaTemplate;

    @InjectMocks
    private ControllerSender controllerSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controllerSender, "topicName", "buy-product");
    }

    @Test
    void send_shouldCallKafkaTemplate() {
        // Given
        BuyProductDTO dto = BuyProductDTO.builder()
                .productId(1L)
                .userId(2L)
                .build();

        // When
        controllerSender.send(dto);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("buy-product"), eq(dto.getUserId().toString()), eq(dto));
    }
}