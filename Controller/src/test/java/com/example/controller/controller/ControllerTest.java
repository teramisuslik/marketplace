package com.example.controller.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.DTO.*;
import com.example.controller.client.CartClient;
import com.example.controller.client.ProductClient;
import com.example.controller.client.UserClient;
import com.example.controller.jwt.JwtTokenUtils;
import com.example.controller.response.BuyerOrderResponse;
import com.example.controller.response.SellerOrderResponse;
import com.example.controller.response.SellerStatsResponse;
import com.example.controller.response.UserProfileResponse;
import com.example.controller.service.ControllerService;
import com.example.controller.service.GatewayPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(Controller.class)
@AutoConfigureMockMvc(addFilters = false)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private CartClient cartClient;

    @MockitoBean
    private ControllerService controllerService;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean
    private GatewayPaymentService gatewayPaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnOk() throws Exception {
        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("pass");

        // when
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string("регистация прошла успешно"));
        verify(userClient).createUser(any(UserDTO.class));
    }

    @Test
    void registerSeller_ShouldReturnOk() throws Exception {
        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("seller");
        userDTO.setPassword("pass");

        // when
        mockMvc.perform(post("/register_seller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                // then
                .andExpect(status().isOk());
        verify(userClient).createSeller(any(UserDTO.class));
    }

    @Test
    void login_ShouldReturnTokenAndRole() throws Exception {
        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setPassword("secret");
        String jwt = "jwt.token.123";
        when(userClient.login(any(UserDTO.class))).thenReturn(jwt);
        when(userClient.getRole("Bearer " + jwt)).thenReturn(Role.SELLER);

        // when
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwt))
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void meProfile_ShouldReturnUserProfile() throws Exception {
        // given
        String token = "Bearer test";
        UserProfileResponse profile = new UserProfileResponse();
        profile.setUsername("john");
        profile.setFullName("John Doe");
        when(userClient.getProfile(token)).thenReturn(profile);

        // when
        mockMvc.perform(get("/me/profile").header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void updateMeProfile_ShouldReturnNoContent() throws Exception {
        // given
        String token = "Bearer test";
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("New Name");

        // when
        mockMvc.perform(put("/me/profile")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isNoContent());
        verify(userClient).updateProfile(eq(token), any(ProfileUpdateRequest.class));
    }

    @Test
    void addProduct_ShouldReturnOk() throws Exception {
        // given
        String token = "Bearer test";
        ProductDTO product = new ProductDTO();
        product.setName("Phone");

        // when
        mockMvc.perform(post("/addproduct")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                // then
                .andExpect(status().isOk());
        verify(productClient).addProduct(eq(token), any(ProductDTO.class));
    }

    @Test
    void allProducts_ShouldReturnList() throws Exception {
        // given
        ProductDTO p1 = new ProductDTO();
        p1.setId(1L);
        when(productClient.allProducts()).thenReturn(List.of(p1));

        // when
        mockMvc.perform(get("/main"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findProductsByWord_ShouldReturnFiltered() throws Exception {
        // given
        ProductDTO p = new ProductDTO();
        p.setName("laptop");
        when(productClient.findProductsByWord("lap")).thenReturn(List.of(p));

        // when
        mockMvc.perform(get("/main/lap"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("laptop"));
    }

    @Test
    void mySellerProducts_ShouldReturnOnlySellerOwnProducts() throws Exception {
        // given
        String token = "Bearer test";
        Long sellerId = 100L;
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        when(userClient.findUserId(token)).thenReturn(sellerId);
        ProductDTO p1 = new ProductDTO();
        p1.setId(1L);
        p1.setSellerId(sellerId);
        ProductDTO p2 = new ProductDTO();
        p2.setId(2L);
        p2.setSellerId(999L);
        when(productClient.allProducts()).thenReturn(List.of(p1, p2));

        // when
        mockMvc.perform(get("/seller/my_products").header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void addProductToCart_ShouldCallCartClient() throws Exception {
        // given
        String token = "Bearer test";
        String productName = "Phone";

        // when
        mockMvc.perform(post("/add_product_to_cart/{name}", productName).header("Authorization", token))
                // then
                .andExpect(status().isOk());
        verify(cartClient).addProductToCart(token, productName);
    }

    @Test
    void displayCast_ShouldReturnCartContents() throws Exception {
        // given
        String token = "Bearer test";
        ProductDTO p = new ProductDTO();
        p.setName("Laptop");
        when(cartClient.displayCast(token)).thenReturn(List.of(p));

        // when
        mockMvc.perform(get("/display/cast").header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void buyProduct_ShouldSendKafkaMessage() throws Exception {
        // given
        String token = "Bearer test";
        Long productId = 5L;
        Long userId = 123L;
        when(userClient.findUserId(token)).thenReturn(userId);

        // when
        mockMvc.perform(post("/buy_product/{productId}", productId).header("Authorization", token))
                // then
                .andExpect(status().isOk());
        verify(controllerService)
                .buyProduct(argThat(dto ->
                        dto.getProductId().equals(productId) && dto.getUserId().equals(userId)));
    }

    @Test
    void checkout_ShouldCreateOrderAndProcessPayment() throws Exception {
        // given
        String token = "Bearer test";
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long productId = 99L;

        CheckoutLineItem line = new CheckoutLineItem();
        line.setProductId(productId);
        line.setQuantity(2);
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentTiming("now");
        request.setLines(List.of(line));

        when(userClient.findUserId(token)).thenReturn(buyerId);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setFullName("Buyer Name");
        when(userClient.getProfile(token)).thenReturn(profile);

        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setSellerId(sellerId);
        when(productClient.findProductById(productId)).thenReturn(product);

        RecordCheckoutResponse recorded = new RecordCheckoutResponse();
        recorded.setTotalRub(200.0);
        when(userClient.recordCheckout(eq(token), any(RecordCheckoutRequest.class)))
                .thenReturn(recorded);

        // Мокируем вызов сервиса оплаты
        CheckoutPaymentResponse paymentResponse = new CheckoutPaymentResponse();
        paymentResponse.setConfirmationUrl("https://yookassa.ru/confirm");
        paymentResponse.setPaymentId(777L);
        paymentResponse.setMessage("Перенаправление на оплату");
        when(gatewayPaymentService.startOnlinePayment(eq(token), any(RecordCheckoutResponse.class)))
                .thenReturn(paymentResponse);

        // when
        mockMvc.perform(post("/checkout")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationUrl").value("https://yookassa.ru/confirm"));

        verify(userClient).recordCheckout(eq(token), any(RecordCheckoutRequest.class));
        verify(controllerService, never()).buyProduct(any(BuyProductDTO.class));
    }

    @Test
    void sellerOrders_ShouldReturnList() throws Exception {
        // given
        String token = "Bearer test";
        SellerOrderResponse order = new SellerOrderResponse();
        order.setId("SO-1");
        when(userClient.listSellerOrders(token)).thenReturn(List.of(order));

        // when
        mockMvc.perform(get("/seller/orders").header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("SO-1"));
    }

    @Test
    void sellerStats_ShouldReturnStats() throws Exception {
        // given
        String token = "Bearer test";
        SellerStatsResponse stats = new SellerStatsResponse();
        stats.setRevenueToday(5000.0);
        when(userClient.sellerStats(token)).thenReturn(stats);

        // when
        mockMvc.perform(get("/seller/stats").header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenueToday").value(5000.0));
    }

    @Test
    void changeMePassword_shouldReturnNoContent() throws Exception {
        String token = "Bearer test";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");

        mockMvc.perform(put("/me/password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(userClient).changePassword(eq(token), any(ChangePasswordRequest.class));
    }

    @Test
    void updateSellerProduct_shouldReturnUpdatedProduct() throws Exception {
        String token = "Bearer test";
        Long productId = 10L;
        ProductDTO product = new ProductDTO();
        product.setName("Updated");
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        when(productClient.updateProduct(eq(token), eq(productId), any(ProductDTO.class)))
                .thenReturn(product);

        mockMvc.perform(put("/seller/product/{id}", productId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateSellerProduct_shouldReturnForbidden_whenNotSeller() throws Exception {
        String token = "Bearer test";
        when(userClient.getRole(token)).thenReturn(Role.USER);
        ProductDTO product = new ProductDTO();

        mockMvc.perform(put("/seller/product/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
        verify(productClient, never()).updateProduct(any(), any(), any());
    }

    @Test
    void listAddresses_shouldReturnList() throws Exception {
        String token = "Bearer test";
        UserAddressResponse address = new UserAddressResponse();
        address.setId(1L);
        when(userClient.listAddresses(token)).thenReturn(List.of(address));

        mockMvc.perform(get("/me/addresses").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createAddress_shouldReturnCreated() throws Exception {
        String token = "Bearer test";
        CreateAddressRequest request = new CreateAddressRequest();
        request.setCity("Moscow");
        UserAddressResponse response = new UserAddressResponse();
        response.setId(5L);
        when(userClient.createAddress(eq(token), any(CreateAddressRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/me/addresses")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void deleteAddress_shouldReturnNoContent() throws Exception {
        String token = "Bearer test";
        Long addressId = 3L;

        mockMvc.perform(delete("/me/addresses/{id}", addressId).header("Authorization", token))
                .andExpect(status().isNoContent());
        verify(userClient).deleteAddress(token, addressId);
    }

    @Test
    void myOrders_shouldReturnList() throws Exception {
        String token = "Bearer test";
        BuyerOrderResponse order = new BuyerOrderResponse();
        order.setId("SO-1");
        when(userClient.listBuyerOrders(token)).thenReturn(List.of(order));

        mockMvc.perform(get("/me/orders").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("SO-1"));
    }

    @Test
    void checkout_withOnlinePayment_shouldReturnPaymentResponse() throws Exception {
        String token = "Bearer test";
        Long buyerId = 10L;
        Long sellerId = 20L;
        Long productId = 99L;

        CheckoutLineItem line = new CheckoutLineItem();
        line.setProductId(productId);
        line.setQuantity(2);
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentTiming("now");
        request.setLines(List.of(line));

        when(userClient.findUserId(token)).thenReturn(buyerId);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setFullName("Buyer Name");
        when(userClient.getProfile(token)).thenReturn(profile);

        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setSellerId(sellerId);
        when(productClient.findProductById(productId)).thenReturn(product);

        RecordCheckoutResponse recorded = new RecordCheckoutResponse();
        recorded.setTotalRub(200.0);
        when(userClient.recordCheckout(eq(token), any(RecordCheckoutRequest.class)))
                .thenReturn(recorded);

        CheckoutPaymentResponse paymentResponse = new CheckoutPaymentResponse();
        paymentResponse.setConfirmationUrl("https://yookassa.ru/confirm");
        paymentResponse.setPaymentId(777L);
        when(gatewayPaymentService.startOnlinePayment(eq(token), any(RecordCheckoutResponse.class)))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/checkout")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationUrl").value("https://yookassa.ru/confirm"));
    }
}
