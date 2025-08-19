package com.example.controller.client;


import com.example.controller.DTO.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product", url = "http://localhost:8081")
public interface ProductClient {

    @PostMapping("/api/product/add")
    void addProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductDTO productDTO
    );

    @GetMapping("/api/product/main")
    List<ProductDTO> allProducts();

    @GetMapping("/api/product/main/{word}")
    List<ProductDTO> findProductsByWord(@PathVariable("word") String word);
}
