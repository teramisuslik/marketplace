package com.example.controller.client;

import com.example.controller.DTO.ProductDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart", url = "http://localhost:8083")
public interface CartClient {

    @PostMapping("/api/cart/add_product_to_cart/{name}")
    void addProductToCart(@RequestHeader("Authorization") String token, @PathVariable String name);

    @GetMapping("/api/cart/display")
    List<ProductDTO> displayCast(@RequestHeader("Authorization") String token);
}
