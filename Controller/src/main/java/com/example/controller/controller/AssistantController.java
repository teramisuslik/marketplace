package com.example.controller.controller;

import com.example.controller.DTO.AssistantChatRequest;
import com.example.controller.DTO.AssistantChatResponse;
import com.example.controller.service.AssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
@Slf4j
public class AssistantController {

    private final AssistantService assistantService;

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
            @RequestBody AssistantChatRequest request) {
        try {
            int messageLen = request.getMessage() != null ? request.getMessage().length() : 0;
            int historySize =
                    request.getHistory() != null ? request.getHistory().size() : 0;
            log.info("Ассистент /chat: длина сообщения={}, реплик в истории={}", messageLen, historySize);
            return ResponseEntity.ok(assistantService.chat(authorization, request));
        } catch (IllegalArgumentException e) {
            log.warn("Ассистент /chat: некорректный запрос — {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
