package com.example.serviceforcast.client;



import com.example.serviceforcast.entity.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product", url = "http://localhost:8081")
public interface ProductClient {

    @GetMapping("/api/product/find_product_by_name/{name}")
    public Long findProductByName(@PathVariable("name") String name);

    @GetMapping("/api/product/find_all_by_id")
    public ProductDTO findProductById(@RequestParam Long id);
}
