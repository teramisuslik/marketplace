package com.example.controller.controller;

import com.example.controller.DTO.ProductDTO;
import com.example.controller.DTO.UserDTO;
import com.example.controller.client.CartClient;
import com.example.controller.client.ProductClient;
import com.example.controller.client.UserClient;
import com.example.controller.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class Controller {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final CartClient cartClient;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) {
        userClient.createUser(userDTO);
        return ResponseEntity.ok("регистация прошла успешно");
    }
    @PostMapping("/register_seller")
    public ResponseEntity<String> registerSeller(@RequestBody UserDTO userDTO) {
        userClient.createSeller(userDTO);
        return ResponseEntity.ok("регистация прошла успешно");
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody UserDTO userDTO) {
        String token = userClient.login(userDTO);
        return ResponseEntity.ok(new Response(token));
    }

    @PostMapping("/addproduct")
    public ResponseEntity<String> addProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductDTO productDTO
    ) {
        log.info("Original token: {}", token);

        productClient.addProduct(token, productDTO);

        return ResponseEntity.ok("товар добавлен");
    }

    @GetMapping("/main")
    public List<ProductDTO> allProducts(){
        return productClient.allProducts();
    }

    @GetMapping("/main/{word}")
    public List<ProductDTO> findProductsByWord(@PathVariable("word") String word){
        return productClient.findProductsByWord(word);
    }

    @PostMapping("/add_product_to_cart/{name}")
    public ResponseEntity<String> addProductToCart(@RequestHeader("Authorization") String token, @PathVariable String name){
        log.info("Original token: {}", token);
        cartClient.addProductToCart(token,name);
        return ResponseEntity.ok("товар добавлен в карзину");
    }

    @GetMapping("/display/cast")
    public List<ProductDTO> displayCast(@RequestHeader("Authorization") String token){
        return cartClient.displayCast(token);
    }
}
