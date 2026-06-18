package com.yun.yunaiagent.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AgentToolsTest {

    @TempDir
    Path tempDir;

    @Test
    void fileToolReadsAndWritesRealFiles() throws Exception {
        FileOperationTool tool = new FileOperationTool();
        Path file = tempDir.resolve("note.txt");

        String writeResult = tool.writeFile(file.toString(), "hello");
        String readResult = tool.readFile(file.toString());

        assertThat(writeResult).contains("写入成功");
        assertThat(readResult).isEqualTo("hello");
    }

    @Test
    void terminalToolExecutesCommandWithTimeoutAndOutputLimit() {
        TerminalOperationTool tool = new TerminalOperationTool(5, 20);

        String result = tool.executeTerminalCommand("cmd /c echo hello-terminal");

        assertThat(result).contains("hello-terminal");
    }

    @Test
    void pdfToolCreatesPdfFile() throws Exception {
        PDFGenerationTool tool = new PDFGenerationTool();
        Path pdf = tempDir.resolve("report.pdf");

        String result = tool.generatePDF("标题", "正文内容", pdf.toString());

        assertThat(result).contains("PDF 生成成功");
        assertThat(Files.exists(pdf)).isTrue();
        assertThat(Files.size(pdf)).isGreaterThan(0);
    }

    @Test
    void webSearchToolReturnsClearMessageWhenApiKeyIsMissing() {
        WebSearchTool tool = new WebSearchTool("", (query, apiKey) -> "{}");

        String result = tool.searchWeb("Java 21");

        assertThat(result).contains("搜索失败：未配置");
    }

    @Test
    void webSearchToolFormatsAnswerBoxAndOrganicResults() {
        String json = """
                {
                  "answer_box": {"answer": "Java 21 是长期支持版本。"},
                  "organic_results": [
                    {"title": "Java 21 Guide", "link": "https://example.com/java21", "snippet": "Java 21 features"},
                    {"title": "Second", "link": "https://example.com/second", "snippet": "Second result"}
                  ]
                }
                """;
        WebSearchTool tool = new WebSearchTool("search-key", (query, apiKey) -> json);

        String result = tool.searchWeb("Java 21");

        assertThat(result)
                .contains("搜索摘要")
                .contains("Java 21 是长期支持版本。")
                .contains("1. Java 21 Guide")
                .contains("https://example.com/java21")
                .contains("Java 21 features");
    }

    @Test
    void imageGenerationToolReturnsClearMessageWhenDashScopeKeyIsMissing() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties("qwen-image-2.0-pro", "2048*2048", "", "", null, null),
                prompt -> { throw new IllegalStateException("未配置 spring.ai.dashscope.api-key"); },
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1}, "image/png"),
                (key, bytes, contentType) -> "https://oss.example/generated.png"
        );

        String result = tool.generateImage("cat poster");

        assertThat(result).contains("图片生成失败：未配置 spring.ai.dashscope.api-key");
    }

    @Test
    void imageGenerationToolUploadsGeneratedImageToOss() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties("qwen-image-2.0-pro", "2048*2048", "", "", null, null),
                prompt -> "https://dashscope.example/temp.png",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/png"),
                (key, bytes, contentType) -> {
                    assertThat(key).startsWith("generated-images/");
                    assertThat(bytes).containsExactly(1, 2, 3);
                    assertThat(contentType).isEqualTo("image/png");
                    return "https://oss.example/generated.png";
                }
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成成功")
                .contains("https://oss.example/generated.png");
    }

    @Test
    void imageGenerationToolUsesSpringAiDashScopeApiKeyFromYaml() {
        new ApplicationContextRunner()
                .withUserConfiguration(ImageGenerationToolTestConfig.class)
                .withPropertyValues(
                        "spring.ai.dashscope.api-key=yml-dashscope-key",
                        "dashscope.image.model=qwen-image-2.0-pro",
                        "dashscope.image.size=2048*2048"
                )
                .run(context -> {
                    ImageGenerationTool tool = context.getBean(ImageGenerationTool.class);

                    String result = tool.generateImage("cat poster");

                    assertThat(result)
                            .contains("图片生成成功")
                            .contains("https://oss.example/generated.png");
                });
    }

    @Test
    void imageGenerationPropertiesDefaultsToDashScopeCompatibleImageApiUrl() {
        ImageGenerationProperties properties = new ImageGenerationProperties(null, null, null, null, null, null);

        assertThat(properties.effectiveApiUrl())
                .isEqualTo("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");
        assertThat(properties.effectiveSize()).isEqualTo("1024*1024");
    }

    @Test
    void imageGenerationPropertiesReadsApiUrlFromYaml() {
        new ApplicationContextRunner()
                .withUserConfiguration(ImageGenerationPropertiesTestConfig.class)
                .withPropertyValues(
                        "dashscope.image.api-url=https://dashscope.example/custom/images/generations",
                        "dashscope.image.model=qwen-image-2.0-pro",
                        "dashscope.image.size=1024*1024"
                )
                .run(context -> {
                    ImageGenerationProperties properties = context.getBean(ImageGenerationProperties.class);

                    assertThat(properties.effectiveApiUrl())
                            .isEqualTo("https://dashscope.example/custom/images/generations");
                });
    }

    // ========== 新增边界测试 ==========

    @Test
    void imageGenerationToolReturnsClearMessageWhenPromptIsEmpty() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> "https://dashscope.example/temp.png",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1}, "image/png"),
                (key, bytes, contentType) -> "https://oss.example/generated.png"
        );

        assertThat(tool.generateImage(""))
                .contains("图片生成失败：提示词不能为空");
        assertThat(tool.generateImage((String) null))
                .contains("图片生成失败：提示词不能为空");
    }

    @Test
    void imageGenerationToolReturnsErrorMessageOnApiFailure() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> { throw new IllegalStateException("model rate limited"); },
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1}, "image/png"),
                (key, bytes, contentType) -> "https://oss.example/generated.png"
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成失败")
                .contains("model rate limited");
    }

    @Test
    void imageGenerationToolReturnsErrorMessageOnDownloadFailure() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> "https://dashscope.example/temp.png",
                url -> { throw new IllegalStateException("connection timeout"); },
                (key, bytes, contentType) -> "https://oss.example/generated.png"
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成失败")
                .contains("connection timeout");
    }

    @Test
    void imageGenerationToolReturnsErrorMessageOnUploadFailure() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> "https://dashscope.example/temp.png",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/png"),
                (key, bytes, contentType) -> { throw new IllegalStateException("OSS bucket not found"); }
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成失败")
                .contains("OSS bucket not found");
    }

    @Test
    void imageGenerationToolUsesJpegExtensionForJpegContentType() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> "https://dashscope.example/temp.jpg",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/jpeg"),
                (key, bytes, contentType) -> {
                    assertThat(contentType).isEqualTo("image/jpeg");
                    assertThat(key).endsWith(".jpg");
                    assertThat(key).startsWith("generated-images/");
                    return "https://oss.example/generated.jpg";
                }
        );

        String result = tool.generateImage("cat poster");

        assertThat(result)
                .contains("图片生成成功")
                .contains("https://oss.example/generated.jpg");
    }

    @Test
    void imageGenerationToolUsesWebpExtensionForWebpContentType() {
        ImageGenerationTool tool = new ImageGenerationTool(
                new ImageGenerationProperties(null, null, null, null, null, null),
                prompt -> "https://dashscope.example/temp.webp",
                url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/webp"),
                (key, bytes, contentType) -> {
                    assertThat(contentType).isEqualTo("image/webp");
                    assertThat(key).endsWith(".webp");
                    return "https://oss.example/generated.webp";
                }
        );

        String result = tool.generateImage("cat poster");

        assertThat(result).contains("图片生成成功");
    }

    // ========== 测试配置类 ==========

    @EnableConfigurationProperties(ImageGenerationProperties.class)
    static class ImageGenerationToolTestConfig {

        @Bean
        ImageGenerationTool imageGenerationTool(
                ImageGenerationProperties properties,
                @Value("${spring.ai.dashscope.api-key:${dashscope.api-key:}}") String apiKey
        ) {
            return new ImageGenerationTool(
                    properties,
                    prompt -> {
                        assertThat(apiKey).isEqualTo("yml-dashscope-key");
                        return "https://dashscope.example/temp.png";
                    },
                    url -> new ImageGenerationTool.DownloadedImage(new byte[]{1, 2, 3}, "image/png"),
                    (key, bytes, contentType) -> "https://oss.example/generated.png"
            );
        }
    }

    @EnableConfigurationProperties(ImageGenerationProperties.class)
    static class ImageGenerationPropertiesTestConfig {
    }
}
