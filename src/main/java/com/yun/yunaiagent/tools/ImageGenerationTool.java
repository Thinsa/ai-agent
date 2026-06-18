package com.yun.yunaiagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
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
    public ImageGenerationTool(
            ImageGenerationProperties properties,
            @Value("${spring.ai.dashscope.api-key:${dashscope.api-key:}}") String apiKey,
            OssObjectStorageService objectStorageService
    ) {
        this(
                properties,
                new RestClientDashScopeImageClient(properties, apiKey, createRestClient(), new ObjectMapper()),
                new UrlImageDownloader(createRestClient()),
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

    /**
     * 创建带超时配置的 RestClient，用于 DashScope API 调用和图片下载。
     * 连接超时 10 秒，读取超时 120 秒（图片生成接口较慢）。
     */
    private static RestClient createRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(120));
        return RestClient.builder().requestFactory(requestFactory).build();
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

        private static final Logger log = LoggerFactory.getLogger(ImageGenerationTool.class);

        private final ImageGenerationProperties properties;

        private final String apiKey;

        private final RestClient restClient;

        private final ObjectMapper objectMapper;

        private RestClientDashScopeImageClient(ImageGenerationProperties properties, String apiKey,
                                               RestClient restClient, ObjectMapper objectMapper) {
            this.properties = properties;
            this.apiKey = apiKey;
            this.restClient = restClient;
            this.objectMapper = objectMapper;
        }

        @Override
        public String generate(String prompt) throws Exception {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("未配置 spring.ai.dashscope.api-key");
            }

            // 构建 DashScope 多模态生成 API 请求体
            Map<String, Object> userContent = new LinkedHashMap<>();
            userContent.put("text", prompt);

            Map<String, Object> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", List.of(userContent));

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("messages", List.of(userMessage));

            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("n", 1);
            parameters.put("size", properties.effectiveSize());
            parameters.put("watermark", properties.effectiveWatermark());
            parameters.put("prompt_extend", properties.effectivePromptExtend());
            if (properties.negativePrompt() != null && !properties.negativePrompt().isBlank()) {
                parameters.put("negative_prompt", properties.negativePrompt().trim());
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", properties.effectiveModel());
            payload.put("input", input);
            payload.put("parameters", parameters);

            String apiUrl = properties.effectiveApiUrl();
            log.info("Calling DashScope image generation API: {} with model: {}", apiUrl, properties.effectiveModel());

            String response = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        byte[] errorBody = res.getBody().readAllBytes();
                        String errorText = new String(errorBody);
                        log.error("DashScope API error ({}): {}", res.getStatusCode().value(), errorText);
                        throw new IllegalStateException("DashScope API 返回错误 " + res.getStatusCode().value() + ": " + errorText);
                    })
                    .body(String.class);

            log.debug("DashScope API response: {}", response);
            return extractImageUrl(response);
        }

        private String extractImageUrl(String response) throws Exception {
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);

            // DashScope 多模态生成响应: output.choices[0].message.content[0].image
            JsonNode choices = root.path("output").path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode content = choices.get(0).path("message").path("content");
                if (content.isArray() && !content.isEmpty()) {
                    String url = content.get(0).path("image").asText("");
                    if (!url.isBlank()) {
                        log.info("Extracted image URL from DashScope response");
                        return url;
                    }
                }
            }

            // 兼容旧版 text2image 响应: output.results[0].url
            JsonNode results = root.path("output").path("results");
            if (results.isArray() && !results.isEmpty()) {
                String url = results.get(0).path("url").asText("");
                if (!url.isBlank()) {
                    return url;
                }
            }

            // 错误信息
            String code = root.path("code").asText("");
            String message = root.path("message").asText("");
            if (!code.isBlank() || !message.isBlank()) {
                throw new IllegalStateException(code.isBlank() ? message : code + ": " + message);
            }
            log.warn("DashScope API returned unexpected response format: {}", response);
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
            ResponseEntity<byte[]> entity = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .toEntity(byte[].class);
            byte[] bytes = entity.getBody();
            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException("generated image is empty");
            }
            MediaType contentType = entity.getHeaders().getContentType();
            String contentTypeStr = contentType != null ? contentType.toString() : "image/png";
            return new DownloadedImage(bytes, contentTypeStr);
        }
    }
}
