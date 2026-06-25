package com.yun.yuimagesearchmcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * MCP 图片搜索工具。
 *
 * <p>封装 Pexels 搜索 API，并通过 Spring AI 的 @Tool 注解暴露给主应用或其他 MCP Client 调用。</p>
 */
public class ImageSearchTool {

    private final String apiKey;

    private final String apiUrl;

    private final RestClient restClient;

    @Autowired
    public ImageSearchTool(@Value("${pexels.api-key:}") String apiKey,
                           @Value("${pexels.api-url:https://api.pexels.com/v1/search}") String apiUrl,
                           RestClient.Builder restClientBuilder) {
        this(apiKey, apiUrl, restClientBuilder.build());
    }

    ImageSearchTool(String apiKey, String apiUrl) {
        this(apiKey, apiUrl, RestClient.create());
    }

    ImageSearchTool(String apiKey, String apiUrl, RestClient restClient) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.apiUrl = apiUrl;
        this.restClient = restClient;
    }

    @Tool(description = "Search images from Pexels and return medium image URLs.")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        // MCP 工具需要优先返回可读错误，避免主应用把配置缺失误判为搜索无结果。
        if (apiKey.isBlank()) {
            return "Image search is unavailable: PEXELS_API_KEY is not configured.";
        }
        if (query == null || query.isBlank()) {
            return "Image search is unavailable: query is required.";
        }
        try {
            List<String> urls = searchMediumImages(query.trim());
            if (urls.isEmpty()) {
                return "No images found for query: " + query.trim();
            }
            return urls.stream().limit(5).collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    List<String> searchMediumImages(String query) {
        // 只提取 medium 图片地址，控制 MCP 响应体积并降低模型上下文占用。
        Map<String, Object> response = restClient.get()
                .uri(apiUrl + "?query={query}", query)
                .header("Authorization", apiKey)
                .retrieve()
                .body(Map.class);
        if (response == null || !(response.get("photos") instanceof List<?> photos)) {
            return List.of();
        }
        return photos.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(photo -> photo.get("src"))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(src -> src.get("medium"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(url -> !url.isBlank())
                .toList();
    }
}
