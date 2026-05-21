package com.example.controller.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.DTO.Role;
import com.example.controller.client.UserClient;
import com.example.controller.jwt.JwtTokenUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;

    @Test
    void uploadProductImage_ShouldReturnUrl_WhenSellerAndValidFile() throws Exception {
        // given
        String token = "Bearer test";
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        MockMultipartFile file =
                new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, "fake content".getBytes());

        // when
        mockMvc.perform(multipart("/seller/product_image").file(file).header("Authorization", token))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relativeUrl").value(org.hamcrest.Matchers.startsWith("/api/files/products/")));
    }

    @Test
    void uploadProductImage_ShouldReturnForbidden_WhenNotSeller() throws Exception {
        // given
        String token = "Bearer test";
        when(userClient.getRole(token)).thenReturn(Role.USER);
        MockMultipartFile file =
                new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, "data".getBytes());

        // when
        mockMvc.perform(multipart("/seller/product_image").file(file).header("Authorization", token))
                // then
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadProductImage_ShouldReturnBadRequest_WhenEmptyFile() throws Exception {
        // given
        String token = "Bearer test";
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        // when
        mockMvc.perform(multipart("/seller/product_image").file(emptyFile).header("Authorization", token))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadProductImage_ShouldReturnBadRequest_WhenInvalidExtension() throws Exception {
        // given
        String token = "Bearer test";
        when(userClient.getRole(token)).thenReturn(Role.SELLER);
        MockMultipartFile file =
                new MockMultipartFile("file", "image.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE, "bad".getBytes());

        // when
        mockMvc.perform(multipart("/seller/product_image").file(file).header("Authorization", token))
                // then
                .andExpect(status().isBadRequest());
    }
}
