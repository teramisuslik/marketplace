package com.example.controller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.controller.DTO.AssistantChatRequest;
import com.example.controller.DTO.AssistantChatResponse;
import com.example.controller.client.UserClient;
import com.example.controller.jwt.JwtTokenUtils;
import com.example.controller.service.AssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AssistantController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssistantService assistantService;

    @MockitoBean
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void chat_ShouldReturnOk_WhenValidRequest() throws Exception {
        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage("Какие телефоны посоветуете?");
        AssistantChatResponse response = AssistantChatResponse.builder()
                .reply("Рекомендую **phone_xiaomi**")
                .mentionedProductIds(List.of(100L))
                .build();

        when(assistantService.chat(any(), any(AssistantChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/assistant/chat")
                        .header("Authorization", "Bearer any")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Рекомендую **phone_xiaomi**"))
                .andExpect(jsonPath("$.mentionedProductIds[0]").value(100));
    }

    @Test
    void chat_ShouldReturnBadRequest_WhenMessageIsBlank() throws Exception {
        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage("   ");
        when(assistantService.chat(any(), any(AssistantChatRequest.class)))
                .thenThrow(new IllegalArgumentException("message is required"));

        mockMvc.perform(post("/assistant/chat")
                        .header("Authorization", "Bearer any")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void chat_ShouldHandleServiceException_AndReturnBadRequest() throws Exception {
        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage("hello");
        when(assistantService.chat(any(), any(AssistantChatRequest.class)))
                .thenThrow(new IllegalArgumentException("message is required"));

        mockMvc.perform(post("/assistant/chat")
                        .header("Authorization", "Bearer any")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
