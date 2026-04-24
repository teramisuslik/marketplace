package com.example.controller.controller;

import com.example.controller.DTO.BuyProductDTO;
import com.example.controller.DTO.CheckoutLineEnriched;
import com.example.controller.DTO.CheckoutLineItem;
import com.example.controller.DTO.CheckoutRequest;
import com.example.controller.DTO.ProductDTO;
import com.example.controller.DTO.ProfileUpdateRequest;
import com.example.controller.DTO.RecordCheckoutRequest;
import com.example.controller.DTO.Role;
import com.example.controller.DTO.SellerCheckoutGroup;
import com.example.controller.DTO.UserDTO;
import com.example.controller.client.CartClient;
import com.example.controller.client.ProductClient;
import com.example.controller.client.UserClient;
import com.example.controller.response.Response;
import com.example.controller.response.SellerOrderResponse;
import com.example.controller.response.SellerStatsResponse;
import com.example.controller.response.UserProfileResponse;
import com.example.controller.service.ControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class Controller {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final CartClient cartClient;
    private final ControllerService controllerService;

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
        Role role = userClient.getRole("Bearer " + token);
        return ResponseEntity.ok(new Response(token, role));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/me/profile")
    public UserProfileResponse meProfile(@RequestHeader("Authorization") String token) {
        return userClient.getProfile(token);
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateMeProfile(
            @RequestHeader("Authorization") String token, @RequestBody ProfileUpdateRequest body) {
        userClient.updateProfile(token, body);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/addproduct")
    public ResponseEntity<String> addProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody ProductDTO productDTO) {
        log.info("Original token: {}", token);

        productClient.addProduct(token, productDTO);

        return ResponseEntity.ok("товар добавлен");
    }

    @GetMapping("/main")
    public List<ProductDTO> allProducts() {
        return productClient.allProducts();
    }

    @GetMapping("/main/{word}")
    public List<ProductDTO> findProductsByWord(@PathVariable("word") String word) {
        return productClient.findProductsByWord(word);
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/seller/my_products")
    public List<ProductDTO> mySellerProducts(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        if (userClient.getRole(token) != Role.SELLER) {
            return List.of();
        }
        Long sellerId = userClient.findUserId(token);
        return productClient.allProducts().stream()
                .filter(p -> p.getSellerId() != null && Objects.equals(p.getSellerId(), sellerId))
                .toList();
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/add_product_to_cart/{name}")
    public ResponseEntity<String> addProductToCart(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token, @PathVariable String name) {
        log.info("Original token: {}", token);
        cartClient.addProductToCart(token, name);
        return ResponseEntity.ok("товар добавлен в карзину");
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/display/cast")
    public List<ProductDTO> displayCast(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        return cartClient.displayCast(token);
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/buy_product/{productId}")
    public ResponseEntity<String> buyProduct(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @PathVariable("productId") Long productId) {
        Long userId = userClient.findUserId(token);
        BuyProductDTO buyProductDTO =
                BuyProductDTO.builder().productId(productId).userId(userId).build();
        controllerService.buyProduct(buyProductDTO);
        return ResponseEntity.ok("оплата прошла");
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token,
            @RequestBody CheckoutRequest request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет позиций в заказе");
        }
        Long buyerId = userClient.findUserId(token);
        UserProfileResponse profile = userClient.getProfile(token);
        String display = profile.getFullName() != null && !profile.getFullName().isBlank()
                ? profile.getFullName()
                : profile.getUsername();
        Map<Long, List<CheckoutLineEnriched>> bySeller = new LinkedHashMap<>();
        for (CheckoutLineItem raw : request.getLines()) {
            if (raw.getProductId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите productId");
            }
            ProductDTO p = productClient.findProductById(raw.getProductId());
            if (p.getSellerId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Товар «" + p.getName() + "» без продавца");
            }
            int qty = raw.getQuantity() != null && raw.getQuantity() > 0 ? raw.getQuantity() : 1;
            double unit = p.getPrice() != null ? p.getPrice() : 0.0;
            CheckoutLineEnriched line = new CheckoutLineEnriched();
            line.setProductId(p.getId());
            line.setProductName(p.getName());
            line.setQuantity(qty);
            line.setLineTotalRub(unit * qty);
            bySeller.computeIfAbsent(p.getSellerId(), k -> new ArrayList<>()).add(line);
        }
        List<SellerCheckoutGroup> groups = new ArrayList<>();
        for (Map.Entry<Long, List<CheckoutLineEnriched>> e : bySeller.entrySet()) {
            SellerCheckoutGroup g = new SellerCheckoutGroup();
            g.setSellerUserId(e.getKey());
            g.setLines(e.getValue());
            groups.add(g);
        }
        RecordCheckoutRequest record = new RecordCheckoutRequest();
        record.setBuyerUserId(buyerId);
        record.setBuyerDisplayName(display);
        record.setPaymentTiming(request.getPaymentTiming() != null ? request.getPaymentTiming() : "on_delivery");
        record.setSellerGroups(groups);
        userClient.recordCheckout(token, record);
        if ("now".equalsIgnoreCase(record.getPaymentTiming())) {
            for (CheckoutLineItem raw : request.getLines()) {
                controllerService.buyProduct(BuyProductDTO.builder()
                        .productId(raw.getProductId())
                        .userId(buyerId)
                        .build());
            }
        }
        return ResponseEntity.ok("заказ оформлен");
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/seller/orders")
    public List<SellerOrderResponse> sellerOrders(
            @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        return userClient.listSellerOrders(token);
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/seller/stats")
    public SellerStatsResponse sellerStats(@Parameter(hidden = true) @RequestHeader("Authorization") String token) {
        return userClient.sellerStats(token);
    }
}
