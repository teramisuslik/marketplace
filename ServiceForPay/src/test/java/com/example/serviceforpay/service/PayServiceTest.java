package com.example.serviceforpay.service;

import static org.assertj.core.api.Assertions.assertThatNoException;

import com.example.serviceforpay.DTO.BuyProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PayServiceTest {

    @InjectMocks
    private PayService payService;

    @Test
    void printMassage_ShouldLogWithoutException_WhenValidDtoProvided() {
        // Given
        BuyProductDTO dto = BuyProductDTO.builder().productId(123L).userId(321L).build();

        // When & Then
        assertThatNoException().isThrownBy(() -> payService.printMassage(dto));
    }
}
