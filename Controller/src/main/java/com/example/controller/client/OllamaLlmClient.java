package com.example.controller.client;

import com.example.controller.config.AssistantProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaLlmClient implements LlmCompletionClient {

    private final RestClient ollamaRestClient;
    private final ObjectMapper objectMapper;
    private final AssistantProperties assistantProperties;

    @Override
    public String complete(String model, List<Map<String, String>> messages) {
        log.info("Ollama: POST /v1/chat/completions, модель={}, сообщений={}", model, messages.size());
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        if (assistantProperties.isResponseFormatJsonObject()) {
            body.put("response_format", Map.of("type", "json_object"));
        }
        try {
            String content = postAndExtractContent(body);
            log.info("Ollama: ответ получен, длина content={} символов", content.length());
            return content;
        } catch (Exception e) {
            if (!assistantProperties.isResponseFormatJsonObject() || !body.containsKey("response_format")) {
                throw e;
            }
            log.warn("Ollama: повтор запроса без response_format — {}", e.getMessage());
            body.remove("response_format");
            String content = postAndExtractContent(body);
            log.info("Ollama: ответ после повтора, длина content={} символов", content.length());
            return content;
        }
    }

    private String postAndExtractContent(Map<String, Object> body) {
        try {
            String raw = ollamaRestClient
                    .post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(raw);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new IllegalStateException("Ollama response has no message content");
            }
            return content.asText();
        } catch (RestClientException e) {
            log.error("Ollama: HTTP-ошибка при вызове chat/completions", e);
            throw e;
        } catch (Exception e) {
            log.error("Ollama: не удалось разобрать тело ответа", e);
            throw new IllegalStateException("Failed to parse Ollama response", e);
        }
    }
}
