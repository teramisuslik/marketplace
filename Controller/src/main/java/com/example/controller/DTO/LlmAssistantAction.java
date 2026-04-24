package com.example.controller.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmAssistantAction {

    private String type;
    private Long productId;
}
