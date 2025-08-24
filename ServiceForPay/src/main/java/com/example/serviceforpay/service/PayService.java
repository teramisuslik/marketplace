package com.example.serviceforpay.service;

import com.example.serviceforpay.DTO.BuyProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayService {

    public void printMassage(BuyProductDTO buyProductDTO) {
        log.info("платеж пользователя {} за товар {} прошел", buyProductDTO.getUserId(), buyProductDTO.getProductId());
    }
}
