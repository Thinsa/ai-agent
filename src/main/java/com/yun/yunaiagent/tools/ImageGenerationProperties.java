package com.yun.yunaiagent.tools;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dashscope.image")
/**
 * 图片生成配置属性。
 *
 * <p>绑定 DashScope 图片模型、接口地址、默认尺寸和水印等参数，供图片生成工具统一读取。</p>
 */
public record ImageGenerationProperties(
        String model,
        String size,
        String negativePrompt,
        String apiUrl,
        Boolean watermark,
        Boolean promptExtend
) {
    public String effectiveModel() {
        return model == null || model.isBlank() ? "qwen-image-2.0-pro" : model.trim();
    }

    public String effectiveSize() {
        return size == null || size.isBlank() ? "1024*1024" : size.trim();
    }

    public String effectiveApiUrl() {
        return apiUrl == null || apiUrl.isBlank()
                ? "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
                : apiUrl.trim();
    }

    public boolean effectiveWatermark() {
        return watermark != null && watermark;
    }

    public boolean effectivePromptExtend() {
        return promptExtend == null || promptExtend;
    }
}
