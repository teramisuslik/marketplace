package com.example.serviceforproduct.controller;

import com.example.serviceforproduct.DTO.ProductDTO;
import com.example.serviceforproduct.client.UserClient;
import com.example.serviceforproduct.entity.Product;
import com.example.serviceforproduct.entity.Role;
import com.example.serviceforproduct.exception.ForbiddenException;
import com.example.serviceforproduct.exception.NotFoundException;
import com.example.serviceforproduct.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final UserClient userClient;

    @PostMapping("/add")
    public void addProduct(@RequestHeader("Authorization") String token, @RequestBody ProductDTO productDTO) {
        Role role = userClient.getRole(token);
        if (role == Role.SELLER) {
            Product product = Product.builder()
                    .name(productDTO.getName())
                    .description(productDTO.getDescription())
                    .countOfProduct(productDTO.getCountOfProduct())
                    .rating(productDTO.getRating())
                    .sellerId(userClient.getUserId(token))
                    .price(productDTO.getPrice() != null ? productDTO.getPrice() : 0.0)
                    .imageUrl(productDTO.getImageUrl())
                    .build();
            productService.createProduct(product);
        } else {
            throw new ForbiddenException("Only sellers can add products");
        }
    }

    @GetMapping("/main")
    public List<ProductDTO> allProducts() {
        return productService.findAll();
    }

    @GetMapping("/main/{word}")
    public List<ProductDTO> allProducts(@PathVariable String word) {
        return productService.findByWord(word);
    }

    @GetMapping("/find_product_by_name/{name}")
    public Long findProductByName(@PathVariable String name) {
        return productService.findByName(name).getId();
    }

    @GetMapping("/find_all_by_id")
    public ProductDTO findProductById(@RequestParam Long id) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setRating(product.getRating());
        productDTO.setSellerId(product.getSellerId());
        productDTO.setCountOfProduct(product.getCountOfProduct());
        productDTO.setPrice(product.getPrice());
        productDTO.setImageUrl(product.getImageUrl());
        return productDTO;
    }
}
