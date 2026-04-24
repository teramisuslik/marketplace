package com.example.controller.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantChatRequest {

    private String message;
    private List<AssistantChatTurn> history = new ArrayList<>();
}
