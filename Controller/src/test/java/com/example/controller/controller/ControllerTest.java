package com.example.controller.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.DTO.*;
import com.example.controller.client.*;
import com.example.controller.jwt.JwtTokenUtils;
import com.example.controller.response.BuyerOrderResponse;
import com.example.controller.response.UserProfileResponse;
import com.example.controller.service.ControllerService;
import com.example.controller.service.GatewayPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = Controller.class,
        excludeAutoConfiguration = {FeignAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void register_shouldReturnOk() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setPassword("password");
        userDTO.setRole(Role.USER);
        userDTO.setFullName("Тест");
        doNothing().when(userClient).createUser(any(UserDTO.class));

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("регистация прошла успешно"));

        verify(userClient, times(1)).createUser(argThat(d -> "testUser".equals(d.getUsername())));
    }

    @Test
    void registerSeller_shouldReturnOk() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("seller");
        userDTO.setPassword("pass");
        userDTO.setRole(Role.SELLER);
        userDTO.setFullName("Продавец");
        doNothing().when(userClient).createSeller(any(UserDTO.class));

        // When & Then
        mockMvc.perform(post("/register_seller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("регистация прошла успешно"));

        verify(userClient, times(1)).createSeller(argThat(d -> "seller".equals(d.getUsername())));
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setPassword("pass");
        userDTO.setRole(Role.USER);
        String token = "jwt-token";
        when(userClient.login(any(UserDTO.class))).thenReturn(token);
        when(userClient.getRole("Bearer " + token)).thenReturn(Role.USER);

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userClient, times(1)).login(argThat(d -> "user".equals(d.getUsername())));
        verify(userClient, times(1)).getRole("Bearer " + token);
    }

    @Test
    void addProduct_shouldReturnOk() throws Exception {
        // Given
        String token = "Bearer jwt-token";
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Phone");
        doNothing().when(productClient).addProduct(anyString(), any(ProductDTO.class));

        // When & Then
        mockMvc.perform(post("/addproduct")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("товар добавлен"));

        verify(productClient, times(1)).addProduct(eq(token), any(ProductDTO.class));
    }

    @Test
    void allProducts_shouldReturnList() throws Exception {
        // Given
        List<ProductDTO> products = List.of(new ProductDTO(), new ProductDTO());
        when(productClient.allProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/main"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(products)));
    }

    @Test
    void findProductsByWord_shouldReturnFilteredList() throws Exception {
        // Given
        String word = "phone";
        List<ProductDTO> products = List.of(new ProductDTO());
        when(productClient.findProductsByWord(word)).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/main/{word}", word))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(products)));
    }

    @Test
    void addProductToCart_shouldReturnOk() throws Exception {
        // Given
        String token = "Bearer jwt-token";
        String productName = "Laptop";
        doNothing().when(cartClient).addProductToCart(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/add_product_to_cart/{name}", productName).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("товар добавлен в карзину"));

        verify(cartClient, times(1)).addProductToCart(eq(token), eq(productName));
    }

    @Test
    void displayCast_shouldReturnCartItems() throws Exception {
        // Given
        String token = "Bearer jwt-token";
        List<ProductDTO> cart = List.of(new ProductDTO(), new ProductDTO());
        when(cartClient.displayCast(token)).thenReturn(cart);

        // When & Then
        mockMvc.perform(get("/display/cast").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cart)));
    }

    @Test
    void mySellerProducts_shouldReturnOnlyOwnProducts() throws Exception {
        String token = "Bearer jwt-token";
        ProductDTO mine = new ProductDTO();
        mine.setId(1L);
        mine.setName("Mine");
        mine.setSellerId(10L);
        ProductDTO other = new ProductDTO();
        other.setId(2L);
        other.setName("Other");
        other.setSellerId(99L);
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        when(userClient.findUserId(token)).thenReturn(10L);
        when(productClient.allProducts()).thenReturn(List.of(mine, other));

        mockMvc.perform(get("/seller/my_products").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(mine))));

        verify(userClient, times(1)).getRole(token);
        verify(userClient, times(1)).findUserId(token);
        verify(productClient, times(1)).allProducts();
    }

    @Test
    void buyProduct_shouldReturnOk() throws Exception {
        // Given
        String token = "Bearer jwt-token";
        Long productId = 1L;
        Long userId = 10L;
        when(userClient.findUserId(token)).thenReturn(userId);
        doNothing().when(controllerService).buyProduct(any(BuyProductDTO.class));

        // When & Then
        mockMvc.perform(post("/buy_product/{productId}", productId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("оплата прошла"));

        verify(userClient, times(1)).findUserId(token);
        verify(controllerService, times(1))
                .buyProduct(BuyProductDTO.builder()
                        .productId(productId)
                        .userId(userId)
                        .build());
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
