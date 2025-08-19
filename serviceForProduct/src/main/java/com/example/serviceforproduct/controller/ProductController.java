package com.example.serviceforproduct.controller;

import com.example.serviceforproduct.client.UserClient;
import com.example.serviceforproduct.entity.Product;
import com.example.serviceforproduct.DTO.ProductDTO;
import com.example.serviceforproduct.entity.Role;
import com.example.serviceforproduct.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;
    private final UserClient userClient;

    @PostMapping("/add")
    public void addProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductDTO productDTO
    ) {
        Role role = userClient.getRole(token);
        if (role == Role.SELLER){
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .countOfProduct(productDTO.getCountOfProduct())
                .rating(productDTO.getRating())
                .sellerId(userClient.getUserId(token))
                .build();
        productService.createProduct(product);
        }
        else {throw new RuntimeException();}
    }

    @GetMapping("/main")
    public List<ProductDTO> allProducts(){
        return productService.findAll();
    }

    @GetMapping("/main/{word}")
    public List<ProductDTO> allProducts(@PathVariable String word){
        return productService.findByWord(word);
    }

    @GetMapping("/find_product_by_name/{name}")
    public Long findProductByName(@PathVariable String name)  {
        return productService.findByName(name).getId();
    }

    @GetMapping("/find_all_by_id")
    public ProductDTO findProductById(@RequestParam Long id)  {
        Product product = productService.findById(id);

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setRating(product.getRating());
        productDTO.setSellerId(product.getSellerId());
        productDTO.setCountOfProduct(product.getCountOfProduct());
        return productDTO;
    }
}
