package com.example.controller.client;

import com.example.controller.DTO.ProductDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product", url = "${app.feign.product.url:http://localhost:8081}")
public interface ProductClient {

    @PostMapping("/api/product/add")
    void addProduct(@RequestHeader("Authorization") String token, @RequestBody ProductDTO productDTO);

    @GetMapping("/api/product/main")
    List<ProductDTO> allProducts();

    @GetMapping("/api/product/main/{word}")
    List<ProductDTO> findProductsByWord(@PathVariable("word") String word);

    @GetMapping("/api/product/find_all_by_id")
    ProductDTO findProductById(@RequestParam("id") Long id);
}
