package com.yun.yunaiagent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(ImageGenerationProperties.class)
public class ImageGenerationTool implements AgentTool {

    private static final int MAX_IMAGES = 3;

    private final ImageGenerationProperties properties;

    private final DashScopeImageClient imageClient;

    private final ImageDownloader imageDownloader;

    private final ObjectUploader objectUploader;

    private volatile boolean alreadyGenerated;

    private volatile String lastPrompt;

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

    @Tool(description = "根据提示词生成图片，返回可公开访问的图片 URL。" +
            "每次调用默认生成 1 张图片。仅当用户明确说出具体数量(例如: 生成2张、生成三张)时才传入 count 大于 1，最大 3 张。")
    public String generateImage(String prompt,
            @ToolParam(description = "生成数量，默认 1，最大 3。仅当用户明确要求多张时才传入大于 1 的值") Integer count) {
        if (prompt == null || prompt.isBlank()) {
            return "图片生成失败：提示词不能为空";
        }

        // 防止在同一次对话中重复调用：新 prompt 重置计数器，相同 prompt 第二次调用直接拒绝
        String trimmedPrompt = prompt.trim();
        if (trimmedPrompt.equals(lastPrompt)) {
            if (alreadyGenerated) {
                return "图片已生成，请勿重复调用。请调用 doTerminate 结束任务。";
            }
        } else {
            lastPrompt = trimmedPrompt;
            alreadyGenerated = false;
        }
        alreadyGenerated = true;

        int n = count == null || count < 1 ? 1 : Math.min(count, MAX_IMAGES);
        try {
            List<String> temporaryUrls = imageClient.generate(prompt.trim(), n);
            if (temporaryUrls.isEmpty()) {
                return "图片生成失败：模型未返回图片 URL";
            }
            List<String> publicUrls = new ArrayList<>();
            for (String temporaryUrl : temporaryUrls) {
                if (temporaryUrl == null || temporaryUrl.isBlank()) {
                    continue;
                }
                DownloadedImage image = imageDownloader.download(temporaryUrl);
                String publicUrl = objectUploader.upload(
                        buildObjectKey(image.contentType()), image.bytes(), image.contentType());
                publicUrls.add(publicUrl);
            }
            if (publicUrls.isEmpty()) {
                return "图片生成失败：模型未返回图片 URL";
            }
            return "图片生成成功：" + String.join("\n", publicUrls);
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
        List<String> generate(String prompt, int n) throws Exception;
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
        public List<String> generate(String prompt, int n) throws Exception {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("未配置 spring.ai.dashscope.api-key");
            }

            Map<String, Object> userContent = new LinkedHashMap<>();
            userContent.put("text", prompt);

            Map<String, Object> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", List.of(userContent));

            Map<String, Object> input = new LinkedHashMap<>();
            input.put("messages", List.of(userMessage));

            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("n", n);
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
            log.info("Calling DashScope image generation API: {} model: {} n: {}", apiUrl, properties.effectiveModel(), n);

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
            return extractImageUrls(response);
        }

        private List<String> extractImageUrls(String response) throws Exception {
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            List<String> urls = new ArrayList<>();

            // DashScope 多模态生成响应: output.choices[0].message.content[*].image
            JsonNode choices = root.path("output").path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode content = choices.get(0).path("message").path("content");
                if (content.isArray()) {
                    for (JsonNode item : content) {
                        String url = item.path("image").asText("");
                        if (!url.isBlank()) {
                            urls.add(url);
                        }
                    }
                }
            }

            // 兼容旧版 text2image 响应: output.results[*].url
            if (urls.isEmpty()) {
                JsonNode results = root.path("output").path("results");
                if (results.isArray()) {
                    for (JsonNode item : results) {
                        String url = item.path("url").asText("");
                        if (!url.isBlank()) {
                            urls.add(url);
                        }
                    }
                }
            }

            if (!urls.isEmpty()) {
                log.info("Extracted {} image URL(s) from DashScope response", urls.size());
                return urls;
            }

            String code = root.path("code").asText("");
            String message = root.path("message").asText("");
            if (!code.isBlank() || !message.isBlank()) {
                throw new IllegalStateException(code.isBlank() ? message : code + ": " + message);
            }
            log.warn("DashScope API returned unexpected response format: {}", response);
            return urls;
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
