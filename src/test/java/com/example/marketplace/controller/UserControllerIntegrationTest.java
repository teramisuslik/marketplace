package com.example.marketplace.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.marketplace.DTO.CheckoutLineEnriched;
import com.example.marketplace.DTO.RecordCheckoutRequest;
import com.example.marketplace.DTO.SellerCheckoutGroup;
import com.example.marketplace.DTO.UserDTO;
import com.example.marketplace.entity.Role;
import com.example.marketplace.entity.User;
import com.example.marketplace.jwt.JwtTockenUtils;
import com.example.marketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTockenUtils jwtTockenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerAndLogin_ShouldWorkEndToEnd() throws Exception {
        // Given
        UserDTO registerDto = new UserDTO();
        registerDto.setUsername("Anthony");
        registerDto.setPassword("secret");
        registerDto.setRole(Role.USER);
        String json = objectMapper.writeValueAsString(registerDto);

        // When
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Then
        User savedUser = userRepository.findByUsername("Anthony").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getPassword()).isNotEqualTo("secret");

        // When
        UserDTO loginDto = new UserDTO();
        loginDto.setUsername("Anthony");
        loginDto.setPassword("secret");
        String loginJson = objectMapper.writeValueAsString(loginDto);

        String token = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        String usernameFromToken = jwtTockenUtils.getUsernameFromToken(token);
        assertThat(usernameFromToken).isEqualTo("Anthony");

        // When
        String userId = mockMvc.perform(post("/api/user/userid").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        assertThat(userId).isEqualTo(savedUser.getId().toString());
    }

    @Test
    void getRole_ShouldReturnCorrectRole() throws Exception {
        // given
        User user = User.builder()
                .username("seller")
                .password("encoded")
                .role(Role.SELLER)
                .build();
        userRepository.save(user);
        String token = jwtTockenUtils.generateTocken(user);

        // when / then
        mockMvc.perform(get("/api/user/get_role").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("SELLER"));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDTO() throws Exception {
        // given
        User user = User.builder()
                .username("Vladislav")
                .password("encoded")
                .role(Role.ADMIN)
                .build();
        userRepository.save(user);

        // when / then
        mockMvc.perform(get("/api/user/load_user_by_username").param("username", "Vladislav"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Vladislav"))
                .andExpect(jsonPath("$.password").value("encoded"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void recordCheckout_ShouldCreateOrders() throws Exception {
        // given: регистрируем покупателя и продавца
        UserDTO buyerDto = new UserDTO();
        buyerDto.setUsername("buyer");
        buyerDto.setPassword("pass");
        buyerDto.setRole(Role.USER);
        buyerDto.setFullName("Buyer Name");
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyerDto)));

        UserDTO sellerDto = new UserDTO();
        sellerDto.setUsername("seller");
        sellerDto.setPassword("pass");
        sellerDto.setRole(Role.SELLER);
        sellerDto.setFullName("Seller Name");
        mockMvc.perform(post("/api/user/register/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellerDto)));

        // логиним покупателя
        UserDTO loginBuyer = new UserDTO();
        loginBuyer.setUsername("buyer");
        loginBuyer.setPassword("pass");
        String buyerToken = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBuyer)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // получаем ID продавца через userid endpoint
        String sellerIdStr = mockMvc.perform(
                        post("/api/user/userid").header("Authorization", "Bearer " + sellerToken(sellerDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long sellerId = Long.valueOf(sellerIdStr);

        // создаём запрос на checkout
        RecordCheckoutRequest request = new RecordCheckoutRequest();
        request.setBuyerUserId(getUserId(buyerToken));
        request.setBuyerDisplayName("Display Buyer");
        request.setPaymentTiming("now");

        CheckoutLineEnriched line = new CheckoutLineEnriched();
        line.setProductId(10L);
        line.setProductName("Test Product");
        line.setQuantity(2);
        line.setLineTotalRub(500.0);

        SellerCheckoutGroup group = new SellerCheckoutGroup();
        group.setSellerUserId(sellerId);
        group.setLines(List.of(line));
        request.setSellerGroups(List.of(group));

        // when
        mockMvc.perform(post("/api/user/checkout/record")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then: проверяем, что у продавца появился заказ
        String sellerToken = sellerToken(sellerDto);
        mockMvc.perform(get("/api/user/seller/orders").header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerName").value("Display Buyer"))
                .andExpect(jsonPath("$[0].totalRub").value(500.0))
                .andExpect(jsonPath("$[0].status").value("assembly"));
    }

    @Test
    void sellerStats_ShouldReturnZeroWhenNoOrders() throws Exception {
        // регистрируем продавца
        UserDTO sellerDto = new UserDTO();
        sellerDto.setUsername("stats_seller");
        sellerDto.setPassword("pass");
        sellerDto.setRole(Role.SELLER);
        mockMvc.perform(post("/api/user/register/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellerDto)));

        String token = sellerToken(sellerDto);

        // when/then
        mockMvc.perform(get("/api/user/seller/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenueToday").value(0.0))
                .andExpect(jsonPath("$.ordersCountToday").value(0))
                .andExpect(jsonPath("$.avgCheckToday").value(0.0));
    }

    // вспомогательные методы
    private Long getUserId(String token) throws Exception {
        String idStr = mockMvc.perform(post("/api/user/userid").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Long.valueOf(idStr);
    }

    private String sellerToken(UserDTO sellerDto) throws Exception {
        UserDTO login = new UserDTO();
        login.setUsername(sellerDto.getUsername());
        login.setPassword(sellerDto.getPassword());
        return mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
