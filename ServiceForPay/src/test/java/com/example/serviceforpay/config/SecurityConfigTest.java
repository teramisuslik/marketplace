package com.example.serviceforpay.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SecurityTestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAccessToApiEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/test")).andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToNonApiEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/other")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToNonApiEndpointsWithAuthentication() throws Exception {
        mockMvc.perform(get("/other")).andExpect(status().isOk());
    }
}
