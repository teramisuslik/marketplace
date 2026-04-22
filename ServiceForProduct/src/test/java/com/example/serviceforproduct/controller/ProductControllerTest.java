package com.example.serviceforproduct.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.serviceforproduct.DTO.ProductDTO;
import com.example.serviceforproduct.client.UserClient;
import com.example.serviceforproduct.config.SecurityConfig;
import com.example.serviceforproduct.entity.Product;
import com.example.serviceforproduct.entity.Role;
import com.example.serviceforproduct.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;
    private Product product;
    private final String token = "Bearer test-token";

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setCountOfProduct(10);
        productDTO.setRating(4.5f);
        productDTO.setSellerId(100L);

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .countOfProduct(10)
                .rating(4.5f)
                .sellerId(100L)
                .build();
    }

    @Test
    void addProduct_ShouldReturnOk_WhenRoleIsSeller() throws Exception {
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        when(userClient.getUserId(token)).thenReturn(100L);
        doNothing().when(productService).createProduct(any(Product.class));

        mockMvc.perform(post("/api/product/add")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());

        verify(userClient).getRole(token);
        verify(userClient).getUserId(token);
        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void addProduct_ShouldReturnForbidden_WhenRoleIsNotSeller() throws Exception {
        when(userClient.getRole(token)).thenReturn(Role.USER);

        mockMvc.perform(post("/api/product/add")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());

        verify(userClient).getRole(token);
        verify(userClient, never()).getUserId(token);
        verify(productService, never()).createProduct(any());
    }

    @Test
    void allProducts_ShouldReturnListOfProductDTOs() throws Exception {
        List<ProductDTO> productDTOs = List.of(productDTO);
        when(productService.findAll()).thenReturn(productDTOs);

        mockMvc.perform(get("/api/product/main"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(productDTO.getName()))
                .andExpect(jsonPath("$[0].description").value(productDTO.getDescription()))
                .andExpect(jsonPath("$[0].countOfProduct").value(productDTO.getCountOfProduct()))
                .andExpect(jsonPath("$[0].rating").value(productDTO.getRating()))
                .andExpect(jsonPath("$[0].sellerId").value(productDTO.getSellerId()));

        verify(productService).findAll();
    }

    @Test
    void allProductsWithWord_ShouldReturnFilteredList() throws Exception {
        String word = "test";
        List<ProductDTO> productDTOs = List.of(productDTO);
        when(productService.findByWord(word)).thenReturn(productDTOs);

        mockMvc.perform(get("/api/product/main/{word}", word))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(productDTO.getName()));

        verify(productService).findByWord(word);
    }

    @Test
    void findProductByName_ShouldReturnId() throws Exception {
        String name = "Test Product";
        when(productService.findByName(name)).thenReturn(product);

        mockMvc.perform(get("/api/product/find_product_by_name/{name}", name))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(productService).findByName(name);
    }

    @Test
    void findProductById_ShouldReturnProductDTO() throws Exception {
        Long id = 1L;
        when(productService.findById(id)).thenReturn(product);

        mockMvc.perform(get("/api/product/find_all_by_id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(product.getName()))
                .andExpect(jsonPath("$.description").value(product.getDescription()))
                .andExpect(jsonPath("$.countOfProduct").value(product.getCountOfProduct()))
                .andExpect(jsonPath("$.rating").value(product.getRating()))
                .andExpect(jsonPath("$.sellerId").value(product.getSellerId()));
    }

    @Test
    void findProductById_ShouldReturnNotFound_WhenProductDoesNotExist() throws Exception {
        Long id = 999L;
        when(productService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/product/find_all_by_id").param("id", id.toString()))
                .andExpect(status().isNotFound());
    }
}
