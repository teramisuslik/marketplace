package com.example.controller.client;

import java.util.List;
import java.util.Map;

public interface LlmCompletionClient {

    String complete(String model, List<Map<String, String>> messages);
}
