package com.yun.yunaiagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(ImageGenerationProperties.class)
public class ImageGenerationTool implements AgentTool {

    private final ImageGenerationProperties properties;

    private final DashScopeImageClient imageClient;

    private final ImageDownloader imageDownloader;

    private final ObjectUploader objectUploader;

    @Autowired
    public ImageGenerationTool(ImageGenerationProperties properties, OssObjectStorageService objectStorageService) {
        this(
                properties,
                new RestClientDashScopeImageClient(properties, RestClient.create(), new ObjectMapper()),
                new UrlImageDownloader(RestClient.create()),
                objectStorageService::upload
        );
    }

    ImageGenerationTool(ImageGenerationProperties properties, DashScopeImageClient imageClient,
                        ImageDownloader imageDownloader, ObjectUploader objectUploader) {
        this.properties = properties;
        this.imageClient = imageClient;
        this.imageDownloader = imageDownloader;
        this.objectUploader = objectUploader;
    }

    @Override
    public String name() {
        return "generateImage";
    }

    @Override
    public String description() {
        return "根据提示词生成图片，并上传到 OSS 返回可预览 URL";
    }

    @Tool(description = "根据提示词生成图片，返回可公开访问的图片 URL")
    public String generateImage(String prompt) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return "图片生成失败：未配置 DASHSCOPE_API_KEY";
        }
        if (prompt == null || prompt.isBlank()) {
            return "图片生成失败：提示词不能为空";
        }
        try {
            String temporaryUrl = imageClient.generate(prompt.trim());
            if (temporaryUrl == null || temporaryUrl.isBlank()) {
                return "图片生成失败：模型未返回图片 URL";
            }
            DownloadedImage image = imageDownloader.download(temporaryUrl);
            String publicUrl = objectUploader.upload(buildObjectKey(image.contentType()), image.bytes(), image.contentType());
            return "图片生成成功：" + publicUrl;
        } catch (Exception e) {
            return "图片生成失败：" + e.getMessage();
        }
    }

    private String buildObjectKey(String contentType) {
        String extension = switch (contentType == null ? "" : contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".png";
        };
        return "generated-images/" + LocalDate.now() + "/" + UUID.randomUUID() + extension;
    }

    @FunctionalInterface
    interface DashScopeImageClient {
        String generate(String prompt) throws Exception;
    }

    @FunctionalInterface
    interface ImageDownloader {
        DownloadedImage download(String url);
    }

    @FunctionalInterface
    interface ObjectUploader {
        String upload(String key, byte[] bytes, String contentType);
    }

    public record DownloadedImage(byte[] bytes, String contentType) {
    }

    private static class RestClientDashScopeImageClient implements DashScopeImageClient {

        private final ImageGenerationProperties properties;

        private final RestClient restClient;

        private final ObjectMapper objectMapper;

        private RestClientDashScopeImageClient(ImageGenerationProperties properties, RestClient restClient, ObjectMapper objectMapper) {
            this.properties = properties;
            this.restClient = restClient;
            this.objectMapper = objectMapper;
        }

        @Override
        public String generate(String prompt) throws Exception {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", properties.effectiveModel());

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("prompt", prompt);
            if (properties.negativePrompt() != null && !properties.negativePrompt().isBlank()) {
                input.put("negative_prompt", properties.negativePrompt().trim());
            }
            payload.put("input", input);
            payload.put("parameters", Map.of("size", properties.effectiveSize(), "n", 1));

            String response = restClient.post()
                    .uri("https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .header("X-DashScope-Async", "disable")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            return extractImageUrl(response);
        }

        private String extractImageUrl(String response) throws Exception {
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            JsonNode results = root.path("output").path("results");
            if (results.isArray() && !results.isEmpty()) {
                String url = results.get(0).path("url").asText("");
                if (!url.isBlank()) {
                    return url;
                }
            }
            String message = root.path("message").asText("");
            if (!message.isBlank()) {
                throw new IllegalStateException(message);
            }
            return "";
        }
    }

    private static class UrlImageDownloader implements ImageDownloader {

        private final RestClient restClient;

        private UrlImageDownloader(RestClient restClient) {
            this.restClient = restClient;
        }

        @Override
        public DownloadedImage download(String url) {
            byte[] bytes = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException("generated image is empty");
            }
            return new DownloadedImage(bytes, "image/png");
        }
    }
}
