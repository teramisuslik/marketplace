package com.example.serviceforpay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.serviceforpay.DTO.BuyProductDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayListenerTest {

    @Mock
    private PayService payService;

    @InjectMocks
    private PayListener payListener;

    @Test
    void handleMassage_ShouldInvokePayServicePrintMassage_WhenValidDtoReceived() {
        // Given
        BuyProductDTO dto = BuyProductDTO.builder().productId(100L).userId(42L).build();

        // When
        payListener.handleMassage(dto);

        // Then
        ArgumentCaptor<BuyProductDTO> captor = ArgumentCaptor.forClass(BuyProductDTO.class);
        verify(payService, times(1)).printMassage(captor.capture());
        BuyProductDTO captured = captor.getValue();
        assertThat(captured.getProductId()).isEqualTo(100L);
        assertThat(captured.getUserId()).isEqualTo(42L);
    }
}
