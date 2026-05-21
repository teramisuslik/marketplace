package com.example.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant")
public class AssistantProperties {

    private String ollamaBaseUrl = "http://127.0.0.1:11434";
    private String model = "llama3.2:latest";

    private int maxCatalogItems = 500;

    private boolean responseFormatJsonObject = true;

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl == null ? "" : ollamaBaseUrl.replaceAll("/+$", "");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxCatalogItems() {
        return maxCatalogItems;
    }

    public void setMaxCatalogItems(int maxCatalogItems) {
        this.maxCatalogItems = maxCatalogItems;
    }

    public boolean isResponseFormatJsonObject() {
        return responseFormatJsonObject;
    }

    public void setResponseFormatJsonObject(boolean responseFormatJsonObject) {
        this.responseFormatJsonObject = responseFormatJsonObject;
    }
}
