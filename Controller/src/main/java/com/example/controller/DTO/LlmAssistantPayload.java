package com.example.controller.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmAssistantPayload {

    private String userMessage;
    private List<Long> mentionedProductIds = new ArrayList<>();
    private List<LlmAssistantAction> actions = new ArrayList<>();
}
