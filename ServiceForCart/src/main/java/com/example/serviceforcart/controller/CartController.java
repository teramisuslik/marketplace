package com.example.serviceforcart.controller;

import com.example.serviceforcart.entity.ProductDTO;
import com.example.serviceforcart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add_product_to_cart/{name}")
    public void addProductToCart(@RequestHeader("Authorization") String token, @PathVariable String name){
        log.info("Original token: {}", token);
         cartService.addProductToCart(token,name);
    }

    @GetMapping("/display")
    public List<ProductDTO> displayCast(@RequestHeader("Authorization") String token){
        log.info("Original token: {}", token);
        return cartService.getCart(token);
    }

}
