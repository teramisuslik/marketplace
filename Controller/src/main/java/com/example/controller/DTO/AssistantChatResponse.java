package com.example.controller.DTO;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {

    private String reply;

    @Builder.Default
    private List<Long> mentionedProductIds = new ArrayList<>();

    @Builder.Default
    private List<String> executedActions = new ArrayList<>();
}
