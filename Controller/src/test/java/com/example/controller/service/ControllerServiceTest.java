package com.example.controller.service;

import com.example.controller.DTO.BuyProductDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerServiceTest {

    @Mock
    private ControllerSender controllerSender;

    @InjectMocks
    private ControllerService controllerService;

    @Test
    void buyProduct_shouldCallSender() {
        // Given
        BuyProductDTO dto = BuyProductDTO.builder()
                .productId(1L)
                .userId(2L)
                .build();

        // When
        controllerService.buyProduct(dto);

        // Then
        verify(controllerSender, times(1)).send(dto);
    }
}