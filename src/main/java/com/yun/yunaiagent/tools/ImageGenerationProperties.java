package com.yun.yunaiagent.tools;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashscope.image")
public record ImageGenerationProperties(
        String apiKey,
        String model,
        String size,
        String negativePrompt
) {
    public String effectiveModel() {
        return model == null || model.isBlank() ? "qwen-image-2.0-pro" : model.trim();
    }

    public String effectiveSize() {
        return size == null || size.isBlank() ? "2048*2048" : size.trim();
    }
}
