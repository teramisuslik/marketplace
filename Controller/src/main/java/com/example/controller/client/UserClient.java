package com.example.controller.client;

import com.example.controller.DTO.ProfileUpdateRequest;
import com.example.controller.DTO.RecordCheckoutRequest;
import com.example.controller.DTO.Role;
import com.example.controller.DTO.UserDTO;
import com.example.controller.response.BuyerOrderResponse;
import com.example.controller.response.SellerOrderResponse;
import com.example.controller.response.SellerStatsResponse;
import com.example.controller.response.UserProfileResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user", url = "${app.feign.user.url:http://localhost:8082}")
public interface UserClient {

    @PostMapping("/api/user/register")
    void createUser(@RequestBody UserDTO userDTO);

    @PostMapping("/api/user/register/seller")
    void createSeller(@RequestBody UserDTO userDTO);

    @PostMapping("/api/user/login")
    String login(@RequestBody UserDTO userDTO);

    @GetMapping("/api/user/get_role")
    Role getRole(@RequestHeader("Authorization") String authorization);

    @GetMapping("/api/user/load_user_by_username")
    UserDTO findUserByUsername(@RequestParam("username") String username);

    @PostMapping("/api/user/userid")
    Long findUserId(@RequestHeader("Authorization") String token);

    @GetMapping("/api/user/profile")
    UserProfileResponse getProfile(@RequestHeader("Authorization") String authorization);

    @PutMapping("/api/user/profile")
    void updateProfile(@RequestHeader("Authorization") String authorization, @RequestBody ProfileUpdateRequest body);

    @PutMapping("/api/user/password")
    void changePassword(
            @RequestHeader("Authorization") String authorization,
            @RequestBody com.example.controller.DTO.ChangePasswordRequest body);

    @PostMapping("/api/user/checkout/record")
    com.example.controller.DTO.RecordCheckoutResponse recordCheckout(
            @RequestHeader("Authorization") String authorization, @RequestBody RecordCheckoutRequest body);

    @GetMapping("/api/user/addresses")
    java.util.List<com.example.controller.DTO.UserAddressResponse> listAddresses(
            @RequestHeader("Authorization") String authorization);

    @PostMapping("/api/user/addresses")
    com.example.controller.DTO.UserAddressResponse createAddress(
            @RequestHeader("Authorization") String authorization,
            @RequestBody com.example.controller.DTO.CreateAddressRequest body);

    @DeleteMapping("/api/user/addresses/{id}")
    void deleteAddress(@RequestHeader("Authorization") String authorization, @PathVariable("id") Long id);

    @PostMapping("/api/user/payments/pending")
    Long createPendingPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestBody com.example.controller.DTO.RecordCheckoutResponse checkoutResult);

    @PostMapping("/api/user/payments/attach-yookassa")
    void attachYookassa(@RequestBody com.example.controller.DTO.AttachYookassaPaymentRequest body);

    @PostMapping("/api/user/payments/webhook-succeeded")
    void webhookPaymentSucceeded(@RequestParam("yookassaPaymentId") String yookassaPaymentId);

    @PostMapping("/api/user/payments/webhook-canceled")
    void webhookPaymentCanceled(@RequestParam("yookassaPaymentId") String yookassaPaymentId);

    @GetMapping("/api/user/payments/{id}/status")
    com.example.controller.DTO.PaymentStatusResponse getPaymentStatus(
            @RequestHeader("Authorization") String authorization, @PathVariable("id") Long id);

    @GetMapping("/api/user/buyer/orders")
    List<BuyerOrderResponse> listBuyerOrders(@RequestHeader("Authorization") String authorization);

    @GetMapping("/api/user/seller/orders")
    List<SellerOrderResponse> listSellerOrders(@RequestHeader("Authorization") String authorization);

    @GetMapping("/api/user/seller/stats")
    SellerStatsResponse sellerStats(@RequestHeader("Authorization") String authorization);
}
