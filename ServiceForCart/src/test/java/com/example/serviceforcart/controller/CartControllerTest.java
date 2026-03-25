package com.example.serviceforcart.controller;

import com.example.serviceforcart.config.SecurityConfig;
import com.example.serviceforcart.entity.ProductDTO;
import com.example.serviceforcart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String token = "Bearer test-token";
    private final String productName = "TestProduct";

    @Test
    void addProductToCart_ShouldCallService_WhenValidRequest() throws Exception {
        doNothing().when(cartService).addProductToCart(anyString(), anyString());

        mockMvc.perform(post("/api/cart/add_product_to_cart/{name}", productName)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService).addProductToCart(token, productName);
    }

    @Test
    void displayCart_ShouldReturnProductList_WhenValidRequest() throws Exception {
        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setName("Product1");
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setName("Product2");
        List<ProductDTO> expectedProducts = List.of(productDTO1, productDTO2);

        when(cartService.getCart(anyString())).thenReturn(expectedProducts);

        mockMvc.perform(get("/api/cart/display")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProducts)));

        verify(cartService).getCart(token);
    }
}