package com.example.controller.service;

import com.example.controller.DTO.BuyProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ControllerService {

    private final ControllerSender controllerSender;

    public void buyProduct(BuyProductDTO buyProductDTO) {
        controllerSender.send(buyProductDTO);
    }
}
