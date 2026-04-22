package com.example.controller.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.DTO.*;
import com.example.controller.client.*;
import com.example.controller.jwt.JwtTokenUtils;
import com.example.controller.service.ControllerService;
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

    @Test
    void register_shouldReturnOk() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO("testUser", "password", Role.USER);
        doNothing().when(userClient).createUser(any(UserDTO.class));

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("регистация прошла успешно"));

        verify(userClient, times(1)).createUser(userDTO);
    }

    @Test
    void registerSeller_shouldReturnOk() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO("seller", "pass", Role.SELLER);
        doNothing().when(userClient).createSeller(any(UserDTO.class));

        // When & Then
        mockMvc.perform(post("/register_seller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("регистация прошла успешно"));

        verify(userClient, times(1)).createSeller(userDTO);
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO("user", "pass", Role.USER);
        String token = "jwt-token";
        when(userClient.login(any(UserDTO.class))).thenReturn(token);

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));

        verify(userClient, times(1)).login(userDTO);
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
}
