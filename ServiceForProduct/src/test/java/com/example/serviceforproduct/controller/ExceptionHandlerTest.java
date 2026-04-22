package com.example.serviceforproduct.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.serviceforproduct.client.UserClient;
import com.example.serviceforproduct.config.SecurityConfig;
import com.example.serviceforproduct.entity.Role;
import com.example.serviceforproduct.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserClient userClient;

    private final String token = "Bearer test-token";

    @Test
    void handleNotFound_ShouldReturn404WithMessage() throws Exception {
        Long id = 999L;
        when(productService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/product/find_all_by_id").param("id", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: " + id));
    }

    @Test
    void handleForbidden_ShouldReturn403WithMessage() throws Exception {
        when(userClient.getRole(token)).thenReturn(Role.USER);

        mockMvc.perform(
                        post("/api/product/add")
                                .header("Authorization", token)
                                .contentType("application/json")
                                .content(
                                        "{\"name\":\"Test\",\"description\":\"Desc\",\"countOfProduct\":10,\"rating\":4.5,\"sellerId\":100}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only sellers can add products"));
    }
}
